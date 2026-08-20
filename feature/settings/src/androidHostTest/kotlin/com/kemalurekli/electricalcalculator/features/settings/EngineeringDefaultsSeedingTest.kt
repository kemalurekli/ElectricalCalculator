package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsViewModel
import com.kemalurekli.electricalcalculator.testing.FakeAppLanguageRepository
import com.kemalurekli.electricalcalculator.testing.FakeRegionProvider
import com.kemalurekli.electricalcalculator.testing.FakeUserPreferencesRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Engineering defaults are guessed once and then belong to the user.
 *
 * This is the property the feature was asked for: pre-filling every form from
 * the region is worth doing, but a value the user has accepted or edited must
 * never move again — least of all because they changed the app's language.
 *
 * The language change is modelled the way it actually happens. Applying a
 * locale recreates the activity, which builds a fresh [MainViewModel] and so
 * re-runs the startup seeding path; the tests therefore construct a second one
 * rather than asserting on a single instance that never sees the change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EngineeringDefaultsSeedingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserPreferencesRepository()
    private val languageRepository = FakeAppLanguageRepository()
    private val region = FakeRegionProvider("TR")

    /**
     * What the shell does on every launch.
     *
     * `ElecAppShell` calls this from a `LaunchedEffect`; it used to be in
     * `MainViewModel`, which only Android has. Calling the repository directly
     * is what the shell does, minus the composition.
     */
    private suspend fun launch() = repository.seedEngineeringDefaults(region.currentRegion())

    private fun settings() = SettingsViewModel(repository, languageRepository, region)

    private suspend fun stored() = repository.preferences.first().engineering

    @Test
    fun `the first launch seeds from the region`() = runTest {
        region.region = "US"
        launch()
        advanceUntilIdle()

        assertEquals("120", stored().singlePhaseVoltage)
        assertEquals("60", stored().frequency)
        assertTrue(repository.preferences.first().engineeringSeeded)
    }

    @Test
    fun `a language change leaves the engineering defaults untouched`() = runTest {
        launch()
        advanceUntilIdle()

        // The user works on a 690 V industrial supply and says so.
        val theirs = EngineeringDefaults(
            singlePhaseVoltage = "400",
            threePhaseVoltage = "690",
            frequency = "60",
            ambientTemperature = "45",
            material = ConductorMaterial.ALUMINIUM,
            insulation = CableInsulation.XLPE,
        )
        val settings = settings()
        settings.onEngineeringDefaultsChange(theirs)
        advanceUntilIdle()

        // They then switch the app to English and the activity is rebuilt.
        settings.onLanguageChange(AppLanguage.ENGLISH)
        launch()
        advanceUntilIdle()

        assertEquals(AppLanguage.ENGLISH, languageRepository.language.value)
        assertEquals("their supply was re-guessed", theirs, stored())
    }

    @Test
    fun `seeding never runs twice, even before the user changes anything`() = runTest {
        region.region = "DE"
        launch()
        advanceUntilIdle()
        val seeded = stored()

        // The seed for Germany and the seed for the United States differ, so a
        // second run would be visible rather than silently identical.
        region.region = "US"
        launch()
        advanceUntilIdle()

        assertEquals(seeded, stored())
    }

    @Test
    fun `resetting is the one thing that guesses again`() = runTest {
        region.region = "US"
        launch()
        advanceUntilIdle()

        val settings = settings()
        settings.onEngineeringDefaultsChange(
            EngineeringDefaults.Default.copy(threePhaseVoltage = "690"),
        )
        advanceUntilIdle()
        assertEquals("690", stored().threePhaseVoltage)

        settings.onResetEngineeringDefaults()
        advanceUntilIdle()

        assertEquals("208", stored().threePhaseVoltage)
        assertEquals("120", stored().singlePhaseVoltage)
    }

    @Test
    fun `resetting reads the device, not the app language`() = runTest {
        // The exact case the region provider exists for: an engineer in the
        // United States reading the app in Turkish. The app's own picker stores
        // a bare `tr` with no country, so anything reading the app locale would
        // hand them a 400 V supply they have never worked on.
        region.region = "US"
        launch()
        advanceUntilIdle()

        val settings = settings()
        settings.onLanguageChange(AppLanguage.TURKISH)
        settings.onResetEngineeringDefaults()
        advanceUntilIdle()

        assertEquals("208", stored().threePhaseVoltage)
        assertEquals("60", stored().frequency)
    }
}
