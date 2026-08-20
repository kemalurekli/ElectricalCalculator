package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Reads and writes the user's persisted settings. */
interface UserPreferencesRepository {

    /**
     * The current preferences, re-emitting on every change.
     *
     * Never completes and never throws: read errors fall back to
     * [UserPreferences.Default] so the UI always has a scheme to render.
     */
    val preferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setUnitSystem(unitSystem: UnitSystem)

    /** Records that the reader has acknowledged the disclaimer. */
    suspend fun setDisclaimerAccepted(accepted: Boolean)

    /** Records that the reader has acknowledged the forum rules. */
    suspend fun setForumRulesAccepted(accepted: Boolean)

    /** Pins or unpins a forum thread, on this device only. */
    suspend fun setThreadPinned(threadId: String, pinned: Boolean)

    /** Replaces the engineering defaults, and marks them as the user's. */
    suspend fun setEngineeringDefaults(defaults: EngineeringDefaults)

    /**
     * Writes a locale-derived first guess, but only on an install that has
     * never had one.
     *
     * Safe to call on every launch: after the first, it is a no-op. That is the
     * whole point — a language change re-runs the startup path and must not
     * move values the user now owns.
     */
    suspend fun seedEngineeringDefaults(regionCode: String)

    /**
     * The one-shot read a calculator uses to open its form.
     *
     * Separate from [preferences] because a calculator wants the values as they
     * are when it opens and must not have its form rewritten underneath the
     * user if Settings changes them while it is on the back stack.
     */
    suspend fun engineeringDefaults(): EngineeringDefaults =
        preferences.map { it.engineering }.first()
}
