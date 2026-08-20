package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry

/**
 * Which permitted fill percentage the calculation is measured against.
 *
 * There is no single international figure, and pretending otherwise would be
 * the wrong kind of convenience:
 *
 * - **NEC Chapter 9, Table 1** is the most widely codified rule and the only one
 *   modelled here as a table: 53 % for a single cable, 31 % for two, 40 % for
 *   three or more. The step at two cables is not a typo — two circles inside a
 *   third pack badly, and the lower figure is what keeps a pull feasible.
 * - **IEC 61386** is a product standard for conduit systems. It specifies
 *   mechanical and electrical performance, *not* a fill percentage, so nothing
 *   here is attributed to it.
 * - **BS 7671 / IET On-Site Guide** does not use a percentage at all: it
 *   compares a summed cable factor against a conduit factor that already
 *   accounts for the run's length and bends.
 *
 * [CUSTOM] exists for the third case and for local rules — a designer working
 * to a 45 % trunking space factor, or to a house standard, enters it directly
 * rather than being told the wrong number confidently.
 */
enum class FillRule {
    /** NEC Chapter 9, Table 1 — the permitted percentage depends on cable count. */
    NEC_TABLE_1,

    /** A percentage the user supplies. */
    CUSTOM,
    ;

    companion object {
        /**
         * The permitted fill fraction for [cableCount] cables under
         * NEC Chapter 9, Table 1.
         */
        fun necTable1(cableCount: Int): Double = when {
            cableCount <= 0 -> 0.0
            cableCount == 1 -> SINGLE_CABLE
            cableCount == 2 -> TWO_CABLES
            else -> THREE_OR_MORE
        }

        const val SINGLE_CABLE = 0.53
        const val TWO_CABLES = 0.31
        const val THREE_OR_MORE = 0.40
    }
}

/**
 * A validated set of conduit fill inputs.
 *
 * @param conduitInnerDiameterMm the conduit's *internal* diameter. Nominal
 *   metric sizes name the outside diameter, and the bore depends on wall
 *   thickness, so this comes from the conduit's datasheet.
 * @param cables what the conduit is to carry.
 * @param rule which permitted percentage applies.
 * @param customLimitFraction the permitted fraction when [rule] is
 *   [FillRule.CUSTOM]. Ignored otherwise.
 */
data class ConduitFillInput(
    val conduitInnerDiameterMm: Double,
    val cables: List<CableBundleEntry>,
    val rule: FillRule = FillRule.NEC_TABLE_1,
    val customLimitFraction: Double = FillRule.THREE_OR_MORE,
)

/**
 * The outcome of a conduit fill calculation.
 *
 * @param conduitAreaMm2 internal cross-sectional area of the conduit.
 * @param cableAreaMm2 summed cross-sectional area of every cable.
 * @param fillFraction how full the conduit actually is.
 * @param permittedFraction the limit the fill is judged against.
 * @param cableCount total number of cables, which is what selects the limit
 *   under [FillRule.NEC_TABLE_1].
 * @param spareAreaMm2 area still available within the limit. Negative when the
 *   conduit is over-filled, which is more useful than clamping at zero: it says
 *   how much has to come out.
 * @param largestAdditionalCableMm diameter of the biggest single cable that
 *   could still be added. Evaluated against the limit that would apply *after*
 *   it is added — pulling a third cable into a two-cable conduit raises the
 *   permitted fill from 31 % to 40 %, so ignoring that would understate what
 *   fits. Zero when nothing more will go in.
 */
data class ConduitFillResult(
    val conduitAreaMm2: Double,
    val cableAreaMm2: Double,
    val fillFraction: Double,
    val permittedFraction: Double,
    val cableCount: Int,
    val spareAreaMm2: Double,
    val largestAdditionalCableMm: Double,
) {
    /** True when the fill is at or below the permitted percentage. */
    val isWithinLimit: Boolean get() = fillFraction <= permittedFraction

    /**
     * True when the fill has passed 90 % of what is permitted. Not a failure,
     * but the point at which adding one more circuit later stops being free.
     */
    val isNearLimit: Boolean
        get() = isWithinLimit && fillFraction >= permittedFraction * NEAR_LIMIT_MARGIN

    private companion object {
        const val NEAR_LIMIT_MARGIN = 0.90
    }
}
