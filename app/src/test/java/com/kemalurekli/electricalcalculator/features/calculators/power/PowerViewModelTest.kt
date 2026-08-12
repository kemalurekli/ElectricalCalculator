package com.kemalurekli.electricalcalculator.features.calculators.power

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerField
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerViewModel
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteCalculatorDao
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
class PowerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timeProvider = FakeTimeProvider()
    private val historyRepository: HistoryRepository = HistoryRepositoryImpl(
        dao = FakeCalculationHistoryDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )
    private val favoritesRepository = FavoritesRepositoryImpl(
        dao = FakeFavoriteCalculatorDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun viewModel() = PowerViewModel(
        calculatePower = CalculatePowerUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.pw_history_title to "Power — %1\$s V, %2\$s A",
                R.string.pw_history_summary to "%1\$s kW",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun PowerViewModel.fillValidForm() {
        onVoltageChange("400")
        onCurrentChange("100")
    }

    // -- Defaults -------------------------------------------------------------------

    @Test
    fun `defaults describe a typical industrial supply`() {
        val state = viewModel().uiState.value

        assertEquals(SupplySystem.THREE_PHASE_AC, state.system)
        assertEquals(PowerFactorType.LAGGING, state.powerFactorType)
        assertEquals("0.85", state.powerFactor)
    }

    // -- Validation ------------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        // Voltage is not listed: it arrives pre-filled with the system default.
        assertNull(errors[PowerField.VOLTAGE])
        assertEquals(ValidationError.Required, errors[PowerField.CURRENT])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a power factor above one is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerFactorChange("1.2")

        model.onCalculate()

        assertEquals(
            ValidationError.OutOfRange(0.0, 1.0),
            model.uiState.value.errors[PowerField.POWER_FACTOR],
        )
    }

    @Test
    fun `a zero power factor is accepted as a purely reactive load`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerFactorChange("0")

        model.onCalculate()

        assertNull(model.uiState.value.errors[PowerField.POWER_FACTOR])
        assertEquals(0.0, model.uiState.value.result!!.activePowerWatts, 1e-9)
    }

    @Test
    fun `power factor is not validated on a dc supply`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.DC)
        model.onPowerFactorChange("5")

        model.onCalculate()

        assertNull(model.uiState.value.errors[PowerField.POWER_FACTOR])
        assertNotNull(model.uiState.value.result)
    }

    // -- Result -----------------------------------------------------------------------------

    @Test
    fun `a valid form produces the expected triangle`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(69_282.0323, result.apparentPowerVa, 1e-4)
        assertEquals(58_889.7275, result.activePowerWatts, 1e-4)
        assertEquals(36_496.5752, result.reactivePowerVar, 1e-4)
    }

    @Test
    fun `switching to leading flips the sign of reactive power`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val lagging = model.uiState.value.result!!.reactivePowerVar

        model.onPowerFactorTypeChange(PowerFactorType.LEADING)
        model.onCalculate()
        val leading = model.uiState.value.result!!.reactivePowerVar

        assertEquals(-lagging, leading, 1e-9)
    }

    @Test
    fun `changing the direction discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onPowerFactorTypeChange(PowerFactorType.LEADING)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the selections`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        model.onPowerFactorTypeChange(PowerFactorType.LEADING)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        // Reset keeps the single-phase selection, so it restores that system's
        // voltage rather than snapping back to the three-phase default.
        assertEquals(SystemVoltageDefaults.SINGLE_PHASE, state.voltage)
        assertEquals(SupplySystem.SINGLE_PHASE_AC, state.system)
        assertEquals(PowerFactorType.LEADING, state.powerFactorType)
        assertNull(state.result)
    }

    // -- History ------------------------------------------------------------------------------

    @Test
    fun `a successful calculation is saved to history`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        assertEquals(CalculatorId.POWER, records.single().calculatorId)
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerFactorTypeChange(PowerFactorType.LEADING)

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("400", inputs["voltage"])
        assertEquals("100", inputs["current"])
        assertEquals("LEADING", inputs["power_factor_type"])
    }

    @Test
    fun `a failed validation is not saved to history`() = runTest {
        val model = viewModel()
        model.onCalculate()
        advanceUntilIdle()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    // -- Favorites -----------------------------------------------------------------------------

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
