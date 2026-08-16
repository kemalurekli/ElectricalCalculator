package com.kemalurekli.electricalcalculator.features.calculators.selectivity

import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.CheckSelectivityUseCase
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Where two devices in series stop being separable.
 *
 * The property that matters is that the answer moves with the *fault level*.
 * A pair of breakers is not selective or unselective in the abstract, and an
 * app that answered without asking for a fault current would be giving a
 * verdict it has no basis for.
 */
class CheckSelectivityUseCaseTest {

    private val check = CheckSelectivityUseCase()

    private fun input(
        upstreamType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_C,
        upstreamRatingAmps: Double = 63.0,
        downstreamType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_B,
        downstreamRatingAmps: Double = 16.0,
        prospectiveFaultAmps: Double = 400.0,
    ) = SelectivityInput(
        upstreamType = upstreamType,
        upstreamRatingAmps = upstreamRatingAmps,
        downstreamType = downstreamType,
        downstreamRatingAmps = downstreamRatingAmps,
        prospectiveFaultAmps = prospectiveFaultAmps,
    )

    @Test
    fun `the limit is the upstream device's own instantaneous threshold`() {
        // A 63 A Type C lets go at 10 x 63 = 630 A. That is the number an
        // engineer carries away, and it is usually lower than people expect.
        val result = check(input())
        assertEquals(630.0, result.limitAmps!!, 1e-9)
    }

    @Test
    fun `the same pair is selective at one fault level and not at another`() {
        // The whole point. Below the upstream threshold only the lower device
        // sees enough to open instantaneously; above it, both do.
        assertEquals(
            SelectivityGrade.SELECTIVE,
            check(input(prospectiveFaultAmps = 500.0)).grade,
        )
        assertEquals(
            SelectivityGrade.PARTIAL,
            check(input(prospectiveFaultAmps = 900.0)).grade,
        )
    }

    @Test
    fun `at the threshold itself selectivity is already lost`() {
        // The upstream device operates *at* its multiple, not above it, so the
        // boundary belongs to the failing side.
        assertEquals(
            SelectivityGrade.PARTIAL,
            check(input(prospectiveFaultAmps = 630.0)).grade,
        )
        assertEquals(
            SelectivityGrade.SELECTIVE,
            check(input(prospectiveFaultAmps = 629.99)).grade,
        )
    }

    @Test
    fun `devices too close in rating discriminate nowhere`() {
        // 20 A above 16 A is a ratio of 1.25, under the conventional 1.6: the
        // thermal curves are close enough to overlap, so an overload is a
        // coin toss even though the magnetic ends are far apart.
        val result = check(input(upstreamRatingAmps = 20.0, prospectiveFaultAmps = 100.0))

        assertFalse(result.overloadSelective)
        assertEquals(SelectivityGrade.PARTIAL, result.grade)
        assertTrue(result.ratio < SelectivityResult.OVERLOAD_RATIO)
    }

    @Test
    fun `an upstream device no larger than the one below it is not selectivity`() {
        listOf(16.0, 10.0).forEach { rating ->
            assertEquals(
                "$rating A upstream of 16 A",
                SelectivityGrade.NONE,
                check(input(upstreamRatingAmps = rating)).grade,
            )
        }
    }

    @Test
    fun `a slower curve downstream defeats the pair however the ratings look`() {
        // A 100 A Type B upstream lets go at 500 A; a 40 A Type D below it
        // holds on until 800 A. Two and a half times the rating is ample
        // margin by the overload rule, and the curves still say the upstream
        // device trips first — which is why the rating ratio alone is not an
        // answer.
        //
        // 63 A over 40 A, the pairing first reached for here, turns out to be
        // 1.575 — just under the 1.6 convention. Worth knowing, and not the
        // case this test is about.
        val result = check(
            input(
                upstreamType = ProtectiveDeviceType.MCB_TYPE_B,
                upstreamRatingAmps = 100.0,
                downstreamType = ProtectiveDeviceType.MCB_TYPE_D,
                downstreamRatingAmps = 40.0,
                prospectiveFaultAmps = 200.0,
            ),
        )

        assertEquals(SelectivityGrade.NONE, result.grade)
        assertTrue(result.overloadSelective)
        assertTrue(result.upstreamInstantaneousAmps!! < result.downstreamInstantaneousAmps!!)
    }

    @Test
    fun `a device whose curve the app cannot read is never called selective`() {
        // An RCD has no instantaneous multiple, and a custom device's rating
        // field holds an operating current rather than a curve. Claiming
        // selectivity from a characteristic the app does not have would be the
        // one genuinely dangerous answer here.
        listOf(ProtectiveDeviceType.RCD, ProtectiveDeviceType.CUSTOM).forEach { type ->
            val result = check(input(upstreamType = type, prospectiveFaultAmps = 50.0))
            assertNull("$type reported a threshold", result.limitAmps)
            assertEquals("$type", SelectivityGrade.PARTIAL, result.grade)
        }
    }
}
