package com.kemalurekli.electricalcalculator.core.designsystem.diagram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The glyphs a circuit diagram is built from, drawn straight onto a [DrawScope].
 *
 * ### Why these are drawn rather than reused from `SymbolDsl`
 *
 * `core/designsystem/symbol` builds `ImageVector`s, and an `ImageVector` is a
 * compile-time asset: it cannot carry "R₁ = 10 Ω" written with the reader's
 * decimal separator, and `Icon(tint = …)` paints every path in it one colour, so
 * a current arrow could not be accented against the wires it sits beside. Those
 * two limits are exactly what a labelled, multi-element diagram needs, so this
 * file draws instead.
 *
 * What it does keep is that file's *geometry*: IEC forms — a resistor is a plain
 * rectangle, not an ANSI zig-zag — round caps, and a stroke of the same visual
 * weight. A diagram and the symbol grid in the reference topics should look like
 * they came from one hand.
 *
 * ### The painting order that makes this work
 *
 * Elements paint a filled body in the surface colour before they stroke their
 * outline. That lets a caller run a wire straight through where an element will
 * sit and then drop the element on top, instead of computing wire segments that
 * stop short at each terminal. Authoring a diagram becomes "draw the loop, then
 * put things on it", which is how someone sketches one on paper.
 */

/** Which way an element lies. */
enum class Axis { HORIZONTAL, VERTICAL }

/** What kind of supply a source represents. */
enum class SourceKind { DC, AC }

/** Which way a current arrow points. */
enum class Direction { LEFT, RIGHT, UP, DOWN }

/** Where a label sits relative to the thing it names. */
enum class LabelAlign { ABOVE, BELOW, START, END }

