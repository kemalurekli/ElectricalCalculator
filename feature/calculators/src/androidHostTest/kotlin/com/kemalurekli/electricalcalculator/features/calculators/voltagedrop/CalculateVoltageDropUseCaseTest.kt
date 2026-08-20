package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop

import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Engineering verification of the voltage-drop calculation.
 *
 * Every expected value is derived from the published formula by hand in the
 * test itself rather than copied from the implementation, so a change in the
 * production code cannot quietly redefine what "correct" means.
 */
class CalculateVoltageDropUseCaseTest {

    private val calculate = CalculateVoltageDropUseCase()

    private fun input(
        voltage: Double = 230.0,
        current: Double = 20.0,
        length: Double = 30.0,
        area: Double = 4.0,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        system: SupplySystem = SupplySystem.SINGLE_PHASE_AC,
        powerFactor: Double = 1.0,
        temperature: Double = 20.0,
        parallel: Int = 1,
    ) = VoltageDropInput(
        systemVoltage = voltage,
        loadCurrent = current,
        lengthMetres = length,
        crossSectionMm2 = area,
        material = material,
        system = system,
        powerFactor = powerFactor,
        conductorTemperatureC = temperature,
        parallelConductors = parallel,
    )

    // -- Core formula ------------------------------------------------------

    @Test
    fun `single phase drop matches the hand calculation`() {
        // R = 0.017241 * 30 / 4 = 0.1293075 Ω
        // ΔU = 2 * 20 * 0.1293075 * 1.0 = 5.17230 V
        val expectedResistance = 0.017241 * 30.0 / 4.0
        val expectedDrop = 2.0 * 20.0 * expectedResistance

        val result = calculate(input())

        assertEquals(expectedResistance, result.conductorResistance, 1e-9)
        assertEquals(expectedDrop, result.voltageDrop, 1e-9)
        assertEquals(5.1723, result.voltageDrop, 1e-4)
    }

    @Test
    fun `three phase uses the root three multiplier`() {
        val expectedResistance = 0.017241 * 30.0 / 4.0
        val expectedDrop = sqrt(3.0) * 20.0 * expectedResistance

        val result = calculate(input(system = SupplySystem.THREE_PHASE_AC))

        assertEquals(expectedDrop, result.voltageDrop, 1e-9)
    }

