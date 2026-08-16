package com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain

import javax.inject.Inject

/**
 * Whether a fault below one device leaves the device above it closed.
 *
 * ### What this can and cannot tell you
 *
 * It reads the two published characteristics — the rating and the
 * instantaneous multiplier — and says where they stop being separable. That is
 * an **upper bound on selectivity, not a guarantee of it**. Real discrimination
 * between two breakers at high fault currents depends on let-through energy and
 * on how the two arcs interact, which is why manufacturers publish selectivity
 * tables for specific pairs and why those tables sometimes claim *more* than
 * the curves do. Where a manufacturer's table exists it wins; this is the
 * answer for the case where one does not, and the screen says so.
 *
 * ### The two ends of the range
 *
 * An overload is slow, and is settled by the thermal elements: the upstream
 * device has to be far enough above the downstream one that their curves do not
 * touch. The convention is a rating ratio of about 1.6 — see
 * [SelectivityResult.OVERLOAD_RATIO].
 *
 * A short circuit is fast, and is settled by the magnetic elements. Each device
 * trips instantaneously above its own multiple of rating, so once the fault
 * exceeds the *upstream* device's threshold both see enough to open and the
 * board goes dark. That threshold is the number worth carrying away, and it is
 * usually far lower than people expect: a 100 A Type B upstream lets go at
 * 500 A, which a fault close to the board will exceed comfortably.
 */
class CheckSelectivityUseCase @Inject constructor() {

    operator fun invoke(input: SelectivityInput): SelectivityResult {
        val ratio = if (input.downstreamRatingAmps > 0.0) {
            input.upstreamRatingAmps / input.downstreamRatingAmps
        } else {
            0.0
        }

        val upstreamInstantaneous = input.upstreamType.instantaneousMultiplier
            ?.times(input.upstreamRatingAmps)
        val downstreamInstantaneous = input.downstreamType.instantaneousMultiplier
            ?.times(input.downstreamRatingAmps)

        val overloadSelective = ratio >= SelectivityResult.OVERLOAD_RATIO

        val grade = when {
            // Nothing to discriminate: the upstream device is not the larger,
            // so an overload below it trips whichever is quicker on the day.
            ratio <= 1.0 -> SelectivityGrade.NONE

            // The magnetic thresholds cross the wrong way — the upstream device
            // reaches its instantaneous region at or below the downstream one,
            // so it is at least as likely to go first at any fault level.
            upstreamInstantaneous != null && downstreamInstantaneous != null &&
                upstreamInstantaneous <= downstreamInstantaneous -> SelectivityGrade.NONE

            !overloadSelective -> SelectivityGrade.PARTIAL

            // Everything the app can check is satisfied at this fault level.
            upstreamInstantaneous == null -> SelectivityGrade.PARTIAL

            input.prospectiveFaultAmps < upstreamInstantaneous -> SelectivityGrade.SELECTIVE

            else -> SelectivityGrade.PARTIAL
        }

        return SelectivityResult(
            grade = grade,
            limitAmps = upstreamInstantaneous,
            upstreamInstantaneousAmps = upstreamInstantaneous,
            downstreamInstantaneousAmps = downstreamInstantaneous,
            ratio = ratio,
            overloadSelective = overloadSelective,
        )
    }
}
