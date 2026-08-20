package com.kemalurekli.electricalcalculator.core.common.util

import android.text.format.DateUtils
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant

actual fun Instant.formatAsDateTime(): String =
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(toJavaInstant())

actual fun Instant.formatAsDate(): String =
    DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(toJavaInstant())

private fun Instant.toJavaInstant(): java.time.Instant =
    java.time.Instant.ofEpochMilli(toEpochMilliseconds())

/**
 * `DateUtils` is the platform's own relative formatter — the one the system UI
 * uses — so the phrasing matches the rest of the device without the app
 * shipping plural rules of its own.
 */
actual fun Instant.formatAsRelativeTime(): String =
    DateUtils.getRelativeTimeSpanString(
        toEpochMilliseconds(),
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
