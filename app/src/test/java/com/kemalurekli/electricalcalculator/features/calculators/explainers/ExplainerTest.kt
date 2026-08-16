package com.kemalurekli.electricalcalculator.features.calculators.explainers

import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryInput
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.explainBatteryRuntime
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeInput
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.explainCableSize
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightInput
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.explainCableWeight
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillInput
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.explainConduitFill
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultInput
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.explainEarthFault
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorInput
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.explainMotorCurrent
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerInput
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.explainPower
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorInput
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.explainPowerFactor
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitInput
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.explainShortCircuit
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerInput
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.explainTransformer
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillInput
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.explainTrayFill
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.explainVoltageDrop
import kotlinx.collections.immutable.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.CalculateEvseUseCase
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseInput
import com.kemalurekli.electricalcalculator.features.calculators.evse.presentation.explainEvse
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.CalculateHarmonicsUseCase
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicComponent
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsInput
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation.explainHarmonics
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.CalculateMotorStartingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingInput
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation.explainMotorStarting
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.CheckSelectivityUseCase
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation.explainSelectivity
import java.util.Locale

/**
 * The worked solutions.
 *
 * Every case passes an explicit [Locale] — relying on the JVM default would
 * make these pass or fail depending on the machine running them, which is a
 * mistake this project has already made once.
 *
 * The property that matters most is that a step's stated result matches what
 * the calculator actually produced. A solution that walks the reader through
 * arithmetic ending somewhere other than the headline figure would be worse
 * than showing no working at all.
 *
 * The inputs below are the same ones the use-case tests pin their worked
 * examples on, so the figures asserted here have already been checked against
 * the physics rather than against the code that produces them. Where a step
 * reports an intermediate the use case does not return, the expected value is
 * derived by hand in a comment beside it.
 */
class ExplainerTest {

    private val us = Locale.US
    private val turkey = Locale.forLanguageTag("tr-TR")

    // -- Voltage drop -------------------------------------------------------------------

    private val voltageDropInput = VoltageDropInput(
        systemVoltage = 400.0,
        loadCurrent = 50.0,
        lengthMetres = 60.0,
        crossSectionMm2 = 16.0,
        material = ConductorMaterial.COPPER,
        system = SupplySystem.THREE_PHASE_AC,
        powerFactor = 0.9,
        conductorTemperatureC = 70.0,
        parallelConductors = 1,
    )

    @Test
    fun `the voltage drop solution walks from resistivity to power loss`() {
        val result = CalculateVoltageDropUseCase()(voltageDropInput)

        val steps = explainVoltageDrop(voltageDropInput, result, us)

        assertEquals(5, steps.size)
        assertTrue(steps[0].formula.startsWith("ρ(θ)"))
        assertTrue(steps[1].formula.startsWith("R ="))
        assertTrue(steps[2].formula.startsWith("ΔU ="))
        assertTrue(steps[3].formula.startsWith("ΔU%"))
        assertTrue(steps[4].formula.startsWith("P ="))
    }

    @Test
    fun `the voltage drop steps end on the figures the calculator reports`() {
        val result = CalculateVoltageDropUseCase()(voltageDropInput)

        val steps = explainVoltageDrop(voltageDropInput, result, us)

        // 0.017241 × [1 + 0.00393 × 50] = 0.020629
        assertEquals("0.020629 Ω·mm²/m", steps[0].result)
        assertEquals("0.0774 Ω", steps[1].result)
        assertEquals("6.03 V", steps[2].result)
        assertEquals("1.51 %", steps[3].result)
    }

    @Test
    fun `the temperature correction shows both resistivities`() {
        // The step exists precisely because this correction is the one people
        // leave out by hand; seeing 0.017241 become 0.020629 makes the 20 % it
        // is worth impossible to miss.
        val result = CalculateVoltageDropUseCase()(voltageDropInput)

        val steps = explainVoltageDrop(voltageDropInput, result, us)

        assertTrue(steps[0].substitution.contains("0.017241"))
        assertEquals("0.020629 Ω·mm²/m", steps[0].result)
    }

    // -- Motor ------------------------------------------------------------------------------

    private val motorInput = MotorInput(
        ratedPower = 5.5,
        powerUnit = PowerUnit.KILOWATT,
        voltage = 400.0,
        efficiency = 0.89,
        powerFactor = 0.85,
        system = SupplySystem.THREE_PHASE_AC,
        startingCurrentRatio = 6.0,
    )