internal fun DrawScope.drawWire(points: List<Offset>, color: Color, stroke: Float) {
    if (points.size < 2) return
    for (index in 0 until points.lastIndex) {
        drawLine(
            color = color,
            start = points[index],
            end = points[index + 1],
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/** A junction dot. Only drawn where wires genuinely connect — see [drawCrossing]. */
internal fun DrawScope.drawNode(at: Offset, color: Color, stroke: Float) {
    drawCircle(color = color, radius = stroke * NODE_RADIUS_IN_STROKES, center = at)
}

/**
 * Two wires passing without touching.
 *
 * Drawn as a gap in the horizontal run rather than a hop, following IEC practice:
 * a hop and a junction dot are the two marks a reader has to tell apart at a
 * glance, and a gap cannot be mistaken for either.
 */
internal fun DrawScope.drawCrossing(at: Offset, background: Color, stroke: Float) {
    drawCircle(color = background, radius = stroke * CROSSING_RADIUS_IN_STROKES, center = at)
}

internal fun DrawScope.drawResistor(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val rect = bodyRect(at, facing, body)
    drawRect(color = background, topLeft = rect.topLeft, size = rect.size)
    drawRect(
        color = color,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = stroke),
    )
}

/**
 * A battery: two cells, long plate positive.
 *
 * The generic circle-with-a-line source is more correct for an abstract topic,
 * but a reader at foundation level is being shown *a circuit*, and a battery is
 * the one supply symbol that needs no key.
 */
internal fun DrawScope.drawDcSource(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val longPlate = body * DC_LONG_PLATE
    val shortPlate = body * DC_SHORT_PLATE
    val gap = body * DC_PLATE_GAP

    // Clear the wire behind the whole symbol so the plates read as a break in it.
    val clear = bodyRect(at, facing, body * DC_CLEAR_SPAN)
    drawRect(color = background, topLeft = clear.topLeft, size = clear.size)

    when (facing) {
        Axis.VERTICAL -> {
            drawLine(
                color, Offset(at.x - longPlate / 2, at.y - gap / 2),
                Offset(at.x + longPlate / 2, at.y - gap / 2), stroke, StrokeCap.Round,
            )
            drawLine(
                color, Offset(at.x - shortPlate / 2, at.y + gap / 2),
                Offset(at.x + shortPlate / 2, at.y + gap / 2), stroke * PLATE_WEIGHT,
                StrokeCap.Butt,
            )
        }
        Axis.HORIZONTAL -> {
            drawLine(
                color, Offset(at.x - gap / 2, at.y - longPlate / 2),
                Offset(at.x - gap / 2, at.y + longPlate / 2), stroke, StrokeCap.Round,
            )
            drawLine(
                color, Offset(at.x + gap / 2, at.y - shortPlate / 2),
                Offset(at.x + gap / 2, at.y + shortPlate / 2), stroke * PLATE_WEIGHT,
                StrokeCap.Butt,
            )
        }
    }
}

/** A circle with a sine through it — the IEC alternating supply. */
internal fun DrawScope.drawAcSource(
    at: Offset,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val radius = body * AC_RADIUS
    drawCircle(color = background, radius = radius, center = at)
    drawCircle(color = color, radius = radius, center = at, style = Stroke(width = stroke))

    val span = radius * AC_SINE_SPAN
    val amplitude = radius * AC_SINE_AMPLITUDE
    val path = Path().apply {
        moveTo(at.x - span, at.y)
        cubicTo(
            at.x - span / 2, at.y - amplitude * CUBIC_LIFT,
            at.x - span / 2, at.y - amplitude * CUBIC_LIFT,
            at.x, at.y,
        )
        cubicTo(
            at.x + span / 2, at.y + amplitude * CUBIC_LIFT,
            at.x + span / 2, at.y + amplitude * CUBIC_LIFT,
            at.x + span, at.y,
        )
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/** An open switch: a contact hinged off the line. */
internal fun DrawScope.drawOpenSwitch(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val half = body / 2
    val lift = body * SWITCH_LIFT
    val clear = bodyRect(at, facing, body)
    drawRect(color = background, topLeft = clear.topLeft, size = clear.size)

    val (hinge, tip, rest) = when (facing) {
        Axis.HORIZONTAL -> Triple(
            Offset(at.x - half, at.y),
            Offset(at.x + half * SWITCH_REACH, at.y - lift),
            Offset(at.x + half, at.y),
        )
        Axis.VERTICAL -> Triple(
            Offset(at.x, at.y - half),
            Offset(at.x + lift, at.y + half * SWITCH_REACH),
            Offset(at.x, at.y + half),
        )
    }
    drawLine(color, hinge, tip, stroke, StrokeCap.Round)
    drawNode(hinge, color, stroke)
    drawNode(rest, color, stroke)
}

/** Two parallel plates, the gap across the conductor — a capacitor. */
internal fun DrawScope.drawCapacitor(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val plate = body * CAP_PLATE
    val gap = body * CAP_GAP
    val clear = bodyRect(at, facing, body * CAP_CLEAR_SPAN)
    drawRect(color = background, topLeft = clear.topLeft, size = clear.size)

    when (facing) {
        Axis.VERTICAL -> listOf(-gap / 2, gap / 2).forEach { dy ->
            drawLine(
                color, Offset(at.x - plate / 2, at.y + dy), Offset(at.x + plate / 2, at.y + dy),
                stroke, StrokeCap.Round,
            )
        }
        Axis.HORIZONTAL -> listOf(-gap / 2, gap / 2).forEach { dx ->
            drawLine(
                color, Offset(at.x + dx, at.y - plate / 2), Offset(at.x + dx, at.y + plate / 2),
                stroke, StrokeCap.Round,
            )
        }
    }
}

/**
 * A run of half-circles — an inductor.
 *
 * The IEC form is a series of semicircular bumps on one side of the line rather
 * than the ANSI spiral, matching `SingleLineSymbols.Inductor`.
 */
internal fun DrawScope.drawInductor(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val span = body * COIL_SPAN
    val bump = span / COIL_BUMPS
    val radius = bump / 2f
    val clear = bodyRect(at, facing, span)
    drawRect(color = background, topLeft = clear.topLeft, size = clear.size)

    val path = Path()
    when (facing) {
        Axis.HORIZONTAL -> {
            val start = at.x - span / 2
            path.moveTo(start, at.y)
            for (index in 0 until COIL_BUMPS) {
                val from = start + bump * index
                path.arcTo(
                    rect = Rect(
                        offset = Offset(from, at.y - radius),
                        size = Size(bump, bump),
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false,
                )
            }
        }
        Axis.VERTICAL -> {
            val start = at.y - span / 2
            path.moveTo(at.x, start)
            for (index in 0 until COIL_BUMPS) {
                val from = start + bump * index
                path.arcTo(
                    rect = Rect(
                        offset = Offset(at.x - radius, from),
                        size = Size(bump, bump),
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false,
                )
            }
        }
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/**
 * One cycle of a sine, drawn inside a box, with a zero line.
 *
 * Sampled rather than assembled from arcs: a sine is not two half-circles, and at
 * this size the difference between the two is visible at the crests.
 */
internal fun DrawScope.drawSine(
    box: Rect,
    cycles: Float,
    color: Color,
    axisColor: Color,
    stroke: Float,
) {
    val midY = box.center.y
    drawLine(
        axisColor, Offset(box.left, midY), Offset(box.right, midY),
        stroke * SINE_AXIS_WEIGHT, StrokeCap.Round,
    )

    val amplitude = box.height / 2f
    val path = Path()
    for (step in 0..SINE_SAMPLES) {
        val t = step.toFloat() / SINE_SAMPLES
        val x = box.left + box.width * t
        val y = midY - amplitude * kotlin.math.sin(TWO_PI * cycles * t)
        if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/** A dashed horizontal rule, for marking a level on a waveform. */
internal fun DrawScope.drawLevel(
    from: Offset,
    to: Offset,
    color: Color,
    stroke: Float,
) {
    val steps = LEVEL_DASHES
    val dx = (to.x - from.x) / steps
    for (index in 0 until steps step 2) {
        drawLine(
            color,
            Offset(from.x + dx * index, from.y),
            Offset(from.x + dx * (index + 1), to.y),
            stroke * LEVEL_WEIGHT,
            StrokeCap.Round,
        )
    }
}

/**
 * A circle with an arrow through it — the IEC ideal current source.
 *
 * Distinguished from the voltage source by what is inside the circle, which is
 * the only difference IEC 60617 draws between them either.
 */
internal fun DrawScope.drawCurrentSource(
    at: Offset,
    facing: Axis,
    body: Float,
    color: Color,
    background: Color,
    stroke: Float,
) {
    val radius = body * AC_RADIUS
    drawCircle(color = background, radius = radius, center = at)
    drawCircle(color = color, radius = radius, center = at, style = Stroke(width = stroke))

    val reach = radius * SOURCE_ARROW_REACH
    val towards = if (facing == Axis.VERTICAL) Direction.UP else Direction.RIGHT
    drawArrow(at, towards, reach * 2f / ARROW_LENGTH, color, stroke)
}

/** Three shortening bars — earth. */
internal fun DrawScope.drawGround(at: Offset, body: Float, color: Color, stroke: Float) {
    val widths = listOf(body * GROUND_WIDE, body * GROUND_MID, body * GROUND_NARROW)
    val gap = body * GROUND_GAP
    widths.forEachIndexed { index, width ->
        val y = at.y + gap * index
        drawLine(
            color, Offset(at.x - width / 2, y), Offset(at.x + width / 2, y),
            stroke, StrokeCap.Round,
        )
    }
}

/** An arrow, used for current direction. Drawn in the accent colour by the caller. */
internal fun DrawScope.drawArrow(
    at: Offset,
    towards: Direction,
    body: Float,
    color: Color,
    stroke: Float,
) {
    val length = body * ARROW_LENGTH
    val head = body * ARROW_HEAD
    val (from, to) = when (towards) {
        Direction.RIGHT -> Offset(at.x - length / 2, at.y) to Offset(at.x + length / 2, at.y)
        Direction.LEFT -> Offset(at.x + length / 2, at.y) to Offset(at.x - length / 2, at.y)
        Direction.DOWN -> Offset(at.x, at.y - length / 2) to Offset(at.x, at.y + length / 2)
        Direction.UP -> Offset(at.x, at.y + length / 2) to Offset(at.x, at.y - length / 2)
    }
    drawLine(color, from, to, stroke, StrokeCap.Round)

    val dx = to.x - from.x
    val dy = to.y - from.y
    val norm = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
    val ux = dx / norm
    val uy = dy / norm
    drawLine(
        color, to, Offset(to.x - head * (ux + uy * ARROW_SPREAD), to.y - head * (uy - ux * ARROW_SPREAD)),
        stroke, StrokeCap.Round,
    )
    drawLine(
        color, to, Offset(to.x - head * (ux - uy * ARROW_SPREAD), to.y - head * (uy + ux * ARROW_SPREAD)),
        stroke, StrokeCap.Round,
    )
}

/** A double-headed measurement span, for a voltage taken between two points. */
internal fun DrawScope.drawSpan(
    from: Offset,
    to: Offset,
    body: Float,
    color: Color,
    stroke: Float,
) {
    val tick = body * SPAN_TICK
    drawLine(color, from, to, stroke * SPAN_WEIGHT, StrokeCap.Round)

    val vertical = kotlin.math.abs(to.y - from.y) > kotlin.math.abs(to.x - from.x)
    listOf(from to (if (vertical) Direction.UP else Direction.LEFT),
           to to (if (vertical) Direction.DOWN else Direction.RIGHT)).forEach { (point, _) ->
        if (vertical) {
            drawLine(
                color, Offset(point.x - tick, point.y), Offset(point.x + tick, point.y),
                stroke * SPAN_WEIGHT, StrokeCap.Round,
            )
        } else {
            drawLine(
                color, Offset(point.x, point.y - tick), Offset(point.x, point.y + tick),
                stroke * SPAN_WEIGHT, StrokeCap.Round,
            )
        }
    }
}

private fun bodyRect(at: Offset, facing: Axis, body: Float): Rect {
    val long = body
    val short = body * BODY_ASPECT
    return when (facing) {
        Axis.HORIZONTAL -> Rect(
            offset = Offset(at.x - long / 2, at.y - short / 2),
            size = Size(long, short),
        )
        Axis.VERTICAL -> Rect(
            offset = Offset(at.x - short / 2, at.y - long / 2),
            size = Size(short, long),
        )
    }
}

// Every constant is a fraction of the diagram's element size, so a diagram keeps
// its proportions at any width. See CircuitScope for how that size is derived.
private const val BODY_ASPECT = 0.42f
private const val NODE_RADIUS_IN_STROKES = 1.7f
private const val CROSSING_RADIUS_IN_STROKES = 2.4f
private const val DC_LONG_PLATE = 0.9f
private const val DC_SHORT_PLATE = 0.45f
private const val DC_PLATE_GAP = 0.28f
private const val DC_CLEAR_SPAN = 0.6f
private const val PLATE_WEIGHT = 2.2f
private const val AC_RADIUS = 0.5f
private const val SOURCE_ARROW_REACH = 0.62f
private const val AC_SINE_SPAN = 0.62f
private const val AC_SINE_AMPLITUDE = 0.5f
private const val CUBIC_LIFT = 1.6f
private const val SWITCH_LIFT = 0.5f
private const val SWITCH_REACH = 0.85f
private const val GROUND_WIDE = 0.8f
private const val GROUND_MID = 0.5f
private const val GROUND_NARROW = 0.22f
private const val GROUND_GAP = 0.18f
private const val ARROW_LENGTH = 0.9f
private const val ARROW_HEAD = 0.3f
private const val ARROW_SPREAD = 0.55f
private const val SPAN_TICK = 0.18f
private const val SPAN_WEIGHT = 0.8f
private const val CAP_PLATE = 0.85f
private const val CAP_GAP = 0.22f
private const val CAP_CLEAR_SPAN = 0.5f
private const val COIL_SPAN = 1.1f
private const val COIL_BUMPS = 4
private const val SINE_SAMPLES = 96
private const val SINE_AXIS_WEIGHT = 0.6f
private const val LEVEL_DASHES = 16
private const val LEVEL_WEIGHT = 0.7f
private const val TWO_PI = (2.0 * Math.PI).toFloat()
