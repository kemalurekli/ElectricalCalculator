package com.kemalurekli.electricalcalculator.core.domain.model

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The four constants every resistance in this app is built on.
 *
 * Checked 2026-09-04. Both resistivities are IEC reference conductivities
 * written the other way up: copper is 1/58 Ω·mm²/m, which is 100 % IACS
 * annealed copper, and aluminium is 1/35,38, which is the 61 % IACS conductor
 * grade. The second one is worth stating plainly, because a table of *pure*
 * aluminium gives 0,0265 and looks like it ought to be right — conductor
 * aluminium is an alloy and is meaningfully worse. The coefficients 0,00393 and
 * 0,00403 per kelvin are the cable-work values.
 *
 * Voltage drop, loop impedance and fault current all pass through these, so a
 * wrong digit here is wrong everywhere at once, in a way no calculator-level
 * test would localise.
 */
class ConductorMaterialTest {

    @Test
    fun `the resistivities are the IEC reference conductivities`() {
        assertEquals(0.017241, ConductorMaterial.COPPER.resistivityAt20C, 1e-9)
        assertEquals(0.028264, ConductorMaterial.ALUMINIUM.resistivityAt20C, 1e-9)
    }

    @Test
    fun `each resistivity is the reciprocal of the conductivity it claims`() {
        // Copper at 58 MS/m and aluminium at 61 % of that. Written out because
        // it is the check a reader can do without a standard to hand.
        assertTrue(abs(ConductorMaterial.COPPER.resistivityAt20C - 1.0 / 58.0) < 5e-7)
        assertTrue(abs(ConductorMaterial.ALUMINIUM.resistivityAt20C - 1.0 / (0.61 * 58.0)) < 5e-6)
    }

    @Test
    fun `the temperature coefficients are the cable values`() {
        assertEquals(0.00393, ConductorMaterial.COPPER.temperatureCoefficient, 1e-12)
        assertEquals(0.00403, ConductorMaterial.ALUMINIUM.temperatureCoefficient, 1e-12)
    }

    @Test
    fun `a warm conductor resists more than a cold one`() {
        ConductorMaterial.entries.forEach { material ->
            val cold = material.resistivityAt(20.0)
            val warm = material.resistivityAt(70.0)
            assertEquals(material.resistivityAt20C, cold, 1e-12)
            assertTrue(warm > cold, "${material.name} does not rise with temperature")
        }
    }

    @Test
    fun `copper at seventy degrees is about twenty per cent worse than at twenty`() {
        // The figure the calculators' notes quote to the user, so it should be
        // the figure the constants actually produce.
        val ratio = ConductorMaterial.COPPER.resistivityAt(70.0) /
            ConductorMaterial.COPPER.resistivityAt20C
        assertTrue(ratio in 1.19..1.21, "copper 20 °C → 70 °C ratio is $ratio")
    }
}
