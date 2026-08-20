package com.kemalurekli.electricalcalculator.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Set once from the Application before anything opens the database.
 *
 * A `Context` is not something the shared code can be given through a common
 * signature, and threading one through every call that might eventually reach
 * storage would put Android in the domain layer. This keeps the platform
 * detail on the platform side of the boundary.
 */
lateinit var databaseContext: Context

internal actual fun databaseBuilder(): RoomDatabase.Builder<ElecToolkitDatabase> {
    check(::databaseContext.isInitialized) {
        "databaseContext must be set before the database is opened"
    }
    return Room.databaseBuilder(
        context = databaseContext,
        name = databaseContext.getDatabasePath(ElecToolkitDatabase.NAME).absolutePath,
    )
}
