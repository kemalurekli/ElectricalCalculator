package com.kemalurekli.electricalcalculator.features.calculators

import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillViewModel
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.conduitFillExamples
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropViewModel
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.voltageDropExamples
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteItemDao
import com.kemalurekli.electricalcalculator.testing.FakeStringResolver
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import com.kemalurekli.electricalcalculator.testing.FakeUserPreferencesRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cf_export_cable_line

/**
 * Reopening a saved calculation puts the form back where it was.
 *
 * A history record stores the text the reader typed rather than the parsed
 * numbers, so the round trip is meant to be exact: work an example, read the
 * record back, and the same result must come out. If a calculator gains a field
 * and forgets to record it, that promise breaks here rather than on a phone.
 *
 * Two calculators stand in for the two shapes the mapping takes. Voltage drop
 * carries enums and plain fields; conduit fill carries a *list* of cable rows,
 * whose readable form ("3 × Ø 8.5 mm") is localised and unparseable — it keeps a
 * separate machine-readable copy, and this is the test that says so.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryRestoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timeProvider = FakeTimeProvider()
    private val historyRepository: HistoryRepository = HistoryRepositoryImpl(
        dao = FakeCalculationHistoryDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )
    private val favoritesRepository = FavoritesRepositoryImpl(
        dao = FakeFavoriteItemDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )
    private val stringResolver = FakeStringResolver(
        mapOf(Res.string.cf_export_cable_line to "%1\$s × Ø %2\$s mm"),
    )

    private fun voltageDrop() = VoltageDropViewModel(
        calculateVoltageDrop = CalculateVoltageDropUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = stringResolver,
        timeProvider = timeProvider,
        userPreferences = FakeUserPreferencesRepository(),
    )

    private fun conduitFill() = ConduitFillViewModel(
        calculateFill = CalculateConduitFillUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = stringResolver,
        timeProvider = timeProvider,
    )

    @Test
    fun `every voltage drop example survives a save and a reopen`() = runTest {
        voltageDropExamples.forEach { example ->
            // Records are ordered newest first; without moving the clock they
            // would all share a timestamp and "the one just saved" would be
            // whichever the database happened to return.
            timeProvider.advanceBy(ONE_MINUTE)
            val worked = voltageDrop()
            worked.onApplyExample(example)
            advanceUntilIdle()

            val expected = worked.uiState.value
            assertNotNull("${example.key} produced no result to save", expected.result)

            val record = historyRepository.observeAll().first().first()
            val reopened = voltageDrop()
            reopened.onRestore(record.id)
            advanceUntilIdle()

            val restored = reopened.uiState.value
            assertEquals("${example.key}: system", expected.system, restored.system)
            assertEquals("${example.key}: material", expected.material, restored.material)
            assertEquals("${example.key}: voltage", expected.voltage, restored.voltage)
            assertEquals("${example.key}: current", expected.current, restored.current)
            assertEquals("${example.key}: length", expected.length, restored.length)
            assertEquals("${example.key}: section", expected.crossSection, restored.crossSection)
            // The whole point: the same inputs give the same answer back.
            assertEquals("${example.key}: result", expected.result, restored.result)
            assertTrue("${example.key} reopened with errors", restored.errors.isEmpty())
        }
    }

    @Test
    fun `a conduit fill reopens with its cable rows, not just its scalars`() = runTest {
        val example = conduitFillExamples.first { it.key == "mixed_bundle" }
        val worked = conduitFill()
        worked.onApplyExample(example)
        advanceUntilIdle()

        val expected = worked.uiState.value
        assertTrue("The example should carry more than one row", expected.cables.size > 1)

        val record = historyRepository.observeAll().first().first()
        val reopened = conduitFill()
        reopened.onRestore(record.id)
        advanceUntilIdle()

        val restored = reopened.uiState.value
        assertEquals(
            "quantities",
            expected.cables.map { it.quantity },
            restored.cables.map { it.quantity },
        )
        assertEquals(
            "diameters",
            expected.cables.map { it.diameter },
            restored.cables.map { it.diameter },
        )
        assertEquals("result", expected.result, restored.result)
    }

    @Test
    fun `a record from another calculator is ignored rather than half-applied`() = runTest {
        // The route carries an id and a calculator key independently, so a stale
        // deep link can pair them wrongly. Loading one calculator's inputs into
        // another would silently produce a plausible, wrong answer.
        val worked = voltageDrop()
        worked.onApplyExample(voltageDropExamples.first())
        advanceUntilIdle()
        val foreign = historyRepository.observeAll().first().first()

        val fill = conduitFill()
        val before = fill.uiState.value
        fill.onRestore(foreign.id)
        advanceUntilIdle()

        assertEquals(before, fill.uiState.value)
    }

    private companion object {
        const val ONE_MINUTE = 60_000L
    }
}
