package com.kemalurekli.electricalcalculator.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema changes, one object per step.
 *
 * Every migration carries the data forward. A user who has pinned calculators
 * across several releases should not find the shelf empty after an update, and
 * "they can just pin them again" is not a migration.
 */

/**
 * Favourites stop being calculator-only.
 *
 * The old table held one column of calculator keys. The new one holds a kind
 * alongside the key so a reference topic, a glossary term, a general-info note
 * or a theory topic can be pinned too. Existing rows carry over as `CALCULATOR`
 * with their pinned-at time intact, so the order the shelf was built in
 * survives.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `favorite_items` (
                `kind` TEXT NOT NULL,
                `item_key` TEXT NOT NULL,
                `pinned_at` INTEGER NOT NULL,
                PRIMARY KEY(`kind`, `item_key`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR REPLACE INTO `favorite_items` (`kind`, `item_key`, `pinned_at`)
            SELECT 'CALCULATOR', `calculator_id`, `pinned_at` FROM `favorite_calculators`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `favorite_calculators`")
    }
}
