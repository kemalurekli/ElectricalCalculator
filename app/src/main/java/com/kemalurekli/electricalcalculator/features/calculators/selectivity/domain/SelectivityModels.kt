package com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain

import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType

/**
 * Two devices in series, and whether the lower one clears a fault on its own.
 *
 * @param upstreamType the device nearer the supply — the one that must *not*
 *   operate for a fault below it.
 * @param prospectiveFaultAmps the fault current available at the downstream
 *   device's position. This is what decides the answer: selectivity is not a
 *   property of a pair of devices, it is a property of a pair of devices *at a
 *   fault level*.
 */
data class SelectivityInput(
    val upstreamType: ProtectiveDeviceType,
    val upstreamRatingAmps: Double,
    val downstreamType: ProtectiveDeviceType,
    val downstreamRatingAmps: Double,
    val prospectiveFaultAmps: Double,
)

/** How far the pair discriminates. */
enum class SelectivityGrade {
    /**
     * The lower device clears every fault the app can reason about, up to the
     * prospective current given.
     */
    SELECTIVE,

    /**
     * Selective up to a current, and not above it. The commonest real answer,
     * and the one worth knowing the number for.
     */
    PARTIAL,

    /**
     * No discrimination worth the name — the devices are too close in rating,
     * or the upstream one is not the larger.
     */
    NONE,
}

/**
 * What the curves allow, which is an upper bound and not a guarantee.
 *
 * @param limitAmps the current above which both devices see enough to operate
 *   instantaneously, so the upstream one goes too. Null when the upstream
 *   device has no instantaneous threshold this app can read.
 * @param ratio upstream rating over downstream rating, which is what governs
 *   the overload end of the range.
 * @param overloadSelective whether that ratio clears the conventional margin.
 */
data class SelectivityResult(
    val grade: SelectivityGrade,
    val limitAmps: Double?,
    val upstreamInstantaneousAmps: Double?,
    val downstreamInstantaneousAmps: Double?,
    val ratio: Double,
    val overloadSelective: Boolean,
) {
    /** True when the fault the user entered is below the point selectivity is lost. */
    val holdsAtGivenFault: Boolean get() = grade == SelectivityGrade.SELECTIVE

    companion object {
        /**
         * The rating ratio conventionally taken as enough in the overload region.
         *
         * A convention, not a requirement. Breakers of the same family whose
         * ratings differ by less than about 1.6 have thermal curves close
         * enough to overlap, and manufacturers publish 1.6 as the point below
         * which they will not claim discrimination. Above it the overload end
         * behaves; the magnetic end is a separate question and is what
         * [limitAmps] answers.
         */
        const val OVERLOAD_RATIO = 1.6
    }
}
