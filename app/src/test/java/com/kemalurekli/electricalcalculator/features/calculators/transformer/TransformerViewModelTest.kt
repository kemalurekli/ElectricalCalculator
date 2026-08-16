package com.kemalurekli.electricalcalculator.features.calculators.transformer

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerField
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerUiState
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerViewModel
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
class TransformerViewModelTest {

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

    private fun viewModel() = TransformerViewModel(
        calculateTransformerCurrent = CalculateTransformerCurrentUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.tx_history_title to "Transformer — %1\$s kVA, %2\$s/%3\$s V",
                R.string.tx_history_summary to "%1\$s A",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun TransformerViewModel.fillValidForm() {
        onRatingChange("1000")
        onPrimaryVoltageChange("34500")
        onSecondaryVoltageChange("400")
    }

    // -- Defaults ---------------------------------------------------------------

    @Test
    fun `defaults to three phase with a typical impedance`() {
        val state = viewModel().uiState.value

        assertEquals(SupplySystem.THREE_PHASE_AC, state.system)
        assertEquals("6", state.impedancePercent)
        assertNull(state.result)
    }

    // -- Validation --------------------------------------------------------------

    @Test
    fun `an empty form reports every required field`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[TransformerField.RATING])
        assertEquals(ValidationError.Required, errors[TransformerField.PRIMARY_VOLTAGE])
        assertEquals(ValidationError.Required, errors[TransformerField.SECONDARY_VOLTAGE])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `zero secondary voltage is rejected rather than dividing by zero`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSecondaryVoltageChange("0")

        model.onCalculate()

        assertEquals(
            ValidationError.MustBePositive,
            model.uiState.value.errors[TransformerField.SECONDARY_VOLTAGE],
        )
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `zero impedance is rejected rather than producing infinite fault current`() {
        val model = viewModel()
        model.fillValidForm()
        model.onImpedanceChange("0")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[TransformerField.IMPEDANCE])
        assertNull(model.uiState.value.result)
    }

    // -- Result -------------------------------------------------------------------

    @Test
    fun `a valid form produces the standard figures`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result
        assertNotNull(result)
        assertEquals(1443.3757, result!!.secondaryCurrent, 1e-4)
        assertEquals(16.7348, result.primaryCurrent, 1e-4)
        assertEquals(86.25, result.voltageRatio, 1e-9)
    }

    @Test
    fun `switching to single phase changes the result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val threePhase = model.uiState.value.result!!.secondaryCurrent

        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        model.onCalculate()
        val singlePhase = model.uiState.value.result!!.secondaryCurrent

        assertTrue(singlePhase > threePhase)
    }

    @Test
    fun `editing an input discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        assertNotNull(model.uiState.value.result)

        model.onRatingChange("630")

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the phase selection`() {
        val model = viewModel()
        model.fillValidForm()
        model.onSystemChange(SupplySystem.SINGLE_PHASE_AC)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.ratingKva)
        assertEquals(SupplySystem.SINGLE_PHASE_AC, state.system)
        assertEquals(TransformerUiState.DEFAULT_IMPEDANCE, state.impedancePercent)
        assertNull(state.result)
    }

    // -- History ---------------------------------------------------------------------

    @Test
    fun `a successful calculation is saved to history`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        val record = records.single()
        assertEquals(CalculatorId.TRANSFORMER_CURRENT, record.calculatorId)
        // Compared digits-only: the stored text is formatted for the active
        // locale, so asserting on a literal "1,443.38" would pass or fail
        // depending on the machine running the test.
        assertEquals("144338", record.results["secondary_current"]!!.filter { it.isDigit() })
    }

    @Test
    fun `history stores the raw inputs so the run can be reproduced`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val inputs = historyRepository.observeAll().first().single().inputs
        assertEquals("1000", inputs["rating_kva"])
        assertEquals("34500", inputs["primary_voltage"])
        assertEquals("400", inputs["secondary_voltage"])
        assertEquals("THREE_PHASE_AC", inputs["system"])
    }

    @Test
    fun `a failed validation is not saved to history`() = runTest {
        val model = viewModel()
        model.onCalculate()
        advanceUntilIdle()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    // -- Favorites ----------------------------------------------------------------------

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