    @Test
    fun `the motor solution starts with the conversion people forget`() {
        // Dividing by the efficiency before anything else is the step that most
        // often surprises, so it is the first thing after the unit conversion.
        val result = CalculateMotorCurrentUseCase()(motorInput)

        val steps = explainMotorCurrent(motorInput, result, us)

        assertEquals("P_out = P_n · c", steps[0].formula)
        assertEquals("P_in = P_out / η", steps[1].formula)
        assertEquals("I = P_in / (k · U · cos φ)", steps[2].formula)
    }

    @Test
    fun `the motor steps end on the figures the calculator reports`() {
        val result = CalculateMotorCurrentUseCase()(motorInput)

        val steps = explainMotorCurrent(motorInput, result, us)

        assertEquals("5,500 W", steps[0].result)
        assertEquals("6,179.78 W", steps[1].result)
        assertEquals("10.49 A", steps[2].result)
        assertEquals("62.96 A", steps[3].result)
    }

    @Test
    fun `the conversion step distinguishes the two horsepowers`() {
        // They look interchangeable on a nameplate and are 1.4 % apart. The
        // step makes which one was used visible instead of implicit.
        val mechanical = motorInput.copy(ratedPower = 10.0, powerUnit = PowerUnit.HORSEPOWER)
        val metric = motorInput.copy(ratedPower = 10.0, powerUnit = PowerUnit.METRIC_HORSEPOWER)

        val mechanicalSteps = explainMotorCurrent(
            mechanical,
            CalculateMotorCurrentUseCase()(mechanical),
            us,
        )
        val metricSteps = explainMotorCurrent(metric, CalculateMotorCurrentUseCase()(metric), us)

        assertTrue(mechanicalSteps[0].substitution.contains("745.6999"))
        assertTrue(metricSteps[0].substitution.contains("735.4987"))
        assertEquals("7,457 W", mechanicalSteps[0].result)
        assertEquals("7,354.99 W", metricSteps[0].result)
    }

    // -- Power ----------------------------------------------------------------------------------

    private val powerInput = PowerInput(
        voltage = 400.0,
        current = 25.0,
        powerFactor = 0.85,
        system = SupplySystem.THREE_PHASE_AC,
        powerFactorType = PowerFactorType.LAGGING,
    )

    @Test
    fun `the power solution derives everything from apparent power`() {
        // The opposite of how most people remember the triangle, and the reason
        // showing the work is worth doing here.
        val result = CalculatePowerUseCase()(powerInput)

        val steps = explainPower(powerInput, result, us)

        assertEquals("S = k · U · I", steps[0].formula)
        assertEquals("P = S · cos φ", steps[1].formula)
        assertEquals("Q = S · sin φ", steps[2].formula)
    }

    @Test
    fun `a DC supply has no reactive or angle step`() {
        // There is no phase angle to speak of; a line reading "Q = 0" would
        // imply a calculation that never happened.
        val dc = powerInput.copy(system = SupplySystem.DC)
        val result = CalculatePowerUseCase()(dc)

        val steps = explainPower(dc, result, us)

        assertEquals(2, steps.size)
        assertFalse(steps.any { it.formula.startsWith("Q") })
    }

    // -- Cable size ---------------------------------------------------------------------------

    private val cableSizeUseCase = CalculateCableSizeUseCase(AmpacityTable(), CorrectionFactors())

    private val cableSizeInput = CableSizeInput(
        systemVoltage = 230.0,
        designCurrent = 25.0,
        lengthMetres = 30.0,
        material = ConductorMaterial.COPPER,
        insulation = CableInsulation.PVC,
        method = InstallationMethod.C_CLIPPED_DIRECT,
        system = SupplySystem.SINGLE_PHASE_AC,
        powerFactor = 1.0,
        maxVoltageDropPercent = 5.0,
        ambientTemperatureC = 30.0,
        groupedCircuits = 1,
        parallelConductors = 1,
    )

    @Test
    fun `the cable size solution works both constraints before comparing them`() {
        val result = cableSizeUseCase(cableSizeInput)

        val steps = explainCableSize(cableSizeInput, result, us)

        assertEquals(8, steps.size)
        assertEquals("I_z ≥ I_b / (n · Ca · Cg)", steps[1].formula)
        assertEquals("A ≥ k · I · ρ(θ) · L · cos φ / (ΔU_max · n)", steps[4].formula)
        assertEquals("A = max(A_I, A_ΔU)", steps[5].formula)
    }

