package com.kemalurekli.electricalcalculator.core.domain.table

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The two derating tables, cell by cell, against the standard.
 *
 * Checked 2026-09-04 against IEC 60364-5-52 Table B.52.14 (ambient air) and
 * Table B.52.17 (groups of more than one circuit, bunched in air, on a surface,
 * embedded or enclosed). BS 7671 carries the same numbers as Tables 4B1 and
 * 4C1, and an IET worked example reading `Cg = 0.54` for seven circuits and
 * `Ca = 0.94` at 35 °C agrees with both.
 *
 * Every cell is written out rather than spot-checked. These factors multiply
 * the ampacity of every cable the app sizes, and a single wrong digit here
 * survives the interpolation, the ordering checks and the worked examples —
 * it just quietly returns a smaller cable. That is the failure this file
 * exists to make impossible to introduce silently.
 */
class CorrectionFactorsTest {

    private val factors = CorrectionFactors()

    @Test
    fun `the ambient table for thermoplastic is the one in the standard`() {
        assertEquals(
            mapOf(
                10.0 to 1.22, 15.0 to 1.17, 20.0 to 1.12, 25.0 to 1.06, 30.0 to 1.00,
                35.0 to 0.94, 40.0 to 0.87, 45.0 to 0.79, 50.0 to 0.71, 55.0 to 0.61,
                60.0 to 0.50,
            ),
            CorrectionFactors.PVC_AMBIENT,
        )
    }

    @Test
    fun `the ambient table for thermoset is the one in the standard`() {
        assertEquals(
            mapOf(
                10.0 to 1.15, 15.0 to 1.12, 20.0 to 1.08, 25.0 to 1.04, 30.0 to 1.00,
                35.0 to 0.96, 40.0 to 0.91, 45.0 to 0.87, 50.0 to 0.82, 55.0 to 0.76,
                60.0 to 0.71, 65.0 to 0.65, 70.0 to 0.58, 75.0 to 0.50, 80.0 to 0.41,
            ),
            CorrectionFactors.XLPE_AMBIENT,
        )
    }

    @Test
    fun `the grouping table is the one in the standard`() {
        assertEquals(
            mapOf(
                1 to 1.00, 2 to 0.80, 3 to 0.70, 4 to 0.65, 5 to 0.60, 6 to 0.57,
                7 to 0.54, 8 to 0.52, 9 to 0.50, 12 to 0.45, 16 to 0.41, 20 to 0.38,
            ),
            CorrectionFactors.GROUPING,
        )
    }

    @Test
    fun `thermoplastic stops where the standard stops`() {
        // The two tables end in different places, and the difference is not
        // arbitrary: a 70 °C conductor in 60 °C air has almost no headroom
        // left, so the standard declines to tabulate past it. A user asking
        // for 70 °C ambient on PVC is asking for something the table cannot
        // answer, and the calculator has to say so rather than extrapolate.
        assertEquals(60.0, factors.maxAmbientC(CableInsulation.PVC))
        assertEquals(80.0, factors.maxAmbientC(CableInsulation.XLPE))
    }

    @Test
    fun `a count between tabulated steps takes the next step down`() {
        // The table jumps 9, 12, 16, 20. Ten circuits are not tabulated, and
        // rounding toward the friendlier neighbour would rate a cable above
        // what the standard allows — so ten takes twelve's factor.
        assertEquals(0.45, factors.groupingFactor(10))
        assertEquals(0.41, factors.groupingFactor(13))
        assertEquals(0.38, factors.groupingFactor(17))
    }

    @Test
    fun `beyond the table the last factor holds`() {
        // Extrapolating a straight line past 20 circuits reaches zero and then
        // negative, which would demand an infinite conductor. Clamping is the
        // honest answer: past the table, the table has nothing more to say.
        assertEquals(0.38, factors.groupingFactor(40))
        assertEquals(0.38, factors.groupingFactor(400))
    }

    @Test
    fun `between two tabulated temperatures the factor is interpolated`() {
        // 37,5 °C sits halfway between the 35 °C and 40 °C rows for PVC.
        assertEquals((0.94 + 0.87) / 2, factors.ambientFactor(37.5, CableInsulation.PVC), 1e-12)
    }

    @Test
    fun `outside the table the nearest end holds`() {
        assertEquals(1.22, factors.ambientFactor(-5.0, CableInsulation.PVC))
        assertEquals(0.50, factors.ambientFactor(99.0, CableInsulation.PVC))
    }

    @Test
    fun `warmer air never earns a cable more capacity`() {
        CableInsulation.entries.forEach { insulation ->
            var previous = Double.MAX_VALUE
            var t = 10.0
            while (t <= factors.maxAmbientC(insulation)) {
                val factor = factors.ambientFactor(t, insulation)
                assertTrue(factor <= previous, "$insulation rises at $t °C")
                previous = factor
                t += 2.5
            }
        }
    }
}
