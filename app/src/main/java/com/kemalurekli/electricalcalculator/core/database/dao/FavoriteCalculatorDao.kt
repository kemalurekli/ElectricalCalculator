package com.kemalurekli.electricalcalculator.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteCalculatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCalculatorDao {

    @Query("SELECT * FROM favorite_calculators ORDER BY pinned_at ASC")
    fun observeAll(): Flow<List<FavoriteCalculatorEntity>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite_calculators WHERE calculator_id = :calculatorId)",
    )
    fun observeIsFavorite(calculatorId: String): Flow<Boolean>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite_calculators WHERE calculator_id = :calculatorId)",
    )
    suspend fun isFavorite(calculatorId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteCalculatorEntity)

    @Query("DELETE FROM favorite_calculators WHERE calculator_id = :calculatorId")
    suspend fun deleteById(calculatorId: String)
}
