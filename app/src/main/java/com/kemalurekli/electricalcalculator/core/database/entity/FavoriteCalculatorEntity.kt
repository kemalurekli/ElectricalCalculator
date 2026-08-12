package com.kemalurekli.electricalcalculator.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A calculator the user has pinned to the home screen.
 *
 * The calculator key is the primary key, which makes pinning idempotent: an
 * `INSERT OR REPLACE` cannot create a duplicate favourite.
 */
@Entity(tableName = FavoriteCalculatorEntity.TABLE_NAME)
data class FavoriteCalculatorEntity(
    @PrimaryKey
    @ColumnInfo(name = "calculator_id")
    val calculatorId: String,

    /** Epoch milliseconds, used to keep favourites in the order they were pinned. */
    @ColumnInfo(name = "pinned_at")
    val pinnedAtEpochMillis: Long,
) {
    companion object {
        const val TABLE_NAME = "favorite_calculators"
    }
}