    @Test
    fun `the cable size steps end on the figures the calculator reports`() {
        val result = cableSizeUseCase(cableSizeInput)

        val steps = explainCableSize(cableSizeInput, result, us)

        assertEquals("1", steps[0].result)
        assertEquals("25 A", steps[1].result)
        assertEquals("0.020629 Ω·mm²/m", steps[2].result)
        // 230 × 5 / 100
        assertEquals("11.5 V", steps[3].result)
        // 2 × 25 × 0.0206286 × 30 / 11.5 = 2.6907
        assertEquals("2.69 mm²", steps[4].result)
        assertEquals("4 mm²", steps[5].result)
        assertEquals("7.74 V", steps[6].result)
        assertEquals("3.36 %", steps[7].result)
    }

    @Test
    fun `the selection step shows the size each constraint asked for`() {
        // 2.5 mm² carries the current; 4 mm² is needed for the drop. Seeing
        // both is what tells the reader that shortening the run would help.
        val result = cableSizeUseCase(cableSizeInput)

        val steps = explainCableSize(cableSizeInput, result, us)

        assertEquals("max(2.5; 4)", steps[5].substitution)
    }

    @Test
    fun `a circuit no standard size satisfies still gets the reasoning`() {
        // The first five steps are why it failed. Withholding them at the one
        // moment the user needs an explanation would be the wrong way round.
        val impossible = cableSizeInput.copy(designCurrent = 5_000.0)
        val result = cableSizeUseCase(impossible)

        val steps = explainCableSize(impossible, result, us)

        assertFalse(result.hasSolution)
        assertEquals(5, steps.size)
    }

    // -- Transformer ---------------------------------------------------------------------------

    private val transformerInput = TransformerInput(
        ratingKva = 1_000.0,
        primaryVoltage = 34_500.0,
        secondaryVoltage = 400.0,
        impedanceVoltagePercent = 6.0,
        system = SupplySystem.THREE_PHASE_AC,
    )

    @Test
    fun `the transformer steps end on the figures the calculator reports`() {
        val result = CalculateTransformerCurrentUseCase()(transformerInput)

        val steps = explainTransformer(transformerInput, result, us)

        assertEquals(6, steps.size)
        assertEquals("1,000,000 VA", steps[0].result)
        assertEquals("16.73 A", steps[1].result)
        // The canonical 1443 A of a 1000 kVA transformer at 400 V.
        assertEquals("1,443.38 A", steps[2].result)
        assertEquals("86.25", steps[3].result)
        assertEquals("24,056.26 A", steps[4].result)
        assertEquals("16,666.67 kVA", steps[5].result)
    }

    @Test
    fun `the fault current is derived from the secondary current, not the rating`() {
        // u_k is a percentage of rated voltage, so what it acts on is the rated
        // current. Written out, the definition is legible.
        val result = CalculateTransformerCurrentUseCase()(transformerInput)

        val steps = explainTransformer(transformerInput, result, us)

        assertEquals("I_sc = I₂ · 100 / u_k", steps[4].formula)
        assertTrue(steps[4].substitution.startsWith("1,443.38"))
    }

    // -- Power factor ----------------------------------------------------------------------------

    private val powerFactorInput = PowerFactorInput(
        activePowerWatts = 100_000.0,
        existingPowerFactor = 0.75,
        targetPowerFactor = 0.95,
        voltage = 400.0,
        frequencyHz = 50.0,
        connection = CapacitorConnection.DELTA,
        system = SupplySystem.THREE_PHASE_AC,
    )

    @Test
    fun `the power factor steps end on the figures the calculator reports`() {
        val result = CalculatePowerFactorCorrectionUseCase()(powerFactorInput)

        val steps = explainPowerFactor(powerFactorInput, result, us)

        assertEquals(9, steps.size)
        assertEquals("0.882", steps[0].result)
        assertEquals("0.329", steps[1].result)
        assertEquals("55,323.3 var", steps[2].result)
        assertEquals("366.87 µF", steps[3].result)
        assertEquals("133,333.33 VA", steps[4].result)
        assertEquals("105,263.16 VA", steps[5].result)
        assertEquals("28,070.18 VA", steps[6].result)
        assertEquals("192.45 A", steps[7].result)
        assertEquals("151.93 A", steps[8].result)
    }

