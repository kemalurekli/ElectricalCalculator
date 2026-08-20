package com.kemalurekli.electricalcalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Theme state for the activity.
 *
 * [MainUiState.Loading] exists so the splash screen can be held for the one
 * frame it takes to read the stored preference. Rendering before then would
 * show the default light theme and then snap to dark, which is the flash every
 * themed app has to design around.
 */
sealed interface MainUiState {

    data object Loading : MainUiState

    data class Ready(val preferences: UserPreferences) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val regionProvider: RegionProvider,
) : ViewModel() {

    init {
        // The first-run guess at supply voltage and frequency. Runs on every
        // launch and does nothing after the first, which is what makes it safe
        // to sit on the path a language change also takes: choosing Turkish
        // recreates the activity and lands here again, finds the values already
        // owned by the user, and leaves them exactly as they are.
        viewModelScope.launch {
            userPreferencesRepository.seedEngineeringDefaults(regionProvider.currentRegion())
        }
    }

    fun onAcceptDisclaimer() {
        viewModelScope.launch { userPreferencesRepository.setDisclaimerAccepted(true) }
    }

    val uiState: StateFlow<MainUiState> = userPreferencesRepository.preferences
        .map<UserPreferences, MainUiState> { MainUiState.Ready(it) }
        .stateIn(
            scope = viewModelScope,
            // Eagerly, not WhileSubscribed: the read starts as the activity is
            // created rather than when composition first collects, which is
            // what keeps the splash hold to a single frame.
            started = SharingStarted.Eagerly,
            initialValue = MainUiState.Loading,
        )
}
