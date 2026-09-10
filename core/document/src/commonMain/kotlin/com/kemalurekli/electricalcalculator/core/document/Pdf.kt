package com.kemalurekli.electricalcalculator.core.document

/**
 * The vocabulary a layout draws in, and the only thing the two renderers know.
 *
 * A layout works out where every piece goes and produces [PdfOp]s; a renderer
 * puts them on a page. The split is what lets one arithmetic serve
 * `android.graphics.pdf` and `UIGraphicsPDFRenderer` without either platform
 * holding an opinion about margins.
 */
enum class PdfStyle(val size: Float, val bold: Boolean, val muted: Boolean) {

    /** The name of the document. */
    TITLE(size = 16f, bold = true, muted = false),

    /** Under the title: what produced it and when. */
    SUBTITLE(size = 11f, bold = false, muted = true),

    /** A section within a document — "Inputs", "Result". */
    HEADING(size = 10f, bold = true, muted = false),

    /** Prose and values in a document somebody reads a line at a time. */
    TEXT(size = 10f, bold = false, muted = false),

    /** A column heading in a table. */
    HEADER(size = 8.5f, bold = true, muted = false),

    /**
     * A table cell.
     *
     * Smaller than [TEXT] on purpose: a schedule is fourteen columns wide and
     * the figures have to stay on one line, where a document's own sentences
     * have a whole page width to run in.
     */
    BODY(size = 8.5f, bold = false, muted = false),

    /** A field label, and the notice at the foot of a page. */
    LABEL(size = 8.5f, bold = false, muted = true),
}

sealed interface PdfOp {
    data class Text(val text: String, val x: Float, val y: Float, val style: PdfStyle) : PdfOp
    data class Rule(val fromX: Float, val toX: Float, val y: Float) : PdfOp
}

fun interface TextMeasurer {
    /** The width of [text] at [style]'s size, in points. */
    fun widthOf(text: String, style: PdfStyle): Float
}

/**
 * A page, in points.
 *
 * Landscape for a schedule, which is a wide table; portrait for a document
 * somebody reads. Both are A4 because both get printed in the countries this
 * app is used in.
 */
data class PdfPageSize(val width: Float, val height: Float) {
    companion object {
        val A4_LANDSCAPE = PdfPageSize(width = 842f, height = 595f)
        val A4_PORTRAIT = PdfPageSize(width = 595f, height = 842f)
    }
}

/**
 * Measures against the font the platform will actually draw with.
 *
 * The one thing a layout cannot do for itself: how wide a string is depends on
 * the typeface, and ellipsising a cell or wrapping a sentence needs that answer
 * before the page exists.
 */
expect val pdfTextMeasurer: TextMeasurer

/** Draws [pages] and returns the whole document. */
expect fun renderPdf(pages: List<List<PdfOp>>, size: PdfPageSize): ByteArray
