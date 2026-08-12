package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent

import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.CalculateNeutralCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateNeutralCurrentUseCaseTest {

    private val calculate = CalculateNeutralCurrentUseCase()

    private fun input(
        i1: Double = 100.0,
        i2: Double = 100.0,
        i3: Double = 100.0,
        thirdHarmonic: Double = 0.0,
    ) = NeutralCurrentInput(Triple(i1, i2, i3), thirdHarmonic)

    // -- The unbalance term ------------------------------------------------------

    @Test
    fun `a balanced linear circuit puts nothing in the neutral`() {
        // The reason the neutral is traditionally the smallest conductor.
        val result = calculate(input())

        assertEquals(0.0, result.fundamentalNeutralAmps, 1e-9)
        assertEquals(0.0, result.neutralCurrentAmps, 1e-9)
    }

    @Test
    fun `one phase loaded alone returns its whole current`() {
        val result = calculate(input(i1 = 100.0, i2 = 0.0, i3 = 0.0))

        assertEquals(100.0, result.fundamentalNeutralAmps, 1e-9)
    }

    @Test
    fun `two equal phases return one phase worth`() {
        // 120° apart, so their vector sum has the same magnitude as either.
        val result = calculate(input(i1 = 100.0, i2 = 100.0, i3 = 0.0))

        assertEquals(100.0, result.fundamentalNeutralAmps, 1e-9)
    }

    @Test
    fun `worked example - a moderately unbalanced board`() {
        // √(100² + 80² + 60² − 8000 − 4800 − 6000) = √(20000 − 18800) = √1200
        val result = calculate(input(i1 = 100.0, i2 = 80.0, i3 = 60.0))

        assertEquals(34.641, result.fundamentalNeutralAmps, 1e-3)
    }

    @Test
    fun `the unbalance term never goes negative on rounding`() {
        // The expression under the root is zero for a balanced set, and floating
        // point can push it a hair below.
        val result = calculate(input(i1 = 33.333333, i2 = 33.333333, i3 = 33.333333))

        assertTrue(result.fundamentalNeutralAmps >= 0.0)
    }

    // -- The triplen term, which does not cancel ------------------------------------

    @Test
    fun `a balanced circuit with third harmonic still loads the neutral`() {
        // The case that surprises people: zero unbalance, and the neutral is
        // carrying three times one line's third-harmonic current.
        val result = calculate(input(thirdHarmonic = 30.0))

        assertEquals(0.0, result.fundamentalNeutralAmps, 1e-9)
        assertEquals(90.0, result.triplenNeutralAmps, 1e-9)
        assertEquals(90.0, result.neutralCurrentAmps, 1e-9)
    }

    @Test
    fun `heavy third harmonic makes the neutral the busiest conductor`() {
        // Past about 33 % the neutral exceeds every line, which is the condition
        // IEC 60364-5-52 derates the cable for.
        val result = calculate(input(thirdHarmonic = 40.0))

        assertTrue(result.neutralExceedsLines)
        assertEquals(1.2, result.neutralToHighestLineRatio, 1e-9)
    }

    @Test
    fun `a purely linear balanced circuit does not exceed its lines`() {
        assertFalse(calculate(input()).neutralExceedsLines)
    }

    @Test
    fun `the two contributions combine in quadrature, not by addition`() {
        // They are at different frequencies; adding them arithmetically would
        // overstate the neutral by a wide margin.
        val result = calculate(input(i1 = 100.0, i2 = 80.0, i3 = 60.0, thirdHarmonic = 30.0))

        val fundamental = result.fundamentalNeutralAmps
        val triplen = result.triplenNeutralAmps
        val quadrature = kotlin.math.hypot(fundamental, triplen)

        assertEquals(quadrature, result.neutralCurrentAmps, 1e-9)
        assertTrue(result.neutralCurrentAmps < fundamental + triplen)
    }

    // -- Behaviour --------------------------------------------------------------------

    @Test
    fun `scaling every line scales the neutral with it`() {
        val small = calculate(input(i1 = 50.0, i2 = 40.0, i3 = 30.0)).neutralCurrentAmps
        val large = calculate(input(i1 = 100.0, i2 = 80.0, i3 = 60.0)).neutralCurrentAmps

        assertEquals(2.0, large / small, 1e-9)
    }

    @Test
    fun `an unloaded circuit reports a ratio of zero rather than dividing by it`() {
        val result = calculate(input(i1 = 0.0, i2 = 0.0, i3 = 0.0))

        assertEquals(0.0, result.neutralToHighestLineRatio, 1e-9)
        assertFalse(result.neutralExceedsLines)
    }
}
