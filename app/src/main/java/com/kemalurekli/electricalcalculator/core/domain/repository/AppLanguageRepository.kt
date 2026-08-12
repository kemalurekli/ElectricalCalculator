package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import kotlinx.coroutines.flow.StateFlow

/**
 * Reads and writes the app's display language.
 *
 * Note this is *not* backed by the app's own DataStore, unlike every other
 * preference. Android owns the per-app language: on Android 13+ the user can
 * change it from system Settings, entirely outside this app. Storing a second
 * copy would let the two disagree the moment that happens, so the platform
 * remains the single source of truth and this contract only reflects it.
 */
interface AppLanguageRepository {

    /** The active language, updating when it is changed from inside the app. */
    val language: StateFlow<AppLanguage>

    /**
     * Applies [language].
     *
     * Changing the locale recreates the running activities so every string is
     * re-resolved; callers do not need to refresh anything themselves.
     */
    fun setLanguage(language: AppLanguage)
}
