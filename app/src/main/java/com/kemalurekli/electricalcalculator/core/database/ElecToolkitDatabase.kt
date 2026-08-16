package com.kemalurekli.electricalcalculator.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kemalurekli.electricalcalculator.core.database.converter.StringMapConverter
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity

/**
 * The application database.
 *
 * Schemas are exported to `app/schemas` (see the `room.schemaLocation` KSP
 * argument) so that every version is committed alongside the code and future
 * migrations can be verified against the real prior schema in tests.
 */
@Database(
    entities = [
        CalculationHistoryEntity::class,
        FavoriteItemEntity::class,
    ],
    version = ElecToolkitDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(StringMapConverter::class)
abstract class ElecToolkitDatabase : RoomDatabase() {

    abstract fun calculationHistoryDao(): CalculationHistoryDao

    abstract fun favoriteItemDao(): FavoriteItemDao

    companion object {
        const val VERSION = 2
        const val NAME = "electoolkit.db"
    }
}