    @Test
    fun `the tangents lead so the non-linearity is visible`() {
        // cos 0.75 and cos 0.95 are 0.20 apart; their tangents are 0.55 apart,
        // and it is the tangents the capacitor bank is sized on.
        val result = CalculatePowerFactorCorrectionUseCase()(powerFactorInput)

        val steps = explainPowerFactor(powerFactorInput, result, us)

        assertTrue(steps[0].formula.startsWith("tan φ₁"))
        assertTrue(steps[1].formula.startsWith("tan φ₂"))
        assertEquals("100,000 × (0.882 − 0.329)", steps[2].substitution)
    }

    @Test
    fun `a single phase bank is not divided between three units`() {
        // Star and delta are a three-phase distinction; on single phase the
        // capacitor simply sits across the supply.
        val single = powerFactorInput.copy(system = SupplySystem.SINGLE_PHASE_AC)
        val result = CalculatePowerFactorCorrectionUseCase()(single)

        val steps = explainPowerFactor(single, result, us)

        assertTrue(steps[3].substitution.startsWith("55,323.3 / (1 ×"))
    }

    // -- Battery ------------------------------------------------------------------------------------

    private val batteryInput = BatteryInput(
        capacityAh = 100.0,
        ratedDischargeHours = 20.0,
        bankVoltage = 48.0,
        loadPowerWatts = 500.0,
        systemEfficiency = 0.90,
        depthOfDischarge = 0.50,
        peukertExponent = 1.15,
    )

    @Test
    fun `the battery steps end on the figures the calculator reports`() {
        val result = CalculateBatteryRuntimeUseCase()(batteryInput)

        val steps = explainBatteryRuntime(batteryInput, result, us)

        assertEquals(8, steps.size)
        assertEquals("11.57 A", steps[0].result)
        // 100 Ah over the 20 h it was rated at.
        assertEquals("5 A", steps[1].result)
        assertEquals("0.116 C", steps[2].result)
        // 3.808959 h of usable runtime is half of a full discharge at DoD 0.5.
        assertEquals("7.62 h", steps[3].result)
        assertEquals("3.81 h", steps[4].result)
        assertEquals("4.32 h", steps[5].result)
        assertEquals("50 Ah", steps[6].result)
        assertEquals("1,904.48 Wh", steps[7].result)
    }

    @Test
    fun `the naive runtime is shown beside the one Peukert gives`() {
        // 4.32 h against 3.81 h: a UPS sized on the optimistic figure runs out
        // before the generator starts.
        val result = CalculateBatteryRuntimeUseCase()(batteryInput)

        val steps = explainBatteryRuntime(batteryInput, result, us)

        assertEquals("t = t_full · DoD", steps[4].formula)
        assertEquals("t_ideal = C / I · DoD", steps[5].formula)
        assertTrue(result.idealRuntimeHours > result.runtimeHours)
    }

    // -- Cable weight ---------------------------------------------------------------------------------

    private val cableWeightInput = CableWeightInput(
        crossSectionMm2 = 25.0,
        conductorCount = 4,
        lengthMeters = 1_000.0,
        material = ConductorMaterial.COPPER,
        overallDiameterMm = 25.0,
        insulation = CableInsulation.PVC,
    )

    @Test
    fun `the cable weight steps end on the figures the calculator reports`() {
        val result = CalculateCableWeightUseCase()(cableWeightInput)

        val steps = explainCableWeight(cableWeightInput, result, us)

        assertEquals(9, steps.size)
        assertEquals("100 mm²", steps[0].result)
        assertEquals("100 dm³", steps[1].result)
        // 1 mm² of copper weighs 8.89 kg per kilometre, as IEC 60228 tabulates.
        assertEquals("889 kg", steps[2].result)
        assertEquals("0.889 kg/m", steps[3].result)
        // π × 25² / 4
        assertEquals("490.87 mm²", steps[4].result)
        assertEquals("547.22 kg", steps[5].result)
        assertEquals("1,436.22 kg", steps[6].result)
        assertEquals("1.4362 kg/m", steps[7].result)
        assertEquals("61.9 %", steps[8].result)
    }

    @Test
    fun `without a diameter the solution stops where the certainty does`() {
        // Sheath thickness is a manufacturer's design choice. Nothing after the
        // conductor can be derived, so nothing after it is shown.
        val noDiameter = cableWeightInput.copy(overallDiameterMm = null)
        val result = CalculateCableWeightUseCase()(noDiameter)

        val steps = explainCableWeight(noDiameter, result, us)

        assertEquals(4, steps.size)
        assertFalse(steps.any { it.formula.contains("m_s") })
    }

