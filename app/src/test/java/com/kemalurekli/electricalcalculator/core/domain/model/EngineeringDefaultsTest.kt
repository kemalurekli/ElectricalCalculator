package com.kemalurekli.electricalcalculator.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

/**
 * The first-run guess at what the user's supply looks like.
 *
 * The property that matters most is the one about language: a guess made from
 * the wrong signal would move a user's working voltage every time they changed
 * the app's language, which is the failure this whole design exists to avoid.
 */
class EngineeringDefaultsTest {

    @Test
    fun `most of the world gets the IEC low-voltage system`() {
        listOf("tr-TR", "de-DE", "en-GB", "fr-FR", "ja-JP", "ar-EG").forEach { tag ->
            val defaults = EngineeringDefaults.seedFor(Locale.forLanguageTag(tag))
            assertEquals(tag, "230", defaults.singlePhaseVoltage)
            assertEquals(tag, "400", defaults.threePhaseVoltage)
            assertEquals(tag, "50", defaults.frequency)
        }
    }

    @Test
    fun `north america gets its own supply`() {
        listOf("en-US", "es-US", "en-CA", "fr-CA").forEach { tag ->
            val defaults = EngineeringDefaults.seedFor(Locale.forLanguageTag(tag))
            assertEquals(tag, "120", defaults.singlePhaseVoltage)
            assertEquals(tag, "208", defaults.threePhaseVoltage)
            assertEquals(tag, "60", defaults.frequency)
        }
    }

    @Test
    fun `the language subtag on its own decides nothing`() {
        // English is spoken at 230 V in Britain and at 120 V in the United
        // States. Reading the language rather than the region would hand every
        // English-reading engineer in the world an American supply.
        assertEquals(
            EngineeringDefaults.seedFor(Locale.forLanguageTag("en-GB")),
            EngineeringDefaults.seedFor(Locale.forLanguageTag("tr-TR")),
        )
        assertEquals(
            EngineeringDefaults.seedFor(Locale.forLanguageTag("en-US")),
            EngineeringDefaults.seedFor(Locale.forLanguageTag("es-US")),
        )
    }

    @Test
    fun `a locale with no region falls to the majority values`() {
        // `en` and `tr` are exactly what the app's own language picker stores.
        listOf("en", "tr", "").forEach { tag ->
            assertEquals(
                "seed for '$tag'",
                EngineeringDefaults.Default,
                EngineeringDefaults.seedFor(Locale.forLanguageTag(tag)),
            )
        }
    }

    @Test
    fun `only the supply is regional`() {
        // Copper, PVC, clipped direct and a 30 °C ambient are the reference
        // conditions the ampacity tables themselves are built on, and they do
        // not change with the country.
        val na = EngineeringDefaults.seedFor(Locale.US)
        assertEquals(EngineeringDefaults.Default.material, na.material)
        assertEquals(EngineeringDefaults.Default.insulation, na.insulation)
        assertEquals(EngineeringDefaults.Default.installationMethod, na.installationMethod)
        assertEquals(EngineeringDefaults.Default.ambientTemperature, na.ambientTemperature)
    }

    @Test
    fun `each AC system reads its own voltage, and DC reads none`() {
        val defaults = EngineeringDefaults(singlePhaseVoltage = "277", threePhaseVoltage = "480")
        assertEquals("277", defaults.voltageFor(SupplySystem.SINGLE_PHASE_AC))
        assertEquals("480", defaults.voltageFor(SupplySystem.THREE_PHASE_AC))
        // 12, 24, 48, 110 and 400 are all ordinary DC systems; there is no norm.
        assertNull(defaults.voltageFor(SupplySystem.DC))
    }
}
