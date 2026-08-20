package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.features.inspection.domain.InspectionRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignResult
import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.design.domain.designInputOrNull
import com.kemalurekli.electricalcalculator.features.inspection.domain.CircuitTest
import com.kemalurekli.electricalcalculator.features.inspection.domain.EvaluateTestUseCase
import com.kemalurekli.electricalcalculator.features.inspection.domain.InsulationTestVoltage
import com.kemalurekli.electricalcalculator.features.inspection.domain.RcdType
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestEvaluation
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One test row: what was recorded, and what the design makes of it. */
@Immutable
data class TestRow(
    val test: CircuitTest,
    val evaluation: TestEvaluation,
)

@Immutable
data class CircuitUiState(
    val project: Project? = null,
    val circuit: Circuit? = null,
    /** Null while the circuit is still missing something the chain needs. */
    val design: CircuitDesignResult? = null,
    val isLoading: Boolean = true,
    val isGone: Boolean = false,
    /** Every test kind, in the order IEC 60364-6 asks for them. */
    val tests: ImmutableList<TestRow> = persistentListOf(),
)

/**
 * One circuit, designed as it is typed.
 *
 * ### Why there is no Calculate button
 *
 * Every other form in the app has one, and every other form is answering a
 * question the user asked once. This one is a row in a schedule: the reader is
 * trying lengths and loads against each other to find the size that works, and
 * a button between each attempt and its answer turns a five-second exploration
 * into a sequence of decisions. The chain is arithmetic over a twenty-row
 * table, so there is nothing to defer.
 *
 * ### Why every keystroke is saved
 *
 * A circuit belongs to a project, and a project is the user's work. Losing a
 * half-typed length because they backed out of the screen would be the same
 * failure the history shelf was built to prevent. The state the screen renders
 * is local, so the write never sits between a key and the field.
 */
class CircuitViewModel(
    private val repository: ProjectRepository,
    private val inspectionRepository: InspectionRepository,
    private val designCircuit: DesignCircuitUseCase,
    private val evaluateTest: EvaluateTestUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircuitUiState())
    val uiState: StateFlow<CircuitUiState> = _uiState.asStateFlow()

    private var loadedId: Long = Circuit.NO_ID
    private var testWatch: Job? = null

    fun onOpen(projectId: Long, circuitId: Long) {
        if (loadedId == circuitId) return
        loadedId = circuitId
        viewModelScope.launch {
            val project = repository.observeProject(projectId).first()
            val circuit = repository.findCircuit(circuitId)
            _uiState.value = CircuitUiState(
                project = project,
                circuit = circuit,
                design = designOrNull(project, circuit),
                isLoading = false,
                isGone = project == null || circuit == null,
            )
        }
        watchTests(circuitId)
    }

    /**
     * Readings are observed rather than read once.
     *
     * Unlike the form above them they are not being typed into from two places,
     * and observing means a reading taken on another screen — or restored from
     * a backup — appears without the circuit having to be reopened.
     */
    private fun watchTests(circuitId: Long) {
        testWatch?.cancel()
        testWatch = viewModelScope.launch {
            inspectionRepository.observeForCircuit(circuitId).collect { stored ->
                _uiState.update { state -> state.copy(tests = state.rows(circuitId, stored)) }
            }
        }
    }

    /**
     * Every kind, whether or not it has been measured.
     *
     * A list that showed only what had been recorded would make an untested
     * circuit look complete. The empty rows are the checklist.
     */
    private fun CircuitUiState.rows(
        circuitId: Long,
        stored: List<CircuitTest>,
    ): ImmutableList<TestRow> = TestKind.entries
        .map { kind ->
            val test = stored.firstOrNull { it.kind == kind }
                ?: CircuitTest(circuitId = circuitId, kind = kind)
            TestRow(test, evaluateTest(test, design))
        }
        .toImmutableList()

    fun onTestValueChange(kind: TestKind, value: String) = editTest(kind) { it.copy(value = value) }

    fun onTestPolarityChange(passed: Boolean?) =
        editTest(TestKind.POLARITY) { it.copy(passed = passed) }

    fun onInsulationVoltageChange(voltage: InsulationTestVoltage) =
        editTest(TestKind.INSULATION) { it.copy(insulationVoltage = voltage) }

    fun onRcdTypeChange(type: RcdType) {
        // The type belongs to the device, not to one measurement, so it moves
        // on both RCD rows together — a device is not general at one current
        // and selective at another.
        editTest(TestKind.RCD_AT_RATED) { it.copy(rcdType = type) }
        editTest(TestKind.RCD_AT_FIVE_TIMES) { it.copy(rcdType = type) }
    }

    private fun editTest(kind: TestKind, transform: (CircuitTest) -> CircuitTest) {
        val state = _uiState.value
        val current = state.tests.firstOrNull { it.test.kind == kind }?.test ?: return
        val updated = transform(current)
        _uiState.update { existing ->
            existing.copy(
                tests = existing.tests
                    .map { row ->
                        if (row.test.kind == kind) {
                            TestRow(updated, evaluateTest(updated, existing.design))
                        } else {
                            row
                        }
                    }
                    .toImmutableList(),
            )
        }
        viewModelScope.launch { inspectionRepository.save(updated) }
    }

    fun onNameChange(value: String) = edit { it.copy(name = value) }

    /**
     * Switching between a current and a power clears the figure.
     *
     * 32 read as amperes and 32 read as watts are different circuits, and
     * carrying the number across would silently redesign the cable against a
     * load nobody entered.
     */
    fun onLoadKindChange(kind: CircuitLoadKind) = edit {
        if (it.loadKind == kind) it else it.copy(loadKind = kind, load = "")
    }

    fun onLoadChange(value: String) = edit { it.copy(load = value) }

    fun onPowerFactorChange(value: String) = edit { it.copy(powerFactor = value) }

    fun onLengthChange(value: String) = edit { it.copy(lengthMetres = value) }

    fun onGroupedCircuitsChange(value: String) = edit { it.copy(groupedCircuits = value) }

    fun onParallelConductorsChange(value: String) = edit { it.copy(parallelConductors = value) }

    fun onDeviceTypeChange(type: ProtectiveDeviceType) = edit { it.copy(deviceType = type.name) }

    fun onDisconnectionTimeChange(value: String) =
        edit { it.copy(disconnectionTimeSeconds = value) }

    fun onDelete() {
        val circuit = _uiState.value.circuit ?: return
        viewModelScope.launch {
            repository.deleteCircuit(circuit)
            _uiState.update { it.copy(isGone = true) }
        }
    }

    private fun edit(transform: (Circuit) -> Circuit) {
        val updated = transform(_uiState.value.circuit ?: return)
        _uiState.update { state ->
            val design = designOrNull(state.project, updated)
            state.copy(
                circuit = updated,
                design = design,
                // A longer run changes what the loop should read, so the
                // readings are re-judged against the design that now applies.
                tests = state.tests
                    .map { TestRow(it.test, evaluateTest(it.test, design)) }
                    .toImmutableList(),
            )
        }
        viewModelScope.launch { repository.saveCircuit(updated) }
    }

    private fun designOrNull(project: Project?, circuit: Circuit?): CircuitDesignResult? {
        if (project == null || circuit == null) return null
        return designInputOrNull(project, circuit)?.let(designCircuit::invoke)
    }
}
