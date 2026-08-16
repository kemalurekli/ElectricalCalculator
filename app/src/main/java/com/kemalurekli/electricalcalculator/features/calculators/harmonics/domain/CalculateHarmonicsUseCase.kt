package com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain

import kotlin.math.sqrt
import javax.inject.Inject

/**
 * A measured spectrum, turned into the four numbers a design needs.
 *
 * ### Why this exists when a neutral current calculator already does
 *
 * That one takes the third harmonic and nothing else, which is the right
 * simplification for a lighting circuit and the wrong one for a floor of
 * switch-mode supplies: the fifth and seventh are what heat the transformer,
 * and the ninth is what the neutral carries alongside the third. Dozens of
 * assumption notes across this app say "harmonics are not modelled". This is
 * where they are.
 *
 * ### The four figures
 *
 * ```
 * THD  = √(Σ Ih²) / I1
 * Irms = I1 · √(1 + THD²)
 * IN   = 3 · √(Σ I_triplen²)          balanced systems only
 * K    = Σ (Ih(pu)² · h²)             with Ih(pu) normalised to Irms
 * ```
 *
 * The neutral term is the one worth reading twice. Triplen harmonics arrive at
 * the star point in phase, so they add rather than cancel — three times, not
 * √3 times — which is how a neutral ends up carrying more than any line
 * without a hint of imbalance.
 *
 * ### What is deliberately not here
 *
 * Cable derating. IEC 60364-5-52 Annex E gives reduction factors banded on
 * third-harmonic content, and those bands are transcribed figures no test in
 * this app can prove. Everything above is derived and provable, and mixing the
 * two in one result would put a number nobody has checked beside four that are
 * checked on every build.
 */
class CalculateHarmonicsUseCase @Inject constructor() {

    operator fun invoke(input: HarmonicsInput): HarmonicsResult {
        val fundamental = input.fundamentalAmps

        // Magnitudes in amperes, which is what everything below is summed in.
        val amps = input.components.associate { it.order to fundamental * it.percentOfFundamental / 100.0 }

        val harmonicRms = sqrt(amps.values.sumOf { it * it })
        val thdFraction = if (fundamental > 0.0) harmonicRms / fundamental else 0.0
        val rms = sqrt(fundamental * fundamental + harmonicRms * harmonicRms)

        val neutral = if (input.balanced) {
            val triplen = input.components
                .filter { it.isTriplen }
                .sumOf { component ->
                    val value = amps[component.order] ?: 0.0
                    value * value
                }
            3.0 * sqrt(triplen)
        } else {
            // Cancellation depends on phase angles this input does not carry.
            // Reporting zero is not a claim that the neutral is empty; the
            // screen says the figure is unavailable rather than showing it.
            0.0
        }

        // K-factor weights each order by the square of its number, because
        // eddy-current loss rises with the square of frequency. Normalised to
        // the total RMS so that K = 1 for a clean sine.
        val kFactor = if (rms > 0.0) {
            val fundamentalPu = fundamental / rms
            var sum = fundamentalPu * fundamentalPu
            amps.forEach { (order, value) ->
                val pu = value / rms
                sum += pu * pu * order * order
            }
            sum
        } else {
            0.0
        }

        return HarmonicsResult(
            thdPercent = thdFraction * 100.0,
            rmsAmps = rms,
            neutralAmps = neutral,
            kFactor = kFactor,
            dominantOrder = amps.maxByOrNull { it.value }?.takeIf { it.value > 0.0 }?.key,
        )
    }
}
