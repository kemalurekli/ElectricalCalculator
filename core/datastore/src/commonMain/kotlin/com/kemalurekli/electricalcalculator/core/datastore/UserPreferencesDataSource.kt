package com.kemalurekli.electricalcalculator.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.random.Random

/**
 * 32 hex characters from the platform's random source.
 *
 * Hand-rolled rather than `Uuid.random()` because that is still experimental in
 * common code, and what is wanted here is thirty-two characters that nobody can
 * guess — not a UUID's version bits.
 */
private fun randomInstallId(): String = buildString(32) {
    repeat(16) {
        val byte = Random.nextInt(256)
        append(HEX[byte shr 4])
        append(HEX[byte and 0x0F])
    }
}

private const val HEX = "0123456789abcdef"

/**
 * Reads and writes settings to Preferences DataStore.
 *
 * Enums are stored by `name` rather than ordinal so that reordering the enum
 * cannot silently change a stored preference. An unrecognised name — from a
 * downgrade, or a constant removed in a later version — falls back to the
 * default instead of throwing.
 */
class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch {
            // DataStore surfaces read failures through the flow. Recovering
            // with defaults keeps a corrupt file from making the app
            // unlaunchable — which was already the behaviour; it used to check
            // for java.io.IOException first, and there is no common equivalent
            // to check for. Every failure reaching here is a failure to read a
            // settings file, and the answer to all of them is the same.
            emit(emptyPreferences())
        }
        .map { it.toUserPreferences() }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { it[Keys.UNIT_SYSTEM] = unitSystem.name }
    }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        dataStore.edit { it[Keys.DISCLAIMER_ACCEPTED] = accepted }
    }

    suspend fun setForumRulesAccepted(accepted: Boolean) {
        dataStore.edit { it[Keys.FORUM_RULES_ACCEPTED] = accepted }
    }

    /**
     * Pins or unpins a thread, on this device only.
     *
     * Deliberately not on the server. Pinning is one reader keeping track of
     * one conversation; it is nobody else's business, it needs no account, and
     * a thread somebody is following should not stop being followed because
     * they are offline.
     */
    suspend fun setThreadPinned(threadId: String, pinned: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_THREADS].orEmpty()
            prefs[Keys.PINNED_THREADS] =
                if (pinned) current + threadId else current - threadId
        }
    }

    /**
     * Writes engineering defaults, marking them as the user's from now on.
     *
     * The seeded flag is set by every write, not only by seeding: a user who
     * edits a value in Settings has just as surely taken ownership of it as one
     * who accepted the first-run guess, and neither should ever be re-derived
     * from a locale.
     */
    suspend fun setEngineeringDefaults(defaults: EngineeringDefaults) {
        dataStore.edit {
            it[Keys.ENG_SINGLE_PHASE_VOLTAGE] = defaults.singlePhaseVoltage
            it[Keys.ENG_THREE_PHASE_VOLTAGE] = defaults.threePhaseVoltage
            it[Keys.ENG_FREQUENCY] = defaults.frequency
            it[Keys.ENG_AMBIENT_TEMPERATURE] = defaults.ambientTemperature
            it[Keys.ENG_MATERIAL] = defaults.material.name
            it[Keys.ENG_INSULATION] = defaults.insulation.name
            it[Keys.ENG_INSTALLATION_METHOD] = defaults.installationMethod.name
            it[Keys.ENG_SEEDED] = true
        }
    }

    /**
     * Writes [defaults] only if nothing has been written before.
     *
     * One `edit` block rather than a read followed by a write, so two callers
     * racing at startup cannot both see "unseeded" and both write: DataStore
     * serialises the transform, and the second one finds the flag already set.
     */
    suspend fun seedEngineeringDefaultsIfUnset(defaults: EngineeringDefaults) {
        dataStore.edit {
            if (it[Keys.ENG_SEEDED] == true) return@edit
            it[Keys.ENG_SINGLE_PHASE_VOLTAGE] = defaults.singlePhaseVoltage
            it[Keys.ENG_THREE_PHASE_VOLTAGE] = defaults.threePhaseVoltage
            it[Keys.ENG_FREQUENCY] = defaults.frequency
            it[Keys.ENG_AMBIENT_TEMPERATURE] = defaults.ambientTemperature
            it[Keys.ENG_MATERIAL] = defaults.material.name
            it[Keys.ENG_INSULATION] = defaults.insulation.name
            it[Keys.ENG_INSTALLATION_METHOD] = defaults.installationMethod.name
            it[Keys.ENG_SEEDED] = true
        }
    }

    /**
     * A random id for this installation, made the first time it is asked for.
     *
     * Not an identity and not a device id. It is attached to no name, survives
     * no reinstall, and is read by exactly one thing: an error report, so that
     * the server can refuse the sixth one in an hour and so that twenty reports
     * of the same fault can be told apart from one person pressing send twenty
     * times.
     *
     * Written inside the same `edit` block that reads it, so two callers racing
     * at startup cannot mint two ids — DataStore serialises the transform, and
     * the second one finds the first one's value.
     */
    suspend fun installId(): String {
        var id = ""
        dataStore.edit { prefs ->
            id = prefs[Keys.INSTALL_ID] ?: randomInstallId().also { prefs[Keys.INSTALL_ID] = it }
        }
        return id
    }

    private fun Preferences.toUserPreferences() = UserPreferences(
        themeMode = enumOrDefault(this[Keys.THEME_MODE], UserPreferences.Default.themeMode),
        unitSystem = enumOrDefault(this[Keys.UNIT_SYSTEM], UserPreferences.Default.unitSystem),
        engineering = toEngineeringDefaults(),
        engineeringSeeded = this[Keys.ENG_SEEDED] ?: false,
        disclaimerAccepted = this[Keys.DISCLAIMER_ACCEPTED] ?: false,
        forumRulesAccepted = this[Keys.FORUM_RULES_ACCEPTED] ?: false,
        pinnedThreadIds = this[Keys.PINNED_THREADS].orEmpty(),
    )

    private fun Preferences.toEngineeringDefaults(): EngineeringDefaults {
        val fallback = EngineeringDefaults.Default
        return EngineeringDefaults(
            singlePhaseVoltage = this[Keys.ENG_SINGLE_PHASE_VOLTAGE] ?: fallback.singlePhaseVoltage,
            threePhaseVoltage = this[Keys.ENG_THREE_PHASE_VOLTAGE] ?: fallback.threePhaseVoltage,
            frequency = this[Keys.ENG_FREQUENCY] ?: fallback.frequency,
            ambientTemperature = this[Keys.ENG_AMBIENT_TEMPERATURE]
                ?: fallback.ambientTemperature,
            material = enumOrDefault(this[Keys.ENG_MATERIAL], fallback.material),
            insulation = enumOrDefault(this[Keys.ENG_INSULATION], fallback.insulation),
            installationMethod = enumOrDefault(
                this[Keys.ENG_INSTALLATION_METHOD],
                fallback.installationMethod,
            ),
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        name?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")

        // "dynamic_color" was written here until the wallpaper-derived scheme
        // was dropped for the iOS port. Nothing reads it now; the orphaned
        // boolean is left in existing stores rather than migrated out, because
        // a migration that deletes one unused key is more code than the key.

        val ENG_SINGLE_PHASE_VOLTAGE = stringPreferencesKey("eng_single_phase_voltage")
        val ENG_THREE_PHASE_VOLTAGE = stringPreferencesKey("eng_three_phase_voltage")
        val ENG_FREQUENCY = stringPreferencesKey("eng_frequency")
        val ENG_AMBIENT_TEMPERATURE = stringPreferencesKey("eng_ambient_temperature")
        val ENG_MATERIAL = stringPreferencesKey("eng_material")
        val ENG_INSULATION = stringPreferencesKey("eng_insulation")
        val ENG_INSTALLATION_METHOD = stringPreferencesKey("eng_installation_method")
        val ENG_SEEDED = booleanPreferencesKey("eng_seeded")
        val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        val FORUM_RULES_ACCEPTED = booleanPreferencesKey("forum_rules_accepted")
        val PINNED_THREADS = stringSetPreferencesKey("pinned_forum_threads")
        val INSTALL_ID = stringPreferencesKey("install_id")
    }

}
