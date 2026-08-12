package com.kemalurekli.electricalcalculator.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.BuildConfig
import com.kemalurekli.electricalcalculator.core.designsystem.theme.supportsDynamicColor
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences.Default,
    val language: AppLanguage = AppLanguage.SYSTEM,
    /** Wallpaper-based colour only exists on Android 12+; hidden elsewhere. */
    val isDynamicColorAvailable: Boolean = supportsDynamicColor,
    val versionName: String = BuildConfig.VERSION_NAME,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val languageRepository: AppLanguageRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.preferences,
        languageRepository.language,
    ) { preferences, language ->
        SettingsUiState(preferences = preferences, language = language)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeChange(themeMode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(themeMode) }
    }

    fun onDynamicColorChange(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }

    fun onUnitSystemChange(unitSystem: UnitSystem) {
        viewModelScope.launch { repository.setUnitSystem(unitSystem) }
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
