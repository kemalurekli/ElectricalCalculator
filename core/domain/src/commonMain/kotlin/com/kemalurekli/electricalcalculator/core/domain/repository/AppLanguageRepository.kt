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

    /**
     * Re-reads the language the platform has applied.
     *
     * Both platforms let the reader change this from the phone's own settings,
     * without the app being involved — and they differ in what that costs. iOS
     * terminates the app, so the next launch reads the new value on the way up
     * and there is nothing to refresh. Android only recreates the activity: the
     * process, and everything held in it including this, survives with the
     * value it read at startup.
     *
     * So this is Android's alone to implement, and it does nothing elsewhere
     * rather than being a platform seam of its own — a whole `expect` for one
     * platform's housekeeping would cost more to read than it saves.
     */
    fun refresh() = Unit
}
