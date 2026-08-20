package com.kemalurekli.electricalcalculator.features.calculators.cablesize

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.core.domain.table.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeField
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CableSizeViewModelTest {

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

    private fun viewModel() = CableSizeViewModel(
        calculateCableSize = CalculateCableSizeUseCase(AmpacityTable(), CorrectionFactors()),
        correctionFactors = CorrectionFactors(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        // Templates are keyed by id rather than sharing one default: the title
        // takes two arguments and the summary one, and `String.format` throws
        // if a template references an argument that was not supplied.
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.cs_history_title to "Cable size — %1\$s A, %2\$s m",
                R.string.cs_history_summary to "%1\$s mm²",
            ),
        ),
        timeProvider = timeProvider,
        userPreferences = FakeUserPreferencesRepository(),
    )

    private fun CableSizeViewModel.fillValidForm() {
        onVoltageChange("230")
        onCurrentChange("25")
        onLengthChange("30")
    }

    // -- Defaults --------------------------------------------------------------

    @Test
    fun `defaults follow the reference conditions of the tables`() {
        val state = viewModel().uiState.value

        assertEquals("30", state.ambientTemperature)
        assertEquals("1", state.groupedCircuits)
        assertEquals("5", state.maxDropPercent)
        assertEquals(CableInsulation.PVC, state.insulation)
        assertNull(state.result)
    }

    // -- Validation ------------------------------------------------------------

    @Test
    fun `an empty form reports every required field`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        // Voltage is not listed: it arrives pre-filled with the system default.
        assertNull(errors[CableSizeField.VOLTAGE])
        assertEquals(ValidationError.Required, errors[CableSizeField.CURRENT])
        assertEquals(ValidationError.Required, errors[CableSizeField.LENGTH])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `an ambient beyond the correction table is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        // PVC corrections stop at 60 °C; past that the cable has no rating.
        model.onAmbientChange("80")

        model.onCalculate()

        assertEquals(
            ValidationError.OutOfRange(-20.0, 60.0),
            model.uiState.value.errors[CableSizeField.AMBIENT],
        )
    }

    @Test
    fun `XLPE accepts a higher ambient than PVC`() {
        val model = viewModel()
        model.fillValidForm()
        model.onInsulationChange(CableInsulation.XLPE)
        model.onAmbientChange("75")

        model.onCalculate()

        assertNull(model.uiState.value.errors[CableSizeField.AMBIENT])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `power factor is not validated on a dc supply`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.DC)
        model.onPowerFactorChange("7")

        model.onCalculate()

        assertNull(model.uiState.value.errors[CableSizeField.POWER_FACTOR])
        assertNotNull(model.uiState.value.result)
    }

    // -- Result lifecycle ------------------------------------------------------

    @Test
    fun `a valid form produces a recommended size`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result
        assertNotNull(result)
        assertEquals(4.0, result!!.recommendedAreaMm2!!, 1e-9)
    }

    @Test
    fun `editing an input discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        assertNotNull(model.uiState.value.result)

        model.onLengthChange("60")

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `changing the installation method discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onInsulationChange(CableInsulation.XLPE)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset keeps the cable selections but clears the numbers`() {
        val model = viewModel()
        model.fillValidForm()
        model.onInsulationChange(CableInsulation.XLPE)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals(SystemVoltageDefaults.SINGLE_PHASE, state.voltage)
        assertEquals(CableInsulation.XLPE, state.insulation)
        assertNull(state.result)
    }

    // -- History ----------------------------------------------------------------

    @Test
    fun `a successful sizing is saved to history`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        assertEquals(CalculatorId.CABLE_SIZE, records.single().calculatorId)
    }

    @Test
    fun `a run with no viable size is not saved to history`() = runTest {
        val model = viewModel()
        model.onVoltageChange("230")
        model.onCurrentChange("5000")
        model.onLengthChange("10")

        model.onCalculate()
        advanceUntilIdle()

        assertFalse(model.uiState.value.result!!.hasSolution)
        // Nothing to reopen, so nothing is stored.
        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onInsulationChange(CableInsulation.XLPE)
        model.onAmbientChange("40")

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("230", inputs["voltage"])
        assertEquals("25", inputs["current"])
        assertEquals("XLPE", inputs["insulation"])
        assertEquals("40", inputs["ambient_temperature"])
    }

    // -- Favorites ---------------------------------------------------------------

    @Test
    fun `toggling favorite is reflected in state`() = runTest {
        val model = viewModel()
        advanceUntilIdle()
        assertFalse(model.uiState.value.isFavorite)

        model.onToggleFavorite()
        advanceUntilIdle()

        assertTrue(model.uiState.value.isFavorite)
    }
}
