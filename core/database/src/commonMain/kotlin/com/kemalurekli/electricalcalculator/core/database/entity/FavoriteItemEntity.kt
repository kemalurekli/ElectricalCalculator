package com.kemalurekli.electricalcalculator.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Something the user has pinned, on any shelf.
 *
 * The kind and the key together are the primary key, which makes pinning
 * idempotent: an `INSERT OR REPLACE` cannot create a duplicate. They are a pair
 * rather than one column because the shelves mint their keys independently and
 * two of them could legitimately choose the same word.
 *
 * Replaces the calculator-only table of version 1; see `Migrations.kt`.
 */
@Entity(tableName = FavoriteItemEntity.TABLE_NAME, primaryKeys = ["kind", "item_key"])
data class FavoriteItemEntity(
    @ColumnInfo(name = "kind")
    val kind: String,

    @ColumnInfo(name = "item_key")
    val key: String,

    /** Epoch milliseconds, used to keep favourites in the order they were pinned. */
    @ColumnInfo(name = "pinned_at")
    val pinnedAtEpochMillis: Long,
) {
    companion object {
        const val TABLE_NAME = "favorite_items"
    }
}
