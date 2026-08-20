package com.kemalurekli.electricalcalculator.core.data.repository

import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryRepositoryImplTest {

    private val dao = FakeCalculationHistoryDao()
    private val timeProvider = FakeTimeProvider()
    private val repository = HistoryRepositoryImpl(
        dao = dao,
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `saved records are returned newest first`() = runTest {
        repository.save(record(title = "Oldest", at = 1_000L))
        repository.save(record(title = "Newest", at = 3_000L))
        repository.save(record(title = "Middle", at = 2_000L))

        repository.observeAll().test {
            assertEquals(
                listOf("Newest", "Middle", "Oldest"),
                awaitItem().map { it.title },
            )
        }
    }

    @Test
    fun `records are filtered by calculator`() = runTest {
        repository.save(record(title = "Drop", calculatorId = CalculatorId.VOLTAGE_DROP))
        repository.save(record(title = "Size", calculatorId = CalculatorId.CABLE_SIZE))

        repository.observeByCalculator(CalculatorId.VOLTAGE_DROP).test {
            assertEquals(listOf("Drop"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `observeRecent caps the result`() = runTest {
        repeat(5) { index -> repository.save(record(title = "Run $index", at = index * 1_000L)) }

        repository.observeRecent(limit = 2).test {
            assertEquals(listOf("Run 4", "Run 3"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `observeRecent with a non-positive limit returns nothing`() = runTest {
        repository.save(record(title = "Run"))

        repository.observeRecent(limit = 0).test {
            assertTrue(awaitItem().isEmpty())
            // Short-circuits to a finite flow rather than querying the database.
            awaitComplete()
        }
    }

    @Test
    fun `search matches title and summary`() = runTest {
        repository.save(record(title = "Kitchen ring", summary = "3.2 V"))
        repository.save(record(title = "Garage feed", summary = "12.8 V"))

        repository.observeSearch("kitchen").test {
            assertEquals(listOf("Kitchen ring"), awaitItem().map { it.title })
        }
        repository.observeSearch("12.8").test {
            assertEquals(listOf("Garage feed"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `a blank search returns every record`() = runTest {
        repository.save(record(title = "One"))
        repository.save(record(title = "Two"))

        repository.observeSearch("  ").test {
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun `wildcards in a search term are treated literally`() = runTest {
        repository.save(record(title = "Load at 50%"))
        repository.save(record(title = "Unrelated"))

        // Unescaped, "%" would match every row and silently disable filtering.
        repository.observeSearch("50%").test {
            assertEquals(listOf("Load at 50%"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `rename updates only the title`() = runTest {
        val id = repository.save(record(title = "Original", summary = "3.2 V"))

        repository.rename(id, "Renamed")

        val stored = repository.findById(id)
        assertEquals("Renamed", stored?.title)
        assertEquals("3.2 V", stored?.summary)
    }

    @Test
    fun `rename trims surrounding whitespace`() = runTest {
        val id = repository.save(record(title = "Original"))

        repository.rename(id, "  Padded  ")

        assertEquals("Padded", repository.findById(id)?.title)
    }

    @Test
    fun `duplicate copies the inputs under a new id and timestamp`() = runTest {
        val id = repository.save(
            record(title = "Source", at = 1_000L, inputs = mapOf("length" to "25")),
        )
        timeProvider.advanceBy(9_000L)

        val duplicateId = repository.duplicate(id)

        assertNotEquals(id, duplicateId)
        val copy = repository.findById(duplicateId!!)
        assertEquals("Source", copy?.title)
        assertEquals(mapOf("length" to "25"), copy?.inputs)
        // Timestamped now, so it sorts above the record it came from.
        assertEquals(timeProvider.now(), copy?.createdAt)
    }

    @Test
    fun `duplicating a missing record returns null`() = runTest {
        assertNull(repository.duplicate(id = 404L))
    }

    @Test
    fun `delete removes a single record`() = runTest {
        val id = repository.save(record(title = "Doomed"))
        repository.save(record(title = "Kept"))

        repository.delete(id)

        repository.observeAll().test {
            assertEquals(listOf("Kept"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `clearAll empties the table`() = runTest {
        repository.save(record(title = "One"))
        repository.save(record(title = "Two"))

        repository.clearAll()

        repository.observeAll().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `inputs and results survive a round trip`() = runTest {
        val inputs = mapOf("voltage" to "230", "length" to "25")
        val results = mapOf("drop" to "3.2 V", "percent" to "1.4 %")

        val id = repository.save(record(inputs = inputs, results = results))

        val stored = repository.findById(id)
        assertEquals(inputs, stored?.inputs)
        assertEquals(results, stored?.results)
    }

    private fun record(
        title: String = "Calculation",
        summary: String = "0 V",
        calculatorId: CalculatorId = CalculatorId.VOLTAGE_DROP,
        at: Long = 1_000L,
        inputs: Map<String, String> = emptyMap(),
        results: Map<String, String> = emptyMap(),
    ) = CalculationRecord(
        calculatorId = calculatorId,
        title = title,
        summary = summary,
        inputs = inputs,
        results = results,
        createdAt = Instant.fromEpochMilliseconds(at),
    )
}
