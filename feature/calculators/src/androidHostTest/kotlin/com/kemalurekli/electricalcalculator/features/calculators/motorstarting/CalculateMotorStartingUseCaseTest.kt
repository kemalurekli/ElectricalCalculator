package com.kemalurekli.electricalcalculator.features.calculators.motorstarting

import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.CalculateMotorStartingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingInput
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * The dip a motor puts on the board while it runs up.
 *
 * Most of this checks relationships rather than transcribed figures, because
 * the arithmetic is short enough to state exactly: a 4 % transformer is twice
 * as stiff as an 8 % one, torque follows the square of voltage, and a gentler
 * start buys a smaller dip at a price.
 */
class CalculateMotorStartingUseCaseTest {

    private val calculate = CalculateMotorStartingUseCase()

    private fun input(
        fullLoadCurrentAmps: Double = 55.0,
        lockedRotorMultiple: Double = 6.0,
        method: StartingMethod = StartingMethod.DIRECT_ON_LINE,
        supplyVoltage: Double = 400.0,
        transformerKva: Double = 400.0,
        transformerImpedancePercent: Double = 4.0,
    ) = MotorStartingInput(
        fullLoadCurrentAmps = fullLoadCurrentAmps,
        lockedRotorMultiple = lockedRotorMultiple,
        method = method,
        supplyVoltage = supplyVoltage,
        transformerKva = transformerKva,
        transformerImpedancePercent = transformerImpedancePercent,
    )

    @Test
    fun `the short circuit power is the transformer rating over its impedance`() {
        // 400 kVA at 4 % is a 10 MVA source, which is the whole of what
        // "stiff supply" means in one number.
        assertEquals(10_000.0, calculate(input()).shortCircuitKva, 1e-9)
    }

    @Test
    fun `the starting kVA is the inrush at the supply voltage`() {
        val result = calculate(input())

        assertEquals(330.0, result.startingCurrentAmps, 1e-9)
        assertEquals(sqrt(3.0) * 400.0 * 330.0 / 1000.0, result.startingKva, 1e-9)
    }

    @Test
    fun `the dip keeps the starting load in the denominator`() {
        // S_start / (S_sc + S_start), not S_start / S_sc. The two agree at
        // small dips and diverge exactly where a motor start lands, so the
        // difference is checked rather than assumed away.
        val result = calculate(input())
        val naive = result.startingKva / result.shortCircuitKva * 100.0

        assertTrue("the honest figure must be the smaller", result.dipPercent < naive)
        assertEquals(
            result.startingKva / (result.shortCircuitKva + result.startingKva) * 100.0,
            result.dipPercent,
            1e-9,
        )
    }

    @Test
    fun `a stiffer transformer dips less, in proportion to its impedance`() {
        val four = calculate(input(transformerImpedancePercent = 4.0))
        val eight = calculate(input(transformerImpedancePercent = 8.0))

        assertTrue(four.dipPercent < eight.dipPercent)
        // Halving uk doubles the short-circuit power exactly.
        assertEquals(2.0, eight.shortCircuitKva.let { four.shortCircuitKva / it }, 1e-9)
    }

    @Test
    fun `star delta buys a third of the current and a third of the torque`() {
        val direct = calculate(input())
        val star = calculate(input(method = StartingMethod.STAR_DELTA))

        assertEquals(direct.startingCurrentAmps / 3.0, star.startingCurrentAmps, 1e-9)
        assertTrue(star.dipPercent < direct.dipPercent)
        // The price. A method that thirds the current also thirds the pull.
        assertTrue(star.startingTorquePercent < direct.startingTorquePercent)
    }

    @Test
    fun `torque falls with the square of what is left at the terminals`() {
        // The reason a soft start that looks gentle stalls a loaded conveyor:
        // halving the voltage quarters the torque before the dip is counted.
        val soft = calculate(input(method = StartingMethod.SOFT_STARTER_50))
        val residualRatio = soft.residualVoltage / 400.0

        assertEquals(0.25 * residualRatio * residualRatio * 100.0, soft.startingTorquePercent, 1e-9)
    }

    @Test
    fun `a large motor on a small transformer trips the warnings`() {
        // 55 A full load — a 30 kW machine — on a 50 kVA supply at 6 %. A real
        // pairing on a small site, and one that takes the board down rather
        // than dimming it.
        //
        // 100 kVA, reached for first, gives 12 %: unpleasant, and not enough to
        // drop a contactor. The threshold is further away than it feels.
        val result = calculate(input(transformerKva = 50.0, transformerImpedancePercent = 6.0))

        assertTrue(result.dipPercent > MotorStartingResult.CONTACTOR_DROPOUT_PERCENT)
        assertTrue(result.risksContactorDropout)
        assertTrue(result.visibleFlicker)
    }

    @Test
    fun `a motor that barely registers is reported as such`() {
        val result = calculate(input(fullLoadCurrentAmps = 4.0, transformerKva = 1000.0))

        assertTrue(result.dipPercent < MotorStartingResult.FLICKER_PERCENT)
        assertFalse(result.visibleFlicker)
        assertFalse(result.risksContactorDropout)
    }

    @Test
    fun `an infinite bus does not dip at all`() {
        // Zero impedance is not a real transformer, but it is what a user types
        // when the field is empty, and dividing by it must not produce a NaN
        // that renders as a blank result.
        val result = calculate(input(transformerImpedancePercent = 0.0))

        assertEquals(0.0, result.dipPercent, 1e-9)
        assertEquals(400.0, result.residualVoltage, 1e-9)
        assertEquals(100.0, result.startingTorquePercent, 1e-9)
    }
}
