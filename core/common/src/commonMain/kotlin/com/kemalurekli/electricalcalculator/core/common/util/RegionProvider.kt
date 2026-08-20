package com.kemalurekli.electricalcalculator.core.common.util

/**
 * Where the device is, as distinct from what language the app is showing.
 *
 * ### Why this is not the default locale
 *
 * Choosing an in-app language sets a per-app locale, and from that point the
 * process default *is* that locale — Android overrides it from 13 onwards and
 * AppCompat's backport does the same below; iOS does the same through
 * `CFBundleAllowMixedLocalizations`. The app's own picker stores plain `en` and
 * `tr`, language subtags with no region, so anything reading the default to
 * find out *where the user is* gets nothing back the moment they pick a
 * language, and quietly falls to the majority answer.
 *
 * That is wrong for the one thing the region is used for. An engineer in Texas
 * who prefers the app in Turkish is still on a 120 V supply, and resetting the
 * engineering defaults must give them 120 V rather than the value most of the
 * world uses.
 *
 * ### Why a country code rather than a locale
 *
 * It used to return a `java.util.Locale` and every caller immediately read
 * `.country` off it. Returning the country outright makes the interface say
 * what it is for and removes a JVM type from shared code in the same move.
 *
 * A ViewModel must not reach into the framework directly, so this is the seam:
 * tests supply a fixed region instead of a system service.
 */
interface RegionProvider {
    /** An ISO 3166-1 alpha-2 country code, or an empty string if unknown. */
    fun currentRegion(): String
}
