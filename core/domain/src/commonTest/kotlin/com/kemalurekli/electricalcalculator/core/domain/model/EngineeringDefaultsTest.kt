package com.kemalurekli.electricalcalculator.core.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

/**
 * The first-run guess at what the user's supply looks like.
 *
 * The property that matters most is that only the *region* is consulted: a
 * guess made from the language would move a user's working voltage every time
 * they changed the app's language, which is the failure this design exists to
 * avoid. The signature says so now — it takes a region code rather than a
 * locale it would have to remember to read correctly.
 */
class EngineeringDefaultsTest {

    @Test
    fun `most of the world gets the IEC low-voltage system`() {
        listOf("TR", "DE", "GB", "FR", "JP", "EG").forEach { region ->
            val defaults = EngineeringDefaults.seedFor(region)
            assertEquals("230", defaults.singlePhaseVoltage, region)
            assertEquals("400", defaults.threePhaseVoltage, region)
            assertEquals("50", defaults.frequency, region)
        }
    }

    @Test
    fun `north america gets its own supply`() {
        listOf("US", "CA").forEach { region ->
            val defaults = EngineeringDefaults.seedFor(region)
            assertEquals("120", defaults.singlePhaseVoltage, region)
            assertEquals("208", defaults.threePhaseVoltage, region)
            assertEquals("60", defaults.frequency, region)
        }
    }

    @Test
    fun `the region decides and nothing else can`() {
        // English is spoken at 230 V in Britain and at 120 V in the United
        // States. The signature now takes a region rather than a locale, so
        // there is no longer a language subtag available to read by mistake —
        // which is what this test used to have to guard against.
        assertEquals(EngineeringDefaults.seedFor("GB"), EngineeringDefaults.seedFor("TR"))
        assertEquals(EngineeringDefaults.seedFor("US"), EngineeringDefaults.seedFor("CA"))
    }

    @Test
    fun `an unknown or absent region falls to the majority values`() {
        // A locale of plain `en` or `tr` — exactly what the app's own language
        // picker stores — yields an empty region.
        listOf("", "ZZ", "XK").forEach { region ->
            assertEquals(
                EngineeringDefaults.Default,
                EngineeringDefaults.seedFor(region),
                "seed for '$region'",
            )
        }
    }

    @Test
    fun `the region is read case-insensitively`() {
        // Platforms disagree about the case of a region subtag.
        assertEquals(EngineeringDefaults.seedFor("US"), EngineeringDefaults.seedFor("us"))
    }

    @Test
    fun `only the supply is regional`() {
        // Copper, PVC, clipped direct and a 30 °C ambient are the reference
        // conditions the ampacity tables themselves are built on, and they do
        // not change with the country.
        val na = EngineeringDefaults.seedFor("US")
        assertEquals(EngineeringDefaults.Default.material, na.material)
        assertEquals(EngineeringDefaults.Default.insulation, na.insulation)
        assertEquals(EngineeringDefaults.Default.installationMethod, na.installationMethod)
        assertEquals(EngineeringDefaults.Default.ambientTemperature, na.ambientTemperature)
    }

    @Test
    fun `each AC system reads its own voltage and DC reads none`() {
        val defaults = EngineeringDefaults(singlePhaseVoltage = "277", threePhaseVoltage = "480")
        assertEquals(defaults.voltageFor(SupplySystem.SINGLE_PHASE_AC), "277")
        assertEquals(defaults.voltageFor(SupplySystem.THREE_PHASE_AC), "480")
        // 12, 24, 48, 110 and 400 are all ordinary DC systems; there is no norm.
        assertNull(defaults.voltageFor(SupplySystem.DC))
    }
}
