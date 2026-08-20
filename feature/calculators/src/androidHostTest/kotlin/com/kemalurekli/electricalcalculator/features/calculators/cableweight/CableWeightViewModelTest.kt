package com.kemalurekli.electricalcalculator.features.calculators.cableweight

import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightField
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightViewModel
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
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_history_title

@OptIn(ExperimentalCoroutinesApi::class)
class CableWeightViewModelTest {

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

    private fun viewModel() = CableWeightViewModel(
        calculateWeight = CalculateCableWeightUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                Res.string.cw_history_title to "Cable weight — %1\$s × %2\$s mm², %3\$s m",
                Res.string.cw_history_summary to "%1\$s kg",
            ),
        ),
        timeProvider = timeProvider,
        userPreferences = FakeUserPreferencesRepository(),
    )

    private fun CableWeightViewModel.fillValidForm() {
        onCrossSectionChange("25")
        onConductorCountChange("4")
        onLengthChange("1000")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `defaults describe a five-core copper cable with no datasheet diameter`() {
        val state = viewModel().uiState.value

        assertEquals(ConductorMaterial.COPPER, state.material)
        assertEquals("5", state.conductorCount)
        assertEquals("", state.diameter)
        assertFalse(state.showInsulation)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields but not the optional diameter`() {
        val model = viewModel()
        model.onConductorCountChange("")

        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(ValidationError.Required, errors[CableWeightField.CROSS_SECTION])
        assertEquals(ValidationError.Required, errors[CableWeightField.CONDUCTOR_COUNT])
        assertEquals(ValidationError.Required, errors[CableWeightField.LENGTH])
        assertNull(errors[CableWeightField.DIAMETER])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `the conductor count refuses anything but whole numbers`() {
        // Filtering as the user types beats truncating "3.7" to 3 silently.
        val model = viewModel()

        model.onConductorCountChange("3.7")

        assertEquals("37", model.uiState.value.conductorCount)
    }

    @Test
    fun `a diameter thinner than the conductors is rejected with the minimum`() {
        // 4 × 25 mm² of metal needs a circle of at least 11.28 mm to sit in.
        val model = viewModel()
        model.fillValidForm()
        model.onDiameterChange("8")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[CableWeightField.DIAMETER])
        assertEquals(11.283792, model.uiState.value.diameterTooSmallFor!!, 1e-6)
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a diameter that clears the conductors is accepted`() {
        val model = viewModel()
        model.fillValidForm()
        model.onDiameterChange("25")

        model.onCalculate()

        assertNull(model.uiState.value.errors[CableWeightField.DIAMETER])
        assertNull(model.uiState.value.diameterTooSmallFor)
        assertTrue(model.uiState.value.result!!.hasTotal)
    }

    @Test
    fun `editing the diameter clears the too-small hint`() {
        val model = viewModel()
        model.fillValidForm()
        model.onDiameterChange("8")
        model.onCalculate()

        model.onDiameterChange("25")

        assertNull(model.uiState.value.diameterTooSmallFor)
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `a form without a diameter yields conductor mass only`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(889.0, result.conductorMassKg, 1e-9)
        assertNull(result.totalMassKg)
        assertFalse(result.hasTotal)
    }

    @Test
    fun `a form with a diameter yields the complete cable`() {
        val model = viewModel()
        model.fillValidForm()
        model.onDiameterChange("25")

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(1436.223393, result.totalMassKg!!, 1e-6)
        assertEquals(1.436223, result.totalMassPerMeterKg!!, 1e-6)
    }

    @Test
    fun `the insulation selector appears once a diameter is entered`() {
        val model = viewModel()
        assertFalse(model.uiState.value.showInsulation)

        model.onDiameterChange("25")

        assertTrue(model.uiState.value.showInsulation)
    }

    @Test
    fun `switching material changes the mass`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        val copper = model.uiState.value.result!!.conductorMassKg

        model.onMaterialChange(ConductorMaterial.ALUMINIUM)
        model.onCalculate()
        val aluminium = model.uiState.value.result!!.conductorMassKg

        assertEquals(8.89 / 2.70, copper / aluminium, 1e-9)
    }

    @Test
    fun `changing the material discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onMaterialChange(ConductorMaterial.ALUMINIUM)

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `reset clears the numbers but keeps the selections`() {
        val model = viewModel()
        model.fillValidForm()
        model.onMaterialChange(ConductorMaterial.ALUMINIUM)
        model.onInsulationChange(CableInsulation.XLPE)
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("", state.crossSection)
        assertEquals("5", state.conductorCount)
        assertEquals(ConductorMaterial.ALUMINIUM, state.material)
        assertEquals(CableInsulation.XLPE, state.insulation)
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
        assertEquals(CalculatorId.CABLE_WEIGHT, records.single().calculatorId)
    }

    @Test
    fun `history omits the diameter when none was given`() = runTest {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val record = historyRepository.observeAll().first().single()
        assertEquals("25", record.inputs["cross_section_mm2"])
        assertEquals("4", record.inputs["conductor_count"])
        assertNull(record.inputs["overall_diameter_mm"])
        // Nothing that was never computed is stored as an empty figure.
        assertNull(record.results["total_mass_kg"])
    }

    @Test
    fun `history records the complete cable when a diameter was given`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onDiameterChange("25")

        model.onCalculate()
        advanceUntilIdle()

        val record = historyRepository.observeAll().first().single()
        assertEquals("25", record.inputs["overall_diameter_mm"])
        assertEquals("PVC", record.inputs["insulation"])
        assertNotNull(record.results["total_mass_kg"])
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
