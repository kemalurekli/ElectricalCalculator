package com.kemalurekli.electricalcalculator.core.data

import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.database.createElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * The repository, the database and the domain model working together on a
 * simulator.
 *
 * The module's own tests cover each layer; this covers the seam. A record goes
 * in as a domain object, through the entity mapping and the type converters,
 * onto a real SQLite file, and comes back out as the same domain object —
 * which is the only claim that matters to a screen.
 *
 * iOS-only for the reason the other round-trips are: the Android half of this
 * needs a `Context`, and Android already exercises the same code every time
 * anyone opens the history screen.
 */
class HistoryRepositoryIosTest {

    private val database = createElecToolkitDatabase()
    private val repository = HistoryRepositoryImpl(
        dao = database.calculationHistoryDao(),
        timeProvider = FixedTime,
        ioDispatcher = Dispatchers.Default,
    )

    private object FixedTime : TimeProvider {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    }

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun `a saved calculation comes back as the domain object it went in as`() = runTest {
        val id = repository.save(
            CalculationRecord(
                calculatorId = CalculatorId.VOLTAGE_DROP,
                title = "Voltage drop",
                summary = "5.36 V",
                inputs = mapOf("length" to "30"),
                results = mapOf("drop" to "5.36"),
                createdAt = FixedTime.now(),
            ),
        )

        val stored = repository.findById(id)

        assertEquals(CalculatorId.VOLTAGE_DROP, stored?.calculatorId)
        assertEquals(mapOf("length" to "30"), stored?.inputs)
        assertEquals(FixedTime.now(), stored?.createdAt)
    }

    @Test
    fun `deleting a record removes it`() = runTest {
        val id = repository.save(
            CalculationRecord(
                calculatorId = CalculatorId.POWER,
                title = "Power",
                summary = "14.72 kW",
                inputs = emptyMap(),
                results = emptyMap(),
                createdAt = FixedTime.now(),
            ),
        )

        repository.delete(id)

        assertNull(repository.findById(id))
        assertEquals(0, repository.observeAll().first().count { it.id == id })
    }
}
