package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.preferredLanguages

/**
 * The locale of the language the app is displaying.
 *
 * Not `NSLocale.currentLocale`, which answers *where the reader is* — the
 * region, the calendar, the number separators. This answers *what they are
 * reading*, and the two come apart the moment a reader on an English phone
 * chooses Turkish in the app: the picker changes the language by writing
 * `AppleLanguages`, which moves `preferredLanguages` and leaves `currentLocale`
 * where it was.
 *
 * Anything whose answer depends on the language reads this — collation, case
 * mapping, the words in a date. Anything whose answer depends on the region
 * does not; that is [RegionProvider], and it is a separate question on purpose.
 */
internal fun appLocale(): NSLocale =
    (NSLocale.preferredLanguages.firstOrNull() as? String)
        ?.let { NSLocale(localeIdentifier = it) }
        ?: NSLocale.currentLocale
