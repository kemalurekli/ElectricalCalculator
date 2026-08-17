package com.kemalurekli.electricalcalculator.core.common.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats a timestamp for display in the user's locale and time zone.
 *
 * Both are resolved per call rather than cached, so a record's timestamp stays
 * correct after the user changes time zone or language without the app having
 * to invalidate anything.
 */
fun Instant.formatAsDateTime(
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    .withLocale(locale)
    .withZone(zoneId)
    .format(this)

/**
 * The date alone, for timestamps where the hour is noise.
 *
 * A join date is the case this exists for: "joined 4 March 2026" is what the
 * reader wants, and the minute they created the account is not information.
 */
fun Instant.formatAsDate(
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(locale)
    .withZone(zoneId)
    .format(this)
