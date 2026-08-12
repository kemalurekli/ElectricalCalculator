package com.kemalurekli.electricalcalculator.core.designsystem.symbol

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The drawing primitives every electrical symbol is built from.
 *
 * ### Why these are drawn and not shipped as assets
 *
 * IEC 60617 is a copyrighted database. The symbols themselves are functional
 * geometric shapes — a circle, a line, an arc — redrawn in every CAD package
 * and every textbook there has ever been, so they are drawn here from the
 * standard's geometric description rather than copied from its artwork. The
 * screen says "drawn to IEC 60617", never "is IEC 60617", which is the same
 * distinction this project already draws for IEC 61386 and 61537.
 *
 * Drawing them also buys three things an asset pipeline does not: they take the
 * theme colour, so they are legible in dark mode without a second set; they
 * scale to any density without blurring; and they look like one family, because
 * they share a grid and a stroke width rather than being traced by different
 * hands.
 *
 * ### The grid
 *
 * Every symbol is drawn in a 48 × 48 viewport with the element centred and the
 * connecting terminals reaching the edges at the midpoints — [LEFT], [RIGHT],
 * [TOP], [BOTTOM]. Keeping the terminals on those lines is what lets symbols sit
 * next to each other in a grid without looking hand-placed.
 */

/** The viewport every symbol is drawn in. */
const val SYMBOL_SIZE = 48f

/** The centre line, horizontally and vertically. */
const val MID = SYMBOL_SIZE / 2f

const val LEFT = 0f
const val RIGHT = SYMBOL_SIZE
const val TOP = 0f
const val BOTTOM = SYMBOL_SIZE

private const val STROKE_WIDTH = 2.4f

/**
 * Builds one symbol.
 *
 * The stroke is declared black and re-tinted by the caller, which is how a
 * vector picks up `onSurface` in light mode and its opposite in dark.
 */
internal fun symbol(name: String, content: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = SYMBOL_SIZE,
        viewportHeight = SYMBOL_SIZE,
    )
        .path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WIDTH,
            strokeLineCap = StrokeCap.Round,
            pathBuilder = content,
        )
        .build()

/** A straight segment. */
internal fun PathBuilder.line(x1: Float, y1: Float, x2: Float, y2: Float) {
    moveTo(x1, y1)
    lineTo(x2, y2)
}

/** The horizontal terminals most two-pole symbols need. */
internal fun PathBuilder.terminalsHorizontal(inset: Float) {
    line(LEFT, MID, inset, MID)
    line(SYMBOL_SIZE - inset, MID, RIGHT, MID)
}

/** The vertical equivalent, for symbols drawn on a vertical run. */
internal fun PathBuilder.terminalsVertical(inset: Float) {
    line(MID, TOP, MID, inset)
    line(MID, SYMBOL_SIZE - inset, MID, BOTTOM)
}

/** A rectangle given its centre and half-extents. */
internal fun PathBuilder.box(halfWidth: Float, halfHeight: Float, cx: Float = MID, cy: Float = MID) {
    moveTo(cx - halfWidth, cy - halfHeight)
    lineTo(cx + halfWidth, cy - halfHeight)
    lineTo(cx + halfWidth, cy + halfHeight)
    lineTo(cx - halfWidth, cy + halfHeight)
    close()
}

/**
 * A circle, as two half-arcs.
 *
 * `arcTo` cannot sweep a full turn in one call — a 360° arc has the same start
 * and end point and is discarded — so every circle here is two 180° sweeps.
 */
internal fun PathBuilder.circle(radius: Float, cx: Float = MID, cy: Float = MID) {
    moveTo(cx - radius, cy)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, cx + radius, cy)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, cx - radius, cy)
}

/** A half-turn arc from one point to another, bulging by [radius]. */
internal fun PathBuilder.arc(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    radius: Float,
    positive: Boolean = true,
) {
    moveTo(fromX, fromY)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = positive, toX, toY)
}

/**
 * A run of `count` bumps along a horizontal line — the winding of a coil.
 *
 * Drawn as semicircles rather than a spiral because that is how IEC 60617 shows
 * an inductance, and because a spiral at 48 px reads as a smudge.
 */
internal fun PathBuilder.coilHorizontal(
    startX: Float,
    y: Float,
    count: Int,
    bumpWidth: Float,
    upwards: Boolean = true,
) {
    var x = startX
    repeat(count) {
        arc(x, y, x + bumpWidth, y, bumpWidth / 2f, positive = upwards)
        x += bumpWidth
    }
}

/** The same, vertical: used for transformer windings drawn side by side. */
internal fun PathBuilder.coilVertical(
    x: Float,
    startY: Float,
    count: Int,
    bumpHeight: Float,
    rightwards: Boolean = true,
) {
    var y = startY
    repeat(count) {
        arc(x, y, x, y + bumpHeight, bumpHeight / 2f, positive = rightwards)
        y += bumpHeight
    }
}

/** An arrowhead at the end of a line, pointing along (dx, dy). */
internal fun PathBuilder.arrowHead(x: Float, y: Float, dx: Float, dy: Float, size: Float = 5f) {
    // Perpendicular, for the two barbs.
    val px = -dy
    val py = dx
    moveTo(x, y)
    lineTo(x - dx * size + px * size * 0.5f, y - dy * size + py * size * 0.5f)
    moveTo(x, y)
    lineTo(x - dx * size - px * size * 0.5f, y - dy * size - py * size * 0.5f)
}

/** A cross, used for the "not connected" and multiplier marks. */
internal fun PathBuilder.cross(halfSize: Float, cx: Float = MID, cy: Float = MID) {
    line(cx - halfSize, cy - halfSize, cx + halfSize, cy + halfSize)
    line(cx - halfSize, cy + halfSize, cx + halfSize, cy - halfSize)
}