    // -- Conduit fill -----------------------------------------------------------------------------------

    private val conduitFillInput = ConduitFillInput(
        conduitInnerDiameterMm = 25.0,
        cables = listOf(CableBundleEntry(diameterMm = 8.5, quantity = 3)),
    )

    @Test
    fun `the conduit fill steps end on the figures the calculator reports`() {
        val result = CalculateConduitFillUseCase()(conduitFillInput)

        val steps = explainConduitFill(conduitFillInput, result, us)

        assertEquals(5, steps.size)
        assertEquals("490.87 mm²", steps[0].result)
        assertEquals("170.24 mm²", steps[1].result)
        assertEquals("34.68 %", steps[2].result)
        // 490.873852 × 0.40 − 170.235052
        assertEquals("26.11 mm²", steps[3].result)
        // √(4 × 26.1145 / π)
        assertEquals("5.77 mm", steps[4].result)
    }

    @Test
    fun `each cable in the bundle is named in the area sum`() {
        // A fill result that disagrees with the reader's own arithmetic is
        // almost always a quantity or a diameter typed wrong, and the summed
        // line is where that becomes obvious.
        val mixed = conduitFillInput.copy(
            cables = listOf(
                CableBundleEntry(diameterMm = 8.5, quantity = 3),
                CableBundleEntry(diameterMm = 12.0, quantity = 2),
            ),
        )
        val result = CalculateConduitFillUseCase()(mixed)

        val steps = explainConduitFill(mixed, result, us)

        assertEquals("3 × π × 8.5² / 4 + 2 × π × 12² / 4", steps[1].substitution)
    }

    @Test
    fun `the spare area names the limit it is measured against`() {
        val result = CalculateConduitFillUseCase()(conduitFillInput)

        val steps = explainConduitFill(conduitFillInput, result, us)

        assertTrue(steps[3].substitution.contains("0.4"))
    }

    // -- Tray fill ---------------------------------------------------------------------------------------

    private val trayFillInput = TrayFillInput(
        trayWidthMm = 300.0,
        trayDepthMm = 100.0,
        cables = listOf(CableBundleEntry(diameterMm = 20.5, quantity = 6)),
        arrangement = TrayArrangement.SINGLE_LAYER,
        clearSpacingMm = 0.0,
        permittedFillFraction = 0.40,
    )

    @Test
    fun `a single layer is solved on width and never mentions a fill percentage`() {
        // This tray is 20 % full by area. Reporting that would say four fifths
        // of it is free when what is free is 177 mm across.
        val result = CalculateTrayFillUseCase()(trayFillInput)

        val steps = explainTrayFill(trayFillInput, result, us)

        assertEquals(5, steps.size)
        assertEquals("123 mm", steps[0].result)
        assertEquals("177 mm", steps[1].result)
        assertEquals("41 %", steps[2].result)
        assertEquals("177 mm", steps[3].result)
        // 1980.38 mm² of cable spread across 300 mm of tray.
        assertEquals("6.6 mm", steps[4].result)
        assertFalse(steps.any { it.formula.startsWith("fill") })
    }

    @Test
    fun `the width sum counts the gaps between cables, not one per cable`() {
        // Six cables leave five gaps. Counting six would quietly demand a wider
        // tray than the installation needs.
        val spaced = trayFillInput.copy(clearSpacingMm = 20.5)
        val result = CalculateTrayFillUseCase()(spaced)

        val steps = explainTrayFill(spaced, result, us)

        assertEquals("6 × 20.5 + 20.5 × 5", steps[0].substitution)
        assertEquals("225.5 mm", steps[0].result)
    }

    @Test
    fun `a stacked tray is solved on area instead`() {
        val stacked = trayFillInput.copy(arrangement = TrayArrangement.MULTI_LAYER)
        val result = CalculateTrayFillUseCase()(stacked)

        val steps = explainTrayFill(stacked, result, us)

        assertEquals(6, steps.size)
        assertEquals("30,000 mm²", steps[0].result)
        assertEquals("1,980.38 mm²", steps[1].result)
        assertEquals("6.6 %", steps[2].result)
        // 30,000 × 0.40 − 1,980.38
        assertEquals("10,019.62 mm²", steps[3].result)
        assertEquals("6.6 mm", steps[4].result)
        // 14 cables fit across 300 mm at 20.5 mm each, so six make one layer.
        assertEquals("1", steps[5].result)
    }

    // -- Short circuit ---------------------------------------------------------------------------------------

