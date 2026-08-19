package com.kemalurekli.electricalcalculator.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.BuildConfig
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences.Default,
    val language: AppLanguage = AppLanguage.SYSTEM,
    /** What every calculator opens with; see [EngineeringDefaults]. */
    val engineering: EngineeringDefaults = EngineeringDefaults.Default,
    val versionName: String = BuildConfig.VERSION_NAME,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val languageRepository: AppLanguageRepository,
    private val regionProvider: RegionProvider,
) : ViewModel() {

    /**
     * The engineering defaults as edited on this screen, ahead of the store.
     *
     * The voltage and temperature controls are text fields, and a text field
     * whose value only arrives back through DataStore drops characters when
     * typed at speed: the field re-renders with the value from before the last
     * keystroke. Holding the edit here makes it immediate; the write still
     * happens, it just no longer sits between the key and the screen.
     *
     * Null until the first edit, so the screen opens on what is stored.
     */
    private val engineeringEdits = MutableStateFlow<EngineeringDefaults?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.preferences,
        languageRepository.language,
        engineeringEdits,
    ) { preferences, language, edits ->
        SettingsUiState(
            preferences = preferences,
            language = language,
            engineering = edits ?: preferences.engineering,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeChange(themeMode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(themeMode) }
    }

    fun onUnitSystemChange(unitSystem: UnitSystem) {
        viewModelScope.launch { repository.setUnitSystem(unitSystem) }
    }

    fun onEngineeringDefaultsChange(defaults: EngineeringDefaults) {
        engineeringEdits.value = defaults
        viewModelScope.launch { repository.setEngineeringDefaults(defaults) }
    }

    /**
     * Puts the engineering defaults back to the guess a fresh install would make.
     *
     * The only thing in the app allowed to re-derive these from the environment,
     * and it is here because the user asked for it. Nothing does this on a
     * language change: someone switching the app to Turkish for readability has
     * not moved to a 230 V country, and their 480 V site must survive the change.
     */
    fun onResetEngineeringDefaults() {
        onEngineeringDefaultsChange(EngineeringDefaults.seedFor(regionProvider.current()))
    }

    /**
     * Applies a new display language.
     *
     * Not launched in a coroutine: the platform applies the locale and
     * recreates the activity synchronously, and deferring it to a scope that
     * the recreation may cancel would drop the change.
     */
    fun onLanguageChange(language: AppLanguage) {
        languageRepository.setLanguage(language)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
