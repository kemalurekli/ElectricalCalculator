package com.kemalurekli.electricalcalculator.features.calculators.conduitfill

import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillInput
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateConduitFillUseCaseTest {

    private val calculate = CalculateConduitFillUseCase()

    private fun input(
        conduit: Double = 25.0,
        cables: List<CableBundleEntry> = listOf(CableBundleEntry(8.5, 3)),
        rule: FillRule = FillRule.NEC_TABLE_1,
        custom: Double = 0.40,
    ) = ConduitFillInput(
        conduitInnerDiameterMm = conduit,
        cables = cables,
        rule = rule,
        customLimitFraction = custom,
    )

    // -- Geometry ---------------------------------------------------------------------

    @Test
    fun `worked example - three 8_5 mm cables in a 25 mm bore`() {
        val result = calculate(input())

        assertEquals(490.873852, result.conduitAreaMm2, 1e-6)
        assertEquals(170.235052, result.cableAreaMm2, 1e-6)
        assertEquals(0.3468, result.fillFraction, 1e-6)
        assertEquals(3, result.cableCount)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `cables of different sizes are summed by area, not by diameter`() {
        // Three 11.9 mm and two 8.5 mm cables in a 32 mm bore.
        val result = calculate(
            input(
                conduit = 32.0,
                cables = listOf(CableBundleEntry(11.9, 3), CableBundleEntry(8.5, 2)),
            ),
        )

        assertEquals(447.150736, result.cableAreaMm2, 1e-6)
        assertEquals(5, result.cableCount)
        assertEquals(0.555986, result.fillFraction, 1e-6)
    }

    @Test
    fun `quantity multiplies the area of one cable`() {
        val one = calculate(input(cables = listOf(CableBundleEntry(8.5, 1)))).cableAreaMm2
        val four = calculate(input(cables = listOf(CableBundleEntry(8.5, 4)))).cableAreaMm2

        assertEquals(4.0, four / one, 1e-9)
    }

    @Test
    fun `doubling the bore quadruples the available area`() {
        val small = calculate(input(conduit = 20.0)).conduitAreaMm2
        val large = calculate(input(conduit = 40.0)).conduitAreaMm2

        assertEquals(4.0, large / small, 1e-9)
    }

    // -- NEC Chapter 9, Table 1 --------------------------------------------------------

    @Test
    fun `a single cable is permitted 53 percent`() {
        val result = calculate(input(cables = listOf(CableBundleEntry(12.0, 1))))

        assertEquals(0.53, result.permittedFraction, 1e-9)
    }

    @Test
    fun `two cables are permitted only 31 percent`() {
        // The step down is deliberate: two circles pack badly inside a third.
        val result = calculate(input(cables = listOf(CableBundleEntry(8.5, 2))))

        assertEquals(0.31, result.permittedFraction, 1e-9)
    }

    @Test
    fun `three or more cables are permitted 40 percent`() {
        val three = calculate(input(cables = listOf(CableBundleEntry(8.5, 3))))
        val twenty = calculate(input(conduit = 63.0, cables = listOf(CableBundleEntry(8.5, 20))))

        assertEquals(0.40, three.permittedFraction, 1e-9)
        assertEquals(0.40, twenty.permittedFraction, 1e-9)
    }

    @Test
    fun `the permitted percentage follows the total count across entries`() {
        // Two entries of one cable each is still two cables.
        val result = calculate(
            input(cables = listOf(CableBundleEntry(8.5, 1), CableBundleEntry(11.9, 1))),
        )

        assertEquals(2, result.cableCount)
        assertEquals(0.31, result.permittedFraction, 1e-9)
    }

    // -- Verdict ------------------------------------------------------------------------

    @Test
    fun `a conduit over its limit is reported as such with the excess area`() {
        // 3 × 11.9 mm in a 32 mm bore is 41.5 %, just past the 40 % limit.
        val result = calculate(input(conduit = 32.0, cables = listOf(CableBundleEntry(11.9, 3))))

        assertEquals(0.414873, result.fillFraction, 1e-6)
        assertFalse(result.isWithinLimit)
        assertEquals(-11.961614, result.spareAreaMm2, 1e-6)
    }

    @Test
    fun `spare area is what remains inside the limit, not inside the conduit`() {
        val result = calculate(input())

        // 40 % of 490.87 is 196.35; 170.24 is used.
        assertEquals(26.114489, result.spareAreaMm2, 1e-6)
        assertTrue(result.spareAreaMm2 < result.conduitAreaMm2 - result.cableAreaMm2)
    }

    @Test
    fun `fill exactly at the limit passes`() {
        // A cable sized to land on precisely 40 % of the bore.
        val result = calculate(
            input(conduit = 100.0, cables = listOf(CableBundleEntry(100.0 * 0.2, 10))),
        )

        assertEquals(0.40, result.fillFraction, 1e-12)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `a conduit close to its limit is flagged without being a failure`() {
        // 38 % against a 40 % limit: compliant, but the next circuit will not fit.
        val result = calculate(
            input(conduit = 25.0, cables = listOf(CableBundleEntry(9.1, 3))),
        )

        assertTrue(result.isWithinLimit)
        assertTrue(result.isNearLimit)
    }

    @Test
    fun `a lightly filled conduit is not flagged`() {
        val result = calculate(input(conduit = 50.0))

        assertTrue(result.isWithinLimit)
        assertFalse(result.isNearLimit)
    }

    @Test
    fun `an over-filled conduit is never reported as near the limit`() {
        val result = calculate(input(conduit = 32.0, cables = listOf(CableBundleEntry(11.9, 3))))

        assertFalse(result.isWithinLimit)
        assertFalse(result.isNearLimit)
    }

    // -- What else fits ------------------------------------------------------------------

    @Test
    fun `the spare diameter is the largest cable that still fits`() {
        val result = calculate(input())

        assertEquals(5.766281, result.largestAdditionalCableMm, 1e-6)
    }

    @Test
    fun `adding that cable lands exactly on the limit`() {
        val before = calculate(input())
        val after = calculate(
            input(
                cables = listOf(
                    CableBundleEntry(8.5, 3),
                    CableBundleEntry(before.largestAdditionalCableMm, 1),
                ),
            ),
        )

        assertEquals(after.permittedFraction, after.fillFraction, 1e-9)
        assertTrue(after.isWithinLimit)
    }

    @Test
    fun `a second cable may not fit even when the first leaves room`() {
        // 12 mm in a 20 mm bore is 36 % against a single-cable limit of 53 %.
        // Adding a second cable drops the limit to 31 %, which the one cable
        // already exceeds — so nothing more goes in, despite the spare area.
        val result = calculate(input(conduit = 20.0, cables = listOf(CableBundleEntry(12.0, 1))))

        assertTrue(result.isWithinLimit)
        assertTrue(result.spareAreaMm2 > 0.0)
        assertEquals(0.0, result.largestAdditionalCableMm, 1e-12)
    }

    @Test
    fun `a third cable can bring an over-filled two-cable conduit into compliance`() {
        // The same quirk in reverse: 2 × 8.5 mm in a 20 mm bore is 36.1 %
        // against a 31 % limit, but a third cable raises the limit to 40 %.
        val result = calculate(input(conduit = 20.0, cables = listOf(CableBundleEntry(8.5, 2))))

        assertFalse(result.isWithinLimit)
        assertEquals(3.937004, result.largestAdditionalCableMm, 1e-6)
    }

    @Test
    fun `a full conduit admits nothing more`() {
        val result = calculate(input(conduit = 20.0, cables = listOf(CableBundleEntry(8.5, 5))))

        assertEquals(0.0, result.largestAdditionalCableMm, 1e-12)
    }

    // -- Custom limit ------------------------------------------------------------------------

    @Test
    fun `a custom limit replaces the table entirely`() {
        // A 45 % trunking space factor, which no NEC count would produce.
        val result = calculate(input(rule = FillRule.CUSTOM, custom = 0.45))

        assertEquals(0.45, result.permittedFraction, 1e-9)
        assertEquals(50.658182, result.spareAreaMm2, 1e-6)
        assertEquals(8.031189, result.largestAdditionalCableMm, 1e-6)
    }

    @Test
    fun `a custom limit does not change with the cable count`() {
        val one = calculate(
            input(cables = listOf(CableBundleEntry(8.5, 1)), rule = FillRule.CUSTOM, custom = 0.45),
        )
        val five = calculate(
            input(cables = listOf(CableBundleEntry(8.5, 5)), rule = FillRule.CUSTOM, custom = 0.45),
        )

        assertEquals(one.permittedFraction, five.permittedFraction, 1e-12)
    }

    @Test
    fun `the fill fraction itself never depends on the rule`() {
        val table = calculate(input())
        val custom = calculate(input(rule = FillRule.CUSTOM, custom = 0.45))

        assertEquals(table.fillFraction, custom.fillFraction, 1e-12)
    }
}
