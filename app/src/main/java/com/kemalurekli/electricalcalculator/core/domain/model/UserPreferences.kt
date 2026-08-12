package com.kemalurekli.electricalcalculator.core.domain.model

/** How the app resolves light versus dark colours. */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

/** Which measurement conventions calculators default to. */
enum class UnitSystem {
    /** mm² conductors, metres, °C. */
    METRIC,

    /** AWG conductors, feet, °F. */
    IMPERIAL,
}

/**
 * The user's persisted settings.
 *
 * Modelled as one immutable snapshot so the UI observes a single value and
 * cannot render a half-applied combination of preferences.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
) {
    companion object {
        val Default = UserPreferences()
    }
}
