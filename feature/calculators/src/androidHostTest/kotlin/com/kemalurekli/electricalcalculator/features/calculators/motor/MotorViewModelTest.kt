package com.kemalurekli.electricalcalculator.features.calculators.motor

import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorField
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorUiState
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorViewModel
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
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_history_title

@OptIn(ExperimentalCoroutinesApi::class)
class MotorViewModelTest {

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

    private fun viewModel() = MotorViewModel(
        calculateMotorCurrent = CalculateMotorCurrentUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                Res.string.mt_history_title to "Motor — %1\$s, %2\$s V",
                Res.string.mt_history_summary to "%1\$s A",
            ),
        ),
        timeProvider = timeProvider,
        userPreferences = FakeUserPreferencesRepository(),
    )

    private fun MotorViewModel.fillValidForm() {
        onPowerChange("7.5")
        onVoltageChange("400")
    }

    // -- Defaults ----------------------------------------------------------------

    @Test
    fun `defaults describe a typical three phase induction motor`() {
        val state = viewModel().uiState.value

        assertEquals(SupplySystem.THREE_PHASE_AC, state.system)
        assertEquals(PowerUnit.KILOWATT, state.powerUnit)
        assertEquals("90", state.efficiency)
        assertEquals(0.85, NumberFormatter.parseOrNull(state.powerFactor)!!, 1e-12)
        assertEquals("6", state.startingRatio)
    }

    // -- Validation ----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[MotorField.POWER])
        // Voltage is not listed: it arrives pre-filled with the system default.
        assertNull(errors[MotorField.VOLTAGE])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `efficiency above one hundred percent is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onEfficiencyChange("120")

        model.onCalculate()

        assertEquals(
            ValidationError.OutOfRange(1.0, 100.0),
            model.uiState.value.errors[MotorField.EFFICIENCY],
        )
    }

    @Test
    fun `power factor is not validated on a dc supply`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.DC)
        model.onPowerFactorChange("8")

        model.onCalculate()

        assertNull(model.uiState.value.errors[MotorField.POWER_FACTOR])
        assertNotNull(model.uiState.value.result)
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `efficiency entered as a percentage is applied as a fraction`() {
        val model = viewModel()
        model.fillValidForm()
        model.onEfficiencyChange("90")

        model.onCalculate()

        // 7.5 kW / 0.90 = 8333 W input, not 7.5 kW / 90.
        assertEquals(8_333.333, model.uiState.value.result!!.inputPowerWatts, 1e-3)
    }

    @Test
    fun `a valid form produces the expected current`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        assertEquals(14.1507, model.uiState.value.result!!.fullLoadCurrent, 1e-4)
    }

    @Test
    fun `switching the power unit changes the result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val kilowatts = model.uiState.value.result!!.fullLoadCurrent

        model.onPowerUnitChange(PowerUnit.HORSEPOWER)
        model.onCalculate()
        val horsepower = model.uiState.value.result!!.fullLoadCurrent

        // 7.5 HP is less shaft power than 7.5 kW.
        assertTrue(horsepower < kilowatts)
    }

    @Test
    fun `editing an input discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        assertNotNull(model.uiState.value.result)

        model.onVoltageChange("230")

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the unit and phase selection`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerUnitChange(PowerUnit.HORSEPOWER)
        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.ratedPower)
        assertEquals(PowerUnit.HORSEPOWER, state.powerUnit)
        assertEquals(SupplySystem.SINGLE_PHASE_AC, state.system)
        assertEquals(MotorUiState.DEFAULT_EFFICIENCY, state.efficiency)
        assertNull(state.result)
    }


    // -- The pre-filled voltage ---------------------------------------------------------

    @Test
    fun `the form opens with the voltage its supply system implies`() {
        // Pre-filled rather than blank: it is the most predictable quantity in
        // the app, and asking for it every time is asking for work rather than
        // for information.
        val state = viewModel().uiState.value

        assertEquals(SystemVoltageDefaults.forSystem(state.system), state.voltage)
    }

    @Test
    fun `an untouched voltage follows the supply system`() {
        val model = viewModel()

        model.onSystemChange(SupplySystem.THREE_PHASE_AC)
        assertEquals(SystemVoltageDefaults.THREE_PHASE, model.uiState.value.voltage)

        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        assertEquals(SystemVoltageDefaults.SINGLE_PHASE, model.uiState.value.voltage)
    }

    @Test
    fun `a voltage the user typed survives a change of supply system`() {
        // Including one that happens to match a default: 230 on three phase is
        // a real delta system, not a leftover.
        val model = viewModel()
        model.onVoltageChange("230")

        model.onSystemChange(SupplySystem.THREE_PHASE_AC)

        assertEquals("230", model.uiState.value.voltage)
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
        assertEquals(CalculatorId.MOTOR_CURRENT, records.single().calculatorId)
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerUnitChange(PowerUnit.HORSEPOWER)

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("7.5", inputs["rated_power"])
        assertEquals("400", inputs["voltage"])
        assertEquals("HORSEPOWER", inputs["power_unit"])
        assertEquals("90", inputs["efficiency_percent"])
    }

    @Test
    fun `a failed validation is not saved to history`() = runTest {
        val model = viewModel()
        model.onCalculate()
        advanceUntilIdle()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    // -- Favorites --------------------------------------------------------------------------

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
