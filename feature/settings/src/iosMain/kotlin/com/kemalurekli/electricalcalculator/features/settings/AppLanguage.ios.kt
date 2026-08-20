package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.common.util.currentLanguageTag
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * Reports the language iOS is showing, and cannot change it.
 *
 * Not a stub: `setLanguage` does nothing because there is nothing for it to do.
 * Changing the language means going to Settings, and
 * [openSystemLanguageSettings] is how the screen gets the reader there.
 */
actual fun createAppLanguageRepository(): AppLanguageRepository = IosAppLanguageRepository()

private class IosAppLanguageRepository : AppLanguageRepository {

    private val state = MutableStateFlow(
        AppLanguage.entries.firstOrNull { it.languageTag == currentLanguageTag() }
            ?: AppLanguage.SYSTEM,
    )

    override val language: StateFlow<AppLanguage> = state.asStateFlow()

    override fun setLanguage(language: AppLanguage) = Unit
}

actual fun openSystemLanguageSettings() {
    NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { url ->
        UIApplication.sharedApplication.openURL(url)
    }
}
