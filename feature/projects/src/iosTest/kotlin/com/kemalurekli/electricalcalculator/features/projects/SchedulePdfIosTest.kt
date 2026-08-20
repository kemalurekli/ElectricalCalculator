package com.kemalurekli.electricalcalculator.features.projects

import com.kemalurekli.electricalcalculator.features.design.domain.ReportField
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import com.kemalurekli.electricalcalculator.features.projects.presentation.renderSchedulePdf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The Core Graphics half of the schedule PDF, on a simulator.
 *
 * `ScheduleLayoutTest` covers the arithmetic on both platforms; this covers the
 * part that only exists here — that `UIGraphicsPDFRenderer` is driven correctly
 * and produces a document rather than an empty one.
 *
 * iOS-only because the Android half needs a device to draw on, and the Android
 * app exercises it every time anyone exports a schedule.
 */
class SchedulePdfIosTest {

    private fun report(rows: Int) = ScheduleReport(
        title = "Blok A",
        supply = listOf(ReportField("Şebeke", "400 V"), ReportField("Malzeme", "Bakır")),
        columns = listOf("Devre", "Yük", "Kesit"),
        rows = List(rows) { index -> listOf("C$index", "16 A", "2,5 mm²") },
    )

    @Test
    fun `a schedule renders as a PDF document`() {
        val bytes = renderSchedulePdf(report(5))

        assertEquals("%PDF", bytes.decodeToString(0, 4))
        // A page of drawn text, not an empty document: an eight-page-empty PDF
        // is about 500 bytes and would pass a header check.
        assertTrue(bytes.size > 2_000, "only ${bytes.size} bytes")
    }

    @Test
    fun `a long schedule produces more than one page`() {
        val short = renderSchedulePdf(report(5))
        val long = renderSchedulePdf(report(200))

        // Compared rather than counted: the page objects are compressed, so the
        // honest assertion is that more rows produce a larger document.
        assertTrue(long.size > short.size, "${long.size} is not larger than ${short.size}")
    }

    @Test
    fun `Turkish text survives the drawing`() {
        // The report carries ş, ğ and ² and the bytes are compressed, so this
        // asserts what can be asserted: the renderer did not fail on them.
        val bytes = renderSchedulePdf(report(1))

        assertEquals("%PDF", bytes.decodeToString(0, 4))
    }
}
