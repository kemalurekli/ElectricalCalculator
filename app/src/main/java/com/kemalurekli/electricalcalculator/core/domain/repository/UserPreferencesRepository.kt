package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

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

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setUnitSystem(unitSystem: UnitSystem)
}
