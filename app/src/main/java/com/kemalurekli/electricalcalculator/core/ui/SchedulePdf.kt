package com.kemalurekli.electricalcalculator.core.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import java.io.OutputStream

/**
 * Draws a circuit schedule onto PDF pages.
 *
 * ### Why drawing rather than printing
 *
 * `PrintManager` is the obvious Android answer and produces no file: it hands
 * pages to a printer or to whatever print service the user has installed. The
 * thing wanted here is a document to attach to an email, so the pages are drawn
 * directly and written out.
 *
 * ### Why landscape
 *
 * The table is ten columns wide. On portrait A4 each one gets about forty
 * points, which is not enough for a circuit name or for "Cross-section (mm²)",
 * and everything arrives ellipsised. A schedule is a wide document; printing it
 * on its side is what people already do with them.
 *
 * ### What this deliberately does not do
 *
 * No fitting of column widths to content, no wrapping inside a cell, no styling
 * beyond a rule under the header. A cell too long for its column is ellipsised,
 * which is honest and stable — a layout that reflows to fit its widest value
 * makes two exports of the same job look like different documents.
 */
object SchedulePdf {

    /** A4 landscape at 72 points to the inch, which is what PdfDocument works in. */
    private const val PAGE_WIDTH = 842
    private const val PAGE_HEIGHT = 595
    private const val MARGIN = 36f

    private const val TITLE_SIZE = 16f
    private const val BODY_SIZE = 8.5f
    private const val ROW_HEIGHT = 15f
    private const val SUPPLY_ROW_HEIGHT = 12f
    private const val COLUMN_GAP = 4f

    /**
     * Relative column widths.
     *
     * The name and the reason a size was chosen are prose and need the room;
     * the rest are short figures. Sized by hand rather than measured, so the
     * same schedule always lays out the same way.
     */
    private val COLUMN_WEIGHTS = floatArrayOf(2.2f, 1f, 1f, 1f, 1.3f, 1f, 1f, 1f, 1.1f, 1.9f)

    fun write(report: ScheduleReport, output: OutputStream) {
        val document = PdfDocument()
        try {
            val widths = columnWidths(report.columns.size)
            var page = document.startPage(pageInfo(1))
            var canvas = page.canvas
            var pageNumber = 1

            var y = drawHeader(canvas, report, widths)

            report.rows.forEach { row ->
                if (y + ROW_HEIGHT > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(pageInfo(pageNumber))
                    canvas = page.canvas
                    // The header is repeated on every page. A continuation
                    // sheet of bare numbers is unreadable on its own, and
                    // schedules get separated.
                    y = drawColumnHeader(canvas, report.columns, widths, MARGIN)
                }
                y = drawRow(canvas, row, widths, y)
            }

            document.finishPage(page)
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()

    /** Title, supply block and column header. Returns the y to start rows at. */
    private fun drawHeader(canvas: Canvas, report: ScheduleReport, widths: FloatArray): Float {
        var y = MARGIN + TITLE_SIZE
        canvas.drawText(report.title, MARGIN, y, titlePaint)
        y += TITLE_SIZE

        // Two columns of parameters, so nine fields do not push the table onto
        // a second page before a single circuit has been drawn.
        val half = (report.supply.size + 1) / 2
        val columnWidth = (PAGE_WIDTH - 2 * MARGIN) / 2f
        report.supply.forEachIndexed { index, field ->
            val column = if (index < half) 0 else 1
            val row = if (index < half) index else index - half
            val x = MARGIN + column * columnWidth
            val lineY = y + (row + 1) * SUPPLY_ROW_HEIGHT
            canvas.drawText("${field.label}:", x, lineY, labelPaint)
            canvas.drawText(field.value, x + LABEL_WIDTH, lineY, bodyPaint)
        }
        y += half * SUPPLY_ROW_HEIGHT + ROW_HEIGHT

        return drawColumnHeader(canvas, report.columns, widths, y)
    }

    private fun drawColumnHeader(
        canvas: Canvas,
        columns: List<String>,
        widths: FloatArray,
        top: Float,
    ): Float {
        var x = MARGIN
        columns.forEachIndexed { index, column ->
            canvas.drawText(ellipsise(column, widths[index], headerPaint), x, top, headerPaint)
            x += widths[index] + COLUMN_GAP
        }
        val ruleY = top + 4f
        canvas.drawLine(MARGIN, ruleY, PAGE_WIDTH - MARGIN, ruleY, rulePaint)
        return ruleY + ROW_HEIGHT
    }

    private fun drawRow(canvas: Canvas, row: List<String>, widths: FloatArray, top: Float): Float {
        var x = MARGIN
        row.forEachIndexed { index, cell ->
            // A row longer than the header is a bug upstream; drawing the extra
            // cells off the page would hide it, so they are simply not drawn.
            if (index >= widths.size) return@forEachIndexed
            canvas.drawText(ellipsise(cell, widths[index], bodyPaint), x, top, bodyPaint)
            x += widths[index] + COLUMN_GAP
        }
        return top + ROW_HEIGHT
    }

    private fun columnWidths(count: Int): FloatArray {
        val weights = if (count == COLUMN_WEIGHTS.size) {
            COLUMN_WEIGHTS
        } else {
            FloatArray(count) { 1f }
        }
        val available = PAGE_WIDTH - 2 * MARGIN - COLUMN_GAP * (count - 1)
        val total = weights.sum()
        return FloatArray(count) { available * weights[it] / total }
    }

    private fun ellipsise(text: String, width: Float, paint: TextPaint): String =
        TextUtils.ellipsize(text, paint, width, TextUtils.TruncateAt.END).toString()

    private const val LABEL_WIDTH = 130f

    private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = TITLE_SIZE
        color = Color.BLACK
        isFakeBoldText = true
    }

    private val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = BODY_SIZE
        color = Color.BLACK
        isFakeBoldText = true
    }

    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = BODY_SIZE
        color = Color.BLACK
    }

    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = BODY_SIZE
        color = Color.DKGRAY
    }

    private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 0.5f
    }
}
