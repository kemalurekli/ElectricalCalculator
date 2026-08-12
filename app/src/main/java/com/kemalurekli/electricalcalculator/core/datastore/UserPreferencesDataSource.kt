package com.kemalurekli.electricalcalculator.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and writes settings to Preferences DataStore.
 *
 * Enums are stored by `name` rather than ordinal so that reordering the enum
 * cannot silently change a stored preference. An unrecognised name — from a
 * downgrade, or a constant removed in a later version — falls back to the
 * default instead of throwing.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            // DataStore surfaces read failures through the flow. Recovering with
            // defaults keeps a corrupt file from making the app unlaunchable.
            if (throwable is IOException) {
                Log.e(TAG, "Failed to read user preferences, falling back to defaults", throwable)
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { it.toUserPreferences() }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { it[Keys.UNIT_SYSTEM] = unitSystem.name }
    }

    private fun Preferences.toUserPreferences() = UserPreferences(
        themeMode = enumOrDefault(this[Keys.THEME_MODE], UserPreferences.Default.themeMode),
        useDynamicColor = this[Keys.DYNAMIC_COLOR] ?: UserPreferences.Default.useDynamicColor,
        unitSystem = enumOrDefault(this[Keys.UNIT_SYSTEM], UserPreferences.Default.unitSystem),
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        name?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    }

    private companion object {
        const val TAG = "UserPreferences"
    }
}
