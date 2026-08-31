package com.kemalurekli.electricalcalculator.core.billing.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The last answer the store gave, kept on disk.
 *
 * The app is offline-first and gets used in places with no signal. Without this
 * a paying reader who opens it in a basement is told they have not paid, for as
 * long as the store takes to reply — which offline is forever.
 *
 * In its own file rather than in the user's settings: a purchase is not a
 * setting. Nobody edits it, and clearing one should not clear the other.
 *
 * It is a cache and not a source of truth. The store is asked again on every
 * launch, and its answer replaces this one. Editing the file by hand would
 * unlock the app until the next successful refresh — which is true of every
 * client-side entitlement check, and is why nothing behind this gate is
 * a secret rather than a convenience.
 */
internal class EntitlementCache(private val dataStore: DataStore<Preferences>) {

    val isPro: Flow<Boolean> = dataStore.data.map { it[IS_PRO] == true }

    suspend fun set(isPro: Boolean) {
        dataStore.edit { it[IS_PRO] = isPro }
    }

    private companion object {
        val IS_PRO = booleanPreferencesKey("is_pro")
    }
}

/** Kept apart from `user_preferences`; see [EntitlementCache]. */
internal const val ENTITLEMENTS_FILE = "entitlements.preferences_pb"
