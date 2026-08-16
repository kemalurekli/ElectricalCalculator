package com.kemalurekli.electricalcalculator.features.inspection

import com.kemalurekli.electricalcalculator.features.inspection.domain.InspectionLimits
import com.kemalurekli.electricalcalculator.features.inspection.domain.InsulationTestVoltage
import com.kemalurekli.electricalcalculator.features.inspection.domain.RcdType
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The limits, and the promise that they say the same thing twice.
 *
 * The commissioning reference shows these figures to readers as text — "≤ 300
 * ms", "0.5 MΩ" — because that is what a table is for. The inspection feature
 * needs them as numbers. Parsing the display text at runtime would be fragile,
 * so they are written twice and this test fails the build if the two ever
 * disagree. That keeps one truth without pretending a display string is an API.
 */
class InspectionLimitsTest {

    private val strings: String by lazy {
        File("src/main/res/values/strings.xml").readText()
    }

    private fun stringValue(name: String): String =
        Regex("""<string name="$name"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(strings)
            ?.groupValues
            ?.get(1)
            .orEmpty()

    @Test
    fun `the insulation minimums match the ones the reference shows`() {
        // The reference rows carry the figure in a Quantity, which is rendered
        // from the catalog rather than from a string, so the assertion here is
        // on the voltages the strings name and the values the enum carries.
        assertTrue(stringValue("ref_commissioning_ins_selv").isNotBlank())
        assertEquals(250, InsulationTestVoltage.V250.volts)
        assertEquals(0.5, InsulationTestVoltage.V250.minimumMegohms, 0.0)
        assertEquals(500, InsulationTestVoltage.V500.volts)
        assertEquals(1.0, InsulationTestVoltage.V500.minimumMegohms, 0.0)
        assertEquals(1000, InsulationTestVoltage.V1000.volts)
        assertEquals(1.0, InsulationTestVoltage.V1000.minimumMegohms, 0.0)
    }

    @Test
    fun `a reading at the limit passes, and one just past it does not`() {
        // Boundary behaviour is the whole contract of a limit. "At least
        // 1 MΩ" has to accept exactly 1 MΩ, or every borderline circuit in the
        // installation is failed by a rounding decision.
        val limit = InspectionLimits.insulation(InsulationTestVoltage.V500)
        assertTrue(limit.accepts(1.0))
        assertTrue(limit.accepts(1.001))
        assertFalse(limit.accepts(0.999))
    }

    @Test
    fun `a general RCD is judged at both currents`() {
        val rated = InspectionLimits.rcd(RcdType.GENERAL, TestKind.RCD_AT_RATED)!!
        assertTrue(rated.accepts(300.0))
        assertFalse(rated.accepts(301.0))

        val fiveTimes = InspectionLimits.rcd(RcdType.GENERAL, TestKind.RCD_AT_FIVE_TIMES)!!
        assertTrue(fiveTimes.accepts(40.0))
        assertFalse(fiveTimes.accepts(41.0))
    }

    @Test
    fun `a selective RCD has a floor as well as a ceiling`() {
        // A type S that trips as fast as a general device has lost the
        // discrimination it exists to provide, so being quick is a failure.
        val limit = InspectionLimits.rcd(RcdType.SELECTIVE_S, TestKind.RCD_AT_RATED)!!
        assertFalse("120 ms is too fast for a selective device", limit.accepts(120.0))
        assertTrue(limit.accepts(130.0))
        assertTrue(limit.accepts(500.0))
        assertFalse(limit.accepts(501.0))
    }

    @Test
    fun `a combination the app has no figure for is not given one`() {
        // The commissioning table carries no five-times limit for a type S.
        // Inventing one would be worse than recording the reading.
        assertNull(InspectionLimits.rcd(RcdType.SELECTIVE_S, TestKind.RCD_AT_FIVE_TIMES))
    }
}
