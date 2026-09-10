package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport

/**
 * Whether this platform can draw a schedule.
 *
 * True on both. It was false on iOS while the Core Graphics side was unwritten,
 * and the flag stayed because a button that produced nothing would have been
 * worse than one that was not there. `UIGraphicsPDFRenderer` does the work now,
 * off the same [ScheduleLayout] the Android renderer uses, so the two draw the
 * same pages.
 *
 * The expectation is kept rather than deleted: it is the seam a third platform
 * would arrive through, and the callers already ask before offering the button.
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
