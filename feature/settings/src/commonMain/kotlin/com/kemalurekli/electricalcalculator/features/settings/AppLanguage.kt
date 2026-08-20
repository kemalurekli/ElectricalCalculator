package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository

/**
 * The platform's answer to "change the display language".
 *
 * Android sets a per-app locale through `AppCompatDelegate` and recreates the
 * activities. iOS has no equivalent and does not want one: per-app language
 * lives in Settings, one screen per app, and an in-app picker would be a second
 * place to set the same thing — with the two able to disagree.
 *
 * So the iOS implementation reports the language and refuses to change it, and
 * the screen offers a way into Settings instead of radio buttons. See
 * [com.kemalurekli.electricalcalculator.core.common.util.canChangeLanguageInApp].
 */
expect fun createAppLanguageRepository(): AppLanguageRepository

/**
 * Opens this app's page in the system settings.
 *
 * Android never calls it: the picker is in the app. On iOS it is the only way
 * to change the language, so the row that says so has to lead somewhere.
 */
expect fun openSystemLanguageSettings()
