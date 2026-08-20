package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain

import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Computes how full a conduit is and whether that is permitted.
 *
 * The arithmetic is only circle areas:
 *
 * ```
 * A_conduit = π · D² / 4
 * A_cables  = Σ n · π · d² / 4
 * fill      = A_cables / A_conduit
 * ```
 *
 * What makes it an engineering calculation rather than a division is the limit
 * it is compared against, and that limit is a code decision — see [FillRule].
 *
 * ### Why a percentage at all
 *
 * A conduit is never packed solid. The limit exists so the bundle can actually
 * be pulled: room for the cables to move past bends without the sheath being
 * stripped, and air around them so heat can escape. Exceeding it produces a run
 * that either cannot be pulled or derates the cables inside it.
 */
class CalculateConduitFillUseCase() {

    operator fun invoke(input: ConduitFillInput): ConduitFillResult {
        val conduitArea = circleArea(input.conduitInnerDiameterMm)

        val cableArea = input.cables.sumOf { entry ->
            circleArea(entry.diameterMm) * entry.quantity
        }
        val cableCount = input.cables.sumOf { it.quantity }

        val permitted = permittedFraction(input, cableCount)
        val permittedArea = conduitArea * permitted

        return ConduitFillResult(
            conduitAreaMm2 = conduitArea,
            cableAreaMm2 = cableArea,
            fillFraction = cableArea / conduitArea,
            permittedFraction = permitted,
            cableCount = cableCount,
            spareAreaMm2 = permittedArea - cableArea,
            largestAdditionalCableMm = largestAdditionalCable(input, conduitArea, cableArea, cableCount),
        )
    }

    private fun permittedFraction(input: ConduitFillInput, cableCount: Int): Double =
        when (input.rule) {
            FillRule.NEC_TABLE_1 -> FillRule.necTable1(cableCount)
            FillRule.CUSTOM -> input.customLimitFraction
        }

    /**
     * The largest cable that could still be pulled in.
     *
     * Judged against the limit that would apply *with* that cable present. Under
     * NEC Table 1 a two-cable conduit is held to 31 %, but the moment a third
     * cable joins it the limit becomes 40 % — so the spare room for that third
     * cable is larger than the current limit suggests.
     */
    private fun largestAdditionalCable(
        input: ConduitFillInput,
        conduitArea: Double,
        cableArea: Double,
        cableCount: Int,
    ): Double {
        val permittedAfter = permittedFraction(input, cableCount + 1)
        val spare = conduitArea * permittedAfter - cableArea
        return if (spare <= 0.0) 0.0 else sqrt(4.0 * spare / PI)
    }

    private fun circleArea(diameter: Double): Double = PI * diameter * diameter / 4.0
}
