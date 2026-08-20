package com.kemalurekli.electricalcalculator.features.calculators.lighting.domain

import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Sizes a general lighting installation by the lumen method.
 *
 * ### Method
 *
 * ```
 * A = L · W
 * K = (L · W) / (Hm · (L + W))          room index
 * N = (E · A) / (Φ · UF · MF)           luminaires needed
 * E_achieved = (N_rounded · Φ · UF · MF) / A
 * ```
 *
 * ### What this is and is not
 *
 * The lumen method gives an *average* illuminance over the working plane. It
 * says nothing about uniformity, glare, or what happens at the edges of the
 * room — all of which a real lighting design has to answer, and none of which a
 * single average can. It is the right tool for "roughly how many fittings", and
 * the wrong tool for a compliance submission.
 *
 * The count is rounded up rather than to nearest. Rounding 4,2 down to 4 is a
 * design that misses its own target by 5 %, which is not a rounding decision the
 * calculator should make on the reader's behalf.
 */
class CalculateLightingUseCase() {

    operator fun invoke(input: LightingInput): LightingResult {
        val area = input.roomLengthMetres * input.roomWidthMetres

        // Mounting height is above the *working plane*; the caller is expected
        // to have subtracted the desk height already.
        val roomIndex = area /
            (input.mountingHeightMetres * (input.roomLengthMetres + input.roomWidthMetres))

        val exactCount = input.targetIlluminanceLux * area /
            (
                input.luminousFluxPerLuminaireLumens *
                    input.utilisationFactor *
                    input.maintenanceFactor
                )

        val count = ceil(exactCount).toInt().coerceAtLeast(1)

        val achieved = count * input.luminousFluxPerLuminaireLumens *
            input.utilisationFactor * input.maintenanceFactor / area

        return LightingResult(
            exactLuminaireCount = exactCount,
            luminaireCount = count,
            achievedIlluminanceLux = achieved,
            roomIndex = roomIndex,
            areaSquareMetres = area,
            luminairesPerRowSuggestion = gridFor(count, input),
        )
    }

    /**
     * A plausible rectangular layout for [count] fittings.
     *
     * The pair of factors closest to square, oriented so the longer run of
     * luminaires goes along the longer wall. A prime count has no useful grid —
     * 7 fittings are 1 × 7 — so nothing is suggested rather than something
     * misleading.
     */
    private fun gridFor(count: Int, input: LightingInput): Pair<Int, Int>? {
        if (count < 2) return null

        var best: Pair<Int, Int>? = null
        for (rows in 1..sqrt(count.toDouble()).toInt()) {
            if (count % rows == 0) best = rows to count / rows
        }

        val (small, large) = best ?: return null
        if (small == 1) return null

        return if (input.roomLengthMetres >= input.roomWidthMetres) {
            large to small
        } else {
            small to large
        }
    }
}
