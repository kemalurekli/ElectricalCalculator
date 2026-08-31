package com.kemalurekli.electricalcalculator.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath

/** Where a preferences file lives, which is the only thing the platforms differ on. */
internal expect fun preferencesFilePath(fileName: String): String

/** The settings file, shared so that neither platform can quietly pick its own. */
const val PREFERENCES_FILE: String = "user_preferences.preferences_pb"

/**
 * Opens the preferences store.
 *
 * The corruption handler is the part worth keeping in common code: a damaged
 * file resets to defaults instead of throwing on every launch, which would
 * otherwise leave a user with an app that cannot start and no way to fix it
 * short of reinstalling. That is a decision about the product, not about a
 * platform, so both get it.
 *
 * @param scope the scope writes are performed in. Supplied by the caller
 *   because its lifetime is the application's, which is something only the
 *   platform side knows how to build.
 * @param fileName which store to open. Defaults to the user's settings. The
 *   entitlement cache asks for its own, because a purchase is not a setting:
 *   clearing one must not clear the other, and the two are written by different
 *   things at different times.
 */
fun createPreferencesDataStore(
    scope: CoroutineScope,
    fileName: String = PREFERENCES_FILE,
): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
        produceFile = { preferencesFilePath(fileName).toPath() },
    )
