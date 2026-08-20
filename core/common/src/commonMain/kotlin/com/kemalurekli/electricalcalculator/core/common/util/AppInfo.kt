package com.kemalurekli.electricalcalculator.core.common.util

/**
 * The version the settings screen shows.
 *
 * `BuildConfig.VERSION_NAME` is an Android build artefact. Each platform reads
 * its own manifest — `PackageManager` on Android, `CFBundleShortVersionString`
 * on iOS — rather than a value generated into shared code, because both are
 * already single sources and generating a third would give two places for the
 * number to be wrong in.
 */
expect fun appVersionName(): String

/**
 * Whether the app can change its own display language.
 *
 * True on Android, where `AppCompatDelegate.setApplicationLocales` sets a
 * per-app locale and recreates the activities.
 *
 * False on iOS, and not for want of an API — iOS puts per-app language in
 * Settings itself, one screen per app, and an in-app picker would be a second
 * place to set the same thing. The settings screen offers a way *there*
 * instead.
 */
expect val canChangeLanguageInApp: Boolean
