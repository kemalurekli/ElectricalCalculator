package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Three sheets of paper, fanned.
 *
 * ### What this replaces and why
 *
 * A paywall opened from Settings used to show the schedule preview: a sample
 * table with invented circuits — Lighting, Sockets, Oven — standing in for work
 * the reader had not done. It asked somebody to imagine themselves into
 * somebody else's job, which is the weakest thing a preview can do, and it put
 * four made-up numbers on a screen whose whole problem is being believed.
 *
 * There is nothing real to show on that path, because the reader has not asked
 * for a document yet. So it shows no figures at all: three sheets, the front
 * one ruled, catching the same gold light as the mark above them. It says
 * "documents" without pretending to be one.
 *
 * The calculator path keeps the real thing, which is the point of that path.
 *
 * ### Drawn rather than an asset
 *
 * `docs/design-language.md` says the app has no illustration. This is the
 * narrowest possible exception and it stays inside the rules that matter: it is
 * geometry with no words in it, it takes its colours from the panel it sits on,
 * and it scales without blurring — the same argument the electrical symbols
 * make for being drawn.
 */
@Composable
internal fun PaperStack(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ASPECT),
    ) {
        val sheet = Size(size.width * SHEET_WIDTH, size.height * SHEET_HEIGHT)
        val centre = Offset((size.width - sheet.width) / 2f, (size.height - sheet.height) / 2f)

        // Back to front, so each sheet's shadow-side edge is covered by the one
        // in front of it.
        TILTS.forEachIndexed { index, tilt ->
            val front = index == TILTS.lastIndex
            rotate(degrees = tilt, pivot = Offset(size.width / 2f, size.height)) {
                drawRoundRect(
                    color = if (front) PAPER_FRONT else PAPER_BACK,
                    topLeft = centre,
                    size = sheet,
                    cornerRadius = CornerRadius(CORNER, CORNER),
                )
                if (front) drawRules(centre, sheet)
            }
        }
    }
}

/** The lines of a page, without a single word on them. */
private fun DrawScope.drawRules(topLeft: Offset, sheet: Size) {
    val margin = sheet.width * RULE_MARGIN
    val top = topLeft.y + sheet.height * RULE_TOP
    val step = sheet.height * RULE_STEP

    repeat(RULE_COUNT) { index ->
        // The first rule is the title: shorter, heavier, and sitting apart
        // from the paragraph under it.
        val title = index == 0
        val width = when {
            title -> sheet.width * 0.42f
            index == RULE_COUNT - 1 -> (sheet.width - 2 * margin) * 0.55f
            else -> sheet.width - 2 * margin
        }
        val y = top + if (title) 0f else step * (index + 0.6f)
        drawRoundRect(
            color = if (title) RULE_TITLE else RULE_LINE,
            topLeft = Offset(topLeft.x + margin, y),
            size = Size(width, if (title) TITLE_WEIGHT else RULE_WEIGHT),
            cornerRadius = CornerRadius(RULE_WEIGHT, RULE_WEIGHT),
        )
    }
}

/** Degrees, back sheet first. The front one sits square. */
private val TILTS = listOf(-7f, 4f, 0f)

private const val ASPECT = 1.5f
private const val SHEET_WIDTH = 0.52f
private const val SHEET_HEIGHT = 0.84f
private const val CORNER = 10f

private val PAPER_FRONT = Color(0xFFF7F9FB)
private val PAPER_BACK = Color(0x40FFFFFF)
private val RULE_TITLE = Color(0xFF1D4C70)
private val RULE_LINE = Color(0xFFC3CCD4)

private const val RULE_MARGIN = 0.12f
private const val RULE_TOP = 0.16f
private const val RULE_STEP = 0.11f
private const val RULE_COUNT = 6
private const val RULE_WEIGHT = 3f
private const val TITLE_WEIGHT = 6f
