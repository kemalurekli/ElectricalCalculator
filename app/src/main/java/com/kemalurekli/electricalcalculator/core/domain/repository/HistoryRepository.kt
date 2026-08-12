package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import kotlinx.coroutines.flow.Flow

/**
 * Stores the calculations the user has run.
 *
 * Every calculator writes here automatically on a successful result; the
 * history screen reads back through the observation methods.
 */
interface HistoryRepository {

    /** All records, newest first. */
    fun observeAll(): Flow<List<CalculationRecord>>

    /** Records produced by one calculator, newest first. */
    fun observeByCalculator(calculatorId: CalculatorId): Flow<List<CalculationRecord>>

    /**
     * Records whose title or summary contains [query], newest first.
     *
     * A blank query yields the same result as [observeAll].
     */
    fun observeSearch(query: String): Flow<List<CalculationRecord>>

    /** The [limit] most recent records, for the home screen's recent section. */
    fun observeRecent(limit: Int): Flow<List<CalculationRecord>>

    /** How many records are stored, re-emitting as the table changes. */
    fun observeCount(): Flow<Int>

    suspend fun findById(id: Long): CalculationRecord?

    /** Persists [record] and returns the id it was stored under. */
    suspend fun save(record: CalculationRecord): Long

    suspend fun rename(id: Long, title: String)

    /**
     * Copies the record at [id] as a new entry timestamped now.
     *
     * Returns the new id, or `null` if [id] no longer exists.
     */
    suspend fun duplicate(id: Long): Long?

    suspend fun delete(id: Long)

    suspend fun clearAll()
}
