package com.kemalurekli.electricalcalculator.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Opens the database, wherever it lives.
 *
 * The two platforms disagree about one thing only: where the file goes.
 * Android resolves it from a `Context`, iOS from the application-support
 * directory. Everything after that — the driver, the migrations, the
 * coroutine context queries run on — is the same for both, and is applied
 * here so it cannot drift apart.
 *
 * [BundledSQLiteDriver] ships its own SQLite rather than using the one on the
 * device. That is the point: a query that behaves one way on Android 9 and
 * another on iOS 18 is a bug nobody can reproduce, and the few hundred
 * kilobytes buy the same engine everywhere.
 */
internal expect fun databaseBuilder(): RoomDatabase.Builder<ElecToolkitDatabase>

/**
 * The application's database, configured identically on both platforms.
 *
 * Migrations are passed explicitly rather than relying on destructive
 * fallback: a user who has kept projects across several releases must not lose
 * them to a schema change.
 *
 * The query dispatcher is left at Room's default. `Dispatchers.IO` does not
 * exist in common code — it is a JVM notion of a thread pool sized for blocking
 * work — and Room's own default already moves queries off the caller's thread
 * on every platform.
 */
fun createElecToolkitDatabase(): ElecToolkitDatabase =
    databaseBuilder()
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .setDriver(BundledSQLiteDriver())
        .build()