    @Test
    fun `three phase drop is lower than single phase for the same run`() {
        // √3 ≈ 1.732 against 2, so a balanced three-phase circuit drops ~13% less.
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC)).voltageDrop
        val three = calculate(input(system = SupplySystem.THREE_PHASE_AC)).voltageDrop

        assertEquals(sqrt(3.0) / 2.0, three / single, 1e-9)
    }

    @Test
    fun `dc uses the same multiplier as single phase`() {
        val dc = calculate(input(system = SupplySystem.DC)).voltageDrop
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC)).voltageDrop

        assertEquals(single, dc, 1e-12)
    }

    // -- Percentage, load voltage, loss -------------------------------------

    @Test
    fun `percentage is referenced to the nominal supply voltage`() {
        val result = calculate(input(voltage = 230.0))

        assertEquals(result.voltageDrop / 230.0 * 100.0, result.dropPercentage, 1e-9)
        assertEquals(2.2488, result.dropPercentage, 1e-4)
    }

    @Test
    fun `load voltage is the supply less the drop`() {
        val result = calculate(input())

        assertEquals(230.0 - result.voltageDrop, result.voltageAtLoad, 1e-9)
    }

    @Test
    fun `single phase loss counts both conductors`() {
        val result = calculate(input())

        // P = 2 · I² · R
        assertEquals(2.0 * 20.0 * 20.0 * result.conductorResistance, result.powerLossWatts, 1e-9)
    }

    @Test
    fun `three phase loss counts three conductors`() {
        val result = calculate(input(system = SupplySystem.THREE_PHASE_AC))

        // A balanced three-phase circuit carries no neutral current, so loss is 3I²R.
        assertEquals(3.0 * 20.0 * 20.0 * result.conductorResistance, result.powerLossWatts, 1e-9)
    }

    // -- Material -----------------------------------------------------------

    @Test
    fun `aluminium drops more than copper for the same section`() {
        val copper = calculate(input(material = ConductorMaterial.COPPER)).voltageDrop
        val aluminium = calculate(input(material = ConductorMaterial.ALUMINIUM)).voltageDrop

        assertTrue(aluminium > copper)
        // Ratio of resistivities: 0.028264 / 0.017241 ≈ 1.639
        assertEquals(0.028264 / 0.017241, aluminium / copper, 1e-9)
    }

    // -- Temperature correction ---------------------------------------------

    @Test
    fun `resistance at 20C uses the uncorrected resistivity`() {
        val result = calculate(input(temperature = 20.0))

        assertEquals(0.017241 * 30.0 / 4.0, result.conductorResistance, 1e-12)
    }

    @Test
    fun `resistance rises with conductor temperature`() {
        // ρ(70) = ρ₂₀ · [1 + 0.00393 · 50] = ρ₂₀ · 1.1965
        val expected = 0.017241 * (1.0 + 0.00393 * 50.0) * 30.0 / 4.0

        val result = calculate(input(temperature = 70.0))

        assertEquals(expected, result.conductorResistance, 1e-12)
    }

    @Test
    fun `ignoring the 70C correction would under-report the drop by about 20 percent`() {
        val at20 = calculate(input(temperature = 20.0)).voltageDrop
        val at70 = calculate(input(temperature = 70.0)).voltageDrop

        assertEquals(1.1965, at70 / at20, 1e-4)
    }

    @Test
    fun `resistance falls below the reference value under 20C`() {
        val result = calculate(input(temperature = 0.0))

        val expected = 0.017241 * (1.0 - 0.00393 * 20.0) * 30.0 / 4.0
        assertEquals(expected, result.conductorResistance, 1e-12)
    }

    // -- Power factor -------------------------------------------------------

    @Test
    fun `power factor scales the ac drop`() {
        val unity = calculate(input(powerFactor = 1.0)).voltageDrop
        val lagging = calculate(input(powerFactor = 0.8)).voltageDrop

        assertEquals(0.8, lagging / unity, 1e-9)
    }

    @Test
    fun `power factor is ignored on a dc supply`() {
        // There is no phase angle on DC; applying cos phi would under-report.
        val withPf = calculate(input(system = SupplySystem.DC, powerFactor = 0.5)).voltageDrop
        val withoutPf = calculate(input(system = SupplySystem.DC, powerFactor = 1.0)).voltageDrop

        assertEquals(withoutPf, withPf, 1e-12)
    }

    // -- Parallel conductors -------------------------------------------------

    @Test
    fun `two conductors in parallel halve the resistance`() {
        val single = calculate(input(parallel = 1))
        val doubled = calculate(input(parallel = 2))

        assertEquals(single.conductorResistance / 2.0, doubled.conductorResistance, 1e-12)
        assertEquals(single.voltageDrop / 2.0, doubled.voltageDrop, 1e-12)
    }

    // -- Linearity -----------------------------------------------------------

    @Test
    fun `drop scales linearly with length and current`() {
        val base = calculate(input(length = 30.0, current = 20.0)).voltageDrop

        assertEquals(2.0 * base, calculate(input(length = 60.0, current = 20.0)).voltageDrop, 1e-9)
        assertEquals(2.0 * base, calculate(input(length = 30.0, current = 40.0)).voltageDrop, 1e-9)
    }

    @Test
    fun `drop is inversely proportional to cross section`() {
        val small = calculate(input(area = 2.5)).voltageDrop
        val large = calculate(input(area = 5.0)).voltageDrop

        assertEquals(small / 2.0, large, 1e-9)
    }

    // -- Compliance status ---------------------------------------------------

    @Test
    fun `status is within the lighting limit at or below 3 percent`() {
        // 230 V, 3% = 6.9 V. A 30 m / 6 mm² run at 20 A drops ~3.45 V (1.5%).
        val result = calculate(input(area = 6.0))

        assertTrue(result.dropPercentage < 3.0)
        assertEquals(VoltageDropStatus.WITHIN_LIGHTING_LIMIT, result.status)
    }

    @Test
    fun `status is within the power limit between 3 and 5 percent`() {
        // 1.5 mm² over 30 m at 20 A: R = 0.34482 Ω, ΔU = 13.79 V = 6.0%.
        // Shorten to 20 m: ΔU = 9.195 V = 4.0%.
        val result = calculate(input(area = 1.5, length = 20.0))

        assertTrue(result.dropPercentage in 3.0..5.0)
        assertEquals(VoltageDropStatus.WITHIN_POWER_LIMIT, result.status)
    }

    @Test
    fun `status exceeds the limits above 5 percent`() {
        val result = calculate(input(area = 1.5, length = 40.0))

        assertTrue(result.dropPercentage > 5.0)
        assertEquals(VoltageDropStatus.EXCEEDS_LIMITS, result.status)
    }

    @Test
    fun `status boundaries are inclusive`() {
        assertEquals(
            VoltageDropStatus.WITHIN_LIGHTING_LIMIT,
            VoltageDropStatus.forPercentage(3.0),
        )
        assertEquals(
            VoltageDropStatus.WITHIN_POWER_LIMIT,
            VoltageDropStatus.forPercentage(5.0),
        )
        assertEquals(
            VoltageDropStatus.EXCEEDS_LIMITS,
            VoltageDropStatus.forPercentage(5.0001),
        )
    }

    // -- Worked reference case ------------------------------------------------

    @Test
    fun `worked example - 3 phase 400V 63A 50m 25mm2 copper at 70C`() {
        // Carried to full precision rather than rounded at each step: the loss
        // term multiplies resistance by 3·I² = 11 907, so an error in the sixth
        // decimal of R shows up in the second decimal of the wattage.
        //
        // ρ(70) = 0.017241 · (1 + 0.00393 · 50) = 0.0206288565 Ω·mm²/m
        // R     = 0.0206288565 · 50 / 25        = 0.0412577130 Ω
        // ΔU    = √3 · 63 · R · 0.9             = 4.0518078 V
        // ΔU%   = ΔU / 400 · 100                = 1.0129520 %
        // P     = 3 · 63² · R                   = 491.2556 W
        val expectedResistance = 0.017241 * (1.0 + 0.00393 * 50.0) * 50.0 / 25.0

        val result = calculate(
            input(
                voltage = 400.0,
                current = 63.0,
                length = 50.0,
                area = 25.0,
                system = SupplySystem.THREE_PHASE_AC,
                powerFactor = 0.9,
                temperature = 70.0,
            ),
        )

        assertEquals(0.041257713, result.conductorResistance, 1e-9)
        assertEquals(4.0518078, result.voltageDrop, 1e-6)
        assertEquals(1.0129520, result.dropPercentage, 1e-6)
        assertEquals(395.9481922, result.voltageAtLoad, 1e-6)
        assertEquals(3.0 * 63.0 * 63.0 * expectedResistance, result.powerLossWatts, 1e-9)
        assertEquals(491.2556, result.powerLossWatts, 1e-3)
        assertEquals(VoltageDropStatus.WITHIN_LIGHTING_LIMIT, result.status)
    }
}
