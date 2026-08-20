package com.kemalurekli.electricalcalculator.core.data.repository

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
class AppLanguageRepositoryImpl() : AppLanguageRepository {

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
     * Reads the language the platform currently has applied.
     *
     * Done on construction rather than cached at build time so that a change
     * made from system Settings — outside this app entirely — is reflected the
     * next time the graph is created.
     */
    private fun readCurrentLanguage(): AppLanguage =
        AppLanguage.fromTagOrSystem(
            AppCompatDelegate.getApplicationLocales()
                .takeUnless { it.isEmpty }
                ?.get(0)
                ?.toLanguageTag(),
        )
}
