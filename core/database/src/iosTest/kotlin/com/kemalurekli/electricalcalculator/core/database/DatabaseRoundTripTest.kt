package com.kemalurekli.electricalcalculator.core.database

import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Opens the real database on a simulator and writes to it.
 *
 * iOS-only, and that is the point rather than a gap. Android's Room is already
 * covered by an instrumented suite that runs on a device; a JVM host test has
 * no Android runtime to give Room, so the same file there would only prove that
 * `check(::databaseContext.isInitialized)` throws.
 *
 * "It compiles" is a weaker claim than usual on this side: Room generates a
 * separate implementation per target, opens the file through a different
 * driver, and resolves the path from a different API. All three could be wrong
 * in ways a compiler cannot see.
 *
 * The map column is deliberate — it goes through [converter.StringMapConverter],
 * so a round-trip that survives proves the type converters were generated too.
 */
class DatabaseRoundTripTest {

    private val database = createElecToolkitDatabase()

    @AfterTest
    fun close() {
        database.close()
    }

    @Test
    fun `a saved calculation comes back as it went in`() = runTest {
        val dao = database.calculationHistoryDao()
        val id = dao.insert(
            CalculationHistoryEntity(
                calculatorId = "voltage_drop",
                title = "Voltage drop — 4 mm², 30 m",
                summary = "5.36 V",
                inputs = mapOf("length" to "30", "csa" to "4"),
                results = mapOf("drop" to "5.36"),
                createdAtEpochMillis = 1_700_000_000_000L,
            ),
        )

        val stored = dao.findById(id)

        assertEquals("Voltage drop — 4 mm², 30 m", stored?.title)
        assertEquals(mapOf("length" to "30", "csa" to "4"), stored?.inputs)
        assertEquals(mapOf("drop" to "5.36"), stored?.results)
    }

    @Test
    fun `deleting a row removes it from the observed list`() = runTest {
        val dao = database.calculationHistoryDao()
        val id = dao.insert(
            CalculationHistoryEntity(
                calculatorId = "power",
                title = "Power",
                summary = "14.72 kW",
                inputs = emptyMap(),
                results = emptyMap(),
                createdAtEpochMillis = 1_700_000_000_001L,
            ),
        )
        assertEquals(1, dao.observeAll().first().count { it.id == id })

        dao.deleteById(id)

        assertEquals(0, dao.observeAll().first().count { it.id == id })
    }
}
