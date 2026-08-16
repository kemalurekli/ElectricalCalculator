package com.kemalurekli.electricalcalculator.features.calculators.harmonics

import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.CalculateHarmonicsUseCase
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicComponent
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * A spectrum, and the four things it does to the installation.
 *
 * The anchors are chosen so the arithmetic can be stated rather than looked up:
 * a single harmonic at a round percentage, a clean sine, and a spectrum whose
 * triplen content is large enough that the neutral overtakes the lines.
 */
class CalculateHarmonicsUseCaseTest {

    private val calculate = CalculateHarmonicsUseCase()

    private fun input(
        fundamentalAmps: Double = 100.0,
        components: List<HarmonicComponent> = emptyList(),
        balanced: Boolean = true,
    ) = HarmonicsInput(fundamentalAmps, components, balanced)

    @Test
    fun `a clean sine has no distortion and a K factor of one`() {
        val result = calculate(input())

        assertEquals(0.0, result.thdPercent, 1e-9)
        assertEquals(100.0, result.rmsAmps, 1e-9)
        assertEquals(0.0, result.neutralAmps, 1e-9)
        assertEquals(1.0, result.kFactor, 1e-9)
        assertNull(result.dominantOrder)
    }

    @Test
    fun `THD is the harmonic content against the fundamental`() {
        // A single 30 % third harmonic: THD is 30 % by definition, and the RMS
        // is √(1 + 0.3²) = 1.0440 times the fundamental.
        val result = calculate(input(components = listOf(HarmonicComponent(3, 30.0))))

        assertEquals(30.0, result.thdPercent, 1e-9)
        assertEquals(100.0 * sqrt(1.09), result.rmsAmps, 1e-9)
    }

    @Test
    fun `triplen harmonics add three times over in the neutral`() {
        // 30 A of third harmonic in each line arrives at the star point in
        // phase, so the neutral carries 90 A — not zero, and not √3 × 30.
        val result = calculate(input(components = listOf(HarmonicComponent(3, 30.0))))

        assertEquals(90.0, result.neutralAmps, 1e-9)
    }

    @Test
    fun `everything that is not a triplen cancels at the star point`() {
        val result = calculate(
            input(components = listOf(HarmonicComponent(5, 40.0), HarmonicComponent(7, 25.0))),
        )

        assertEquals(0.0, result.neutralAmps, 1e-9)
        // But they still heat the conductor and the transformer.
        assertTrue(result.thdPercent > 0.0)
        assertTrue(result.kFactor > 1.0)
    }

    @Test
    fun `a neutral can carry more than the lines it serves`() {
        // The finding people refuse to believe. 70 % third harmonic — an
        // ordinary figure for a floor of switch-mode supplies — puts 210 A in a
        // neutral whose lines carry 122 A.
        val result = calculate(input(components = listOf(HarmonicComponent(3, 70.0))))

        assertTrue(result.neutralExceedsLines)
        assertEquals(210.0, result.neutralAmps, 1e-9)
        assertTrue("the lines carry far less", result.rmsAmps < result.neutralAmps)
    }

    @Test
    fun `an unbalanced spectrum reports no neutral rather than a wrong one`() {
        // Cancellation depends on phase angles the input does not carry.
        // Guessing would be worse than declining.
        val result = calculate(
            input(components = listOf(HarmonicComponent(3, 30.0)), balanced = false),
        )

        assertEquals(0.0, result.neutralAmps, 1e-9)
        assertFalse(result.neutralExceedsLines)
    }

    @Test
    fun `the K factor weights each order by the square of its number`() {
        // One 100 % fifth harmonic: the RMS is √2 of the fundamental, so both
        // components are 1/√2 per unit, and K = 0.5 + 0.5 × 25 = 13.
        val result = calculate(input(components = listOf(HarmonicComponent(5, 100.0))))

        assertEquals(13.0, result.kFactor, 1e-9)
    }

    @Test
    fun `the dominant order is the largest one, not the lowest`() {
        val result = calculate(
            input(
                components = listOf(
                    HarmonicComponent(3, 12.0),
                    HarmonicComponent(5, 41.0),
                    HarmonicComponent(7, 9.0),
                ),
            ),
        )

        assertEquals(5, result.dominantOrder)
    }

    @Test
    fun `a zero fundamental does not produce a NaN`() {
        // What an empty form sends. A result of NaN renders as a blank line and
        // reads as a broken screen.
        val result = calculate(input(fundamentalAmps = 0.0, components = listOf(HarmonicComponent(3, 30.0))))

        assertEquals(0.0, result.thdPercent, 1e-9)
        assertEquals(0.0, result.rmsAmps, 1e-9)
        assertEquals(0.0, result.kFactor, 1e-9)
    }
}
