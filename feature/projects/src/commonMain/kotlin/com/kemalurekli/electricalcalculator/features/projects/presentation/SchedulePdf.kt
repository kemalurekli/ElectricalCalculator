package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.core.document.pdfTextMeasurer
import com.kemalurekli.electricalcalculator.core.document.renderPdf
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport

/**
 * Draws [report] as a PDF.
 *
 * No longer an expectation. It was one while `:feature:projects` owned the
 * drawing, and it carried an `isSchedulePdfSupported` flag from the months when
 * the iOS half was unwritten — a button that produced nothing would have been
 * worse than one that was not there. Both halves exist now and live in
 * `:core:document`, so what is left here is the schedule's own arithmetic and
 * one line handing it to a renderer.
 */
fun renderSchedulePdf(report: ScheduleReport): ByteArray =
    renderPdf(ScheduleLayout.pages(report, pdfTextMeasurer), ScheduleLayout.PAGE)
