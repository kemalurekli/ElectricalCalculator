package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport

/**
 * Not implemented.
 *
 * The Android version is 186 lines of `android.graphics` drawing — a table with
 * measured column widths, wrapped cells and page breaks. Core Graphics can do
 * all of it, but it is a rewrite rather than a port, and the schedule screen is
 * usable without it: the CSV export carries the same figures and opens in
 * anything.
 *
 * Deliberately absent rather than stubbed to an empty document. A share sheet
 * offering a zero-byte PDF is a bug report; a missing button is a gap.
 */
actual val isSchedulePdfSupported: Boolean = false

actual fun renderSchedulePdf(report: ScheduleReport): ByteArray =
    error("The schedule PDF is not implemented on iOS; check isSchedulePdfSupported first")
