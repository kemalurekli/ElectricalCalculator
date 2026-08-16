package com.kemalurekli.electricalcalculator.features.calculators.battery

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryField
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryViewModel
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteItemDao
import com.kemalurekli.electricalcalculator.testing.FakeStringResolver
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
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
class BatteryViewModelTest {

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

    private fun viewModel() = BatteryViewModel(
        calculateRuntime = CalculateBatteryRuntimeUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.bt_history_title to "Battery runtime — %1\$s Ah, %2\$s W",
                R.string.bt_history_summary to "%1\$s h",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun BatteryViewModel.fillValidForm() {
        onCapacityChange("100")
        onVoltageChange("48")
        onLoadPowerChange("500")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `defaults describe a typical lead-acid bank`() {
        val state = viewModel().uiState.value

        assertEquals("20", state.ratedHours)
        assertEquals("90", state.efficiency)
        assertEquals("50", state.depthOfDischarge)
        assertEquals("1.15", state.peukert)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[BatteryField.CAPACITY])
        assertEquals(ValidationError.Required, errors[BatteryField.VOLTAGE])
        assertEquals(ValidationError.Required, errors[BatteryField.LOAD_POWER])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a Peukert exponent below one is rejected`() {
        // Below 1.0 the battery would gain capacity as it is discharged harder,
        // which no chemistry does.
        val model = viewModel()
        model.fillValidForm()
        model.onPeukertChange("0.8")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[BatteryField.PEUKERT])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a depth of discharge above one hundred percent is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onDepthOfDischargeChange("120")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[BatteryField.DEPTH_OF_DISCHARGE])
    }

    @Test
    fun `an efficiency above one hundred percent is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onEfficiencyChange("110")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[BatteryField.EFFICIENCY])
    }

    @Test
    fun `editing a field clears only its own error`() {
        val model = viewModel()
        model.onCalculate()

        model.onCapacityChange("100")

        val errors = model.uiState.value.errors
        assertNull(errors[BatteryField.CAPACITY])
        assertEquals(ValidationError.Required, errors[BatteryField.VOLTAGE])
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `percentages are converted to fractions before calculating`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        // 90 % efficiency, not 90×: 500 W / (48 V × 0.90) = 11.574 A.
        val result = model.uiState.value.result!!
        assertEquals(11.574074, result.dischargeCurrentAmps, 1e-6)
        // 50 % depth of discharge leaves half of 100 Ah usable.
        assertEquals(50.0, result.usableCapacityAh, 1e-9)
    }

    @Test
    fun `a valid form produces the expected figures`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(3.808959, result.runtimeHours, 1e-6)
        assertEquals(4.32, result.idealRuntimeHours, 1e-6)
        assertEquals(0.115741, result.cRate, 1e-6)
    }

    @Test
    fun `a comma decimal separator is accepted`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPeukertChange("1,15")

        model.onCalculate()

        assertNull(model.uiState.value.errors[BatteryField.PEUKERT])
        assertEquals(3.808959, model.uiState.value.result!!.runtimeHours, 1e-6)
    }

    @Test
    fun `editing an input discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onLoadPowerChange("750")

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset restores the defaults`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPeukertChange("1.30")
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.capacityAh)
        assertEquals("1.15", state.peukert)
        assertEquals("20", state.ratedHours)
        assertNull(state.result)
    }

    // -- History ---------------------------------------------------------------------------

    @Test
    fun `a successful calculation is saved to history`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        assertEquals(CalculatorId.BATTERY_RUNTIME, records.single().calculatorId)
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onPeukertChange("1.25")

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("100", inputs["capacity_ah"])
        assertEquals("20", inputs["rated_discharge_hours"])
        assertEquals("48", inputs["bank_voltage"])
        assertEquals("500", inputs["load_watts"])
        // Percentages are stored as typed, not as the fractions passed to the
        // use case, so restoring the form reproduces the user's entry exactly.
        assertEquals("90", inputs["system_efficiency_percent"])
        assertEquals("1.25", inputs["peukert_exponent"])
    }

    @Test
    fun `a failed validation is not saved to history`() = runTest {
        val model = viewModel()
        model.onCalculate()
        advanceUntilIdle()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    // -- Favorites ---------------------------------------------------------------------------

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