    private val shortCircuitInput = ShortCircuitInput(
        faultType = FaultType.THREE_PHASE,
        nominalVoltage = 400.0,
        supplyFaultCurrentAmps = 20_000.0,
        lengthMetres = 50.0,
        crossSectionMm2 = 25.0,
        neutralCrossSectionMm2 = 25.0,
        parallelConductors = 1,
        material = ConductorMaterial.COPPER,
        insulation = CableInsulation.PVC,
        reactancePerKmOhms = ShortCircuitInput.DEFAULT_REACTANCE_PER_KM,
    )

    @Test
    fun `the short circuit steps end on the figures the calculator reports`() {
        val result = CalculateShortCircuitUseCase()(shortCircuitInput)

        val steps = explainShortCircuit(shortCircuitInput, result, us)

        assertEquals(8, steps.size)
        assertEquals("0.0121 Ω", steps[0].result)
        assertEquals("0.0345 Ω", steps[1].result)
        assertEquals("0.0413 Ω", steps[2].result)
        assertEquals("0.004 Ω", steps[3].result)
        assertEquals("0.0468 Ω", steps[4].result)
        assertEquals("5,177.19 A", steps[5].result)
        assertEquals("0.0536 Ω", steps[6].result)
        assertEquals("4,095.03 A", steps[7].result)
    }

    @Test
    fun `both currents are worked in full, each on its own impedance`() {
        // The whole point of this calculator is that the two are not
        // interchangeable, so neither is quoted as a variation of the other.
        val result = CalculateShortCircuitUseCase()(shortCircuitInput)

        val steps = explainShortCircuit(shortCircuitInput, result, us)

        assertEquals("I_max = c_max · U / (k · Z_cold)", steps[5].formula)
        assertEquals("I_min = c_min · U / (k · Z_hot)", steps[7].formula)
        assertTrue(steps[5].substitution.startsWith("1.05"))
        assertTrue(steps[7].substitution.startsWith("0.95"))
    }

    @Test
    fun `a line to neutral fault shows the neutral as its own term`() {
        // A reduced neutral is the case most likely to fail to trip, and
        // folding the two conductors into one number would hide the cause.
        val lineToNeutral = shortCircuitInput.copy(
            faultType = FaultType.LINE_TO_NEUTRAL,
            nominalVoltage = 230.0,
            neutralCrossSectionMm2 = 16.0,
        )
        val result = CalculateShortCircuitUseCase()(lineToNeutral)

        val steps = explainShortCircuit(lineToNeutral, result, us)

        assertTrue(steps[1].substitution.contains(" + "))
        assertTrue(steps[1].substitution.contains("(16 × 1)"))
    }

    // -- Earth fault ---------------------------------------------------------------------------------------

    private val earthFaultInput = EarthFaultInput(
        externalImpedanceOhms = 0.35,
        phaseVoltage = 230.0,
        lengthMetres = 30.0,
        lineCrossSectionMm2 = 4.0,
        protectiveCrossSectionMm2 = 2.5,
        parallelConductors = 1,
        material = ConductorMaterial.COPPER,
        insulation = CableInsulation.PVC,
        deviceType = ProtectiveDeviceType.MCB_TYPE_B,
        deviceRatingAmps = 32.0,
        clearingTimeSeconds = 0.1,
    )

    @Test
    fun `the earth fault steps end on the figures the calculator reports`() {
        val result = CalculateEarthFaultUseCase()(earthFaultInput)

        val steps = explainEarthFault(earthFaultInput, result, us)

        assertEquals(8, steps.size)
        assertEquals("0.020629 Ω·mm²/m", steps[0].result)
        assertEquals("0.1547 Ω", steps[1].result)
        assertEquals("0.2475 Ω", steps[2].result)
        assertEquals("0.7523 Ω", steps[3].result)
        // A Type B trips magnetically by 5 × In.
        assertEquals("160 A", steps[4].result)
        assertEquals("1.3656 Ω", steps[5].result)
        assertEquals("290.46 A", steps[6].result)
        // √(290.46² × 0.1) / 115
        assertEquals("0.8 mm²", steps[7].result)
    }

