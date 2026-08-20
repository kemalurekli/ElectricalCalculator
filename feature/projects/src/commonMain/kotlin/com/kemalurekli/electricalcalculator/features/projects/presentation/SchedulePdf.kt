package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport

/**
 * Whether this platform can draw a schedule.
 *
 * False on iOS. The Android version draws with `android.graphics.pdf`; the
 * counterpart is Core Graphics work that has not been done, and a button that
 * produced nothing would be worse than one that is not there. The CSV export
 * works on both and carries the same figures.
 */
expect val isSchedulePdfSupported: Boolean

/**
 * Draws [report] as a PDF.
 *
 * Returns the whole document rather than writing into a stream. The Android
 * version used to take an `OutputStream`, which avoided buffering — but a
 * stream is a JVM type, and a schedule is a few pages. See
 * [com.kemalurekli.electricalcalculator.core.designsystem.platform.FileSharing]
 * for the other half of that trade.
 *
 * Throws where [isSchedulePdfSupported] is false. Callers check first.
 */
expect fun renderSchedulePdf(report: ScheduleReport): ByteArray
