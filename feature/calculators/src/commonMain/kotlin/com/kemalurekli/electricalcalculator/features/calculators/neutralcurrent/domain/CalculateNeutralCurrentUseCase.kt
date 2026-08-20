package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain

import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Computes the current in the neutral of a three-phase circuit.
 *
 * ### The unbalance term
 *
 * For three line currents 120° apart, the vector sum returning through the
 * neutral has magnitude:
 *
 * ```
 * I_N = √(I₁² + I₂² + I₃² − I₁I₂ − I₂I₃ − I₃I₁)
 * ```
 *
 * Equal currents give zero, which is why the neutral is traditionally the
 * smallest conductor in the cable.
 *
 * ### The triplen term, which does not cancel
 *
 * The third harmonic and its multiples arrive in the neutral **in phase with
 * each other** rather than 120° apart, so they add arithmetically:
 *
 * ```
 * I_N3 = 3 · I₃ᵣᵈ(per line)
 * ```
 *
 * A perfectly balanced circuit — zero unbalance — can therefore still put three
 * times one line's third-harmonic current into the neutral. That is the case
 * that surprises people, and it is why the two contributions are reported
 * separately rather than as one number.
 *
 * The two are at different frequencies, so they combine in quadrature rather
 * than by addition.
 *
 * ### Assumptions
 *
 * The harmonic percentage is taken as the same on all three lines, which is the
 * usual case for a board full of similar loads and the conservative one when it
 * is not. Only the third harmonic is modelled; the ninth and fifteenth behave
 * the same way and are smaller.
 */
class CalculateNeutralCurrentUseCase() {

    operator fun invoke(input: NeutralCurrentInput): NeutralCurrentResult {
        val (i1, i2, i3) = input.lineCurrents

        val fundamental = sqrt(
            (i1 * i1 + i2 * i2 + i3 * i3 - i1 * i2 - i2 * i3 - i3 * i1)
                // Rounding can push a perfectly balanced set a hair below zero.
                .coerceAtLeast(0.0),
        )

        val averageLine = (i1 + i2 + i3) / PHASES
        val triplen = PHASES * averageLine * input.thirdHarmonicPercent / PERCENT

        val neutral = hypot(fundamental, triplen)
        val highest = input.highestLineCurrent

        return NeutralCurrentResult(
            fundamentalNeutralAmps = fundamental,
            triplenNeutralAmps = triplen,
            neutralCurrentAmps = neutral,
            neutralToHighestLineRatio = if (highest > 0.0) neutral / highest else 0.0,
        )
    }

    private companion object {
        const val PHASES = 3.0
        const val PERCENT = 100.0
    }
}
