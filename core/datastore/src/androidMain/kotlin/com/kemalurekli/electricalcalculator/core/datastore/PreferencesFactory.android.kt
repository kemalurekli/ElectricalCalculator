package com.kemalurekli.electricalcalculator.core.datastore

import android.content.Context

/**
 * Set once from the Application, for the same reason the database context is —
 * a `Context` cannot travel through a common signature, and threading one
 * through every call that might reach storage would put Android in the domain.
 */
lateinit var preferencesContext: Context

internal actual fun preferencesFilePath(fileName: String): String {
    check(::preferencesContext.isInitialized) {
        "preferencesContext must be set before preferences are read"
    }
    return preferencesContext.filesDir.resolve("datastore/$fileName").absolutePath
}
