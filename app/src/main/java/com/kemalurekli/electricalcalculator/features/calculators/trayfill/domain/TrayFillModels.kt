package com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry

/**
 * How the cables are to lie in the tray.
 *
 * This is the choice that decides which calculation is even relevant, which is
 * why it is not a display option:
 *
 * - [SINGLE_LAYER] is governed by **width**. Power cables are normally laid in
 *   one layer so each can shed heat, and the question is whether they fit
 *   across the tray. An area percentage says nothing useful here — a tray can
 *   be 15 % "full" by area and still have no room left across its width.
 * - [MULTI_LAYER] is governed by **area**. Control and instrument cables are
 *   routinely stacked, and then the tray's cross-section is what fills up.
 */
enum class TrayArrangement {
    /** One layer across the tray's width. */
    SINGLE_LAYER,

    /** Cables stacked to a permitted fraction of the tray's cross-section. */
    MULTI_LAYER,
}

/**
 * A validated set of cable tray inputs.
 *
 * @param trayWidthMm the tray's *usable* internal width.
 * @param trayDepthMm the usable loading depth. Only used by
 *   [TrayArrangement.MULTI_LAYER]; a single layer is bounded by width alone.
 * @param cables what the tray is to carry.
 * @param arrangement which of the two questions is being asked.
 * @param clearSpacingMm gap left between adjacent cables in a single layer.
 *   Zero means touching. Spacing is not decoration: IEC 60364-5-52 gives a
 *   noticeably better grouping factor for cables separated by one diameter than
 *   for cables in contact, so many designs pay for the width to get the
 *   ampacity back.
 * @param permittedFillFraction the area limit for [TrayArrangement.MULTI_LAYER].
 */
data class TrayFillInput(
    val trayWidthMm: Double,
    val trayDepthMm: Double = 0.0,
    val cables: List<CableBundleEntry>,
    val arrangement: TrayArrangement = TrayArrangement.SINGLE_LAYER,
    val clearSpacingMm: Double = 0.0,
    val permittedFillFraction: Double = DEFAULT_FILL_FRACTION,
) {
    companion object {
        /**
         * A widely specified design convention for tray loading, and nothing
         * more than that — see [TrayFillResult] for why no standard is cited.
         */
        const val DEFAULT_FILL_FRACTION = 0.40
    }
}

/**
 * The outcome of a cable tray calculation.
 *
 * Modelled as two cases rather than one type with half its fields null: a
 * single-layer tray has no meaningful "fill percentage" and a stacked tray has
 * no meaningful "spare width", and a screen that had to guess which fields
 * applied would eventually show the wrong one.
 *
 * ### On standards
 *
 * **IEC 61537** is the product standard for cable tray systems. Like IEC 61386
 * for conduit it classifies mechanical performance — safe working load, span,
 * corrosion class — and sets **no fill percentage**, so none is attributed to
 * it. **NEC 392.22** does regulate fill, but by absolute allowances in mm² that
 * depend on tray width and cable size rather than by a percentage.
 *
 * The percentage offered here is therefore presented as what it is: a design
 * convention, adjustable, and not a code citation.
 */
sealed interface TrayFillResult {

    /** Total number of cables across every entry. */
    val cableCount: Int

    /** Summed cross-sectional area of the cables. */
    val cableAreaMm2: Double

    /** Whether the tray accepts this bundle in this arrangement. */
    val isWithinLimit: Boolean

    /**
     * One layer, judged on width.
     *
     * @param requiredWidthMm the width the bundle occupies, including the gaps
     *   between cables. `n` cables leave `n − 1` gaps, not `n`.
     * @param spareWidthMm width left over. Negative when the bundle does not
     *   fit, which says how much wider the tray has to be.
     * @param widthUsedFraction how much of the tray's width is taken.
     * @param largestAdditionalCableMm the biggest cable that could still be laid
     *   beside the others. Accounts for the extra gap that cable brings with it.
     * @param occupiedDepthMm the depth the same cables would need if they were
     *   instead stacked, for comparison against a tray's usable depth.
     */
    data class SingleLayer(
        override val cableCount: Int,
        override val cableAreaMm2: Double,
        val trayWidthMm: Double,
        val requiredWidthMm: Double,
        val spareWidthMm: Double,
        val widthUsedFraction: Double,
        val largestAdditionalCableMm: Double,
        val occupiedDepthMm: Double,
    ) : TrayFillResult {
        override val isWithinLimit: Boolean get() = requiredWidthMm <= trayWidthMm
    }

    /**
     * Stacked cables, judged on area.
     *
     * @param trayAreaMm2 usable cross-section, width × depth.
     * @param fillFraction how full the tray's cross-section is.
     * @param permittedFraction the design limit being applied.
     * @param spareAreaMm2 area left within that limit; negative when over.
     * @param occupiedDepthMm the depth the cables occupy assuming ideal packing.
     *   Real bundles pack loosely, so treat it as a lower bound.
     * @param estimatedLayers how many cables deep the stack sits, using the
     *   largest cable in the bundle as the layer height.
     */
    data class MultiLayer(
        override val cableCount: Int,
        override val cableAreaMm2: Double,
        val trayAreaMm2: Double,
        val fillFraction: Double,
        val permittedFraction: Double,
        val spareAreaMm2: Double,
        val occupiedDepthMm: Double,
        val estimatedLayers: Int,
    ) : TrayFillResult {
        override val isWithinLimit: Boolean get() = fillFraction <= permittedFraction

        /** Compliant, but with under a tenth of the allowance left. */
        val isNearLimit: Boolean
            get() = isWithinLimit && fillFraction >= permittedFraction * NEAR_LIMIT_MARGIN

        private companion object {
            const val NEAR_LIMIT_MARGIN = 0.90
        }
    }
}
