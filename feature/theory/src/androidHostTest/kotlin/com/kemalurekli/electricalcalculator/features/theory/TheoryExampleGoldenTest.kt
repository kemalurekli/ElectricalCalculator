package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryInputs
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

/**
 * Every worked example, pinned to the answer it gives today.
 *
 * The solvers each have their own tests against hand-worked figures; this is a
 * different guarantee. It says the *examples* — the values a reader taps and
 * the answers they see — have not moved. A solver corrected on purpose fails
 * this and the figure is updated in the same commit, deliberately; one changed
 * by accident fails it too, which is the point.
 *
 * It also guards the quiz, whose mark scheme is exactly these numbers.
 *
 * These figures came from the solvers rather than from hand working, so they
 * prove stability rather than correctness. Correctness belongs to
 * `TheorySolverTest`, which works samples out independently.
 */
class TheoryExampleGoldenTest {

    private data class Golden(
        val topic: String,
        val solution: String,
        val example: String,
        val value: Double,
        val unit: String,
    ) {
        val path: String get() = "$topic/$solution/$example"
    }

    private val expected: List<Golden> = listOf(
        Golden("ac_power_triangle", "triangle", "corrected", 7.894736842105264, "kVA"),
        Golden("ac_power_triangle", "triangle", "motor", 8.823529411764707, "kVA"),
        Golden("capacitive_reactance", "reactance", "correction", 31.830988618379067, "Ω"),
        Golden("capacitive_reactance", "reactance", "small", 1446.8631190172302, "Ω"),
        Golden("conductor_resistance", "aluminium", "radial", 0.33959196, "Ω"),
        Golden("conductor_resistance", "aluminium", "submain", 0.11026139700000001, "Ω"),
        Golden("conductor_resistance", "copper", "radial", 0.20628856499999998, "Ω"),
        Golden("conductor_resistance", "copper", "submain", 0.06719464237499999, "Ω"),
        Golden("current_divider", "branches", "equal", 5.0, "A"),
        Golden("current_divider", "branches", "uneven", 9.0, "A"),
        Golden("dc_power", "current", "nameplate", 9.565217391304348, "A"),
        Golden("dc_power", "power", "kettle", 2185.0, "W"),
        Golden("dc_power", "power", "led_strip", 18.0, "W"),
        Golden("dc_power", "voltage", "motor", 234.375, "V"),
        Golden("induced_emf", "emf", "sixty_hertz", 266.572976289502, "V"),
        Golden("induced_emf", "emf", "small_transformer", 222.1441469079183, "V"),
        Golden("inductive_reactance", "reactance", "choke", 31.415926535897935, "Ω"),
        Golden("inductive_reactance", "reactance", "coil", 157.07963267948966, "Ω"),
        Golden("kirchhoff_laws", "loop", "three_drops", 220.3, "V"),
        Golden("kirchhoff_laws", "node", "junction", 8.0, "A"),
        Golden("magnetic_circuit", "flux", "air_core", 6.2831853106E-4, "T"),
        Golden("magnetic_circuit", "flux", "steel_core", 1.25663706212, "T"),
        Golden("max_power_transfer", "transfer", "efficient", 0.23801652892561986, "W"),
        Golden("max_power_transfer", "transfer", "matched", 0.72, "W"),
        Golden("norton_equivalent", "equivalent", "converted", 0.012, "A"),
        Golden("ohm_law", "current", "heater", 8.679245283018869, "A"),
        Golden("ohm_law", "current", "lamp", 0.43478260869565216, "A"),
        Golden("ohm_law", "resistance", "element", 26.436781609195403, "Ω"),
        Golden("ohm_law", "voltage", "shunt", 0.1, "V"),
        Golden("parallel_resistance", "total", "mixed_load", 19.982864736846015, "Ω"),
        Golden("parallel_resistance", "total", "two_lamps", 264.5, "Ω"),
        Golden("per_unit", "pu", "feeder", 0.4591368227731864, "pu"),
        Golden("per_unit", "pu", "low_voltage", 0.12499999999999997, "pu"),
        Golden("rc_time_constant", "charging", "debounce", 0.001, "s"),
        Golden("rc_time_constant", "charging", "timer", 1.0, "s"),
        Golden("rlc_impedance", "impedance", "capacitive", 30.382255361056842, "Ω"),
        Golden("rlc_impedance", "impedance", "inductive", 32.573817658652196, "Ω"),
        Golden("rms_and_peak", "from_average", "meter", 229.91919204969543, "V"),
        Golden("rms_and_peak", "from_peak", "scope", 229.80970388562793, "V"),
        Golden("rms_and_peak", "from_rms", "mains", 230.0, "V"),
        Golden("series_resistance", "total", "three_resistors", 790.0, "Ω"),
        Golden("series_resistance", "total", "two_resistors", 320.0, "Ω"),
        Golden("series_resonance", "resonance", "filter", 1591.5494309189535, "Hz"),
        Golden("series_resonance", "resonance", "sharp", 1591.5494309189535, "Hz"),
        Golden("star_delta_transform", "delta_to_star", "equal", 10.0, "Ω"),
        Golden("star_delta_transform", "star_to_delta", "equal", 30.0, "Ω"),
        Golden("star_delta_transform", "star_to_delta", "uneven", 36.666666666666664, "Ω"),
        Golden("thevenin_equivalent", "equivalent", "divider", 6.0, "V"),
        Golden("three_phase_star_delta", "delta", "motor", 400.0, "V"),
        Golden("three_phase_star_delta", "star", "board", 230.94010767585033, "V"),
        Golden("transformer_ratio", "secondary", "step_down", 20.0, "V"),
        Golden("transformer_ratio", "secondary", "step_up", 400.0, "V"),
        Golden("voltage_divider", "output", "loaded", 4.0, "V"),
        Golden("voltage_divider", "output", "stiff", 5.970149253731343, "V"),
        Golden("voltage_divider", "output", "unloaded", 6.0, "V"),
    )

    @Test
    fun `every example still gives the answer it gave before`() {
        expected.forEach { g ->
            val topic = requireNotNull(TheoryCatalog.topicOrNull(g.topic)) {
                "${g.path}: the topic is gone from the catalog"
            }
            val solution = requireNotNull(topic.solutions.firstOrNull { it.key == g.solution }) {
                "${g.path}: the solution is gone"
            }
            val example = requireNotNull(solution.examples.firstOrNull { it.key == g.example }) {
                "${g.path}: the example is gone"
            }

            val inputs = TheoryInputs(
                solution.fields.mapNotNull { field ->
                    val raw = example.values[field.key] ?: field.default
                    if (raw.isBlank()) null else field.key to raw.toDouble()
                }.toMap(),
            )
            val produced = solution.solve(inputs).primary.number

            assertEquals(
                "${g.path} moved",
                g.value,
                produced.value,
                abs(g.value) * 1e-9 + 1e-12,
            )
            assertEquals("${g.path} changed unit", g.unit, produced.unit)
        }
    }

    @Test
    fun `every example in the catalog is pinned`() {
        // An example nobody pinned is an example nobody is guarding, and a
        // topic added without one would slip past the check above unnoticed.
        val actual = TheoryCatalog.all.flatMap { topic ->
            topic.solutions.flatMap { solution ->
                solution.examples.map { "${topic.key}/${solution.key}/${it.key}" }
            }
        }.toSortedSet()

        assertEquals(actual, expected.map { it.path }.toSortedSet())
    }
}
