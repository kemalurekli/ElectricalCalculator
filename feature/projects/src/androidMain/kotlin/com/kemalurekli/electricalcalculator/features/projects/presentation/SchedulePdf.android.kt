package com.kemalurekli.electricalcalculator.features.projects.presentation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import java.io.ByteArrayOutputStream

actual val isSchedulePdfSupported: Boolean = true

/**
 * Draws the pages [ScheduleLayout] worked out.
 *
 * Everything with a decision in it — column widths, the page break, where to
 * cut a cell that does not fit — is in common code. What is left here is the
 * platform's drawing API and four paints, which is short enough to read beside
 * the iOS version and confirm the two agree.
 */
actual fun renderSchedulePdf(report: ScheduleReport): ByteArray {
    val document = PdfDocument()
    return try {
        ScheduleLayout.pages(report, AndroidTextMeasurer).forEachIndexed { index, operations ->
            val page = document.startPage(pageInfo(index + 1))
            operations.forEach { it.drawOn(page.canvas) }
            document.finishPage(page)
        }
        ByteArrayOutputStream().also(document::writeTo).toByteArray()
    } finally {
        document.close()
    }
}

private fun pageInfo(number: Int) = PdfDocument.PageInfo.Builder(
    ScheduleLayout.PAGE_WIDTH.toInt(),
    ScheduleLayout.PAGE_HEIGHT.toInt(),
    number,
).create()

private fun PdfOp.drawOn(canvas: Canvas) = when (this) {
    is PdfOp.Text -> canvas.drawText(text, x, y, paintFor(style))
    is PdfOp.Rule -> canvas.drawLine(fromX, y, toX, y, rulePaint)
}

private object AndroidTextMeasurer : TextMeasurer {
    override fun widthOf(text: String, style: PdfStyle): Float = paintFor(style).measureText(text)
}

private fun paintFor(style: PdfStyle): TextPaint = when (style) {
    PdfStyle.TITLE -> titlePaint
    PdfStyle.HEADER -> headerPaint
    PdfStyle.BODY -> bodyPaint
    PdfStyle.LABEL -> labelPaint
}

private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = ScheduleLayout.TITLE_SIZE
    color = Color.BLACK
    isFakeBoldText = true
}

private val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = ScheduleLayout.BODY_SIZE
    color = Color.BLACK
    isFakeBoldText = true
}

private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = ScheduleLayout.BODY_SIZE
    color = Color.BLACK
}

private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = ScheduleLayout.BODY_SIZE
    color = Color.DKGRAY
}

private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.LTGRAY
    strokeWidth = 0.5f
}
