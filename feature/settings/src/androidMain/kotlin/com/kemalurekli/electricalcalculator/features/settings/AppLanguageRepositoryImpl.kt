package com.kemalurekli.electricalcalculator.features.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Backs [AppLanguageRepository] with `AppCompatDelegate`.
 *
 * `AppCompatDelegate` is the one API that spans both mechanisms: on Android 13+
 * it delegates to the platform `LocaleManager`, and below that it applies and
 * persists the locale itself through the `AppLocalesMetadataHolderService`
 * declared in the manifest. Using it rather than the platform API directly is
 * what lets this work down to minSdk 28.
 */
internal class AppLanguageRepositoryImpl() : AppLanguageRepository {

    private val _language = MutableStateFlow(readCurrentLanguage())
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        val locales = language.languageTag
            ?.let { LocaleListCompat.forLanguageTags(it) }
            // An empty list restores "follow the system", which is not the same
            // as forcing English.
            ?: LocaleListCompat.getEmptyLocaleList()

        AppCompatDelegate.setApplicationLocales(locales)
        _language.value = language
    }

    /**
     * Re-reads what the platform has applied, and tells anyone listening.
     *
     * Called when an activity is created, which is what Android does after the
     * reader changes the language from system Settings. The graph is not
     * rebuilt for that — this object is a singleton and outlives it — so
     * without asking again the app would go on serving the language it started
     * with. The forum is where that showed: it picks its board from here.
     */
    override fun refresh() {
        _language.value = readCurrentLanguage()
    }

    /** The language the platform currently has applied, or none. */
    private fun readCurrentLanguage(): AppLanguage =
        AppLanguage.fromTagOrSystem(
            AppCompatDelegate.getApplicationLocales()
                .takeUnless { it.isEmpty }
                ?.get(0)
                ?.toLanguageTag(),
        )
}
