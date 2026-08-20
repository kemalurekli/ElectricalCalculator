package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectSummary
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

@Immutable
data class ProjectsUiState(
    val projects: ImmutableList<ProjectSummary> = persistentListOf(),
    /** Tells "nothing saved" apart from "not read yet", so the empty state does not flash. */
    val isLoading: Boolean = true,
)

class ProjectsViewModel(
    private val repository: ProjectRepository,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<ProjectsUiState> = repository.observeProjects()
        .map { ProjectsUiState(projects = it.toImmutableList(), isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ProjectsUiState(),
        )

    /**
     * The project a tap on "new" just created, for the screen to open.
     *
     * A one-shot value rather than a callback parameter: the row id only exists
     * after the insert has come back, which is after the tap has been handled.
     */
    private val _created = MutableStateFlow<Long?>(null)
    val created: StateFlow<Long?> = _created.asStateFlow()

    /**
     * Starts a job from the user's own engineering defaults.
     *
     * A new project is not blank. The supply, cable type and ambient are
     * exactly the settings the user has already given the app, and asking for
     * them again on every job is the friction the whole feature exists to
     * remove — they remain editable per project for the job that differs.
     */
    fun onCreateProject() {
        viewModelScope.launch {
            val defaults = userPreferences.engineeringDefaults()
            val id = repository.saveProject(
                Project(
                    reference = "",
                    site = "",
                    system = SupplySystem.THREE_PHASE_AC,
                    systemVoltage = defaults.threePhaseVoltage,
                    material = defaults.material,
                    insulation = defaults.insulation,
                    method = defaults.installationMethod,
                    ambientTemperatureC = defaults.ambientTemperature,
                    maxVoltageDropPercent = DEFAULT_MAX_DROP,
                    externalImpedanceOhms = DEFAULT_ZE,
                    // Overwritten by the repository, which owns these.
                    createdAt = Instant.fromEpochMilliseconds(0),
                    updatedAt = Instant.fromEpochMilliseconds(0),
                ),
            )
            _created.value = id
        }
    }

    fun onCreatedHandled() {
        _created.value = null
    }

    fun onDeleteProject(id: Long) {
        viewModelScope.launch { repository.deleteProject(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L

        /** IEC 60364-5-52 gives 4 % as informative guidance, and it is the usual starting point. */
        const val DEFAULT_MAX_DROP = "4"

        /**
         * A placeholder Ze, not a measurement.
         *
         * 0.35 Ω is a common TN-C-S figure, low enough to be plausible and high
         * enough that a reader who leaves it alone still gets a design that is
         * not silently optimistic. The field asks to be replaced with the
         * measured value.
         */
        const val DEFAULT_ZE = "0.35"
    }
}
