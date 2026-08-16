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

/**
 * Projects arrive, and with them a place to keep a job rather than a pile of
 * one-off calculations.
 *
 * Purely additive: nothing existing is touched, so a user who never opens a
 * project sees no change at all. Circuits cascade on delete because a circuit
 * has no meaning without the supply parameters it was designed against.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `projects` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `reference` TEXT NOT NULL,
                `site` TEXT NOT NULL,
                `system` TEXT NOT NULL,
                `system_voltage` TEXT NOT NULL,
                `material` TEXT NOT NULL,
                `insulation` TEXT NOT NULL,
                `method` TEXT NOT NULL,
                `ambient_temperature_c` TEXT NOT NULL,
                `max_voltage_drop_percent` TEXT NOT NULL,
                `external_impedance_ohms` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_updated_at` ON `projects` (`updated_at`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `circuits` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `project_id` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `load_kind` TEXT NOT NULL,
                `load` TEXT NOT NULL,
                `power_factor` TEXT NOT NULL,
                `length_metres` TEXT NOT NULL,
                `grouped_circuits` TEXT NOT NULL,
                `parallel_conductors` TEXT NOT NULL,
                `device_type` TEXT NOT NULL,
                `disconnection_time_seconds` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                FOREIGN KEY(`project_id`) REFERENCES `projects`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_circuits_project_id` ON `circuits` (`project_id`)")
    }
}

/**
 * Somewhere to write down what was measured.
 *
 * Additive again: an installation designed before this release keeps every
 * circuit it had, with no readings against them, which is exactly what was true
 * before the table existed.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `circuit_tests` (
                `circuit_id` INTEGER NOT NULL,
                `kind` TEXT NOT NULL,
                `value` TEXT NOT NULL,
                `passed` INTEGER,
                `insulation_voltage` TEXT NOT NULL,
                `rcd_type` TEXT NOT NULL,
                PRIMARY KEY(`circuit_id`, `kind`),
                FOREIGN KEY(`circuit_id`) REFERENCES `circuits`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_circuit_tests_circuit_id` ON `circuit_tests` (`circuit_id`)",
        )
    }
}
