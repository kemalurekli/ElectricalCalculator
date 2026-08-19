package com.kemalurekli.electricalcalculator.features.settings

import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsViewModel
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.testing.FakeAppLanguageRepository
import com.kemalurekli.electricalcalculator.testing.FakeRegionProvider
import com.kemalurekli.electricalcalculator.testing.FakeUserPreferencesRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserPreferencesRepository()
    private val languageRepository = FakeAppLanguageRepository()
    private val viewModel = SettingsViewModel(repository, languageRepository, FakeRegionProvider())

    @Test
    fun `initial state exposes the defaults`() = runTest {
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.preferences.themeMode)
        assertEquals(UnitSystem.METRIC, viewModel.uiState.value.preferences.unitSystem)
        assertTrue(viewModel.uiState.value.versionName.isNotEmpty())
    }

    @Test
    fun `changing the theme mode is reflected in state`() = runTest {
        viewModel.uiState.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().preferences.themeMode)

            viewModel.onThemeModeChange(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().preferences.themeMode)
        }
    }

    @Test
    fun `changing the unit system is reflected in state`() = runTest {
        viewModel.uiState.test {
            assertEquals(UnitSystem.METRIC, awaitItem().preferences.unitSystem)

            viewModel.onUnitSystemChange(UnitSystem.IMPERIAL)
            assertEquals(UnitSystem.IMPERIAL, awaitItem().preferences.unitSystem)
        }
    }

    @Test
    fun `the language defaults to following the system`() = runTest {
        assertEquals(AppLanguage.SYSTEM, viewModel.uiState.value.language)
    }

    @Test
    fun `choosing a language is reflected in state`() = runTest {
        viewModel.uiState.test {
            assertEquals(AppLanguage.SYSTEM, awaitItem().language)

            viewModel.onLanguageChange(AppLanguage.TURKISH)
            assertEquals(AppLanguage.TURKISH, awaitItem().language)
        }
    }

    @Test
    fun `changing one preference leaves the others untouched`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onThemeModeChange(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem().preferences.themeMode)

            viewModel.onUnitSystemChange(UnitSystem.IMPERIAL)
            val state = awaitItem()

            assertEquals(UnitSystem.IMPERIAL, state.preferences.unitSystem)
            // The theme survives an unrelated write.
            assertEquals(ThemeMode.LIGHT, state.preferences.themeMode)
        }
    }
}
