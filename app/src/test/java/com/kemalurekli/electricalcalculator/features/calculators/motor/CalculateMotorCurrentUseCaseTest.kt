package com.kemalurekli.electricalcalculator.features.calculators.motor

import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class CalculateMotorCurrentUseCaseTest {

    private val calculate = CalculateMotorCurrentUseCase()

    private fun input(
        power: Double = 7.5,
        unit: PowerUnit = PowerUnit.KILOWATT,
        voltage: Double = 400.0,
        efficiency: Double = 0.90,
        powerFactor: Double = 0.85,
        startingRatio: Double = 6.0,
        system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    ) = MotorInput(
        ratedPower = power,
        powerUnit = unit,
        voltage = voltage,
        efficiency = efficiency,
        powerFactor = powerFactor,
        startingCurrentRatio = startingRatio,
        system = system,
    )

    // -- Full load current ------------------------------------------------------

    @Test
    fun `three phase current tracks typical nameplate values`() {
        // 7.5 kW, 400 V, η 0.90, cos φ 0.85 → about 14.1 A; nameplates for this
        // frame size read 14–15 A depending on the actual η and cos φ.
        assertEquals(14.1507, calculate(input()).fullLoadCurrent, 1e-4)
        // 22 kW under the same assumptions → about 39.7 A against a ~41 A plate.
        assertEquals(
            39.6730,
            calculate(input(power = 22.0, efficiency = 0.92, powerFactor = 0.87)).fullLoadCurrent,
            1e-4,
        )
    }

    @Test
    fun `the formula is input power over root three times voltage and power factor`() {
        val result = calculate(input())
        val expected = (7500.0 / 0.90) / (sqrt(3.0) * 400.0 * 0.85)

        assertEquals(expected, result.fullLoadCurrent, 1e-9)
    }

    @Test
    fun `single phase omits the root three factor`() {
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC))
        val expected = (7500.0 / 0.90) / (400.0 * 0.85)

        assertEquals(expected, single.fullLoadCurrent, 1e-9)
    }

    @Test
    fun `single phase draws root three times the three phase current`() {
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC)).fullLoadCurrent
        val three = calculate(input(system = SupplySystem.THREE_PHASE_AC)).fullLoadCurrent

        assertEquals(sqrt(3.0), single / three, 1e-9)
    }

    // -- Efficiency ---------------------------------------------------------------

    @Test
    fun `nameplate power is shaft output so efficiency raises the current`() {
        // The defining behaviour of this calculator. Treating 7.5 kW as
        // electrical input would give 12.73 A instead of 14.15 A — an 11 %
        // under-report, enough to undersize a cable.
        val withEfficiency = calculate(input(efficiency = 0.90)).fullLoadCurrent
        val ifTreatedAsInput = calculate(input(efficiency = 1.0)).fullLoadCurrent

        assertTrue(withEfficiency > ifTreatedAsInput)
        assertEquals(1.0 / 0.90, withEfficiency / ifTreatedAsInput, 1e-9)
    }

    @Test
    fun `input power is output power divided by efficiency`() {
        val result = calculate(input(efficiency = 0.90))

        assertEquals(7500.0 / 0.90, result.inputPowerWatts, 1e-9)
        assertEquals(8333.333, result.inputPowerWatts, 1e-3)
    }

    @Test
    fun `losses are the difference between input and output`() {
        val result = calculate(input(efficiency = 0.90))

        assertEquals(7500.0 / 0.90 - 7500.0, result.lossesWatts, 1e-9)
        assertEquals(833.333, result.lossesWatts, 1e-3)
    }

    @Test
    fun `a perfectly efficient motor has no losses`() {
        val result = calculate(input(efficiency = 1.0))

        assertEquals(0.0, result.lossesWatts, 1e-9)
        assertEquals(7500.0, result.inputPowerWatts, 1e-9)
    }

    // -- Power units ----------------------------------------------------------------

    @Test
    fun `mechanical and metric horsepower are not interchangeable`() {
        val imperial = calculate(input(power = 10.0, unit = PowerUnit.HORSEPOWER))
        val metric = calculate(input(power = 10.0, unit = PowerUnit.METRIC_HORSEPOWER))

        assertTrue(imperial.fullLoadCurrent > metric.fullLoadCurrent)
        // The definitions differ by about 1.4 %.
        assertEquals(745.6998715822702 / 735.49875, imperial.fullLoadCurrent / metric.fullLoadCurrent, 1e-9)
    }

    @Test
    fun `ten horsepower converts to the expected current`() {
        val result = calculate(input(power = 10.0, unit = PowerUnit.HORSEPOWER))

        assertEquals(14.0696, result.fullLoadCurrent, 1e-4)
    }

    @Test
    fun `one kilowatt equals one thousand watts of shaft output`() {
        val result = calculate(input(power = 1.0, unit = PowerUnit.KILOWATT, efficiency = 1.0))

        assertEquals(1_000.0, result.inputPowerWatts, 1e-9)
    }

    // -- Power triangle -----------------------------------------------------------------

    @Test
    fun `apparent power is input power over the power factor`() {
        val result = calculate(input(powerFactor = 0.85))

        assertEquals(result.inputPowerWatts / 0.85, result.apparentPowerVa, 1e-9)
    }

    @Test
    fun `the power triangle closes`() {
        val result = calculate(input())

        val s = result.apparentPowerVa
        val p = result.inputPowerWatts
        val q = result.reactivePowerVar
        assertEquals(s * s, p * p + q * q, 1e-3)
    }

    @Test
    fun `unity power factor leaves no reactive component`() {
        val result = calculate(input(powerFactor = 1.0))

        assertEquals(0.0, result.reactivePowerVar, 1e-6)
        assertEquals(result.inputPowerWatts, result.apparentPowerVa, 1e-9)
    }

    // -- DC ---------------------------------------------------------------------------------

    @Test
    fun `power factor is ignored on a dc supply`() {
        val withPf = calculate(input(system = SupplySystem.DC, powerFactor = 0.5))
        val withoutPf = calculate(input(system = SupplySystem.DC, powerFactor = 1.0))

        assertEquals(withoutPf.fullLoadCurrent, withPf.fullLoadCurrent, 1e-12)
        assertEquals(0.0, withPf.reactivePowerVar, 1e-9)
    }

    @Test
    fun `dc current is input power over voltage`() {
        val result = calculate(input(system = SupplySystem.DC, voltage = 220.0, efficiency = 0.85))

        assertEquals((7500.0 / 0.85) / 220.0, result.fullLoadCurrent, 1e-9)
    }

    // -- Starting current ---------------------------------------------------------------------

    @Test
    fun `starting current is the full load current times the ratio`() {
        val result = calculate(input(startingRatio = 6.5))

        assertEquals(result.fullLoadCurrent * 6.5, result.startingCurrent, 1e-9)
    }

    @Test
    fun `a direct-on-line motor draws around six times its rated current`() {
        val result = calculate(input(startingRatio = 6.0))

        assertEquals(6.0 * 14.1507, result.startingCurrent, 1e-3)
    }

    // -- Scaling -------------------------------------------------------------------------------

    @Test
    fun `current scales linearly with rated power`() {
        val small = calculate(input(power = 5.5)).fullLoadCurrent
        val large = calculate(input(power = 11.0)).fullLoadCurrent

        assertEquals(2.0 * small, large, 1e-9)
    }

    @Test
    fun `current is inversely proportional to voltage`() {
        val low = calculate(input(voltage = 230.0)).fullLoadCurrent
        val high = calculate(input(voltage = 460.0)).fullLoadCurrent

        assertEquals(low / 2.0, high, 1e-9)
    }

    // -- Worked reference case ---------------------------------------------------------------------

    @Test
    fun `worked example - 11 kW 400V three phase at 91 percent and 0point86`() {
        val result = calculate(input(power = 11.0, efficiency = 0.91, powerFactor = 0.86))

        assertEquals(20.2877, result.fullLoadCurrent, 1e-4)
        assertEquals(12_087.912, result.inputPowerWatts, 1e-3)
        assertEquals(14_055.712, result.apparentPowerVa, 1e-3)
        assertEquals(1_087.912, result.lossesWatts, 1e-3)
        assertEquals(6.0 * 20.2877, result.startingCurrent, 1e-3)
    }
}
