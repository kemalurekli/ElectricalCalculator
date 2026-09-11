package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.document.PdfInk
import com.kemalurekli.electricalcalculator.core.document.PdfOp
import com.kemalurekli.electricalcalculator.core.document.PdfPageSize
import com.kemalurekli.electricalcalculator.core.document.PdfStyle
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_watermark

/**
 * The page the reader was about to export, drawn on screen.
 *
 * ### Why this is the document and not a picture of one
 *
 * A layout produces [PdfOp]s — text at a coordinate, and rules — and a renderer
 * puts them on paper. There is nothing platform-specific in that vocabulary, so
 * a third renderer can put the *same operations* on a Canvas. This preview is
 * therefore not a mock-up that has to be kept in step with the real output: it
 * is the real output, at a different scale, with a stamp across it.
 *
 * That matters for what the paywall is claiming. A sample with somebody else's
 * circuits in it asks the reader to imagine their own; this shows them, with
 * the numbers they typed a moment ago, and the disclaimer that will be on the
 * document they hand over.
 *
 * ### What is honestly different
 *
 * The typeface. The two PDF renderers draw with the platform's system font and
 * this draws with the app's, because a Canvas has no access to the other. The
 * arithmetic that placed every line was done against the PDF's font, so lines
 * start where they will start and wrap where they will wrap; they will simply
 * be set in a different face. At this scale that is not a promise anybody can
 * read, but it is a difference and it is written down here rather than not.
 *
 * ### The stamp
 *
 * Diagonal, repeated, and drawn over the text rather than under it, so a
 * screenshot of this can never be passed off as the document. It is deliberately
 * not opaque: the point of the screen is that the reader can see what they
 * would get.
 */
@Composable
internal fun DocumentPreview(
    page: List<PdfOp>,
    size: PdfPageSize,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val measurer = rememberTextMeasurer()
    val watermark = stringResource(Res.string.pro_preview_watermark)
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    // The same red the two PDF renderers use, so the disclaimer looks on screen
    // the way it will look on paper.
    val warning = WARNING_RED
    val stamp = WARNING_RED.copy(alpha = STAMP_ALPHA)

    Column(modifier = modifier.fillMaxWidth()) {
        // Paper, not a themed surface: a document is white in every scheme,
        // and showing it on the dark grey of a night-time card would be a
        // picture of something the reader is never going to receive.
        // Fills whatever box the caller gives it, and scales the page by
        // width. Asking for the paper's aspect ratio and clipping the overflow
        // was the obvious way and it did not survive contact: inside a fixed
        // height the constraint negotiation put the middle of the page on
        // screen instead of the top. Scaling by width and simply not drawing
        // what falls below the box has one answer and it is the same on both
        // platforms.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PAPER,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = this.size.width / size.width
                page.forEach { op ->
                    when (op) {
                        is PdfOp.Text -> drawPdfText(op, scale, measurer, ink, muted, warning)
                        is PdfOp.Rule -> drawLine(
                            color = RULE,
                            start = Offset(op.fromX * scale, op.y * scale),
                            end = Offset(op.toX * scale, op.y * scale),
                            strokeWidth = RULE_WIDTH,
                        )
                    }
                }
                drawWatermark(watermark, measurer, stamp)
            }
        }

    }
}

/**
 * One operation, at the page's scale.
 *
 * A PDF places text by its baseline; Compose places it by the top of its line
 * box. The offset is an approximation of the ascent — good enough at a sixth of
 * actual size, and wrong enough to be worth naming.
 */
private fun DrawScope.drawPdfText(
    op: PdfOp.Text,
    scale: Float,
    measurer: TextMeasurer,
    ink: Color,
    muted: Color,
    warning: Color,
) {
    val fontSize = op.style.size * scale
    if (fontSize < MIN_LEGIBLE) return
    val layout = measurer.measure(
        text = op.text,
        style = TextStyle(
            fontSize = fontSize.toSp(),
            fontWeight = if (op.style.bold) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (op.style.mono) FontFamily.Monospace else null,
            color = when (op.style.ink) {
                PdfInk.DEFAULT -> ink
                PdfInk.MUTED -> muted
                PdfInk.WARNING -> warning
            },
        ),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            x = op.x * scale,
            y = op.y * scale - fontSize * BASELINE_FRACTION,
        ),
    )
}

/**
 * A field of small diagonal stamps, tiled across the whole sheet.
 *
 * It was one large one. That is what a watermark usually looks like and it was
 * the wrong tool here: a single band is easy to crop around, easy to cover, and
 * a reader who wanted the document rather than the subscription could take a
 * screenshot and use it. A grid of small ones cannot be removed without
 * removing the page, and it sits between the lines of text rather than across
 * them, so the document stays readable — which is the whole reason this screen
 * exists.
 *
 * Faint red rather than the brand blue: red is the colour of a stamp, and at
 * this weight it reads as something printed over the page rather than as part
 * of the design.
 */
private fun DrawScope.drawWatermark(
    text: String,
    measurer: TextMeasurer,
    colour: Color,
) {
    val layout = measurer.measure(
        text = text,
        style = TextStyle(
            fontSize = (size.width * STAMP_FRACTION).toSp(),
            fontWeight = FontWeight.Medium,
            color = colour,
            letterSpacing = STAMP_TRACKING.sp,
        ),
    )

    val stepX = layout.size.width + size.width * STAMP_GAP_X
    val stepY = size.height * STAMP_GAP_Y

    rotate(degrees = -STAMP_DEGREES) {
        // Rotating about the centre swings the corners outside the page, so
        // the field is laid out well beyond it and clipped by the surface.
        var y = -size.height * STAMP_BLEED
        var row = 0
        while (y < size.height * (1f + STAMP_BLEED)) {
            // Every other row is offset by half a step, so the stamps read as
            // a texture rather than as a grid somebody forgot to align.
            val offset = if (row % 2 == 0) 0f else stepX / 2f
            var x = -size.width * STAMP_BLEED + offset
            while (x < size.width * (1f + STAMP_BLEED)) {
                drawText(textLayoutResult = layout, topLeft = Offset(x, y))
                x += stepX
            }
            y += stepY
            row++
        }
    }
}

/** Paper. Fixed in both schemes, because paper is. */
private val PAPER = Color(0xFFFDFDFD)
private val RULE = Color(0xFFBFC6CC)

/** Matches `WARNING_RED` in both PDF renderers. */
private val WARNING_RED = Color(0xFFB02020)
private const val RULE_WIDTH = 1f

/**
 * Below this the glyphs are a grey smear that costs a measure pass and says
 * nothing. A page footnote at a sixth of size is not text any more.
 */
private const val MIN_LEGIBLE = 3f

/** Where a baseline sits within a line box, near enough. */
private const val BASELINE_FRACTION = 0.82f

private const val STAMP_ALPHA = 0.26f
private const val STAMP_FRACTION = 0.020f
private const val STAMP_TRACKING = 0.5f
private const val STAMP_DEGREES = 30f

/** Space between stamps, as a share of the page. */
private const val STAMP_GAP_X = 0.085f
private const val STAMP_GAP_Y = 0.105f

/** How far past each edge the field is drawn, so rotation leaves no bare corner. */
private const val STAMP_BLEED = 0.6f

