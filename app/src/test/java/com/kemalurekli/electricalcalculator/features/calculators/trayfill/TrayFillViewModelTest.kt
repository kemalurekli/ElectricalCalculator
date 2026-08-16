package com.kemalurekli.electricalcalculator.features.calculators.trayfill

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillField
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillUiState
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillViewModel
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
class TrayFillViewModelTest {

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

    private fun viewModel() = TrayFillViewModel(
        calculateFill = CalculateTrayFillUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.tf_history_title to "Tray fill — %1\$s mm, %2\$s cables",
                R.string.tf_history_summary_width to "%1\$s mm",
                R.string.tf_history_summary_area to "%1\$s %%",
                R.string.cf_export_cable_line to "%1\$s × Ø %2\$s mm",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun TrayFillViewModel.fillValidForm() {
        onTrayWidthChange("300")
        val row = uiState.value.cables.first().id
        onCableDiameterChange(row, "20.5")
        onCableQuantityChange(row, "6")
    }

    private fun TrayFillViewModel.switchToStacked() {
        onArrangementChange(TrayArrangement.MULTI_LAYER)
        onTrayDepthChange("100")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `the form starts as a single layer of touching cables`() {
        val state = viewModel().uiState.value

        assertEquals(TrayArrangement.SINGLE_LAYER, state.arrangement)
        assertEquals("0", state.spacing)
        assertTrue(state.showSpacing)
        assertFalse(state.showDepthAndLimit)
        assertEquals(1, state.cables.size)
    }

    @Test
    fun `switching to stacked swaps which fields are asked for`() {
        val model = viewModel()

        model.onArrangementChange(TrayArrangement.MULTI_LAYER)

        val state = model.uiState.value
        assertTrue(state.showDepthAndLimit)
        assertFalse(state.showSpacing)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty single-layer form reports the width and the cable row`() {
        val model = viewModel()

        model.onCalculate()

        val state = model.uiState.value
        assertEquals(ValidationError.Required, state.errors[TrayFillField.TRAY_WIDTH])
        assertEquals(ValidationError.Required, state.cables.single().diameterError)
        // Depth is not part of a single-layer calculation.
        assertNull(state.errors[TrayFillField.TRAY_DEPTH])
        assertNull(state.result)
    }

    @Test
    fun `a stacked form requires the depth as well`() {
        val model = viewModel()
        model.fillValidForm()
        model.onArrangementChange(TrayArrangement.MULTI_LAYER)

        model.onCalculate()

        assertEquals(ValidationError.Required, model.uiState.value.errors[TrayFillField.TRAY_DEPTH])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `zero spacing is accepted because touching is the normal case`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        assertNull(model.uiState.value.errors[TrayFillField.SPACING])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `a hidden field does not block the calculation`() {
        // The depth field is not shown for a single layer, so a stale blank in
        // it must not reject a form the user has no way to fix.
        val model = viewModel()
        model.fillValidForm()
        model.onArrangementChange(TrayArrangement.MULTI_LAYER)
        model.onArrangementChange(TrayArrangement.SINGLE_LAYER)

        model.onCalculate()

        assertNull(model.uiState.value.errors[TrayFillField.TRAY_DEPTH])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `every bad cable row is reported at once`() {
        val model = viewModel()
        model.onTrayWidthChange("300")
        model.onAddCable()
        model.onAddCable()
        val ids = model.uiState.value.cables.map { it.id }
        model.onCableDiameterChange(ids[1], "20.5")

        model.onCalculate()

        val cables = model.uiState.value.cables
        assertNotNull(cables[0].diameterError)
        assertNull(cables[1].diameterError)
        assertNotNull(cables[2].diameterError)
    }

    @Test
    fun `a limit above one hundred percent is rejected`() {
        val model = viewModel()
        model.fillValidForm()
        model.switchToStacked()
        model.onLimitChange("150")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[TrayFillField.LIMIT])
        assertNull(model.uiState.value.result)
    }

    // -- The cable list -----------------------------------------------------------------

    @Test
    fun `adding and removing rows keeps ids unique`() {
        val model = viewModel()
        model.onAddCable()
        val removed = model.uiState.value.cables.last().id
        model.onRemoveCable(removed)

        model.onAddCable()

        val ids = model.uiState.value.cables.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertFalse(ids.contains(removed))
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

        repeat(TrayFillUiState.MAX_CABLE_ROWS + 3) { model.onAddCable() }

        assertEquals(TrayFillUiState.MAX_CABLE_ROWS, model.uiState.value.cables.size)
    }

    @Test
    fun `a cable quantity refuses anything but whole numbers`() {
        val model = viewModel()
        val row = model.uiState.value.cables.single().id

        model.onCableQuantityChange(row, "2.5")

        assertEquals("25", model.uiState.value.cables.single().quantity)
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `a single-layer form is judged on width`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result as TrayFillResult.SingleLayer
        assertEquals(123.0, result.requiredWidthMm, 1e-9)
        assertEquals(177.0, result.spareWidthMm, 1e-9)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `spacing is applied to the single-layer width`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSpacingChange("20.5")

        model.onCalculate()

        val result = model.uiState.value.result as TrayFillResult.SingleLayer
        assertEquals(225.5, result.requiredWidthMm, 1e-9)
    }

    @Test
    fun `a stacked form is judged on area`() {
        val model = viewModel()
        model.onTrayWidthChange("300")
        val row = model.uiState.value.cables.first().id
        model.onCableDiameterChange(row, "11.9")
        model.onCableQuantityChange(row, "30")
        model.switchToStacked()

        model.onCalculate()

        val result = model.uiState.value.result as TrayFillResult.MultiLayer
        assertEquals(30_000.0, result.trayAreaMm2, 1e-9)
        assertEquals(0.11122, result.fillFraction, 1e-6)
        assertEquals(2, result.estimatedLayers)
    }

    @Test
    fun `the limit is read as a percentage`() {
        val model = viewModel()
        model.fillValidForm()
        model.switchToStacked()
        model.onLimitChange("50")

        model.onCalculate()

        val result = model.uiState.value.result as TrayFillResult.MultiLayer
        assertEquals(0.50, result.permittedFraction, 1e-9)
    }

    @Test
    fun `changing the arrangement discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onArrangementChange(TrayArrangement.MULTI_LAYER)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset restores a single empty row and keeps the arrangement`() {
        val model = viewModel()
        model.fillValidForm()
        model.switchToStacked()
        model.onAddCable()
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals(1, state.cables.size)
        assertEquals("", state.trayWidth)
        assertEquals(TrayArrangement.MULTI_LAYER, state.arrangement)
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
        assertEquals(CalculatorId.CABLE_TRAY_FILL, records.single().calculatorId)
    }

    @Test
    fun `a single-layer run is summarised by width, a stacked one by percentage`() = runTest {
        // The binding constraint differs, so the one-line summary has to as
        // well: a width means nothing for a stack, and a percentage means
        // nothing for a single layer.
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        advanceUntilIdle()

        model.switchToStacked()
        model.onCalculate()
        advanceUntilIdle()

        // Both records carry the same fake timestamp, so they are matched by
        // the arrangement they recorded rather than by their order.
        val records = historyRepository.observeAll().first()
        assertEquals(2, records.size)
        val singleLayer = records.single { it.inputs["arrangement"] == "SINGLE_LAYER" }
        val stacked = records.single { it.inputs["arrangement"] == "MULTI_LAYER" }

        assertTrue(singleLayer.summary.endsWith("mm"))
        assertTrue(stacked.summary.endsWith("%"))
    }

    @Test
    fun `history omits the fields the arrangement does not use`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("300", inputs["tray_width_mm"])
        assertEquals("0", inputs["clear_spacing_mm"])
        assertNull(inputs["tray_depth_mm"])
        assertNull(inputs["permitted_fill_percent"])
    }

    @Test
    fun `history records every cable row`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onAddCable()
        val second = model.uiState.value.cables.last().id
        model.onCableDiameterChange(second, "11.9")
        model.onCableQuantityChange(second, "4")

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("6 × Ø 20.5 mm", inputs["cable_1"])
        assertEquals("4 × Ø 11.9 mm", inputs["cable_2"])
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
