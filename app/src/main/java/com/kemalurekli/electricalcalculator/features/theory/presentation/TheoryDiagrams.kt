package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.diagram.Axis
import com.kemalurekli.electricalcalculator.core.designsystem.diagram.Direction
import com.kemalurekli.electricalcalculator.core.designsystem.diagram.ElecCircuitDiagram
import com.kemalurekli.electricalcalculator.core.designsystem.diagram.LabelAlign
import com.kemalurekli.electricalcalculator.core.designsystem.diagram.SourceKind
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryDiagram

/**
 * Every circuit the theory shelf draws.
 *
 * The `when` is exhaustive, so a [TheoryDiagram] added without a drawing is a
 * build error. That is the whole enforcement — no test is needed for it, and none
 * exists.
 *
 * ### Labels
 *
 * [labels] carries the reader's own numbers, already formatted, keyed by a slot
 * name each drawing knows. A slot with nothing in it falls back to its symbol, so
 * the diagram is readable before anything has been calculated and gains the
 * figures once it has. That fallback is what lets one drawing serve both states
 * instead of there being an empty variant and a filled one.
 */
@Composable
internal fun TheoryDiagramFigure(
    diagram: TheoryDiagram,
    labels: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    fun slot(key: String, fallback: String) = labels[key] ?: fallback

    when (diagram) {
        TheoryDiagram.SIMPLE_LOOP -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_simple_loop_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(
                listOf(
                    Offset(0.15f, 0.72f), Offset(0.15f, 0.22f),
                    Offset(0.85f, 0.22f), Offset(0.85f, 0.72f),
                    Offset(0.15f, 0.72f),
                ),
            )
            source(Offset(0.15f, 0.47f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))
            switchOpen(Offset(0.42f, 0.22f), Axis.HORIZONTAL)
            resistor(Offset(0.85f, 0.47f), Axis.VERTICAL, slot("r", "R"))
            currentArrow(Offset(0.66f, 0.22f), Direction.RIGHT, slot("i", "I"))
        }

        TheoryDiagram.SERIES_RESISTORS -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_series_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(
                listOf(
                    Offset(0.10f, 0.74f), Offset(0.10f, 0.26f),
                    Offset(0.90f, 0.26f), Offset(0.90f, 0.74f),
                    Offset(0.10f, 0.74f),
                ),
            )
            source(Offset(0.10f, 0.50f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))
            resistor(Offset(0.34f, 0.26f), Axis.HORIZONTAL, slot("r1", "R₁"))
            resistor(Offset(0.58f, 0.26f), Axis.HORIZONTAL, slot("r2", "R₂"))
            resistor(Offset(0.82f, 0.26f), Axis.HORIZONTAL, slot("r3", "R₃"))
            currentArrow(Offset(0.50f, 0.74f), Direction.LEFT, slot("i", "I"))
        }

        TheoryDiagram.PARALLEL_RESISTORS -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_parallel_desc),
            modifier = modifier,
            aspectRatio = 1.7f,
        ) {
            wire(listOf(Offset(0.10f, 0.72f), Offset(0.10f, 0.22f), Offset(0.88f, 0.22f)))
            wire(Offset(0.10f, 0.72f), Offset(0.88f, 0.72f))
            source(Offset(0.10f, 0.47f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))

            listOf(0.40f to "r1", 0.62f to "r2", 0.84f to "r3").forEach { (x, key) ->
                val symbol = when (key) {
                    "r1" -> "R₁"
                    "r2" -> "R₂"
                    else -> "R₃"
                }
                wire(Offset(x, 0.22f), Offset(x, 0.72f))
                resistor(Offset(x, 0.47f), Axis.VERTICAL, slot(key, symbol))
                node(Offset(x, 0.22f))
                node(Offset(x, 0.72f))
            }
            currentArrow(Offset(0.24f, 0.22f), Direction.RIGHT, slot("i", "I"))
        }

        TheoryDiagram.VOLTAGE_DIVIDER -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_voltage_divider_desc),
            modifier = modifier,
            aspectRatio = 1.5f,
        ) {
            wire(
                listOf(
                    Offset(0.12f, 0.80f), Offset(0.12f, 0.16f),
                    Offset(0.48f, 0.16f), Offset(0.48f, 0.80f),
                    Offset(0.12f, 0.80f),
                ),
            )
            source(Offset(0.12f, 0.48f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))
            resistor(Offset(0.48f, 0.32f), Axis.VERTICAL, slot("r1", "R₁"))
            resistor(Offset(0.48f, 0.64f), Axis.VERTICAL, slot("r2", "R₂"))

            node(Offset(0.48f, 0.48f))
            wire(Offset(0.48f, 0.48f), Offset(0.78f, 0.48f))
            voltageSpan(Offset(0.78f, 0.48f), Offset(0.78f, 0.80f), slot("uout", "U_out"))
            wire(Offset(0.48f, 0.80f), Offset(0.78f, 0.80f))
        }

        TheoryDiagram.CURRENT_DIVIDER -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_current_divider_desc),
            modifier = modifier,
            aspectRatio = 1.7f,
        ) {
            wire(Offset(0.08f, 0.24f), Offset(0.72f, 0.24f))
            wire(Offset(0.08f, 0.76f), Offset(0.72f, 0.76f))
            currentArrow(Offset(0.20f, 0.24f), Direction.RIGHT, slot("i", "I"))

            listOf(0.46f to "r1", 0.72f to "r2").forEach { (x, key) ->
                val symbol = if (key == "r1") "R₁" else "R₂"
                wire(Offset(x, 0.24f), Offset(x, 0.76f))
                resistor(Offset(x, 0.50f), Axis.VERTICAL, slot(key, symbol))
                node(Offset(x, 0.24f))
                node(Offset(x, 0.76f))
            }
            label(Offset(0.46f, 0.84f), slot("i1", "I₁"), LabelAlign.BELOW, emphasised = true)
            label(Offset(0.72f, 0.84f), slot("i2", "I₂"), LabelAlign.BELOW, emphasised = true)
        }

        TheoryDiagram.CONDUCTOR_RUN -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_conductor_run_desc),
            modifier = modifier,
            aspectRatio = 2.4f,
        ) {
            wire(Offset(0.08f, 0.34f), Offset(0.92f, 0.34f))
            wire(Offset(0.08f, 0.60f), Offset(0.92f, 0.60f))
            node(Offset(0.08f, 0.34f))
            node(Offset(0.08f, 0.60f))
            node(Offset(0.92f, 0.34f))
            node(Offset(0.92f, 0.60f))

            voltageSpan(Offset(0.08f, 0.82f), Offset(0.92f, 0.82f), slot("l", "L"))
            label(Offset(0.50f, 0.20f), slot("a", "A"), LabelAlign.ABOVE)
            label(Offset(0.50f, 0.47f), slot("r", "R"), LabelAlign.BELOW, emphasised = true)
        }

        TheoryDiagram.NODE_AND_LOOP -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_node_and_loop_desc),
            modifier = modifier,
            aspectRatio = 2.0f,
        ) {
            // Left: a junction. Two currents in, two out, and they balance.
            wire(Offset(0.06f, 0.50f), Offset(0.30f, 0.50f))
            wire(Offset(0.30f, 0.20f), Offset(0.30f, 0.80f))
            node(Offset(0.30f, 0.50f))
            currentArrow(Offset(0.17f, 0.50f), Direction.RIGHT, slot("i1", "I₁"))
            currentArrow(Offset(0.30f, 0.32f), Direction.UP, slot("i2", "I₂"))
            currentArrow(Offset(0.30f, 0.68f), Direction.DOWN, slot("i3", "I₃"))

            // Right: a loop. The drops around it sum to the supply.
            wire(
                listOf(
                    Offset(0.58f, 0.76f), Offset(0.58f, 0.24f),
                    Offset(0.94f, 0.24f), Offset(0.94f, 0.76f),
                    Offset(0.58f, 0.76f),
                ),
            )
            source(Offset(0.58f, 0.50f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))
            resistor(Offset(0.76f, 0.24f), Axis.HORIZONTAL, slot("u1", "U₁"))
            resistor(Offset(0.94f, 0.50f), Axis.VERTICAL, slot("u2", "U₂"))
        }

        TheoryDiagram.SINE_WAVE -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_sine_wave_desc),
            modifier = modifier,
            aspectRatio = 2.0f,
        ) {
            // The levels sit where the arithmetic puts them: RMS is 1/√2 of the
            // amplitude and the average is 2/π of it, so they land close together
            // and well below the peak. Drawing them at eyeballed heights would
            // teach the wrong proportion, which is the one thing this figure is
            // for.
            val centre = 0.50f
            val amplitude = 0.36f
            val peakY = centre - amplitude
            val rmsY = centre - amplitude * RMS_OF_PEAK
            val averageY = centre - amplitude * AVERAGE_OF_PEAK

            sine(Offset(0.08f, peakY), Offset(0.76f, centre + amplitude), cycles = 1f)
            level(Offset(0.08f, peakY), Offset(0.72f, peakY))
            level(Offset(0.08f, rmsY), Offset(0.72f, rmsY))
            level(Offset(0.08f, averageY), Offset(0.72f, averageY))

            // The labels are placed rather than hung off the line ends. At true
            // scale RMS and average are two per cent of the height apart, so
            // three labels stacked at one x would collide and one hung off the
            // longest line would run past the edge. Anchoring them all clear of
            // the wave and sending the lowest one downward spreads them without
            // moving the lines they name.
            label(Offset(0.82f, peakY), slot("peak", "Û"), LabelAlign.ABOVE)
            label(Offset(0.82f, rmsY), slot("rms", "U"), LabelAlign.ABOVE, emphasised = true)
            label(Offset(0.82f, averageY), slot("avg", "U̅"), LabelAlign.BELOW)
        }

        TheoryDiagram.AC_CAPACITOR -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_ac_capacitor_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(
                listOf(
                    Offset(0.18f, 0.74f), Offset(0.18f, 0.26f),
                    Offset(0.82f, 0.26f), Offset(0.82f, 0.74f),
                    Offset(0.18f, 0.74f),
                ),
            )
            source(Offset(0.18f, 0.50f), SourceKind.AC, Axis.VERTICAL, slot("u", "U"))
            capacitor(Offset(0.82f, 0.50f), Axis.VERTICAL, slot("c", "C"))
            currentArrow(Offset(0.50f, 0.26f), Direction.RIGHT, slot("i", "I"))
            label(Offset(0.50f, 0.82f), slot("f", "f"), LabelAlign.BELOW)
        }

        TheoryDiagram.AC_INDUCTOR -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_ac_inductor_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(
                listOf(
                    Offset(0.18f, 0.74f), Offset(0.18f, 0.26f),
                    Offset(0.82f, 0.26f), Offset(0.82f, 0.74f),
                    Offset(0.18f, 0.74f),
                ),
            )
            source(Offset(0.18f, 0.50f), SourceKind.AC, Axis.VERTICAL, slot("u", "U"))
            inductor(Offset(0.82f, 0.50f), Axis.VERTICAL, slot("l", "L"))
            currentArrow(Offset(0.50f, 0.26f), Direction.RIGHT, slot("i", "I"))
            label(Offset(0.50f, 0.82f), slot("f", "f"), LabelAlign.BELOW)
        }

        TheoryDiagram.POWER_TRIANGLE -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_power_triangle_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            // Not a circuit: the right triangle whose sides are the three powers.
            val origin = Offset(0.16f, 0.78f)
            val corner = Offset(0.74f, 0.78f)
            val apex = Offset(0.74f, 0.24f)
            wire(listOf(origin, corner, apex, origin))
            node(origin)

            label(Offset(0.45f, 0.80f), slot("p", "P"), LabelAlign.BELOW, emphasised = true)
            label(Offset(0.76f, 0.51f), slot("q", "Q"), LabelAlign.END, emphasised = true)
            label(Offset(0.43f, 0.48f), slot("s", "S"), LabelAlign.ABOVE, emphasised = true)
            label(Offset(0.25f, 0.74f), slot("phi", "φ"), LabelAlign.ABOVE)
        }

        TheoryDiagram.STAR_DELTA_SUPPLY -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_star_delta_desc),
            modifier = modifier,
            aspectRatio = 2.1f,
        ) {
            // Star: three windings to a common point.
            val star = Offset(0.25f, 0.52f)
            wire(star, Offset(0.25f, 0.16f))
            wire(star, Offset(0.08f, 0.82f))
            wire(star, Offset(0.42f, 0.82f))
            node(star)
            label(Offset(0.25f, 0.84f), slot("star", "Y"), LabelAlign.BELOW)

            // Delta: the same three windings closed on themselves.
            val a = Offset(0.75f, 0.18f)
            val b = Offset(0.60f, 0.74f)
            val c = Offset(0.90f, 0.74f)
            wire(listOf(a, b, c, a))
            node(a)
            node(b)
            node(c)
            label(Offset(0.75f, 0.84f), slot("delta", "Δ"), LabelAlign.BELOW)
        }

        TheoryDiagram.RC_CHARGING -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_rc_charging_desc),
            modifier = modifier,
            aspectRatio = 1.8f,
        ) {
            wire(
                listOf(
                    Offset(0.14f, 0.76f), Offset(0.14f, 0.22f),
                    Offset(0.84f, 0.22f), Offset(0.84f, 0.76f),
                    Offset(0.14f, 0.76f),
                ),
            )
            source(Offset(0.14f, 0.49f), SourceKind.DC, Axis.VERTICAL, slot("u", "U"))
            switchOpen(Offset(0.38f, 0.22f), Axis.HORIZONTAL)
            resistor(Offset(0.62f, 0.22f), Axis.HORIZONTAL, slot("r", "R"))
            capacitor(Offset(0.84f, 0.49f), Axis.VERTICAL, slot("c", "C"))
            label(Offset(0.50f, 0.80f), slot("tau", "τ"), LabelAlign.BELOW, emphasised = true)
        }

        TheoryDiagram.SERIES_RLC -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_series_rlc_desc),
            modifier = modifier,
            aspectRatio = 2.0f,
        ) {
            wire(
                listOf(
                    Offset(0.10f, 0.74f), Offset(0.10f, 0.26f),
                    Offset(0.90f, 0.26f), Offset(0.90f, 0.74f),
                    Offset(0.10f, 0.74f),
                ),
            )
            source(Offset(0.10f, 0.50f), SourceKind.AC, Axis.VERTICAL, slot("u", "U"))
            resistor(Offset(0.34f, 0.26f), Axis.HORIZONTAL, slot("r", "R"))
            inductor(Offset(0.58f, 0.26f), Axis.HORIZONTAL, slot("l", "L"))
            capacitor(Offset(0.82f, 0.26f), Axis.HORIZONTAL, slot("c", "C"))
            currentArrow(Offset(0.50f, 0.74f), Direction.LEFT, slot("i", "I"))
        }

        TheoryDiagram.THEVENIN -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_thevenin_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(
                listOf(
                    Offset(0.12f, 0.76f), Offset(0.12f, 0.24f),
                    Offset(0.84f, 0.24f), Offset(0.84f, 0.76f),
                    Offset(0.12f, 0.76f),
                ),
            )
            source(Offset(0.12f, 0.50f), SourceKind.DC, Axis.VERTICAL, slot("u", "U_th"))
            resistor(Offset(0.44f, 0.24f), Axis.HORIZONTAL, slot("rth", "R_th"))
            resistor(Offset(0.84f, 0.50f), Axis.VERTICAL, slot("rload", "R_L"))
            currentArrow(Offset(0.68f, 0.24f), Direction.RIGHT, slot("i", "I"))
        }

        TheoryDiagram.NORTON -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_norton_desc),
            modifier = modifier,
            aspectRatio = 1.9f,
        ) {
            wire(Offset(0.14f, 0.24f), Offset(0.86f, 0.24f))
            wire(Offset(0.14f, 0.76f), Offset(0.86f, 0.76f))

            wire(Offset(0.14f, 0.24f), Offset(0.14f, 0.76f))
            currentSource(Offset(0.14f, 0.50f), Axis.VERTICAL, slot("in", "I_N"))

            wire(Offset(0.50f, 0.24f), Offset(0.50f, 0.76f))
            resistor(Offset(0.50f, 0.50f), Axis.VERTICAL, slot("rn", "R_N"))
            node(Offset(0.50f, 0.24f))
            node(Offset(0.50f, 0.76f))

            wire(Offset(0.86f, 0.24f), Offset(0.86f, 0.76f))
            resistor(Offset(0.86f, 0.50f), Axis.VERTICAL, slot("rload", "R_L"))
        }

        TheoryDiagram.Y_DELTA_TRANSFORM -> ElecCircuitDiagram(
            contentDescription = stringResource(R.string.th_diagram_y_delta_desc),
            modifier = modifier,
            aspectRatio = 2.1f,
        ) {
            // Star on the left: three arms to a common point, each with a body.
            val hub = Offset(0.24f, 0.50f)
            wire(hub, Offset(0.24f, 0.14f))
            wire(hub, Offset(0.06f, 0.82f))
            wire(hub, Offset(0.42f, 0.82f))
            node(hub)
            resistor(Offset(0.24f, 0.30f), Axis.VERTICAL, slot("ra", "R_a"))
            resistor(Offset(0.14f, 0.68f), Axis.VERTICAL, slot("rb", "R_b"))
            resistor(Offset(0.34f, 0.68f), Axis.VERTICAL, slot("rc", "R_c"))

            // Delta on the right: the same three, closed on themselves.
            val top = Offset(0.76f, 0.16f)
            val left = Offset(0.60f, 0.80f)
            val right = Offset(0.94f, 0.80f)
            wire(listOf(top, left, right, top))
            node(top)
            node(left)
            node(right)
            resistor(Offset(0.68f, 0.48f), Axis.VERTICAL, slot("rab", "R_ab"))
            resistor(Offset(0.77f, 0.80f), Axis.HORIZONTAL, slot("rbc", "R_bc"))
            resistor(Offset(0.85f, 0.48f), Axis.VERTICAL, slot("rca", "R_ca"))
        }
    }
}

/** A sine's RMS value, as a fraction of its peak. */
private const val RMS_OF_PEAK = 0.70711f

/** The average of a rectified sine, as a fraction of its peak. */
private const val AVERAGE_OF_PEAK = 0.63662f
