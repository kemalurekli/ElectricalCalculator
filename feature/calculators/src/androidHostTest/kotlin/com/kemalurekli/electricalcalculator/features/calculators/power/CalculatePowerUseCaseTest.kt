package com.kemalurekli.electricalcalculator.features.calculators.power

import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class CalculatePowerUseCaseTest {

    private val calculate = CalculatePowerUseCase()

    private fun input(
        voltage: Double = 400.0,
        current: Double = 100.0,
        powerFactor: Double = 0.85,
        type: PowerFactorType = PowerFactorType.LAGGING,
        system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    ) = PowerInput(
        voltage = voltage,
        current = current,
        powerFactor = powerFactor,
        powerFactorType = type,
        system = system,
    )

    // -- Core triangle ----------------------------------------------------------

    @Test
    fun `worked example - 400V 100A three phase at 0point85 lagging`() {
        val result = calculate(input())

        assertEquals(69_282.0323, result.apparentPowerVa, 1e-4)
        assertEquals(58_889.7275, result.activePowerWatts, 1e-4)
        assertEquals(36_496.5752, result.reactivePowerVar, 1e-4)
        assertEquals(31.7883, result.phaseAngleDegrees, 1e-4)
        assertEquals(0.619744, result.tangentPhi, 1e-6)
    }

    @Test
    fun `apparent power uses the root three factor on three phase`() {
        val result = calculate(input())

        assertEquals(sqrt(3.0) * 400.0 * 100.0, result.apparentPowerVa, 1e-9)
    }

    @Test
    fun `single phase apparent power is voltage times current`() {
        val result = calculate(input(voltage = 230.0, current = 10.0, system = SupplySystem.SINGLE_PHASE_AC))

        assertEquals(2_300.0, result.apparentPowerVa, 1e-9)
    }

    @Test
    fun `the power triangle closes for every power factor`() {
        listOf(0.1, 0.5, 0.707, 0.85, 0.95, 1.0).forEach { pf ->
            val result = calculate(input(powerFactor = pf))
            val s = result.apparentPowerVa
            val p = result.activePowerWatts
            val q = result.reactivePowerVar

            assertEquals("pf=$pf", s * s, p * p + q * q, 1e-3)
        }
    }

    @Test
    fun `active power is apparent power times the power factor`() {
        val result = calculate(input(powerFactor = 0.8))

        assertEquals(result.apparentPowerVa * 0.8, result.activePowerWatts, 1e-9)
    }

    // -- Unity and zero power factor ----------------------------------------------

    @Test
    fun `unity power factor leaves no reactive power at all`() {
        // Derived via sqrt(1 - pf²) rather than sin(acos(pf)) precisely so this
        // is exactly zero rather than a floating-point residue.
        val result = calculate(input(powerFactor = 1.0))

        assertEquals(0.0, result.reactivePowerVar, 0.0)
        assertEquals(result.apparentPowerVa, result.activePowerWatts, 1e-9)
        assertEquals(0.0, result.phaseAngleDegrees, 1e-9)
        assertEquals(0.0, result.tangentPhi, 0.0)
    }

    @Test
    fun `a purely reactive load has no active power`() {
        val result = calculate(input(powerFactor = 0.0))

        assertEquals(0.0, result.activePowerWatts, 1e-9)
        assertEquals(result.apparentPowerVa, result.reactivePowerVar, 1e-9)
        assertEquals(90.0, result.phaseAngleDegrees, 1e-9)
        // Reported as zero rather than infinity, which would render as "∞".
        assertEquals(0.0, result.tangentPhi, 0.0)
    }

    // -- Lagging versus leading -------------------------------------------------------

    @Test
    fun `leading power factor reverses the sign of reactive power`() {
        val lagging = calculate(input(type = PowerFactorType.LAGGING))
        val leading = calculate(input(type = PowerFactorType.LEADING))

        assertTrue(lagging.reactivePowerVar > 0.0)
        assertTrue(leading.reactivePowerVar < 0.0)
        assertEquals(-lagging.reactivePowerVar, leading.reactivePowerVar, 1e-9)
    }

    @Test
    fun `the direction does not change active or apparent power`() {
        val lagging = calculate(input(type = PowerFactorType.LAGGING))
        val leading = calculate(input(type = PowerFactorType.LEADING))

        assertEquals(lagging.activePowerWatts, leading.activePowerWatts, 1e-9)
        assertEquals(lagging.apparentPowerVa, leading.apparentPowerVa, 1e-9)
    }

    @Test
    fun `the phase angle is signed with the direction`() {
        val leading = calculate(input(type = PowerFactorType.LEADING))

        assertTrue(leading.phaseAngleDegrees < 0.0)
        assertEquals(-31.7883, leading.phaseAngleDegrees, 1e-4)
    }

    @Test
    fun `tan phi follows the sign of reactive power`() {
        val leading = calculate(input(type = PowerFactorType.LEADING))

        assertTrue(leading.tangentPhi < 0.0)
        assertEquals(leading.reactivePowerVar / leading.activePowerWatts, leading.tangentPhi, 1e-9)
    }

    // -- DC -----------------------------------------------------------------------------

    @Test
    fun `dc has only active power`() {
        val result = calculate(input(voltage = 220.0, current = 5.0, system = SupplySystem.DC))

        assertEquals(1_100.0, result.activePowerWatts, 1e-9)
        assertEquals(1_100.0, result.apparentPowerVa, 1e-9)
        assertEquals(0.0, result.reactivePowerVar, 0.0)
        assertEquals(0.0, result.phaseAngleDegrees, 1e-9)
    }

    @Test
    fun `a power factor entered on dc is ignored`() {
        val withPf = calculate(input(system = SupplySystem.DC, powerFactor = 0.5))
        val withoutPf = calculate(input(system = SupplySystem.DC, powerFactor = 1.0))

        assertEquals(withoutPf.activePowerWatts, withPf.activePowerWatts, 1e-12)
        assertEquals(0.0, withPf.reactivePowerVar, 0.0)
    }

    // -- Scaling ---------------------------------------------------------------------------

    @Test
    fun `power scales linearly with voltage and with current`() {
        val base = calculate(input(voltage = 400.0, current = 100.0)).apparentPowerVa

        assertEquals(2.0 * base, calculate(input(voltage = 800.0, current = 100.0)).apparentPowerVa, 1e-6)
        assertEquals(2.0 * base, calculate(input(voltage = 400.0, current = 200.0)).apparentPowerVa, 1e-6)
    }

    @Test
    fun `tan phi matches the well known tariff thresholds`() {
        // A tan φ of 0.20 corresponds to cos φ ≈ 0.98, the threshold many
        // tariffs use before charging for reactive energy.
        val result = calculate(input(powerFactor = 0.98))

        assertEquals(0.2030, result.tangentPhi, 1e-4)
    }
}
