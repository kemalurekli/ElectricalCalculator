package com.kemalurekli.electricalcalculator.features.converter

import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitScale
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.Test

/**
 * The one scale in the converter that is not a factor.
 *
 * Everything else here is `base = value × factor`, and the catalog-wide tests
 * cover those by construction. A logarithm is the case where a round trip can
 * silently lose its meaning, so it is checked against the anchors people
 * actually remember.
 */
class DecibelScaleTest {

    private val dbm = UnitScale.Decibel(referenceWatts = 1e-3)

    @Test
    fun `the anchors every engineer knows come out right`() {
        // 0 dBm is a milliwatt, +30 dBm is a watt, and every 10 dB is ten times.
        assertEquals(1e-3, dbm.toBase(0.0), 1e-15)
        assertEquals(1.0, dbm.toBase(30.0), 1e-12)
        assertEquals(1e-2, dbm.toBase(10.0), 1e-14)
        assertEquals(1e-4, dbm.toBase(-10.0), 1e-16)
    }

    @Test
    fun `3 dB is very nearly a doubling`() {
        assertEquals(2.0, dbm.toBase(3.0) / dbm.toBase(0.0), 0.01)
    }

    @Test
    fun `a value survives the round trip`() {
        listOf(-40.0, -3.0, 0.0, 13.0, 30.0, 46.0).forEach { db ->
            assertEquals(db, dbm.fromBase(dbm.toBase(db)), 1e-9, "$db dBm")
        }
    }

    @Test
    fun `dBW sits exactly thirty decibels below dBm`() {
        // Same power, two references: 30 dBm and 0 dBW are both one watt.
        val dbw = UnitScale.Decibel(referenceWatts = 1.0)
        assertEquals(dbm.toBase(30.0), dbw.toBase(0.0), 1e-12)
        assertEquals(30.0, dbm.fromBase(1.0) - dbw.fromBase(1.0), 1e-9)
    }

    @Test
    fun `zero power has no decibel value and says so`() {
        // The logarithm diverges. Negative infinity is the honest answer; a
        // large negative number would read as a measurement.
        assertTrue(dbm.fromBase(0.0).isInfinite())
        assertTrue(dbm.fromBase(0.0) < 0.0)
    }

    @Test
    fun `the new categories are registered and self-consistent`() {
        listOf("capacitance", "inductance", "signal_power", "torque", "illuminance", "angle")
            .forEach { key ->
                val category = UnitCatalog.categoryOrNull(key)
                assertNotNull(category, "$key is not in the catalog")
                val units = category!!.units
                assertTrue(units.size >= 2, "$key has no units")
                assertTrue(
                    units.any { it.key == category.baseUnitKey },
                    "$key names a base unit it does not contain",
                )
                assertTrue(units.any { it.key == category.defaultFromKey })
                assertTrue(units.any { it.key == category.defaultToKey })
            }
    }
}