    @Test
    fun `an RCD is judged on touch voltage and has no operating current step`() {
        // It responds to the imbalance between line and neutral, not to how big
        // the fault current is. Inventing an Ia would describe a mechanism it
        // does not have.
        val rcd = earthFaultInput.copy(
            deviceType = ProtectiveDeviceType.RCD,
            deviceRatingAmps = 0.03,
        )
        val result = CalculateEarthFaultUseCase()(rcd)

        val steps = explainEarthFault(rcd, result, us)

        assertEquals(7, steps.size)
        assertFalse(steps.any { it.formula.startsWith("I_a") })
        assertEquals("Z_s,max = U_L / I_Δn", steps[4].formula)
        assertEquals("50 / 0.03", steps[4].substitution)
    }

    @Test
    fun `a device read from its own curve skips the multiplier step`() {
        // For CUSTOM the rating field already holds Ia, so multiplying it by
        // anything would be wrong.
        val custom = earthFaultInput.copy(
            deviceType = ProtectiveDeviceType.CUSTOM,
            deviceRatingAmps = 250.0,
        )
        val result = CalculateEarthFaultUseCase()(custom)

        val steps = explainEarthFault(custom, result, us)

        assertEquals(7, steps.size)
        assertEquals("Z_s,max = c · U₀ / I_a", steps[4].formula)
        assertTrue(steps[4].substitution.endsWith("250"))
    }

    // -- Locale ------------------------------------------------------------------------------------

    @Test
    fun `substitutions follow the reader's decimal separator`() {
        val result = CalculateMotorCurrentUseCase()(motorInput)

        val english = explainMotorCurrent(motorInput, result, us)
        val turkish = explainMotorCurrent(motorInput, result, turkey)

        assertEquals("6,179.78 W", english[1].result)
        assertEquals("6.179,78 W", turkish[1].result)
    }

    @Test
    fun `the formula line is language-neutral`() {
        // Symbols are not translated anywhere else in the app and are not here.
        val result = CalculateMotorCurrentUseCase()(motorInput)

        val english = explainMotorCurrent(motorInput, result, us)
        val turkish = explainMotorCurrent(motorInput, result, turkey)

        assertEquals(english.map { it.formula }, turkish.map { it.formula })
    }

    @Test
    fun `every solution is language-neutral in its formulas`() {
        everySolution(us).zip(everySolution(turkey)).forEach { (english, turkish) ->
            assertEquals(english.map { it.formula }, turkish.map { it.formula })
        }
    }

    // -- Properties every solution must hold ------------------------------------------------------

    @Test
    fun `no step is empty`() {
        everySolution(us).forEach { steps ->
            assertTrue(steps.isNotEmpty())
            steps.forEach { step ->
                assertTrue("blank formula", step.formula.isNotBlank())
                assertTrue("blank substitution", step.substitution.isNotBlank())
                assertTrue("blank result", step.result.isNotBlank())
                assertTrue("unresolved label", step.labelRes != 0)
            }
        }
    }

    @Test
    fun `a substitution never still contains its symbols`() {
        // The point of the line is that the symbols have been replaced by
        // numbers; one that still reads "P_out / η" has substituted nothing.
        val symbols = listOf("P_", "I_z", "I_b", "Z_s", "ρ(θ)", "η", "cos φ", "DoD", "u_k")

        everySolution(us).forEach { steps ->
            steps.forEach { step ->
                symbols.forEach { symbol ->
                    assertFalse(
                        "${step.substitution} still contains $symbol",
                        step.substitution.contains(symbol),
                    )
                }
            }
        }
    }

    /** Every worked solution the app can produce, for the properties above. */
    private val selectivityInput = SelectivityInput(
        upstreamType = ProtectiveDeviceType.MCB_TYPE_C,
        upstreamRatingAmps = 63.0,
        downstreamType = ProtectiveDeviceType.MCB_TYPE_B,
        downstreamRatingAmps = 16.0,
        prospectiveFaultAmps = 480.0,
    )

    @Test
    fun `the selectivity solution ends on the headroom before the upstream device joins in`() {
        val result = CheckSelectivityUseCase()(selectivityInput)

        val steps = explainSelectivity(selectivityInput, result, us)

        assertEquals(3, steps.size)
        // 63 / 16, then 10 × 63, then 630 − 480.
        assertEquals("3.94", steps[0].result)
        assertEquals("630 A", steps[1].result)
        assertEquals("150 A", steps[2].result)
    }

    @Test
    fun `a device with no readable curve is not given a threshold step`() {
        // An RCD has no instantaneous multiple. Writing a step that says so
        // would put prose where every other result line carries a figure.
        val result = CheckSelectivityUseCase()(
            selectivityInput.copy(upstreamType = ProtectiveDeviceType.RCD),
        )

        assertEquals(1, explainSelectivity(selectivityInput.copy(upstreamType = ProtectiveDeviceType.RCD), result, us).size)
    }

