package com.kemalurekli.electricalcalculator.features.calculators.conduitfill

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillField
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillUiState
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillViewModel
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
class ConduitFillViewModelTest {

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

    private fun viewModel() = ConduitFillViewModel(
        calculateFill = CalculateConduitFillUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.cf_history_title to "Conduit fill — %1\$s mm, %2\$s cables",
                R.string.cf_history_summary to "%1\$s %%",
                R.string.cf_export_cable_line to "%1\$s × Ø %2\$s mm",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun ConduitFillViewModel.fillValidForm() {
        onConduitDiameterChange("25")
        val firstRow = uiState.value.cables.first().id
        onCableDiameterChange(firstRow, "8.5")
        onCableQuantityChange(firstRow, "3")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `the form starts with one empty cable row`() {
        val state = viewModel().uiState.value

        assertEquals(1, state.cables.size)
        assertEquals("", state.cables.single().diameter)
        assertEquals("1", state.cables.single().quantity)
        assertEquals(FillRule.NEC_TABLE_1, state.rule)
        assertFalse(state.showCustomLimit)
        // The last row cannot be removed: there would be nothing to calculate.
        assertFalse(state.canRemoveCable)
    }

    // -- The cable list -----------------------------------------------------------------

    @Test
    fun `adding a cable appends an empty row with a fresh id`() {
        val model = viewModel()

        model.onAddCable()

        val cables = model.uiState.value.cables
        assertEquals(2, cables.size)
        assertEquals(2, cables.map { it.id }.distinct().size)
        assertTrue(model.uiState.value.canRemoveCable)
    }

    @Test
    fun `removing a row leaves the others untouched`() {
        val model = viewModel()
        model.onAddCable()
        model.onAddCable()
        val (first, second, third) = model.uiState.value.cables.map { it.id }
        model.onCableDiameterChange(first, "8.5")
        model.onCableDiameterChange(third, "11.9")

        model.onRemoveCable(second)

        val cables = model.uiState.value.cables
        assertEquals(2, cables.size)
        assertEquals("8.5", cables[0].diameter)
        assertEquals("11.9", cables[1].diameter)
    }

    @Test
    fun `a removed row's id is never handed to a new row`() {
        // Ids identify rows across edits; reuse would let an old error or an
        // in-flight edit land on the wrong cable.
        val model = viewModel()
        model.onAddCable()
        val removed = model.uiState.value.cables.last().id
        model.onRemoveCable(removed)

        model.onAddCable()

        assertFalse(model.uiState.value.cables.any { it.id == removed })
    }

    @Test
    fun `the last row cannot be removed`() {
        val model = viewModel()
        val only = model.uiState.value.cables.single().id

        model.onRemoveCable(only)

        assertEquals(1, model.uiState.value.cables.size)
    }

    @Test
    fun `the list stops growing at the form limit`() {
        val model = viewModel()

        repeat(ConduitFillUiState.MAX_CABLE_ROWS + 5) { model.onAddCable() }

        assertEquals(ConduitFillUiState.MAX_CABLE_ROWS, model.uiState.value.cables.size)
        assertFalse(model.uiState.value.canAddCable)
    }

    @Test
    fun `a cable quantity refuses anything but whole numbers`() {
        val model = viewModel()
        val row = model.uiState.value.cables.single().id

        model.onCableQuantityChange(row, "2.5")

        assertEquals("25", model.uiState.value.cables.single().quantity)
    }

    @Test
    fun `editing any row discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onAddCable()

        assertNull(model.uiState.value.result)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the conduit and the cable row`() {
        val model = viewModel()

        model.onCalculate()

        val state = model.uiState.value
        assertEquals(
            ValidationError.Required,
            state.errors[ConduitFillField.CONDUIT_DIAMETER],
        )
        assertEquals(ValidationError.Required, state.cables.single().diameterError)
        assertNull(state.result)
    }

    @Test
    fun `every bad row is reported at once`() {
        // Fixing one field per calculate would be tedious with a long list.
        val model = viewModel()
        model.onConduitDiameterChange("25")
        model.onAddCable()
        model.onAddCable()
        val ids = model.uiState.value.cables.map { it.id }
        model.onCableDiameterChange(ids[1], "8.5")

        model.onCalculate()

        val cables = model.uiState.value.cables
        assertNotNull(cables[0].diameterError)
        assertNull(cables[1].diameterError)
        assertNotNull(cables[2].diameterError)
    }

    @Test
    fun `a row error clears as soon as that row is edited`() {
        val model = viewModel()
        model.onCalculate()
        val row = model.uiState.value.cables.single().id

        model.onCableDiameterChange(row, "8.5")

        assertNull(model.uiState.value.cables.single().diameterError)
    }

    @Test
    fun `a stale custom limit does not block a table calculation`() {
        // The field is hidden under the table rule, so validating it would
        // reject the form over something the user cannot see or fix.
        val model = viewModel()
        model.fillValidForm()
        model.onRuleChange(FillRule.CUSTOM)
        model.onCustomLimitChange("")
        model.onRuleChange(FillRule.NEC_TABLE_1)

        model.onCalculate()

        assertNull(model.uiState.value.errors[ConduitFillField.CUSTOM_LIMIT])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `a custom limit above one hundred percent is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.onRuleChange(FillRule.CUSTOM)
        model.onCustomLimitChange("150")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[ConduitFillField.CUSTOM_LIMIT])
        assertNull(model.uiState.value.result)
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `a valid form produces the expected fill`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(0.3468, result.fillFraction, 1e-6)
        assertEquals(0.40, result.permittedFraction, 1e-9)
        assertEquals(3, result.cableCount)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `several rows are summed into one bundle`() {
        val model = viewModel()
        model.onConduitDiameterChange("32")
        model.onAddCable()
        val ids = model.uiState.value.cables.map { it.id }
        model.onCableDiameterChange(ids[0], "11.9")
        model.onCableQuantityChange(ids[0], "3")
        model.onCableDiameterChange(ids[1], "8.5")
        model.onCableQuantityChange(ids[1], "2")

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(5, result.cableCount)
        assertEquals(447.150736, result.cableAreaMm2, 1e-6)
    }

    @Test
    fun `the custom limit is read as a percentage`() {
        val model = viewModel()
        model.fillValidForm()
        model.onRuleChange(FillRule.CUSTOM)
        model.onCustomLimitChange("45")

        model.onCalculate()

        assertEquals(0.45, model.uiState.value.result!!.permittedFraction, 1e-9)
    }

    @Test
    fun `reset restores a single empty row and keeps the rule`() {
        val model = viewModel()
        model.fillValidForm()
        model.onRuleChange(FillRule.CUSTOM)
        model.onAddCable()
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals(1, state.cables.size)
        assertEquals("", state.cables.single().diameter)
        assertEquals("", state.conduitDiameter)
        assertEquals(FillRule.CUSTOM, state.rule)
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
        assertEquals(CalculatorId.CONDUIT_FILL, records.single().calculatorId)
    }

    @Test
    fun `history records every cable row`() = runTest {
        val model = viewModel()
        model.onConduitDiameterChange("32")
        model.onAddCable()
        val ids = model.uiState.value.cables.map { it.id }
        model.onCableDiameterChange(ids[0], "11.9")
        model.onCableQuantityChange(ids[0], "3")
        model.onCableDiameterChange(ids[1], "8.5")
        model.onCableQuantityChange(ids[1], "2")

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("3 × Ø 11.9 mm", inputs["cable_1"])
        assertEquals("2 × Ø 8.5 mm", inputs["cable_2"])
        assertEquals("32", inputs["conduit_inner_diameter_mm"])
    }

    @Test
    fun `history records the verdict, not just the number`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val results = historyRepository.observeAll().first().single().results
        assertEquals("true", results["within_limit"])
        assertNotNull(results["fill_percent"])
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
