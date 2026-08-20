package com.kemalurekli.electricalcalculator.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {

    @Query("SELECT * FROM calculation_history ORDER BY created_at DESC")
    fun observeAll(): Flow<List<CalculationHistoryEntity>>

    @Query(
        "SELECT * FROM calculation_history WHERE calculator_id = :calculatorId " +
            "ORDER BY created_at DESC",
    )
    fun observeByCalculator(calculatorId: String): Flow<List<CalculationHistoryEntity>>

    @Query("SELECT * FROM calculation_history ORDER BY created_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CalculationHistoryEntity>>

    /**
     * Case-insensitive substring search over the title and summary.
     *
     * The caller passes an already-wrapped pattern (`%term%`) with any literal
     * wildcards backslash-escaped, which the `ESCAPE` clause here makes
     * effective. `LIKE` is case-insensitive for ASCII in SQLite by default,
     * which covers the calculator names and units this table stores.
     */
    @Query(
        "SELECT * FROM calculation_history " +
            "WHERE title LIKE :pattern ESCAPE '\\' OR summary LIKE :pattern ESCAPE '\\' " +
            "ORDER BY created_at DESC",
    )
    fun observeSearch(pattern: String): Flow<List<CalculationHistoryEntity>>

    /** Total stored records, for the dashboard's saved-count badge. */
    @Query("SELECT COUNT(*) FROM calculation_history")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM calculation_history WHERE id = :id")
    suspend fun findById(id: Long): CalculationHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CalculationHistoryEntity): Long

    @Query("UPDATE calculation_history SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()
}
