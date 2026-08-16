package com.kemalurekli.electricalcalculator.core.designsystem.diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.FormulaTextStyle

/**
 * A labelled circuit diagram.
 *
 * ### Coordinates
 *
 * Positions are normalised: x and y both run 0..1 across the box the diagram is
 * given. A diagram is therefore written once and is correct at every width, which
 * is the thing the 48 × 48 viewport in `SymbolDsl` cannot do — a star-delta
 * network wants to be wide and a divider wants to be tall, and both have to sit
 * in the same column of the same screen.
 *
 * Element *sizes* are not normalised, or a resistor would stretch with the aspect
 * ratio. They are derived from the shorter side, so proportions hold.
 *
 * ### Why the content block is not composable
 *
 * `content` runs inside a `DrawScope`, which is not a composition, so a diagram
 * body cannot read `MaterialTheme`. That is deliberate rather than a limitation
 * worked around: colours are resolved once into [DiagramPalette] and handed in,
 * which keeps every diagram definition plain Kotlin — previewable, movable, and
 * impossible to accidentally couple to a screen's theme.
 *
 * @param contentDescription what the circuit is, in a sentence. Required, and a
 *   string resource at every call site: a picture of a circuit tells a reader who
 *   cannot see it nothing at all unless someone writes down what it shows. The
 *   canvas is one semantics node carrying this and nothing else, because the
 *   labels inside it are drawn text and would otherwise be announced as loose
 *   fragments in painting order.
 * @param aspectRatio width divided by height. Wider than tall for most circuits.
 */
@Composable
fun ElecCircuitDiagram(
    contentDescription: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = DEFAULT_ASPECT_RATIO,
    palette: DiagramPalette = DiagramPalette.default(),
    content: CircuitScope.() -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = FormulaTextStyle.copy(
        fontSize = LABEL_SIZE_SP.sp,
        lineHeight = LABEL_LINE_HEIGHT_SP.sp,
    )
    val padding = ElecTheme.spacing.sm

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            // The diagram paints its own ground in the same colour elements use
            // to mask the wire behind them. Matching them here rather than
            // hoping the caller's card happens to agree is what stops a
            // resistor reading as a grey patch on a lighter card.
            .background(color = palette.surface, shape = MaterialTheme.shapes.medium)
            .padding(padding)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
    ) {
        CircuitScope(this, measurer, palette, labelStyle).content()
    }
}

/**
 * The colours a diagram draws with.
 *
 * Five roles rather than one tint, because that separation is most of what makes
 * a multi-element drawing readable: wires recede, elements sit forward, a current
 * arrow stands out, labels read as text rather than as part of the circuit, and
 * [surface] is the colour an element paints over the wire behind it.
 */
