package com.kemalurekli.electricalcalculator.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteItemDao {

    @Query("SELECT * FROM favorite_items ORDER BY pinned_at ASC")
    fun observeAll(): Flow<List<FavoriteItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE kind = :kind AND item_key = :key)")
    fun observeIsFavorite(kind: String, key: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE kind = :kind AND item_key = :key)")
    suspend fun isFavorite(kind: String, key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteItemEntity)

    @Query("DELETE FROM favorite_items WHERE kind = :kind AND item_key = :key")
    suspend fun delete(kind: String, key: String)
}
