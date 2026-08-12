package com.kemalurekli.electricalcalculator.features.calculators.earthfault

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultField
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultUiState
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultViewModel
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
class EarthFaultViewModelTest {

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

    private fun viewModel() = EarthFaultViewModel(
        calculateLoop = CalculateEarthFaultUseCase(),
        historyRepository = historyRepository,
        favoritesRepository = favoritesRepository,
        stringResolver = FakeStringResolver(
            mapOf(
                R.string.ef_history_title to "Earth fault loop — %1\$s, %2\$s m",
                R.string.ef_history_summary to "%1\$s Ω",
                R.string.ef_device_b to "Type B",
                R.string.ef_device_c to "Type C",
                R.string.ef_device_d to "Type D",
                R.string.ef_device_custom to "Custom",
                R.string.ef_device_rcd to "RCD",
            ),
        ),
        timeProvider = timeProvider,
    )

    private fun EarthFaultViewModel.fillValidForm() {
        onExternalImpedanceChange("0.35")
        onLengthChange("30")
        onLineSectionChange("4")
        onProtectiveSectionChange("2.5")
        onDeviceRatingChange("32")
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `the form opens on a type B breaker at 230 volts`() {
        val state = viewModel().uiState.value

        assertEquals(ProtectiveDeviceType.MCB_TYPE_B, state.deviceType)
        assertEquals("230", state.voltage)
        assertEquals("0.1", state.clearingTime)
        assertEquals("1", state.parallelConductors)
    }

    // -- The rating field changes meaning with the device ------------------------------

    @Test
    fun `switching to an RCD replaces the rating with a residual default`() {
        // The field means In for an MCB and IΔn for an RCD, and they differ by
        // three orders of magnitude. Carrying 32 across would read as 32 A of
        // residual current.
        val model = viewModel()
        model.onDeviceRatingChange("32")

        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)

        assertEquals("0.03", model.uiState.value.deviceRating)
    }

    @Test
    fun `switching back from an RCD clears the residual rating`() {
        val model = viewModel()
        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)

        model.onDeviceTypeChange(ProtectiveDeviceType.MCB_TYPE_C)

        assertEquals("", model.uiState.value.deviceRating)
    }

    @Test
    fun `switching between overcurrent devices keeps the rating`() {
        // In means the same thing for every MCB type, so retyping it would be
        // busywork.
        val model = viewModel()
        model.onDeviceRatingChange("32")

        model.onDeviceTypeChange(ProtectiveDeviceType.MCB_TYPE_D)

        assertEquals("32", model.uiState.value.deviceRating)
    }

    // -- Validation -----------------------------------------------------------------

    @Test
    fun `an empty form reports the required fields`() {
        val model = viewModel()

        model.onCalculate()

        val errors = model.uiState.value.errors
        // Ze is not listed: it arrives pre-filled with a typical maximum.
        assertNull(errors[EarthFaultField.EXTERNAL_IMPEDANCE])
        assertEquals(ValidationError.Required, errors[EarthFaultField.LENGTH])
        assertEquals(ValidationError.Required, errors[EarthFaultField.LINE_SECTION])
        assertEquals(ValidationError.Required, errors[EarthFaultField.PROTECTIVE_SECTION])
        assertEquals(ValidationError.Required, errors[EarthFaultField.DEVICE_RATING])
        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a zero external impedance is accepted`() {
        // Meaningful at the origin of an installation fed directly.
        val model = viewModel()
        model.fillValidForm()
        model.onExternalImpedanceChange("0")

        model.onCalculate()

        assertNull(model.uiState.value.errors[EarthFaultField.EXTERNAL_IMPEDANCE])
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `a residual rating above the residual limit is rejected`() {
        // 30 A of residual current is not a device, it is a typo for 30 mA.
        val model = viewModel()
        model.fillValidForm()
        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)
        model.onDeviceRatingChange("30")

        model.onCalculate()

        assertNotNull(model.uiState.value.errors[EarthFaultField.DEVICE_RATING])
    }

    // -- Result ----------------------------------------------------------------------

    @Test
    fun `a valid form produces the expected loop`() {
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertEquals(0.752263, result.loopImpedanceOhms, 1e-6)
        assertEquals(1.365625, result.maximumPermittedOhms, 1e-6)
        assertTrue(result.isCompliant)
    }

    @Test
    fun `the same circuit fails once the breaker curve changes`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()
        assertTrue(model.uiState.value.result!!.disconnectsInTime)

        model.onDeviceTypeChange(ProtectiveDeviceType.MCB_TYPE_C)
        model.onCalculate()

        assertFalse(model.uiState.value.result!!.disconnectsInTime)
    }

    @Test
    fun `an RCD reports no operating current`() {
        val model = viewModel()
        model.fillValidForm()
        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)

        model.onCalculate()

        val result = model.uiState.value.result!!
        assertNull(result.operatingCurrentAmps)
        assertEquals(50.0 / 0.03, result.maximumPermittedOhms, 1e-9)
    }

    @Test
    fun `changing the device discards the stale result`() {
        val model = viewModel()
        model.fillValidForm()
        model.onCalculate()

        model.onDeviceTypeChange(ProtectiveDeviceType.MCB_TYPE_D)

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
        assertEquals(EarthFaultUiState.DEFAULT_EXTERNAL_IMPEDANCE, state.externalImpedance)
        assertEquals(ConductorMaterial.ALUMINIUM, state.material)
        assertEquals(EarthFaultUiState.DEFAULT_CLEARING_TIME, state.clearingTime)
        assertNull(state.result)
    }

    @Test
    fun `reset keeps the residual default when an RCD is selected`() {
        val model = viewModel()
        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)
        model.fillValidForm()
        model.onCalculate()

        model.onReset()

        assertEquals(
            EarthFaultUiState.DEFAULT_RESIDUAL_RATING,
            model.uiState.value.deviceRating,
        )
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
        assertEquals(CalculatorId.EARTH_FAULT_LOOP, records.single().calculatorId)
    }

    @Test
    fun `history records both verdicts, not just one`() = runTest {
        // A run that disconnected but did not withstand must not read as a pass.
        val model = viewModel()
        model.fillValidForm()

        model.onCalculate()
        advanceUntilIdle()

        val results = historyRepository.observeAll().first().single().results
        assertEquals("true", results["disconnects_in_time"])
        assertEquals("true", results["protective_conductor_withstands"])
    }

    @Test
    fun `history omits the operating current for an RCD`() = runTest {
        val model = viewModel()
        model.fillValidForm()
        model.onDeviceTypeChange(ProtectiveDeviceType.RCD)

        model.onCalculate()
        advanceUntilIdle()

        val results = historyRepository.observeAll().first().single().results
        assertNull(results["operating_current"])
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
