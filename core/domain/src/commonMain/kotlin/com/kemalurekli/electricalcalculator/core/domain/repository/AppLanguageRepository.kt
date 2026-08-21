package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import kotlinx.coroutines.flow.StateFlow

/**
 * Reads and writes the app's display language.
 *
 * Note this is *not* backed by the app's own DataStore, unlike every other
 * preference. Both platforms own the per-app language themselves and expose it
 * in their own settings — Android 13+ under the app's entry, iOS under the
 * Language row — so the reader can change it from entirely outside this app.
 * Storing a second copy would let the two disagree the moment that happens, so
 * the platform remains the single source of truth and this contract only
 * reflects it.
 */
interface AppLanguageRepository {

    /** The active language, updating when it is changed from inside the app. */
    val language: StateFlow<AppLanguage>

    /**
     * Applies [language].
     *
     * Every string is re-resolved as a result — Android recreates the running
     * activities, and on iOS `ElecToolkitApp` restarts the composition when
     * [language] changes. Callers do not need to refresh anything themselves.
     */
    fun setLanguage(language: AppLanguage)
}
