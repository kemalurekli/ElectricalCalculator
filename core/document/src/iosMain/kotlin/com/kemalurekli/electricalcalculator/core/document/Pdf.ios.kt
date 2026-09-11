package com.kemalurekli.electricalcalculator.core.document

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererContext
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.posix.memcpy

/**
 * The counterpart of the Android renderer, and deliberately the same length:
 * everything with a decision in it is in the layout that produced [pages], so
 * this is a loop over operations and one set of text attributes per style. If
 * the two ever disagree about how a page looks, the difference is in one of
 * these two short files rather than in two independent layout passes.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun renderPdf(pages: List<List<PdfOp>>, size: PdfPageSize): ByteArray {
    val bounds = CGRectMake(0.0, 0.0, size.width.toDouble(), size.height.toDouble())
    val renderer = UIGraphicsPDFRenderer(bounds = bounds)

    val data: NSData = renderer.PDFDataWithActions { context ->
        val pdf = context ?: return@PDFDataWithActions
        pages.forEach { operations ->
            pdf.beginPage()
            operations.forEach { it.drawIn(pdf) }
        }
    }
    return data.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
actual val pdfTextMeasurer: TextMeasurer = TextMeasurer { text, style ->
    (text as NSString).sizeWithAttributes(attributesFor(style)).useContents { width.toFloat() }
}

@OptIn(ExperimentalForeignApi::class)
private fun PdfOp.drawIn(context: UIGraphicsPDFRendererContext) = when (this) {
    is PdfOp.Text -> {
        // Both platforms are given a baseline y, because that is what
        // `Canvas.drawText` takes. UIKit draws from the top of the line box, so
        // the ascender is subtracted here rather than in the shared layout.
        val attributes = attributesFor(style)
        val font = attributes[NSFontAttributeName] as UIFont
        (text as NSString).drawAtPoint(
            CGPointMake(x.toDouble(), y.toDouble() - font.ascender),
            attributes,
        )
    }

    is PdfOp.Rule -> context.CGContext.let { cg ->
        CGContextSetStrokeColorWithColor(cg, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(cg, RULE_WIDTH)
        CGContextMoveToPoint(cg, fromX.toDouble(), y.toDouble())
        CGContextAddLineToPoint(cg, toX.toDouble(), y.toDouble())
        CGContextStrokePath(cg)
    }
}

/**
 * Derived from the style rather than listed per case.
 *
 * The `when` this replaces had one branch per style and repeated the same three
 * lines in each, which is how the two platforms drift: a style added to the
 * enum compiled here as a missing branch and there as a silent default.
 */
private fun attributesFor(style: PdfStyle): Map<Any?, Any> = mapOf(
    NSFontAttributeName to when {
        style.mono -> UIFont.monospacedSystemFontOfSize(
            style.size.toDouble(),
            weight = if (style.bold) UIFontWeightSemibold else UIFontWeightRegular,
        )

        style.bold -> UIFont.boldSystemFontOfSize(style.size.toDouble())
        else -> UIFont.systemFontOfSize(style.size.toDouble())
    },
    NSForegroundColorAttributeName to when (style.ink) {
        PdfInk.DEFAULT -> UIColor.blackColor
        PdfInk.MUTED -> UIColor.darkGrayColor
        PdfInk.WARNING -> WARNING_RED
    },
)

/**
 * Darker than a signal red, because this is print.
 *
 * The same value the Android renderer uses, for the same reason: a
 * full-saturation red is drawn for a backlit screen and goes muddy on paper
 * and on a greyscale printer.
 */
private val WARNING_RED: UIColor = UIColor.colorWithRed(
    red = 176.0 / 255.0,
    green = 32.0 / 255.0,
    blue = 32.0 / 255.0,
    alpha = 1.0,
)

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

private const val RULE_WIDTH = 0.5
