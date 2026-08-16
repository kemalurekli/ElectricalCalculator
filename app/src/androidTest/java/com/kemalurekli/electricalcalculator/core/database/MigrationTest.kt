package com.kemalurekli.electricalcalculator.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The schema steps, run against the real exported schemas.
 *
 * A migration is the one change in this app that can destroy something the user
 * made. The pinned shelf and the saved calculations are all the data there is,
 * and "they can pin them again" is not a migration — so each step is checked
 * here for carrying its rows across, not merely for leaving a valid schema
 * behind.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ElecToolkitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun pinnedCalculatorsSurviveTheMoveToGeneralFavourites() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO favorite_calculators (calculator_id, pinned_at) " +
                    "VALUES ('voltage_drop', 111), ('cable_size', 222)",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query(
            "SELECT kind, item_key, pinned_at FROM favorite_items ORDER BY pinned_at ASC",
        ).use { cursor ->
            assertEquals("both pins carried over", 2, cursor.count)

            assertTrue(cursor.moveToFirst())
            assertEquals("CALCULATOR", cursor.getString(0))
            assertEquals("voltage_drop", cursor.getString(1))
            // The order the shelf was built in is part of what was pinned.
            assertEquals(111L, cursor.getLong(2))

            assertTrue(cursor.moveToNext())
            assertEquals("cable_size", cursor.getString(1))
            assertEquals(222L, cursor.getLong(2))
        }
    }

    @Test
    fun theOldTableIsGone() {
        helper.createDatabase(TEST_DB, 1).close()
        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'favorite_calculators'",
        ).use { cursor ->
            assertFalse("the calculator-only table should not outlive the migration", cursor.moveToFirst())
        }
    }

    @Test
    fun savedCalculationsAreUntouched() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO calculation_history " +
                    "(calculator_id, title, summary, inputs, results, created_at) " +
                    "VALUES ('power', 'A title', 'A summary', '{}', '{}', 999)",
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT title, created_at FROM calculation_history").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("A title", cursor.getString(0))
            assertEquals(999L, cursor.getLong(1))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
