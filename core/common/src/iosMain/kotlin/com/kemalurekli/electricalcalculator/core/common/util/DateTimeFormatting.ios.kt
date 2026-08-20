package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.preferredLanguages
import platform.Foundation.NSLocale
import platform.Foundation.NSRelativeDateTimeFormatterStyleNamed
import platform.Foundation.NSRelativeDateTimeFormatter
import kotlin.time.Instant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun Instant.formatAsDateTime(): String =
    formatter(dateStyle = NSDateFormatterMediumStyle, timeStyle = NSDateFormatterShortStyle)
        .stringFromDate(toNSDate())

actual fun Instant.formatAsDate(): String =
    formatter(dateStyle = NSDateFormatterMediumStyle, timeStyle = NSDateFormatterNoStyle)
        .stringFromDate(toNSDate())

/**
 * A fresh formatter per call rather than a cached one.
 *
 * `NSDateFormatter` snapshots the locale and time zone when it is created, so a
 * cached instance keeps formatting in yesterday's language after the reader
 * changes theirs. Constructing one is cheap next to being quietly wrong.
 */
private fun formatter(dateStyle: ULong, timeStyle: ULong) = NSDateFormatter().apply {
    setDateStyle(dateStyle)
    setTimeStyle(timeStyle)
}

private fun Instant.toNSDate(): NSDate =
    NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / 1000.0)

/**
 * `NSRelativeDateTimeFormatter` is Foundation's answer to `DateUtils`, and
 * arrived in iOS 13. `.named` lets it say "yesterday" rather than "1 day ago"
 * where the language has a word for it, which is what the Android formatter
 * does too.
 *
 * Built fresh per call, like the absolute formatter above and for the same
 * reason: a cached one keeps the language it was created with.
 */
actual fun Instant.formatAsRelativeTime(): String {
    // `NSLocale.currentLocale` is the *formats* locale — dates, numbers, the
    // region — and on a device set to English with Turkish added it is English.
    // The app is showing Turkish because Compose Resources resolves against
    // `preferredLanguages`, so that is what this has to follow: "2 hours ago"
    // under Turkish prose is the app disagreeing with itself.
    val formatter = NSRelativeDateTimeFormatter().apply {
        dateTimeStyle = NSRelativeDateTimeFormatterStyleNamed
        NSLocale.preferredLanguages.firstOrNull()?.let {
            locale = NSLocale(localeIdentifier = it as String)
        }
    }
    val date = NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / 1000.0)
    return formatter.localizedStringForDate(date, relativeToDate = NSDate())
}
