package com.kemalurekli.electricalcalculator.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabaseConstructor
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kemalurekli.electricalcalculator.core.database.converter.StringMapConverter
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitTestDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitEntity
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitTestEntity
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import com.kemalurekli.electricalcalculator.core.database.entity.ProjectEntity

/**
 * The application database.
 *
 * Schemas are exported to `core/database/schemas` so that every version is
 * committed alongside the code and future migrations can be verified against
 * the real prior schema in tests.
 *
 * ### On [Constructor]
 *
 * Room generates its implementation class per platform. On Android it could
 * find that class by reflection; Kotlin/Native has none, so multiplatform Room
 * requires the link to be declared. That is all [Constructor] is — an
 * `expect object` Room fills in for each target, named here so the compiler
 * checks it exists rather than the app discovering at launch that it does not.
 */
@Database(
    entities = [
        CalculationHistoryEntity::class,
        FavoriteItemEntity::class,
        ProjectEntity::class,
        CircuitEntity::class,
        CircuitTestEntity::class,
    ],
    version = ElecToolkitDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(StringMapConverter::class)
@ConstructedBy(ElecToolkitDatabaseConstructor::class)
abstract class ElecToolkitDatabase : RoomDatabase() {

    abstract fun calculationHistoryDao(): CalculationHistoryDao

    abstract fun favoriteItemDao(): FavoriteItemDao

    abstract fun projectDao(): ProjectDao

    abstract fun circuitDao(): CircuitDao

    abstract fun circuitTestDao(): CircuitTestDao

    companion object {
        const val VERSION = 4
        const val NAME = "electoolkit.db"
    }
}

/**
 * The per-platform implementation Room generates.
 *
 * `expect` with no `actual` anywhere in this repository is deliberate and is
 * not a mistake: Room's compiler writes the `actual` for every target it runs
 * against. The `@Suppress` is what the Room documentation prescribes, because
 * the Kotlin compiler cannot see a declaration that does not exist yet.
 */
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object ElecToolkitDatabaseConstructor : RoomDatabaseConstructor<ElecToolkitDatabase> {
    override fun initialize(): ElecToolkitDatabase
}
