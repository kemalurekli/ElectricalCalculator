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
