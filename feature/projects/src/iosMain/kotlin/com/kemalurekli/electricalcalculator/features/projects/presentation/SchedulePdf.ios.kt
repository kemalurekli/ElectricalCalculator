package com.kemalurekli.electricalcalculator.features.projects.presentation

import platform.posix.memcpy
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererContext
import platform.Foundation.NSString
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes

actual val isSchedulePdfSupported: Boolean = true

/**
 * Draws the pages [ScheduleLayout] worked out.
 *
 * The counterpart of the Android renderer, and deliberately the same length:
 * everything with a decision in it is in common code, so this is a loop over
 * operations and four sets of text attributes. If the two ever disagree about
 * how a schedule looks, the difference is in one of these two short files
 * rather than buried in two independent layout passes.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun renderSchedulePdf(report: ScheduleReport): ByteArray {
    val pages = ScheduleLayout.pages(report, IosTextMeasurer)
    val bounds = CGRectMake(0.0, 0.0, ScheduleLayout.PAGE_WIDTH.toDouble(), ScheduleLayout.PAGE_HEIGHT.toDouble())
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

@OptIn(ExperimentalForeignApi::class)
private object IosTextMeasurer : TextMeasurer {
    override fun widthOf(text: String, style: PdfStyle): Float =
        (text as NSString).sizeWithAttributes(attributesFor(style)).useContents { width.toFloat() }
}

private fun attributesFor(style: PdfStyle): Map<Any?, Any> = when (style) {
    PdfStyle.TITLE -> mapOf<Any?, Any>(
        NSFontAttributeName to UIFont.boldSystemFontOfSize(ScheduleLayout.TITLE_SIZE.toDouble()),
        NSForegroundColorAttributeName to UIColor.blackColor,
    )

    PdfStyle.HEADER -> mapOf<Any?, Any>(
        NSFontAttributeName to UIFont.boldSystemFontOfSize(ScheduleLayout.BODY_SIZE.toDouble()),
        NSForegroundColorAttributeName to UIColor.blackColor,
    )

    PdfStyle.BODY -> mapOf<Any?, Any>(
        NSFontAttributeName to UIFont.systemFontOfSize(ScheduleLayout.BODY_SIZE.toDouble()),
        NSForegroundColorAttributeName to UIColor.blackColor,
    )

    PdfStyle.LABEL -> mapOf<Any?, Any>(
        NSFontAttributeName to UIFont.systemFontOfSize(ScheduleLayout.BODY_SIZE.toDouble()),
        NSForegroundColorAttributeName to UIColor.darkGrayColor,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

private const val RULE_WIDTH = 0.5
