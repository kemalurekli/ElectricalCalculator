package com.kemalurekli.electricalcalculator.core.datastore

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Documents, unlike the database.
 *
 * The database is bookkeeping the user never sees; their settings are small and
 * worth carrying to a restored device, and Documents is what iCloud backs up
 * by default.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun preferencesFilePath(fileName: String): String {
    val directory: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    ) ?: error("no documents directory")

    return requireNotNull(directory.path) + "/" + fileName
}
