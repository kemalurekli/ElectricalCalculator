package com.kemalurekli.electricalcalculator.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteCalculatorEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real SQLite schema, which the in-memory fakes used by the unit
 * tests cannot cover: type converters, index-backed ordering and the `LIKE`
 * escape clause all only exist in the generated implementation.
 */
@RunWith(AndroidJUnit4::class)
class ElecToolkitDatabaseTest {

    private lateinit var database: ElecToolkitDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ElecToolkitDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun mapsSurviveTheTypeConverter() = runTest {
        val dao = database.calculationHistoryDao()
        val inputs = mapOf("voltage" to "230", "length" to "25")
        val results = mapOf("drop" to "3.2 V")

        val id = dao.insert(historyEntity(inputs = inputs, results = results))

        val stored = dao.findById(id)
        assertEquals(inputs, stored?.inputs)
        assertEquals(results, stored?.results)
    }

    @Test
    fun historyIsOrderedNewestFirst() = runTest {
        val dao = database.calculationHistoryDao()
        dao.insert(historyEntity(title = "Old", createdAt = 1_000L))
        dao.insert(historyEntity(title = "New", createdAt = 3_000L))
        dao.insert(historyEntity(title = "Middle", createdAt = 2_000L))

        assertEquals(
            listOf("New", "Middle", "Old"),
            dao.observeAll().first().map { it.title },
        )
    }

    @Test
    fun searchTreatsWildcardsInTheTermLiterally() = runTest {
        val dao = database.calculationHistoryDao()
        dao.insert(historyEntity(title = "Load at 50%"))
        dao.insert(historyEntity(title = "Unrelated"))

        // The repository escapes "%" and the query declares ESCAPE '\'.
        val results = dao.observeSearch("%50\\%%").first()

        assertEquals(listOf("Load at 50%"), results.map { it.title })
    }

    @Test
    fun favoritesCannotBeDuplicated() = runTest {
        val dao = database.favoriteCalculatorDao()
        dao.insert(FavoriteCalculatorEntity("voltage_drop", pinnedAtEpochMillis = 1_000L))
        dao.insert(FavoriteCalculatorEntity("voltage_drop", pinnedAtEpochMillis = 2_000L))

        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun favoriteLookupReflectsInsertAndDelete() = runTest {
        val dao = database.favoriteCalculatorDao()
        assertFalse(dao.isFavorite("power"))

        dao.insert(FavoriteCalculatorEntity("power", pinnedAtEpochMillis = 1_000L))
        assertTrue(dao.isFavorite("power"))

        dao.deleteById("power")
        assertFalse(dao.isFavorite("power"))
    }

    private fun historyEntity(
        title: String = "Calculation",
        createdAt: Long = 1_000L,
        inputs: Map<String, String> = emptyMap(),
        results: Map<String, String> = emptyMap(),
    ) = CalculationHistoryEntity(
        calculatorId = "voltage_drop",
        title = title,
        summary = "0 V",
        inputs = inputs,
        results = results,
        createdAtEpochMillis = createdAt,
    )
}