@Immutable
data class DiagramPalette(
    val wire: Color,
    val element: Color,
    val accent: Color,
    val label: Color,
    val surface: Color,
) {
    companion object {
        /** The palette a diagram uses when a caller has no reason to override it. */
        @Composable
        fun default() = DiagramPalette(
            wire = MaterialTheme.colorScheme.onSurfaceVariant,
            element = MaterialTheme.colorScheme.onSurface,
            accent = MaterialTheme.colorScheme.primary,
            label = MaterialTheme.colorScheme.onSurfaceVariant,
            surface = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
    }
}

/**
 * The drawing surface a diagram body is written against.
 *
 * Every position is normalised — see [ElecCircuitDiagram]. Call [wire] first and
 * drop elements on top: an element paints its own background, so wires do not
 * need to stop short of anything.
 */
class CircuitScope internal constructor(
    private val draw: DrawScope,
    private val measurer: TextMeasurer,
    private val palette: DiagramPalette,
    private val labelStyle: TextStyle,
) {
    /** The size elements are drawn at, derived from the shorter side. */
    private val body: Float = minOf(draw.size.width, draw.size.height) * BODY_FRACTION

    private val stroke: Float = with(draw) { STROKE_DP.dp.toPx() }

    private fun at(point: Offset) = Offset(point.x * draw.size.width, point.y * draw.size.height)

    /**
     * A run of wire through the given points, in order.
     *
     * Takes a list rather than a vararg because [Offset] is a value class and
     * Kotlin will not spread one.
     */
    fun wire(points: List<Offset>) {
        draw.drawWire(points.map(::at), palette.wire, stroke)
    }

    /** A single straight segment — the common case, without the list. */
    fun wire(from: Offset, to: Offset) {
        wire(listOf(from, to))
    }

    /** A junction: three or more conductors meeting. */
    fun node(point: Offset) {
        draw.drawNode(at(point), palette.wire, stroke)
    }

    /** Two conductors crossing without connecting. */
    fun crossing(point: Offset) {
        draw.drawCrossing(at(point), palette.surface, stroke)
    }

    fun resistor(point: Offset, facing: Axis = Axis.VERTICAL, label: String? = null) {
        draw.drawResistor(at(point), facing, body, palette.element, palette.surface, stroke)
        label?.let { labelBeside(point, facing, it, RESISTOR_REACH) }
    }

    fun source(
        point: Offset,
        kind: SourceKind = SourceKind.DC,
        facing: Axis = Axis.VERTICAL,
        label: String? = null,
    ) {
        when (kind) {
            SourceKind.DC ->
                draw.drawDcSource(at(point), facing, body, palette.element, palette.surface, stroke)
            SourceKind.AC ->
                draw.drawAcSource(at(point), body, palette.element, palette.surface, stroke)
        }
        label?.let { labelBeside(point, facing, it, SOURCE_REACH) }
    }

    fun switchOpen(point: Offset, facing: Axis = Axis.HORIZONTAL, label: String? = null) {
        draw.drawOpenSwitch(at(point), facing, body, palette.element, palette.surface, stroke)
        label?.let { labelBeside(point, facing, it, SWITCH_REACH) }
    }

    /** An ideal current source: what a Norton equivalent is built around. */
    fun currentSource(point: Offset, facing: Axis = Axis.VERTICAL, label: String? = null) {
        draw.drawCurrentSource(at(point), facing, body, palette.element, palette.surface, stroke)
        label?.let { labelBeside(point, facing, it, SOURCE_REACH) }
    }

    fun capacitor(point: Offset, facing: Axis = Axis.VERTICAL, label: String? = null) {
        draw.drawCapacitor(at(point), facing, body, palette.element, palette.surface, stroke)
        label?.let { labelBeside(point, facing, it, CAPACITOR_REACH) }
    }

    fun inductor(point: Offset, facing: Axis = Axis.VERTICAL, label: String? = null) {
        draw.drawInductor(at(point), facing, body, palette.element, palette.surface, stroke)
        label?.let { labelBeside(point, facing, it, INDUCTOR_REACH) }
    }

    /**
     * A waveform.
     *
     * [box] is in normalised coordinates like everything else: top-left and
     * bottom-right of the area the cycle fills.
     */
    fun sine(from: Offset, to: Offset, cycles: Float = 1f) {
        val a = at(from)
        val b = at(to)
        draw.drawSine(
            box = androidx.compose.ui.geometry.Rect(a.x, a.y, b.x, b.y),
            cycles = cycles,
            color = palette.accent,
            axisColor = palette.wire,
            stroke = stroke,
        )
    }

    /**
     * A dashed rule marking a level on a waveform.
     *
     * Carries no label of its own: levels that sit close together need their
     * labels placed deliberately, and a label hung off the end of the longest
     * rule runs off the edge. Call [label] for them.
     */
    fun level(from: Offset, to: Offset) {
        draw.drawLevel(at(from), at(to), palette.wire, stroke)
    }

    fun ground(point: Offset) {
        draw.drawGround(at(point), body, palette.wire, stroke)
    }

    /** A current arrow, drawn in the accent colour so it reads over the circuit. */
    fun currentArrow(point: Offset, towards: Direction, label: String? = null) {
        draw.drawArrow(at(point), towards, body, palette.accent, stroke)
        label?.let {
            val vertical = towards == Direction.UP || towards == Direction.DOWN
            drawLabel(point, if (vertical) LabelAlign.END else LabelAlign.ABOVE, it, palette.accent)
        }
    }

    /** A measured span, for a voltage taken between two points. */
    fun voltageSpan(from: Offset, to: Offset, label: String) {
        draw.drawSpan(at(from), at(to), body, palette.accent, stroke)
        val midpoint = Offset((from.x + to.x) / 2, (from.y + to.y) / 2)
        val vertical = kotlin.math.abs(to.y - from.y) > kotlin.math.abs(to.x - from.x)
        drawLabel(midpoint, if (vertical) LabelAlign.END else LabelAlign.ABOVE, label, palette.accent)
    }

    /** Free text, for anything the elements do not name themselves. */
    fun label(
        point: Offset,
        text: String,
        align: LabelAlign = LabelAlign.BELOW,
        emphasised: Boolean = false,
    ) {
        drawLabel(point, align, text, if (emphasised) palette.accent else palette.label)
    }

    /**
     * An element's own label: above a horizontal body, beside a vertical one.
     *
     * [reach] is how far the element itself extends from its centre, as a
     * fraction of the element size. A battery is twice as wide as a resistor is
     * thick, so one fixed clearance either overlaps the plates or leaves the
     * resistor's label floating away from it.
     */
    private fun labelBeside(point: Offset, facing: Axis, text: String, reach: Float) {
        val align = if (facing == Axis.HORIZONTAL) LabelAlign.ABOVE else LabelAlign.END
        drawLabel(point, align, text, palette.label, reach)
    }

    private fun drawLabel(
        point: Offset,
        align: LabelAlign,
        text: String,
        color: Color,
        reach: Float = ARROW_REACH,
    ) {
        val layout = measurer.measure(text, labelStyle.copy(color = color, fontWeight = FontWeight.Medium))
        val anchor = at(point)
        val width = layout.size.width.toFloat()
        val height = layout.size.height.toFloat()
        val clearance = body * (reach + LABEL_GAP)

        val topLeft = when (align) {
            LabelAlign.ABOVE -> Offset(anchor.x - width / 2, anchor.y - clearance - height)
            LabelAlign.BELOW -> Offset(anchor.x - width / 2, anchor.y + clearance)
            LabelAlign.START -> Offset(anchor.x - clearance - width, anchor.y - height / 2)
            LabelAlign.END -> Offset(anchor.x + clearance, anchor.y - height / 2)
        }
        draw.drawText(layout, topLeft = topLeft)
    }
}

private const val DEFAULT_ASPECT_RATIO = 1.8f
private const val BODY_FRACTION = 0.22f
private const val STROKE_DP = 2f
private const val LABEL_SIZE_SP = 11
private const val LABEL_LINE_HEIGHT_SP = 14

// How far each element reaches from its centre, as a fraction of the element
// size, so a label clears the thing it names rather than sitting on it.
private const val RESISTOR_REACH = 0.24f
private const val SOURCE_REACH = 0.50f
private const val SWITCH_REACH = 0.55f
private const val CAPACITOR_REACH = 0.46f
private const val INDUCTOR_REACH = 0.30f
private const val ARROW_REACH = 0.20f

private const val LABEL_GAP = 0.16f
