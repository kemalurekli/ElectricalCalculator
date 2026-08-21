package com.kemalurekli.electricalcalculator.core.common.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * Set once from the Application, like the database and preference contexts —
 * a `Context` cannot travel through a common signature.
 */
lateinit var appInfoContext: Context

/**
 * Empty rather than a check, unlike the database and preference contexts.
 *
 * Those throw because using them unset would open the wrong file; this is one
 * line on a settings screen. A unit test constructing the UI state has no
 * package manager and should not have to care.
 */
actual fun appVersionName(): String {
    if (!::appInfoContext.isInitialized) return ""
    return runCatching {
        appInfoContext.packageManager
            .getPackageInfo(appInfoContext.packageName, PackageManager.PackageInfoFlags.of(0))
            .versionName
    }.getOrNull().orEmpty()
}
