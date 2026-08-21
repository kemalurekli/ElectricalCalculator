package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository

/**
 * The platform's answer to "change the display language".
 *
 * Both platforms have one, and both keep it where the system keeps it rather
 * than in the app's own store: `AppCompatDelegate` on Android, the
 * `AppleLanguages` default on iOS. The picker in settings is the same picker on
 * both, because the reader's expectation is the same on both — the app opens in
 * the phone's language until they say otherwise, and in theirs after.
 */
expect fun createAppLanguageRepository(): AppLanguageRepository
