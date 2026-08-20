package com.kemalurekli.electricalcalculator.core.common.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * `LocaleManager.getSystemLocales()` is the one reading that survives a per-app
 * locale: it reports the device's setting whatever the app has been switched
 * to. It arrived in Android 13, the same release that introduced per-app
 * locales in the first place.
 *
 * Below that the backport replaces the process default, so there is nothing
 * left in the process that still remembers the device's region and
 * `Locale.getDefault()` is the closest available answer. The cost is bounded: a
 * pre-Android-13 user who has both picked an in-app language and pressed reset
 * gets the majority values and edits the two fields that are wrong for them.
 *
 * Takes a `Context` rather than being an `expect fun` because Android has
 * nothing to read it from otherwise; the iOS side needs no such handle, which
 * is why the platform split is at the implementation and not the interface.
 */
/**
 * Set once from the Application, beside the database and preference contexts.
 */
lateinit var regionContext: Context

actual fun createRegionProvider(): RegionProvider {
    check(::regionContext.isInitialized) { "regionContext must be set before the region is read" }
    return PlatformRegionProvider(regionContext)
}

class PlatformRegionProvider(private val context: Context) : RegionProvider {

    override fun currentRegion(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val system = context.getSystemService(LocaleManager::class.java)?.systemLocales
            if (system != null && !system.isEmpty) return system[0].country
        }
        return Locale.getDefault().country
    }
}
