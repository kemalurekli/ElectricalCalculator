package com.kemalurekli.electricalcalculator.core.common.util

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
