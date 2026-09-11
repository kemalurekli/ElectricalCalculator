package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.document.PdfOp
import com.kemalurekli.electricalcalculator.core.document.PdfPageSize
import com.kemalurekli.electricalcalculator.core.document.PdfStyle
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_document_caption
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
    val stamp = MaterialTheme.colorScheme.primary.copy(alpha = STAMP_ALPHA)

    Column(modifier = modifier.fillMaxWidth()) {
        // Paper, not a themed surface: a document is white in every scheme,
        // and showing it on the dark grey of a night-time card would be a
        // picture of something the reader is never going to receive.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(size.width / size.height),
            shape = MaterialTheme.shapes.small,
            color = PAPER,
        ) {
            Canvas(modifier = Modifier.fillMaxWidth()) {
                val scale = this.size.width / size.width
                page.forEach { op ->
                    when (op) {
                        is PdfOp.Text -> drawPdfText(op, scale, measurer, ink, muted)
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

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = stringResource(Res.string.pro_preview_document_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.xs),
        )
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
) {
    val fontSize = op.style.size * scale
    if (fontSize < MIN_LEGIBLE) return
    val layout = measurer.measure(
        text = op.text,
        style = TextStyle(
            fontSize = fontSize.toSp(),
            fontWeight = if (op.style.bold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (op.style.muted) muted else ink,
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
 * One diagonal pass across the middle of the sheet.
 *
 * It was three, which is what a watermark usually is, and three made the
 * document hard to read — which is the one thing this screen cannot afford,
 * since the whole argument is that the reader can see what they would get. One
 * is still impossible to crop out of a screenshot without cropping the page,
 * and on a short report it lands below the text rather than across it.
 *
 * Measured twice when it has to be. The word is a different length in every
 * language — "PREVIEW" against "PRÉ-VISUALIZAÇÃO" — and a stamp that runs off
 * the paper in Portuguese is not a stamp. So it is laid out at the size the
 * design wants, and if that is wider than the page will take, laid out again
 * at the size that fits.
 */
private fun DrawScope.drawWatermark(
    text: String,
    measurer: TextMeasurer,
    colour: Color,
) {
    fun layoutAt(fontSize: Float) = measurer.measure(
        text = text,
        style = TextStyle(
            fontSize = fontSize.toSp(),
            fontWeight = FontWeight.SemiBold,
            color = colour,
            letterSpacing = STAMP_TRACKING.sp,
        ),
    )

    val wanted = size.width * STAMP_FRACTION
    val first = layoutAt(wanted)
    val room = size.width * STAMP_MAX_WIDTH
    val layout = if (first.size.width <= room) {
        first
    } else {
        layoutAt(wanted * room / first.size.width)
    }
    rotate(degrees = -STAMP_DEGREES) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = (size.width - layout.size.width) / 2f,
                y = size.height * STAMP_HEIGHT - layout.size.height / 2f,
            ),
        )
    }
}

/** Paper. Fixed in both schemes, because paper is. */
private val PAPER = Color(0xFFFDFDFD)
private val RULE = Color(0xFFBFC6CC)
private const val RULE_WIDTH = 1f

/**
 * Below this the glyphs are a grey smear that costs a measure pass and says
 * nothing. A page footnote at a sixth of size is not text any more.
 */
private const val MIN_LEGIBLE = 3f

/** Where a baseline sits within a line box, near enough. */
private const val BASELINE_FRACTION = 0.82f

private const val STAMP_ALPHA = 0.16f
private const val STAMP_FRACTION = 0.11f
private const val STAMP_TRACKING = 2f
private const val STAMP_DEGREES = 24f

/** Down the page, as a fraction of its height. */
private const val STAMP_HEIGHT = 0.52f

/** The widest a stamp may be, as a share of the page. */
private const val STAMP_MAX_WIDTH = 0.78f
