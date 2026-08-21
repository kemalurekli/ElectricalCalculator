package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual fun createAppLanguageRepository(): AppLanguageRepository = IosAppLanguageRepository()

/**
 * Backs [AppLanguageRepository] with `AppleLanguages`.
 *
 * `AppleLanguages` in the app's own defaults domain is not a private key this
 * app invented — it is where iOS itself stores a per-app language, and what the
 * Language row in Settings writes. Using it means the picker in this app and
 * the row in Settings are the *same* setting rather than two that can disagree:
 * change it in either place and the other shows the new value.
 *
 * It is also what makes the choice stick. `NSLocale.preferredLanguages` is
 * assembled from this key, and Compose Resources resolves `values-*` against
 * `preferredLanguages` — so the next launch comes up in the chosen language
 * with nothing else having to remember it. Removing the key restores "follow
 * the phone", which is not the same as choosing English.
 *
 * The write takes effect in this process immediately, so the running UI can be
 * restarted into the new language rather than waiting for a relaunch. See
 * `ElecToolkitApp`, which is where that happens.
 */
internal class IosAppLanguageRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val domain: String = NSBundle.mainBundle.bundleIdentifier.orEmpty(),
) : AppLanguageRepository {

    private val state = MutableStateFlow(readStoredLanguage())
    override val language: StateFlow<AppLanguage> = state.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        val tag = language.languageTag
        if (tag == null) {
            defaults.removeObjectForKey(APPLE_LANGUAGES)
        } else {
            defaults.setObject(listOf(tag), forKey = APPLE_LANGUAGES)
        }
        state.value = language
    }

    /**
     * Reads the app's *own* choice, not the language it happens to be showing.
     *
     * Deliberately `persistentDomainForName` rather than `objectForKey`: the
     * standard defaults search list falls through to the global domain, where
     * every device has an `AppleLanguages` list. Reading through it would make
     * "follow the phone" indistinguishable from "the reader picked this", and
     * the picker would show a language nobody chose.
     */
    private fun readStoredLanguage(): AppLanguage {
        val stored = defaults.persistentDomainForName(domain)?.get(APPLE_LANGUAGES)
        return AppLanguage.fromTagOrSystem((stored as? List<*>)?.firstOrNull() as? String)
    }

    private companion object {
        const val APPLE_LANGUAGES = "AppleLanguages"
    }
}
