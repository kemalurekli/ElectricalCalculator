package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

/**
 * `NSLocale.currentLocale` reports the device's region regardless of which of
 * the app's localisations is being displayed — the region comes from Settings ›
 * General › Language & Region, and picking a language inside the app does not
 * touch it. So iOS needs none of the version dance Android does.
 *
 * Read fresh on every call rather than cached, for the same reason the date
 * formatter is: a locale captured at construction goes on answering with
 * yesterday's setting.
 */
class PlatformRegionProvider : RegionProvider {
    override fun currentRegion(): String = NSLocale.currentLocale.countryCode ?: ""
}

actual fun createRegionProvider(): RegionProvider = PlatformRegionProvider()
