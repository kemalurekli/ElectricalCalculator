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
    /** What every calculator opens with. See [EngineeringDefaults]. */
    val engineering: EngineeringDefaults = EngineeringDefaults.Default,
    /**
     * Whether [engineering] has ever been written.
     *
     * False means the values above are the compile-time fallbacks and a
     * locale-derived guess is still allowed to replace them. Once true, nothing
     * derives them from the environment again — a later language change finds
     * this already set and leaves the values alone.
     */
    val engineeringSeeded: Boolean = false,
    /**
     * Whether the reader has acknowledged the disclaimer.
     *
     * Stored rather than shown every launch: it is an acknowledgement, and one
     * that reappears each time reads as a bug and gets dismissed unread.
     */
    val disclaimerAccepted: Boolean = false,
    /**
     * Whether the forum rules have been acknowledged.
     *
     * Separate from [disclaimerAccepted] because they cover different things:
     * that one is about the app's own calculations, this one is about content
     * other readers wrote. Someone who accepted the first has not been told
     * anything about the second.
     */
    val forumRulesAccepted: Boolean = false,
) {
    companion object {
        val Default = UserPreferences()
    }
}
