package com.kemalurekli.electricalcalculator.core.ui.model

import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The rule that decides when a pre-filled voltage may be replaced.
 *
 * The whole value of pre-filling is undone if it ever overwrites something the
 * user typed, so that is the property most of these cover.
 */
class SystemVoltageDefaultsTest {

    @Test
    fun `each AC system opens on its own voltage`() {
        assertEquals("400", SystemVoltageDefaults.forSystem(SupplySystem.THREE_PHASE_AC))
        assertEquals("230", SystemVoltageDefaults.forSystem(SupplySystem.SINGLE_PHASE_AC))
    }

    @Test
    fun `DC has no default worth guessing at`() {
        // 12, 24, 48, 110 and 400 are all ordinary DC systems; there is no norm.
        assertNull(SystemVoltageDefaults.forSystem(SupplySystem.DC))
    }

    @Test
    fun `switching between AC systems moves an untouched voltage`() {
        assertEquals(
            "230",
            SystemVoltageDefaults.follow("400", SupplySystem.SINGLE_PHASE_AC, userEdited = false),
        )
        assertEquals(
            "400",
            SystemVoltageDefaults.follow("230", SupplySystem.THREE_PHASE_AC, userEdited = false),
        )
    }

    @Test
    fun `a voltage the user typed is never overwritten`() {
        // Including one that happens to equal a default. A 230 V three-phase
        // delta system is real, and a user who typed it must not have it
        // replaced by 400 the moment they touch the supply selector — which is
        // exactly what a "does it look like a default" heuristic would do.
        listOf("690", "110", "480", "230", "400").forEach { typed ->
            SupplySystem.entries.forEach { system ->
                assertEquals(
                    "$typed was overwritten by $system",
                    typed,
                    SystemVoltageDefaults.follow(typed, system, userEdited = true),
                )
            }
        }
    }

    @Test
    fun `switching to DC leaves the field alone`() {
        // There is nothing to replace it with, so the last value stands until
        // the user corrects it.
        assertEquals(
            "400",
            SystemVoltageDefaults.follow("400", SupplySystem.DC, userEdited = false),
        )
        assertEquals(
            "48",
            SystemVoltageDefaults.follow("48", SupplySystem.DC, userEdited = false),
        )
    }
}
