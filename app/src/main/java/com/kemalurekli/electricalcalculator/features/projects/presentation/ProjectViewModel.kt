package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignResult
import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.design.domain.ReportCsv
import com.kemalurekli.electricalcalculator.features.design.domain.designInputOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

/**
 * One line of the schedule.
 *
 * [design] is null while the circuit is still missing something the chain
 * needs, which is an ordinary state for a row the user has just added — not an
 * error to be reported.
 */
@Immutable
data class CircuitRow(
    val circuit: Circuit,
    val design: CircuitDesignResult?,
)

/** A rendered schedule, ready for the screen to write and share. */
@Immutable
data class ExportedSchedule(
    val reference: String,
    val csv: String,
)

@Immutable
data class ProjectUiState(
    val project: Project? = null,
    val rows: ImmutableList<CircuitRow> = persistentListOf(),
    val isLoading: Boolean = true,
    /** True once the project has been deleted, which the screen reads as "go back". */
    val isGone: Boolean = false,
)

/**
 * A job and everything in it.
 *
 * ### Why the schedule is recomputed rather than stored
 *
 * A circuit's cross-section follows from the project's ambient, its cable type
 * and its supply. Storing the answer would create a second copy that goes stale
 * the moment any of those change, and the failure would be silent: a schedule
 * showing sizes calculated against last week's ambient. Redesigning every
 * circuit on every emission is arithmetic over a twenty-row table, which costs
 * nothing next to being wrong.
 *
 * ### Why the project is loaded once and the circuits are observed
 *
 * The header is a form. A form whose values arrive back through the database
 * drops characters when typed at speed, because the field re-renders with what
 * was stored before the last keystroke. The circuits below are not a form —
 * they are a list that has to refresh when the reader comes back from editing
 * one — so those stay a flow.
 */
@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val designCircuit: DesignCircuitUseCase,
    private val reportBuilder: ScheduleReportBuilder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    private var projectId: Long = Project.NO_ID
    private var circuitWatch: Job? = null

    /** The circuit a tap on "add" just created, for the screen to open. */
    private val _created = MutableStateFlow<Long?>(null)
    val created: StateFlow<Long?> = _created.asStateFlow()

    fun onOpen(id: Long) {
        if (projectId == id) return
        projectId = id
        viewModelScope.launch {
            // One read, not a subscription: the header is a form and must not be
            // rewritten underneath the user by the saves it makes itself.
            val project = repository.observeProject(id).first()
            _uiState.update { state ->
                state.copy(
                    project = project,
                    rows = design(project, state.rows.map { it.circuit }),
                    isLoading = false,
                    isGone = project == null,
                )
            }
        }
        watchCircuits(id)
    }

    private fun watchCircuits(id: Long) {
        circuitWatch?.cancel()
        circuitWatch = viewModelScope.launch {
            repository.observeCircuits(id).collect { circuits ->
                _uiState.update { state -> state.copy(rows = design(state.project, circuits)) }
            }
        }
    }

    private fun design(project: Project?, circuits: List<Circuit>): ImmutableList<CircuitRow> {
        if (project == null) return circuits.map { CircuitRow(it, null) }.toImmutableList()
        return circuits
            .map { circuit ->
                CircuitRow(circuit, designInputOrNull(project, circuit)?.let(designCircuit::invoke))
            }
            .toImmutableList()
    }

    // -- The shared supply ---------------------------------------------------

    fun onReferenceChange(value: String) = edit { it.copy(reference = value) }

    fun onSiteChange(value: String) = edit { it.copy(site = value) }

    /** Switching the supply moves an untouched voltage, exactly as a calculator does. */
    fun onSystemChange(system: SupplySystem) = edit {
        it.copy(
            system = system,
            systemVoltage = SystemVoltageDefaults.forSystem(system) ?: it.systemVoltage,
        )
    }

    fun onVoltageChange(value: String) = edit { it.copy(systemVoltage = value) }

    fun onMaterialChange(material: ConductorMaterial) = edit { it.copy(material = material) }

    fun onInsulationChange(insulation: CableInsulation) = edit { it.copy(insulation = insulation) }

    fun onMethodChange(method: InstallationMethod) = edit { it.copy(method = method) }

    fun onAmbientChange(value: String) = edit { it.copy(ambientTemperatureC = value) }

    fun onMaxDropChange(value: String) = edit { it.copy(maxVoltageDropPercent = value) }

    fun onExternalImpedanceChange(value: String) = edit { it.copy(externalImpedanceOhms = value) }

    /**
     * Applies a change to the shared parameters and redesigns the whole schedule.
     *
     * The redesign is the point: raising the ambient by five degrees is meant to
     * be visible immediately in every circuit below, which is what a project is
     * for and what twelve separate calculator runs cannot do.
     */
    private fun edit(transform: (Project) -> Project) {
        val updated = transform(_uiState.value.project ?: return)
        _uiState.update { state ->
            state.copy(project = updated, rows = design(updated, state.rows.map { it.circuit }))
        }
        viewModelScope.launch { repository.saveProject(updated) }
    }

    // -- The schedule --------------------------------------------------------

    /**
     * Adds a circuit carrying the conventions a final circuit usually has.
     *
     * Blank where only the user can answer — the load, the length — and filled
     * where the answer is nearly always the same. A form that opens empty in
     * every field makes the reader type 0.4 and 1 and 1 on every circuit.
     */
    fun onAddCircuit() {
        val id = projectId
        if (id == Project.NO_ID) return
        viewModelScope.launch {
            _created.value = repository.saveCircuit(
                Circuit(
                    projectId = id,
                    name = "",
                    loadKind = CircuitLoadKind.CURRENT,
                    load = "",
                    powerFactor = DEFAULT_POWER_FACTOR,
                    lengthMetres = "",
                    groupedCircuits = "1",
                    parallelConductors = "1",
                    deviceType = ProtectiveDeviceType.MCB_TYPE_B.name,
                    disconnectionTimeSeconds = DEFAULT_DISCONNECTION,
                    position = 0,
                ),
            )
        }
    }

    fun onCreatedHandled() {
        _created.value = null
    }

    /**
     * The schedule as a comma-separated document, or null when there is nothing
     * to export.
     *
     * Returns the text rather than writing it: the file and the share sheet are
     * platform concerns that belong to the screen, and keeping them out of here
     * is what lets this be tested without an Android runtime.
     */
    fun exportCsv(): ExportedSchedule? {
        val project = _uiState.value.project ?: return null
        val rows = _uiState.value.rows
        if (rows.isEmpty()) return null
        val report = reportBuilder.build(project, rows)
        return ExportedSchedule(reference = project.reference, csv = ReportCsv.render(report))
    }

    fun onDeleteProject() {
        val id = projectId
        if (id == Project.NO_ID) return
        viewModelScope.launch {
            repository.deleteProject(id)
            _uiState.update { it.copy(isGone = true) }
        }
    }

    private companion object {
        const val DEFAULT_POWER_FACTOR = "0.9"

        /** IEC 60364-4-41: 0.4 s for a final circuit on a TN system up to 63 A. */
        const val DEFAULT_DISCONNECTION = "0.4"
    }
}
