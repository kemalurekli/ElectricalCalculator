package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignResult
import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.design.domain.designInputOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class CircuitUiState(
    val project: Project? = null,
    val circuit: Circuit? = null,
    /** Null while the circuit is still missing something the chain needs. */
    val design: CircuitDesignResult? = null,
    val isLoading: Boolean = true,
    val isGone: Boolean = false,
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
@HiltViewModel
class CircuitViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val designCircuit: DesignCircuitUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircuitUiState())
    val uiState: StateFlow<CircuitUiState> = _uiState.asStateFlow()

    private var loadedId: Long = Circuit.NO_ID

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
            state.copy(circuit = updated, design = designOrNull(state.project, updated))
        }
        viewModelScope.launch { repository.saveCircuit(updated) }
    }

    private fun designOrNull(project: Project?, circuit: Circuit?): CircuitDesignResult? {
        if (project == null || circuit == null) return null
        return designInputOrNull(project, circuit)?.let(designCircuit::invoke)
    }
}
