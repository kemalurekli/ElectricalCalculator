package com.kemalurekli.electricalcalculator.features.calculators.powerfactor

import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorField
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorViewModel
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
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pf_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pf_history_title

@OptIn(ExperimentalCoroutinesApi::class)
class PowerFactorViewModelTest {

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

    private fun viewModel() = PowerFactorViewModel(
        calculateCorrection = CalculatePowerFactorCorrectionUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                Res.string.pf_history_title to "Power factor — %1\$s → %2\$s",
                Res.string.pf_history_summary to "%1\$s kvar",
            ),
        ),
        timeProvider = timeProvider,
        userPreferences = FakeUserPreferencesRepository(),
    )

    private fun PowerFactorViewModel.fillValidForm() {
        onActivePowerChange("100")
        onExistingFactorChange("0.75")
        onVoltageChange("400")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `defaults target a common tariff threshold at 50 hertz`() {
        val state = viewModel().uiState.value

        assertEquals(0.95, NumberFormatter.parseOrNull(state.targetFactor)!!, 1e-12)
        assertEquals("50", state.frequency)
        assertEquals(CapacitorConnection.DELTA, state.connection)
        assertEquals(SupplySystem.THREE_PHASE_AC, state.system)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[PowerFactorField.ACTIVE_POWER])
        assertEquals(ValidationError.Required, errors[PowerFactorField.EXISTING_FACTOR])
        // Voltage is not listed: it arrives pre-filled with the system default.
        assertNull(errors[PowerFactorField.VOLTAGE])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a target below the existing factor is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onExistingFactorChange("0.95")
        model.onTargetFactorChange("0.80")

        model.onCalculate()

        // Correcting downward would ask for a negative capacitor.
        assertEquals(
            ValidationError.BelowMinimum(0.95),
            model.uiState.value.errors[PowerFactorField.TARGET_FACTOR],
        )
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a target equal to the existing factor is allowed and needs nothing`() {
        val model = viewModel()
        model.fillValidForm()
        model.onExistingFactorChange("0.95")
        model.onTargetFactorChange("0.95")

        model.onCalculate()

        assertNull(model.uiState.value.errors[PowerFactorField.TARGET_FACTOR])
        assertEquals(0.0, model.uiState.value.result!!.requiredCapacitorVar, 1e-6)
    }

    @Test
    fun `a power factor above one is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onExistingFactorChange("1.4")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[PowerFactorField.EXISTING_FACTOR])
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `kilowatts are converted to watts before calculating`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        // 100 kW, not 100 W: the required bank is ~55 kvar.
        assertEquals(55_323.3, model.uiState.value.result!!.requiredCapacitorVar, 0.1)
    }

    @Test
    fun `a valid form produces the expected figures`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(28_070.2, result.releasedCapacityVa, 0.1)
        assertEquals(192.4501, result.currentBeforeAmps, 1e-4)
        assertEquals(151.9343, result.currentAfterAmps, 1e-4)
        assertEquals(366.874e-6, result.capacitancePerPhaseFarads, 1e-9)
    }

    @Test
    fun `switching to star triples the capacitance`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val delta = model.uiState.value.result!!.capacitancePerPhaseFarads

        model.onConnectionChange(CapacitorConnection.STAR)
        model.onCalculate()
        val star = model.uiState.value.result!!.capacitancePerPhaseFarads

        assertEquals(3.0, star / delta, 1e-9)
    }

    @Test
    fun `changing the connection discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onConnectionChange(CapacitorConnection.STAR)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the selections`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.activePowerKw)
        assertEquals(SupplySystem.SINGLE_PHASE_AC, state.system)
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
        assertEquals(CalculatorId.POWER_FACTOR_CORRECTION, records.single().calculatorId)
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onConnectionChange(CapacitorConnection.STAR)

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("100", inputs["active_power_kw"])
        assertEquals("0.75", inputs["existing_power_factor"])
        assertEquals(0.95, NumberFormatter.parseOrNull(inputs["target_power_factor"]!!)!!, 1e-12)
        assertEquals("STAR", inputs["connection"])
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
