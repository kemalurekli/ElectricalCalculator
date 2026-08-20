package com.kemalurekli.electricalcalculator.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Application Support, not Documents.
 *
 * iOS backs both up, but Documents is the directory a user can be shown in the
 * Files app. A database is the app's own bookkeeping, not a document somebody
 * opened, so it belongs where the system puts private state.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun databaseBuilder(): RoomDatabase.Builder<ElecToolkitDatabase> {
    val directory: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    ) ?: error("no application support directory")

    return Room.databaseBuilder<ElecToolkitDatabase>(
        name = requireNotNull(directory.path) + "/" + ElecToolkitDatabase.NAME,
    )
}
