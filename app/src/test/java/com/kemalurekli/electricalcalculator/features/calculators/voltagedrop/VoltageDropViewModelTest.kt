package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop

import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropField
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropUiState
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoltageDropViewModelTest {

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

    private fun viewModel() = VoltageDropViewModel(
        calculateVoltageDrop = CalculateVoltageDropUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        // The history title and summary are format templates; the placeholders
        // keep `String.format` working without real Android resources.
        stringResolver = FakeStringResolver(default = "%s / %s"),
        timeProvider = timeProvider,
    )

    private fun VoltageDropViewModel.fillValidForm() {
        onVoltageChange("230")
        onCurrentChange("20")
        onLengthChange("30")
        onCrossSectionChange("4")
        onTemperatureChange("20")
    }

    // -- Defaults -----------------------------------------------------------

    @Test
    fun `defaults are conservative and standards-based`() {
        val state = viewModel().uiState.value

        assertEquals(SupplySystem.SINGLE_PHASE_AC, state.system)
        assertEquals(ConductorMaterial.COPPER, state.material)
        // Unity power factor: with reactance neglected, anything lower would
        // report a smaller drop than reality.
        assertEquals("1", state.powerFactor)
        // 70 °C is the PVC conductor rating at full load.
        assertEquals("70", state.temperature)
        assertEquals("1", state.parallelConductors)
        assertNull(state.result)
    }

    // -- Validation ---------------------------------------------------------

    @Test
    fun `calculating an empty form reports every required field`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        // Voltage is not listed: it arrives pre-filled with the system default.
        assertNull(errors[VoltageDropField.VOLTAGE])
        assertEquals(ValidationError.Required, errors[VoltageDropField.CURRENT])
        assertEquals(ValidationError.Required, errors[VoltageDropField.LENGTH])
        assertEquals(ValidationError.Required, errors[VoltageDropField.CROSS_SECTION])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `zero cross section is rejected rather than dividing by zero`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCrossSectionChange("0")

        model.onCalculate()

        assertEquals(
            ValidationError.MustBePositive,
            model.uiState.value.errors[VoltageDropField.CROSS_SECTION],
        )
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `power factor above one is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onPowerFactorChange("1.5")

        model.onCalculate()

        assertEquals(
            ValidationError.OutOfRange(0.01, 1.0),
            model.uiState.value.errors[VoltageDropField.POWER_FACTOR],
        )
    }

    @Test
    fun `power factor is not validated on a dc supply`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.DC)
        // Invalid, but the field is hidden on DC so it must not block the run.
        model.onPowerFactorChange("9")

        model.onCalculate()

        assertNull(model.uiState.value.errors[VoltageDropField.POWER_FACTOR])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `sub-zero conductor temperature is accepted`() {
        val model = viewModel()
        model.fillValidForm()
        model.onTemperatureChange("-20")

        model.onCalculate()

        assertNull(model.uiState.value.errors[VoltageDropField.TEMPERATURE])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `editing a field clears only its own error`() {
        val model = viewModel()
        model.onCalculate()
        assertTrue(model.uiState.value.errors.size >= 3)

        model.onVoltageChange("230")

        val errors = model.uiState.value.errors
        assertNull(errors[VoltageDropField.VOLTAGE])
        assertEquals(ValidationError.Required, errors[VoltageDropField.CURRENT])
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

    // -- Result lifecycle ----------------------------------------------------

    @Test
    fun `a valid form produces a result`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result
        assertNotNull(result)
        assertEquals(5.1723, result!!.voltageDrop, 1e-4)
        assertTrue(model.uiState.value.errors.isEmpty())
    }

    @Test
    fun `editing an input discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        assertNotNull(model.uiState.value.result)

        model.onLengthChange("60")

        // Leaving the old figure on screen would show a number that no longer
        // matches the form above it.
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `changing the supply system discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onSystemChange(SupplySystem.THREE_PHASE_AC)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears inputs but keeps the system and material selection`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.THREE_PHASE_AC)
        model.onMaterialChange(ConductorMaterial.ALUMINIUM)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        // The three-phase selection survives the reset, so the voltage it
        // restores is that system's default rather than the form's opening one.
        assertEquals(SystemVoltageDefaults.THREE_PHASE, state.voltage)
        assertEquals("", state.current)
        assertNull(state.result)
        assertEquals(SupplySystem.THREE_PHASE_AC, state.system)
        assertEquals(ConductorMaterial.ALUMINIUM, state.material)
        assertEquals(VoltageDropUiState.DEFAULT_TEMPERATURE, state.temperature)
    }

    // -- History -------------------------------------------------------------

    @Test
    fun `a successful calculation is saved to history`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        assertEquals(CalculatorId.VOLTAGE_DROP, records.single().calculatorId)
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.THREE_PHASE_AC)
        model.onMaterialChange(ConductorMaterial.ALUMINIUM)

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("230", inputs["voltage"])
        assertEquals("20", inputs["current"])
        assertEquals("30", inputs["length"])
        assertEquals("4", inputs["cross_section"])
        assertEquals("THREE_PHASE_AC", inputs["system"])
        assertEquals("ALUMINIUM", inputs["material"])
    }

    @Test
    fun `a failed validation is not saved to history`() = runTest {
        val model = viewModel()
        model.onCalculate()
        advanceUntilIdle()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    // -- Favorites -----------------------------------------------------------

    @Test
    fun `toggling favorite is reflected in state`() = runTest {
        val model = viewModel()
        advanceUntilIdle()
        assertEquals(false, model.uiState.value.isFavorite)

        model.onToggleFavorite()
        advanceUntilIdle()

        assertEquals(true, model.uiState.value.isFavorite)
    }
}
