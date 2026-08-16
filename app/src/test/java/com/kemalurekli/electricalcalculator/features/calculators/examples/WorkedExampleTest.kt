package com.kemalurekli.electricalcalculator.features.calculators.examples

import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryViewModel
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.batteryExamples
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeViewModel
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.cableSizeExamples
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightViewModel
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.cableWeightExamples
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillViewModel
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.conduitFillExamples
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultViewModel
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.earthFaultExamples
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.CalculateEnergyCostUseCase
import com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation.EnergyCostViewModel
import com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation.energyCostExamples
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.CalculateLightingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation.LightingViewModel
import com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation.lightingExamples
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorViewModel
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.motorExamples
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.CalculateNeutralCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation.NeutralCurrentViewModel
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation.neutralCurrentExamples
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerViewModel
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.powerExamples
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorViewModel
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.powerFactorExamples
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitViewModel
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.shortCircuitExamples
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.CalculateSolarStringUseCase
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation.SolarStringViewModel
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation.solarStringExamples
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerViewModel
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.transformerExamples
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillViewModel
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.trayFillExamples
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropViewModel
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.voltageDropExamples
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteItemDao
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import com.kemalurekli.electricalcalculator.testing.FakeUserPreferencesRepository
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.CalculateEvseUseCase
import com.kemalurekli.electricalcalculator.features.calculators.evse.presentation.EvseViewModel
import com.kemalurekli.electricalcalculator.features.calculators.evse.presentation.evseExamples
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.CalculateHarmonicsUseCase
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation.HarmonicsViewModel
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation.harmonicsExamples
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.CalculateMotorStartingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation.MotorStartingViewModel
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation.motorStartingExamples
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.CheckSelectivityUseCase
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation.SelectivityViewModel
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation.selectivityExamples
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Every worked example, applied and run.
 *
 * This is the test the feature needs. An example is a promise that tapping one
 * chip produces a finished calculation, and the way that promise breaks is
 * silent: a digit in the wrong field, a section that no tabulated cable
 * satisfies, a rating outside its validator. The form would simply sit there
 * showing a red field, in the one place the app was supposed to be teaching.
 *
 * So each example is applied to a real ViewModel and expected to leave no
 * validation errors, a result, and a worked solution behind it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkedExampleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timeProvider = FakeTimeProvider()

    /** Every label resolves; this test is about arithmetic, not about copy. */
    private val strings = object : StringResolver {
        override fun get(id: Int): String = "text"
    }

    private fun history() = HistoryRepositoryImpl(
        dao = FakeCalculationHistoryDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun favorites() = FavoritesRepositoryImpl(
        dao = FakeFavoriteItemDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    /**
     * Applies every example and checks each one finished.
     *
     * @param apply loads one example into the ViewModel and runs it.
     * @param inspect reports (errorCount, hasResult, stepCount) afterwards.
     */
    private fun <S> assertEveryExampleRuns(
        label: String,
        examples: List<WorkedExample<S>>,
        apply: (WorkedExample<S>) -> Unit,
        inspect: () -> Triple<Int, Boolean, Int>,
    ) {
        assertTrue("$label has no examples", examples.isNotEmpty())

        examples.forEach { example ->
            apply(example)
            val (errorCount, hasResult, stepCount) = inspect()

            assertEquals("$label/${example.key} left validation errors", 0, errorCount)
            assertTrue("$label/${example.key} produced no result", hasResult)
            assertTrue("$label/${example.key} produced no worked solution", stepCount > 0)
        }
    }

    private fun preferences() = FakeUserPreferencesRepository()

    @Test
    fun `every voltage drop example runs`() {
        val model = VoltageDropViewModel(
            CalculateVoltageDropUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("voltage drop", voltageDropExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every cable size example runs`() {
        val model = CableSizeViewModel(
            CalculateCableSizeUseCase(AmpacityTable(), CorrectionFactors()),
            CorrectionFactors(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("cable size", cableSizeExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every cable size example resolves to a real cable`() {
        // A sizing run that finds no tabulated size is a legitimate result of
        // the calculator and a useless example.
        val model = CableSizeViewModel(
            CalculateCableSizeUseCase(AmpacityTable(), CorrectionFactors()),
            CorrectionFactors(), history(), favorites(), strings, timeProvider, preferences(),
        )
        cableSizeExamples.forEach { example ->
            model.onApplyExample(example)
            assertNotNull(
                "${example.key} found no standard size",
                model.uiState.value.result?.recommendedAreaMm2,
            )
        }
    }

    @Test
    fun `every transformer example runs`() {
        val model = TransformerViewModel(
            CalculateTransformerCurrentUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("transformer", transformerExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every motor example runs`() {
        val model = MotorViewModel(
            CalculateMotorCurrentUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("motor", motorExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every power example runs`() {
        val model = PowerViewModel(
            CalculatePowerUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("power", powerExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every power factor example runs`() {
        val model = PowerFactorViewModel(
            CalculatePowerFactorCorrectionUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("power factor", powerFactorExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every battery example runs`() {
        val model = BatteryViewModel(
            CalculateBatteryRuntimeUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("battery", batteryExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every cable weight example runs`() {
        val model = CableWeightViewModel(
            CalculateCableWeightUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("cable weight", cableWeightExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the cable weight example without a diameter reports only what is derivable`() {
        // It is in the set precisely to show the calculator stopping rather than
        // estimating, so the absence has to be the real thing.
        val model = CableWeightViewModel(
            CalculateCableWeightUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        val example = cableWeightExamples.single { it.key == "no_datasheet" }

        model.onApplyExample(example)

        val result = requireNotNull(model.uiState.value.result)
        assertTrue("the conductor mass should still be exact", result.conductorMassKg > 0.0)
        assertTrue("no complete-cable figure should be offered", !result.hasTotal)
    }

    @Test
    fun `every conduit fill example runs`() {
        val model = ConduitFillViewModel(
            CalculateConduitFillUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("conduit fill", conduitFillExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every tray fill example runs`() {
        val model = TrayFillViewModel(
            CalculateTrayFillUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("tray fill", trayFillExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `spacing the tray bundle costs the width it claims to`() {
        // The pair exists to make that cost a number rather than advice, so the
        // two must actually differ.
        val model = TrayFillViewModel(
            CalculateTrayFillUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(trayFillExamples.single { it.key == "single_layer_touching" })
        val touching = model.uiState.value.result
        model.onApplyExample(trayFillExamples.single { it.key == "single_layer_spaced" })
        val spaced = model.uiState.value.result

        assertTrue(
            "spacing did not change the width required",
            (spaced as? com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult.SingleLayer)!!
                .requiredWidthMm >
                (touching as? com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult.SingleLayer)!!
                    .requiredWidthMm,
        )
    }

    @Test
    fun `every short circuit example runs`() {
        val model = ShortCircuitViewModel(
            CalculateShortCircuitUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("short circuit", shortCircuitExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the far end of the run really does collapse the fault current`() {
        // The two short-circuit examples exist to be compared. If they ever stop
        // differing the pair has lost its point.
        val model = ShortCircuitViewModel(
            CalculateShortCircuitUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )

        model.onApplyExample(shortCircuitExamples.single { it.key == "at_the_board" })
        val atBoard = requireNotNull(model.uiState.value.result).minimumFaultCurrentAmps
        model.onApplyExample(shortCircuitExamples.single { it.key == "end_of_run" })
        val farEnd = requireNotNull(model.uiState.value.result).minimumFaultCurrentAmps

        assertTrue("the far end should see far less current", farEnd < atBoard / 10)
    }

    @Test
    fun `every earth fault example runs`() {
        val model = EarthFaultViewModel(
            CalculateEarthFaultUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("earth fault", earthFaultExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the earth fault examples show a circuit that passes and one that does not`() {
        // A set where everything passes teaches nothing about the boundary, and
        // the failing one is the reason this calculator exists.
        val model = EarthFaultViewModel(
            CalculateEarthFaultUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )

        model.onApplyExample(earthFaultExamples.single { it.key == "tncs_type_b" })
        assertTrue(
            "the compliant example does not disconnect",
            requireNotNull(model.uiState.value.result).disconnectsInTime,
        )

        model.onApplyExample(earthFaultExamples.single { it.key == "too_long_type_c" })
        assertTrue(
            "the cautionary example disconnects after all",
            !requireNotNull(model.uiState.value.result).disconnectsInTime,
        )
    }

    @Test
    fun `every lighting example runs`() {
        val model = LightingViewModel(
            CalculateLightingUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("lighting", lightingExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every solar string example runs`() {
        val model = SolarStringViewModel(
            CalculateSolarStringUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("solar string", solarStringExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `every solar string example is a workable pairing`() {
        // An example that reports "this module and this inverter do not go
        // together" teaches the wrong thing about the calculator's defaults.
        val model = SolarStringViewModel(
            CalculateSolarStringUseCase(), history(), favorites(), strings, timeProvider,
        )
        solarStringExamples.forEach { example ->
            model.onApplyExample(example)
            assertTrue(
                "${example.key} has no feasible string length",
                requireNotNull(model.uiState.value.result).isFeasible,
            )
        }
    }

    @Test
    fun `the colder site really does allow a shorter string`() {
        // The two solar examples exist to be compared: same module, same
        // inverter, and the only thing that changed is the weather.
        val model = SolarStringViewModel(
            CalculateSolarStringUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(solarStringExamples.single { it.key == "temperate" })
        val mild = requireNotNull(model.uiState.value.result).maximumModules
        model.onApplyExample(solarStringExamples.single { it.key == "continental" })
        val cold = requireNotNull(model.uiState.value.result).maximumModules

        assertTrue("the colder site should be the more restrictive", cold < mild)
    }

    @Test
    fun `every neutral current example runs`() {
        val model = NeutralCurrentViewModel(
            CalculateNeutralCurrentUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("neutral current", neutralCurrentExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the balanced electronic example puts more in the neutral than any line`() {
        // The reason that example is in the set at all.
        val model = NeutralCurrentViewModel(
            CalculateNeutralCurrentUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(neutralCurrentExamples.single { it.key == "balanced_linear" })
        assertFalse(requireNotNull(model.uiState.value.result).neutralExceedsLines)

        model.onApplyExample(neutralCurrentExamples.single { it.key == "electronic_load" })
        assertTrue(requireNotNull(model.uiState.value.result).neutralExceedsLines)
    }

    @Test
    fun `every EV charging example runs`() {
        val model = EvseViewModel(
            CalculateEvseUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )
        assertEveryExampleRuns("evse", evseExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the same current is three times the power on three phases`() {
        // The first two examples differ only in the connection, which is the
        // misunderstanding they exist to make visible.
        val model = EvseViewModel(
            CalculateEvseUseCase(), history(), favorites(), strings, timeProvider, preferences(),
        )

        model.onApplyExample(evseExamples.single { it.key == "home_single_phase" })
        val single = requireNotNull(model.uiState.value.result)

        model.onApplyExample(evseExamples.single { it.key == "home_three_phase" })
        val three = requireNotNull(model.uiState.value.result)

        assertEquals(single.totalConnectedAmps, three.totalConnectedAmps, 1e-9)
        assertTrue("same current, far more power", three.powerPerPointKw > single.powerPerPointKw * 2.5)
    }

    @Test
    fun `every harmonics example runs`() {
        val model = HarmonicsViewModel(
            CalculateHarmonicsUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("harmonics", harmonicsExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the office floor puts more in the neutral than the drive does`() {
        // The two spectra exist to be contrasted. A six-pulse drive produces no
        // triplen at all: it heats the transformer and leaves the neutral
        // alone, which is the opposite failure to the office floor.
        val model = HarmonicsViewModel(
            CalculateHarmonicsUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(harmonicsExamples.single { it.key == "office_floor" })
        val office = requireNotNull(model.uiState.value.result)

        model.onApplyExample(harmonicsExamples.single { it.key == "six_pulse_drive" })
        val drive = requireNotNull(model.uiState.value.result)

        assertTrue("the office neutral should overtake its lines", office.neutralExceedsLines)
        assertEquals(0.0, drive.neutralAmps, 1e-9)
        assertTrue("and the drive should still load the transformer", drive.kFactor > 1.0)
    }

    @Test
    fun `every motor starting example runs`() {
        val model = MotorStartingViewModel(
            CalculateMotorStartingUseCase(), history(), favorites(), strings, timeProvider,
            preferences(),
        )
        assertEveryExampleRuns("motor starting", motorStartingExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `star delta rescues the start it is paired with, and costs torque doing it`() {
        // The second and third examples are the same impossible start with and
        // without a gentler method. If they ever stop differing, the pair has
        // stopped making its point.
        val model = MotorStartingViewModel(
            CalculateMotorStartingUseCase(), history(), favorites(), strings, timeProvider,
            preferences(),
        )

        model.onApplyExample(motorStartingExamples.single { it.key == "small_site" })
        val direct = requireNotNull(model.uiState.value.result)

        model.onApplyExample(motorStartingExamples.single { it.key == "star_delta_rescue" })
        val star = requireNotNull(model.uiState.value.result)

        assertTrue("star-delta should dip less", star.dipPercent < direct.dipPercent)
        assertTrue("and should cost torque", star.startingTorquePercent < direct.startingTorquePercent)
    }

    @Test
    fun `every selectivity example runs`() {
        val model = SelectivityViewModel(
            CheckSelectivityUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("selectivity", selectivityExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the same pair changes verdict when the fault moves`() {
        // The two examples exist to be compared. If they ever stop disagreeing
        // the pair has lost its point — and so has the calculator.
        val model = SelectivityViewModel(
            CheckSelectivityUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(
            selectivityExamples.single { it.key == "final_circuit_far_from_the_board" },
        )
        val far = requireNotNull(model.uiState.value.result)

        model.onApplyExample(selectivityExamples.single { it.key == "fault_at_the_board" })
        val near = requireNotNull(model.uiState.value.result)

        assertEquals(SelectivityGrade.SELECTIVE, far.grade)
        assertEquals(SelectivityGrade.PARTIAL, near.grade)
        // Same devices, so the limit itself has not moved; only the fault has.
        assertEquals(far.limitAmps, near.limitAmps)
    }

    @Test
    fun `every energy cost example runs`() {
        val model = EnergyCostViewModel(
            CalculateEnergyCostUseCase(), history(), favorites(), strings, timeProvider,
        )
        assertEveryExampleRuns("energy cost", energyCostExamples, model::onApplyExample) {
            val s = model.uiState.value
            Triple(s.errors.size, s.result != null, s.steps.size)
        }
    }

    @Test
    fun `the lighting upgrade example actually pays back`() {
        // A comparison example whose "improvement" costs more to run would be
        // demonstrating the opposite of its own title.
        val model = EnergyCostViewModel(
            CalculateEnergyCostUseCase(), history(), favorites(), strings, timeProvider,
        )

        model.onApplyExample(energyCostExamples.single { it.key == "lighting_upgrade" })

        val result = requireNotNull(model.uiState.value.result)
        assertTrue(result.savesMoney)
        assertNotNull(result.paybackYears)
    }

    @Test
    fun `example keys are unique within each calculator`() {
        listOf(
            "voltage drop" to voltageDropExamples,
            "cable size" to cableSizeExamples,
            "transformer" to transformerExamples,
            "motor" to motorExamples,
            "power" to powerExamples,
            "power factor" to powerFactorExamples,
            "battery" to batteryExamples,
            "cable weight" to cableWeightExamples,
            "conduit fill" to conduitFillExamples,
            "tray fill" to trayFillExamples,
            "short circuit" to shortCircuitExamples,
            "earth fault" to earthFaultExamples,
            "lighting" to lightingExamples,
            "solar string" to solarStringExamples,
            "neutral current" to neutralCurrentExamples,
            "energy cost" to energyCostExamples,
        ).forEach { (label, examples) ->
            val keys = examples.map { it.key }
            assertEquals("$label repeats an example key", keys.size, keys.distinct().size)
            examples.forEach { example ->
                assertTrue("$label/${example.key} has no title", example.titleRes != 0)
            }
        }
    }
}
