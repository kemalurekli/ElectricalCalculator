package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitField
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitUiState
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitViewModel
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
class ShortCircuitViewModelTest {

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

    private fun viewModel() = ShortCircuitViewModel(
        calculateFault = CalculateShortCircuitUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.sc_history_title to "Short circuit — %1\$s mm², %2\$s m",
                R.string.sc_history_summary to "%1\$s kA",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun ShortCircuitViewModel.fillValidForm() {
        onSupplyCurrentChange("20000")
        onLengthChange("50")
        onCrossSectionChange("25")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `the form opens on a three-phase fault at 400 volts`() {
        val state = viewModel().uiState.value

        assertEquals(FaultType.THREE_PHASE, state.faultType)
        assertEquals("400", state.voltage)
        assertEquals("1", state.parallelConductors)
        assertEquals("0.08", state.reactance)
        assertFalse(state.showNeutralSection)
    }

    // -- The voltage follows the fault type ------------------------------------------

    @Test
    fun `switching to a line-neutral fault moves the default voltage to 230`() {
        // The two faults are driven by different voltages. Leaving 400 V in
        // place would overstate a line–neutral fault current by √3.
        val model = viewModel()

        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)

        assertEquals("230", model.uiState.value.voltage)
        assertTrue(model.uiState.value.showNeutralSection)
    }

    @Test
    fun `switching back restores the line-to-line default`() {
        val model = viewModel()
        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)

        model.onFaultTypeChange(FaultType.THREE_PHASE)

        assertEquals("400", model.uiState.value.voltage)
    }

    @Test
    fun `a voltage the user typed is never overwritten`() {
        // 690 V industrial is not one of the defaults, so the fault-type switch
        // must leave it alone rather than helpfully destroying it.
        val model = viewModel()
        model.onVoltageChange("690")

        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)

        assertEquals("690", model.uiState.value.voltage)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[ShortCircuitField.SUPPLY_CURRENT])
        assertEquals(ValidationError.Required, errors[ShortCircuitField.LENGTH])
        assertEquals(ValidationError.Required, errors[ShortCircuitField.CROSS_SECTION])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `zero length is accepted and gives the current at the origin`() {
        // Not a mistake: it asks what the fault current is before any cable.
        val model = viewModel()
        model.fillValidForm()
        model.onLengthChange("0")

        model.onCalculate()

        assertNull(model.uiState.value.errors[ShortCircuitField.LENGTH])
        assertEquals(20_000.0, model.uiState.value.result!!.maximumFaultCurrentAmps, 1e-6)
    }

    @Test
    fun `zero reactance is accepted as a deliberate simplification`() {
        val model = viewModel()
        model.fillValidForm()
        model.onReactanceChange("0")

        model.onCalculate()

        assertNull(model.uiState.value.errors[ShortCircuitField.REACTANCE])
        assertEquals(0.0, model.uiState.value.result!!.cableReactanceOhms, 1e-12)
    }

    @Test
    fun `a blank neutral section falls back to the line section`() {
        // The usual construction, so it should not be a required field.
        val model = viewModel()
        model.fillValidForm()
        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)

        model.onCalculate()

        assertNull(model.uiState.value.errors[ShortCircuitField.NEUTRAL_SECTION])
        val result = model.uiState.value.result!!
        // Line and neutral equal means exactly double the three-phase loop.
        assertEquals(0.068964, result.cableResistanceColdOhms, 1e-6)
    }

    @Test
    fun `the neutral section is not validated on a three-phase fault`() {
        // The field is hidden, so a stale value must not block the form.
        val model = viewModel()
        model.fillValidForm()
        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)
        model.onNeutralSectionChange("nonsense")
        model.onFaultTypeChange(FaultType.THREE_PHASE)

        model.onCalculate()

        assertNull(model.uiState.value.errors[ShortCircuitField.NEUTRAL_SECTION])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `the conductor count refuses anything but whole numbers`() {
        val model = viewModel()

        model.onParallelChange("2.5")

        assertEquals("25", model.uiState.value.parallelConductors)
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `a valid form produces the expected figures`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(5177.190725, result.maximumFaultCurrentAmps, 1e-3)
        assertEquals(4095.025291, result.minimumFaultCurrentAmps, 1e-3)
    }

    @Test
    fun `the insulation choice moves only the minimum current`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val pvc = model.uiState.value.result!!

        model.onInsulationChange(CableInsulation.XLPE)
        model.onCalculate()
        val xlpe = model.uiState.value.result!!

        assertEquals(pvc.maximumFaultCurrentAmps, xlpe.maximumFaultCurrentAmps, 1e-9)
        assertTrue(xlpe.minimumFaultCurrentAmps < pvc.minimumFaultCurrentAmps)
    }

    @Test
    fun `changing the fault type discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the selections`() {
        val model = viewModel()
        model.fillValidForm()
        model.onMaterialChange(ConductorMaterial.ALUMINIUM)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.supplyCurrent)
        assertEquals(ConductorMaterial.ALUMINIUM, state.material)
        assertEquals(ShortCircuitUiState.DEFAULT_VOLTAGE, state.voltage)
        assertNull(state.result)
    }

    @Test
    fun `reset keeps the phase voltage when the fault type calls for it`() {
        val model = viewModel()
        model.onFaultTypeChange(FaultType.LINE_TO_NEUTRAL)
        model.fillValidForm()
        model.onCalculate()

        model.onReset()

        assertEquals(ShortCircuitUiState.DEFAULT_PHASE_VOLTAGE, model.uiState.value.voltage)
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
        assertEquals(CalculatorId.SHORT_CIRCUIT, records.single().calculatorId)
    }

    @Test
    fun `history is summarised by the minimum current`() = runTest {
        // The figure that decides whether the circuit is protected is the one
        // worth seeing in a list.
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val record = historyRepository.observeAll().first().single()
        assertTrue(record.summary.endsWith("kA"))
        assertNotNull(record.results["minimum_fault_current"])
        assertNotNull(record.results["maximum_fault_current"])
    }

    @Test
    fun `history omits the neutral section when the fault does not use it`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("THREE_PHASE", inputs["fault_type"])
        assertNull(inputs["neutral_cross_section_mm2"])
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
