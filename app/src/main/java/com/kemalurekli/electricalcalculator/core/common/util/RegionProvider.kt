package com.kemalurekli.electricalcalculator.core.common.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the device is, as distinct from what language the app is showing.
 *
 * ### Why this is not `Locale.getDefault()`
 *
 * Choosing an in-app language sets a per-app locale, and from that point the
 * process default *is* that locale — the platform overrides it on Android 13
 * and above, and AppCompat's backport does the same below. The app's own picker
 * stores plain `en` and `tr`, language subtags with no region, so anything
 * reading the default to find out *where the user is* gets nothing back the
 * moment they pick a language and quietly falls to the majority answer.
 *
 * That is wrong for the one thing the region is used for. An engineer in Texas
 * who prefers the app in Turkish is still on a 120 V supply, and resetting the
 * engineering defaults must give them 120 V rather than the value most of the
 * world uses.
 *
 * A ViewModel must not reach into the framework directly, so this is the seam:
 * tests supply a fixed locale instead of an Android system service.
 */
interface RegionProvider {
    /** The device's locale, carrying its region. */
    fun current(): Locale
}

@Singleton
class AndroidRegionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RegionProvider {

    /**
     * `LocaleManager.getSystemLocales()` is the one reading that survives a
     * per-app locale: it reports the device's setting whatever the app has been
     * switched to. It arrived in Android 13, the same release that introduced
     * per-app locales in the first place.
     *
     * Below that the backport replaces the process default, so there is nothing
     * left in the process that still remembers the device's region and
     * `Locale.getDefault()` is the closest available answer. The cost is
     * bounded: a pre-Android-13 user who has both picked an in-app language and
     * pressed reset gets the majority values and edits the two fields that are
     * wrong for them.
     */
    override fun current(): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val system = context.getSystemService(LocaleManager::class.java)?.systemLocales
            if (system != null && !system.isEmpty) return system[0]
        }
        return Locale.getDefault()
    }
}
