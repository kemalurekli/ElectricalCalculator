package com.kemalurekli.electricalcalculator.core.common.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats a timestamp for display in the user's locale and time zone.
 *
 * Takes `kotlin.time.Instant` — the multiplatform one the domain now speaks —
 * and converts at the boundary, because the formatting itself is still
 * `java.time.DateTimeFormatter`. There is no common equivalent that produces a
 * locale-appropriate medium date, and writing one would mean shipping the
 * pattern for every language. This stays in `:app` until the screens that call
 * it move and force the question.
 *
 * Both are resolved per call rather than cached, so a record's timestamp stays
 * correct after the user changes time zone or language without the app having
 * to invalidate anything.
 */
fun kotlin.time.Instant.formatAsDateTime(
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    .withLocale(locale)
    .withZone(zoneId)
    .format(java.time.Instant.ofEpochMilli(toEpochMilliseconds()))

/**
 * The date alone, for timestamps where the hour is noise.
 *
 * A join date is the case this exists for: "joined 4 March 2026" is what the
 * reader wants, and the minute they created the account is not information.
 */
fun kotlin.time.Instant.formatAsDate(
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(locale)
    .withZone(zoneId)
    .format(java.time.Instant.ofEpochMilli(toEpochMilliseconds()))
