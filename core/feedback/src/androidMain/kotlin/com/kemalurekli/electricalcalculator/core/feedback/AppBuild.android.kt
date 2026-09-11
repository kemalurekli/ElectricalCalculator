package com.kemalurekli.electricalcalculator.core.feedback.domain

import android.content.Context

/**
 * Set once from the Application, the way `preferencesContext` is and for the
 * same reason: a `Context` cannot travel through a common signature.
 */
lateinit var feedbackContext: Context

/**
 * Read from the package manager rather than from `BuildConfig`.
 *
 * `BuildConfig` belongs to whichever module generates it, and this one has no
 * version of its own — it would report the library's, which is blank.
 *
 * Returns an empty version rather than throwing if the Application never set
 * the context. A report that does not say which build it came from is worth
 * having; a crash on the way to sending one is not.
 */
actual fun appBuild(): AppBuild = AppBuild(
    version = runCatching {
        val context = feedbackContext
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault(""),
    platform = "android",
)