    private val motorStartingInput = MotorStartingInput(
        fullLoadCurrentAmps = 55.0,
        lockedRotorMultiple = 6.0,
        method = StartingMethod.DIRECT_ON_LINE,
        supplyVoltage = 400.0,
        transformerKva = 400.0,
        transformerImpedancePercent = 4.0,
    )

    @Test
    fun `the motor starting solution builds the dip from the two powers`() {
        val result = CalculateMotorStartingUseCase()(motorStartingInput)

        val steps = explainMotorStarting(motorStartingInput, result, us)

        assertEquals(4, steps.size)
        assertEquals("330 A", steps[0].result)
        assertEquals("10,000 kVA", steps[2].result)
        // The dip is the last line, because it is a ratio of the two above it.
        assertTrue(steps[3].formula.startsWith("ΔU%"))
    }

    private val harmonicsInput = HarmonicsInput(
        fundamentalAmps = 100.0,
        components = listOf(
            HarmonicComponent(3, 70.0),
            HarmonicComponent(5, 40.0),
            HarmonicComponent(9, 10.0),
        ),
        balanced = true,
    )

    @Test
    fun `the harmonics solution names the orders that reached the neutral`() {
        // The neutral line is written as a sum over the triplen orders alone,
        // so a reader can see which harmonics ended up there rather than being
        // handed a figure to trust.
        val steps = explainHarmonics(
            harmonicsInput,
            CalculateHarmonicsUseCase()(harmonicsInput),
            us,
        )

        assertEquals(4, steps.size)
        assertTrue(steps[2].substitution.contains("h3"))
        assertTrue(steps[2].substitution.contains("h9"))
        assertFalse("the fifth does not reach the star point", steps[2].substitution.contains("h5"))
    }

    private val evseInput = EvseInput(
        pointCount = 8,
        ratedCurrentPerPoint = 32.0,
        connection = EvseConnection.THREE_PHASE,
        supplyVoltage = 400.0,
        simultaneityFactor = 1.0,
        dcFaultDetection = DcFaultDetection.NONE,
    )

    private fun everySolution(locale: Locale): List<ImmutableList<CalculationStep>> = listOf(
        explainEvse(evseInput, CalculateEvseUseCase()(evseInput), locale),
        explainHarmonics(harmonicsInput, CalculateHarmonicsUseCase()(harmonicsInput), locale),
        explainSelectivity(selectivityInput, CheckSelectivityUseCase()(selectivityInput), locale),
        explainMotorStarting(
            motorStartingInput,
            CalculateMotorStartingUseCase()(motorStartingInput),
            locale,
        ),
        explainVoltageDrop(
            voltageDropInput,
            CalculateVoltageDropUseCase()(voltageDropInput),
            locale,
        ),
        explainMotorCurrent(motorInput, CalculateMotorCurrentUseCase()(motorInput), locale),
        explainPower(powerInput, CalculatePowerUseCase()(powerInput), locale),
        explainCableSize(cableSizeInput, cableSizeUseCase(cableSizeInput), locale),
        explainTransformer(
            transformerInput,
            CalculateTransformerCurrentUseCase()(transformerInput),
            locale,
        ),
        explainPowerFactor(
            powerFactorInput,
            CalculatePowerFactorCorrectionUseCase()(powerFactorInput),
            locale,
        ),
        explainBatteryRuntime(batteryInput, CalculateBatteryRuntimeUseCase()(batteryInput), locale),
        explainCableWeight(
            cableWeightInput,
            CalculateCableWeightUseCase()(cableWeightInput),
            locale,
        ),
        explainConduitFill(
            conduitFillInput,
            CalculateConduitFillUseCase()(conduitFillInput),
            locale,
        ),
        explainTrayFill(trayFillInput, CalculateTrayFillUseCase()(trayFillInput), locale),
        explainTrayFill(
            trayFillInput.copy(arrangement = TrayArrangement.MULTI_LAYER),
            CalculateTrayFillUseCase()(
                trayFillInput.copy(arrangement = TrayArrangement.MULTI_LAYER),
            ),
            locale,
        ),
        explainShortCircuit(
            shortCircuitInput,
            CalculateShortCircuitUseCase()(shortCircuitInput),
            locale,
        ),
        explainEarthFault(earthFaultInput, CalculateEarthFaultUseCase()(earthFaultInput), locale),
    )
}
