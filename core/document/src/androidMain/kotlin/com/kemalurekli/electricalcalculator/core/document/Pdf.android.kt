package com.kemalurekli.electricalcalculator.core.document

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import java.io.ByteArrayOutputStream

/**
 * Everything with a decision in it is in the layout that produced [pages].
 * What is left here is the platform's drawing API and one paint per style,
 * which is short enough to read beside the iOS version and confirm the two
 * agree.
 */
actual fun renderPdf(pages: List<List<PdfOp>>, size: PdfPageSize): ByteArray {
    val document = PdfDocument()
    return try {
        pages.forEachIndexed { index, operations ->
            val page = document.startPage(pageInfo(size, index + 1))
            operations.forEach { it.drawOn(page.canvas) }
            document.finishPage(page)
        }
        ByteArrayOutputStream().also(document::writeTo).toByteArray()
    } finally {
        document.close()
    }
}

actual val pdfTextMeasurer: TextMeasurer = TextMeasurer { text, style ->
    paintFor(style).measureText(text)
}

private fun pageInfo(size: PdfPageSize, number: Int) = PdfDocument.PageInfo.Builder(
    size.width.toInt(),
    size.height.toInt(),
    number,
).create()

private fun PdfOp.drawOn(canvas: Canvas) = when (this) {
    is PdfOp.Text -> canvas.drawText(text, x, y, paintFor(style))
    is PdfOp.Rule -> canvas.drawLine(fromX, y, toX, y, rulePaint)
}

/**
 * Darker than a signal red, because this is print.
 *
 * A full-saturation red is drawn for a screen with a backlight behind it; on
 * paper, and on the greyscale printer half of these documents will come out
 * of, it goes muddy. This one stays legibly red on colour and reads as heavy
 * black without.
 */
private val WARNING_RED = Color.rgb(176, 32, 32)

/**
 * Built once per style rather than per operation.
 *
 * A full schedule is a few thousand draws, and a `TextPaint` allocated inside
 * that loop is a few thousand objects for four distinct configurations.
 */
private val paints: Map<PdfStyle, TextPaint> = PdfStyle.entries.associateWith { style ->
    TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = style.size
        color = when (style.ink) {
            PdfInk.DEFAULT -> Color.BLACK
            PdfInk.MUTED -> Color.DKGRAY
            PdfInk.WARNING -> WARNING_RED
        }
        isFakeBoldText = style.bold
        if (style.mono) typeface = Typeface.MONOSPACE
    }
}


private fun paintFor(style: PdfStyle): TextPaint = paints.getValue(style)

private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.LTGRAY
    strokeWidth = 0.5f
}
