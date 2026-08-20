package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.time.Instant

/**
 * Formats a timestamp the way the reader's device would.
 *
 * ### Why this is a platform capability and not common code
 *
 * Almost everything else in this module was made common because the platforms
 * agreed and only the plumbing differed. Dates are the opposite. "17 Aug 2026,
 * 14:30" in English, "17 Ağu 2026 14:30" in Turkish, and a different order
 * again in Japanese — the ordering, the separators, the abbreviations and
 * whether a comma appears are all per-language data, and both platforms already
 * ship it.
 *
 * Writing this in common Kotlin would mean carrying a pattern for every
 * language the app ships in and getting them wrong slowly. `DateTimeFormatter`
 * and `NSDateFormatter` are the right answer on their respective sides, and the
 * time zone and locale both resolve per call so a timestamp stays correct after
 * the reader travels or changes language.
 *
 * This is also the reason a formatted date is *not* part of the design
 * language's "same pixels on both platforms" rule: a Turkish reader on iOS
 * should see what every other iOS app shows them, not what Android shows.
 */
expect fun Instant.formatAsDateTime(): String

/**
 * The date alone, for timestamps where the hour is noise.
 *
 * A join date is the case this exists for: "joined 4 March 2026" is what the
 * reader wants, and the minute they created the account is not information.
 */
expect fun Instant.formatAsDate(): String
