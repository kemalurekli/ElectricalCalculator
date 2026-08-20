package com.kemalurekli.electricalcalculator.core.domain.table

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation

/**
 * Derating factors applied to the tabulated current-carrying capacity.
 *
 * The tables assume one circuit at 30 °C ambient. Real installations rarely
 * match that, and the corrections are not small — six circuits bunched together
 * lose 43 % of their rating. Ignoring them is the single most common way a
 * cable calculation ends up optimistic.
 *
 * After IEC 60364-5-52 Table B.52.14 (ambient) and Table B.52.17 (grouping).
 * As with the ampacity table, these are transcribed values that need
 * verification against the standard before release.
 *
 * ### Why it lives in `:core:domain` rather than with the cable calculator
 *
 * Two features read it: the cable calculator applies the factors, and the
 * reference section tabulates them for a reader who wants the numbers without
 * a calculation. Leaving it in the calculator made those two mutually
 * dependent — references reached into `calculators.cablesize.domain`, and the
 * calculators reached back into `references.domain` for topic titles — which is
 * a cycle no module boundary can express. It is table data with no feature of
 * its own, so it belongs a layer down.
 */
class CorrectionFactors {

    /**
     * Ambient temperature factor for air, interpolated between table points.
     *
     * Linear interpolation between the tabulated 5 K steps is the accepted
     * practice; the standard tabulates only the steps.
     */
    fun ambientFactor(ambientC: Double, insulation: CableInsulation): Double {
        val points = when (insulation) {
            CableInsulation.PVC -> PVC_AMBIENT
            CableInsulation.XLPE -> XLPE_AMBIENT
        }
        return interpolate(ambientC, points)
    }

    /**
     * Grouping factor for [circuits] bunched together.
     *
     * Values above the tabulated range clamp to the last entry rather than
     * extrapolating toward zero, which would produce a nonsensical size.
     */
    fun groupingFactor(circuits: Int): Double {
        if (circuits <= 1) return 1.0
        GROUPING.entries.forEach { (count, factor) ->
            if (circuits <= count) return factor
        }
        return GROUPING.values.last()
    }

    /** The highest ambient temperature the table covers for an insulation. */
    fun maxAmbientC(insulation: CableInsulation): Double =
        when (insulation) {
            CableInsulation.PVC -> PVC_AMBIENT
            CableInsulation.XLPE -> XLPE_AMBIENT
        }.keys.max()

    private fun interpolate(value: Double, points: Map<Double, Double>): Double {
        points[value]?.let { return it }

        val keys = points.keys.sorted()
        if (value <= keys.first()) return points.getValue(keys.first())
        if (value >= keys.last()) return points.getValue(keys.last())

        val upperIndex = keys.indexOfFirst { it > value }
        val lower = keys[upperIndex - 1]
        val upper = keys[upperIndex]
        val ratio = (value - lower) / (upper - lower)
        return points.getValue(lower) + ratio * (points.getValue(upper) - points.getValue(lower))
    }

    /**
     * The tabulated points themselves, so the reference screen can show the
     * table the calculator applies rather than a second copy of it.
     *
     * Public for that reason alone; nothing outside the reference library and
     * this class should be reading them.
     */
    companion object {

        /** IEC 60364-5-52 Table B.52.14, PVC 70 °C in air. */
        val PVC_AMBIENT = mapOf(
            10.0 to 1.22, 15.0 to 1.17, 20.0 to 1.12, 25.0 to 1.06, 30.0 to 1.00,
            35.0 to 0.94, 40.0 to 0.87, 45.0 to 0.79, 50.0 to 0.71, 55.0 to 0.61,
            60.0 to 0.50,
        )

        /** IEC 60364-5-52 Table B.52.14, XLPE/EPR 90 °C in air. */
        val XLPE_AMBIENT = mapOf(
            10.0 to 1.15, 15.0 to 1.12, 20.0 to 1.08, 25.0 to 1.04, 30.0 to 1.00,
            35.0 to 0.96, 40.0 to 0.91, 45.0 to 0.87, 50.0 to 0.82, 55.0 to 0.76,
            60.0 to 0.71, 65.0 to 0.65, 70.0 to 0.58, 75.0 to 0.50, 80.0 to 0.41,
        )

        /**
         * IEC 60364-5-52 Table B.52.17 arrangement 1 — cables bunched in air,
         * on a surface, embedded or enclosed.
         *
         * Ordered ascending so the first entry at or above the circuit count
         * wins; entries are the tabulated break points.
         */
        val GROUPING = linkedMapOf(
            1 to 1.00, 2 to 0.80, 3 to 0.70, 4 to 0.65, 5 to 0.60, 6 to 0.57,
            7 to 0.54, 8 to 0.52, 9 to 0.50, 12 to 0.45, 16 to 0.41, 20 to 0.38,
        )
    }
}
