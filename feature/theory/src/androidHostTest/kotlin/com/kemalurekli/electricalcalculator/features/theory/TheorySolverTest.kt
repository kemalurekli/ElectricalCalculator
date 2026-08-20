package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryInputs
import com.kemalurekli.electricalcalculator.features.theory.domain.TheorySolution
import com.kemalurekli.electricalcalculator.features.theory.domain.TheorySolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * The arithmetic, checked two ways.
 *
 * **Every example, automatically.** The first block runs every worked example in
 * the catalog through the same validation the view model applies and then through
 * its solver, asserting the properties a derivation must have. It is the
 * equivalent of `WorkedExampleTest.assertEveryExampleRuns` for the calculators,
 * with one difference that is the point of the catalog design: that one needs a
 * new `@Test` and a new line in a hand-kept list for every calculator added,
 * while this one covers a topic the moment it is in the catalog.
 *
 * **A sample, by hand.** Properties cannot tell you the answer is right. The
 * second block works figures out independently and asserts them, which is the
 * only check here that would catch a transposed formula.
 */
class TheorySolverTest {

    // -- Every example, automatically ----------------------------------------

    @Test
    fun `every example passes the validation its own fields declare`() {
        forEachExample { id, solution, values ->
            solution.fields.forEach { field ->
                val raw = values[field.key].orEmpty()
                if (field.optional && raw.isBlank()) return@forEach

                val outcome = NumericInput.validate(
                    raw = raw,
                    min = field.min,
                    max = field.max,
                    allowZero = field.allowZero,
                    allowNegative = field.allowNegative,
                )
                assertTrue(
                    "$id: ${field.key} = \"$raw\" fails its own validation " +
                        "(${(outcome as? Outcome.Failure)?.error})",
                    outcome is Outcome.Success,
                )
            }
        }
    }

    @Test
    fun `every example produces a result and a worked solution`() {
        forEachExample { id, solution, values ->
            val solved = solve(solution, values)
            assertTrue("$id: primary result is not a finite number", solved.primary.number.value.isFinite())
            assertTrue("$id: produced no worked solution", solved.steps.isNotEmpty())
            solved.secondary.forEach {
                assertTrue("$id: secondary \"${it.label}\" is not finite", it.number.value.isFinite())
            }
        }
    }

    @Test
    fun `every step fills exactly the slots it opens`() {
        // A substitution with a slot the operands do not reach renders a literal
        // "{2}" to the reader; a spare operand is silently dropped. Neither is
        // visible in a screenshot of a topic nobody opened.
        val slot = Regex("""\{(\d+)}""")
        forEachExample { id, solution, values ->
            solve(solution, values).steps.forEachIndexed { index, step ->
                val indices = slot.findAll(step.substitution).map { it.groupValues[1].toInt() }.toSet()
                val expected = step.operands.indices.toSet()
                assertEquals(
                    "$id: step $index opens slots $indices but supplies ${expected.size} operands",
                    expected,
                    indices,
                )
            }
        }
    }

    @Test
    fun `every step is labelled and says something`() {
        forEachExample { id, solution, values ->
            solve(solution, values).steps.forEachIndexed { index, step ->
                assertTrue("$id: step $index has no label", step.label.key.isNotEmpty())
                assertTrue("$id: step $index has no formula", step.formula.isNotBlank())
                assertTrue("$id: step $index has no substitution", step.substitution.isNotBlank())
            }
        }
    }

    // -- A sample, by hand ----------------------------------------------------

    @Test
    fun `ohms law divides, multiplies and divides back`() {
        // A 100 W lamp on 230 V is 529 Ω, and the three targets have to agree.
        val current = solveByKey("ohm_law", "current", mapOf("u" to "230", "r" to "529"))
        assertEquals(0.434783, current.primary.number.value, 1e-6)
        assertEquals(100.0, current.secondary.single().number.value, 1e-3)

        val voltage = solveByKey("ohm_law", "voltage", mapOf("i" to "10", "r" to "0.01"))
        assertEquals(0.1, voltage.primary.number.value, 1e-9)
        assertEquals(1.0, voltage.secondary.single().number.value, 1e-9)

        val resistance = solveByKey("ohm_law", "resistance", mapOf("u" to "230", "i" to "8.7"))
        assertEquals(26.436782, resistance.primary.number.value, 1e-6)
    }

    @Test
    fun `the three forms of power agree with each other`() {
        // The topic's whole claim is that U·I, I²R and U²/R are one product. If
        // they ever disagree the page is teaching something false.
        val solved = solveByKey("dc_power", "power", mapOf("u" to "12", "i" to "1.5"))
        assertEquals(18.0, solved.primary.number.value, 1e-9)

        val results = solved.steps.map { it.result.value }
        assertEquals("P = U · I", 18.0, results[0], 1e-9)
        assertEquals("R = U / I", 8.0, results[1], 1e-9)
        assertEquals("P = I² · R", 18.0, results[2], 1e-9)
        assertEquals("P = U² / R", 18.0, results[3], 1e-9)
    }

    @Test
    fun `series drops add back to the supply`() {
        // The check an electrician makes with a meter, made here: measure around
        // the loop and the drops have to total what went in.
        val solved = solveByKey(
            "series_resistance", "total",
            mapOf("u" to "24", "r1" to "100", "r2" to "220", "r3" to "470"),
        )
        assertEquals(790.0, solved.primary.number.value, 1e-9)

        val drops = solved.secondary.drop(1).map { it.number.value }
        assertEquals(3, drops.size)
        assertEquals(24.0, drops.sum(), 1e-9)
    }

    @Test
    fun `a parallel combination is smaller than its smallest branch`() {
        val solved = solveByKey(
            "parallel_resistance", "total",
            mapOf("u" to "230", "r1" to "529", "r2" to "26.5", "r3" to "96"),
        )
        assertEquals(19.9828647, solved.primary.number.value, 1e-6)
        assertTrue(
            "The combination is not below the smallest branch",
            solved.primary.number.value < 26.5,
        )

        // And the branch currents have to add up to what the supply carries.
        val supply = solved.secondary.first().number.value
        val branches = solved.secondary.drop(1).map { it.number.value }
        assertEquals(supply, branches.sum(), 1e-9)
    }

    @Test
    fun `a divider built from megohms collapses and a stiff one does not`() {
        // The topic's point, as a test: the same load moves one divider by a
        // third and the other by half a percent.
        val soft = solveByKey(
            "voltage_divider", "output",
            mapOf("u" to "12", "r1" to "10000", "r2" to "10000", "r_load" to "10000"),
        )
        assertEquals(4.0, soft.primary.number.value, 1e-9)
        assertEquals(-33.333333, soft.secondary[1].number.value, 1e-6)

        val stiff = solveByKey(
            "voltage_divider", "output",
            mapOf("u" to "12", "r1" to "100", "r2" to "100", "r_load" to "10000"),
        )
        assertEquals(5.970149, stiff.primary.number.value, 1e-6)
        assertEquals(-0.497512, stiff.secondary[1].number.value, 1e-6)
    }

    @Test
    fun `an unloaded divider is the plain ratio and stops at one step`() {
        val solved = solveByKey(
            "voltage_divider", "output",
            mapOf("u" to "12", "r1" to "10000", "r2" to "10000"),
        )
        assertEquals(6.0, solved.primary.number.value, 1e-9)
        // No load means no parallel arm, no loaded output and no error line. A
        // derivation that showed them would be describing a circuit that is not
        // on the screen.
        assertEquals(1, solved.steps.size)
    }

    @Test
    fun `the smaller branch of a current divider takes the larger share`() {
        val solved = solveByKey(
            "current_divider", "branches",
            mapOf("i" to "10", "r1" to "10", "r2" to "90"),
        )
        assertEquals(9.0, solved.primary.number.value, 1e-9)
        assertEquals(1.0, solved.secondary.first().number.value, 1e-9)
        assertEquals(10.0, solved.primary.number.value + solved.secondary.first().number.value, 1e-9)
    }

    @Test
    fun `a warm copper conductor is measurably more resistive than a cold one`() {
        // 0.017241 × [1 + 0.00393 × 50] = 0.0206288 Ω·mm²/m, then × 25 / 2.5.
        val warm = solveByKey(
            "conductor_resistance", "copper",
            mapOf("l" to "25", "a" to "2.5", "theta" to "70"),
        )
        assertEquals(0.2062885650, warm.primary.number.value, 1e-9)
        assertEquals("out and back", 0.4125771300, warm.secondary[0].number.value, 1e-9)

        val cold = solveByKey(
            "conductor_resistance", "copper",
            mapOf("l" to "25", "a" to "2.5", "theta" to "20"),
        )
        // The correction is worth about a fifth at the PVC rating. If this ratio
        // ever comes out near 1 the temperature term has been dropped.
        assertEquals(1.1965, warm.primary.number.value / cold.primary.number.value, 1e-4)
    }

    @Test
    fun `aluminium needs more section than copper for the same resistance`() {
        val copper = solveByKey(
            "conductor_resistance", "copper",
            mapOf("l" to "100", "a" to "16", "theta" to "20"),
        )
        val aluminium = solveByKey(
            "conductor_resistance", "aluminium",
            mapOf("l" to "100", "a" to "16", "theta" to "20"),
        )
        assertEquals(0.028264 / 0.017241, aluminium.primary.number.value / copper.primary.number.value, 1e-9)
    }

    @Test
    fun `the currents at a junction balance and the drops round a loop close`() {
        val node = solveByKey(
            "kirchhoff_laws", "node",
            mapOf("i_in1" to "10", "i_in2" to "4", "i_out1" to "6"),
        )
        assertEquals(8.0, node.primary.number.value, 1e-9)

        val loop = solveByKey(
            "kirchhoff_laws", "loop",
            mapOf("u" to "230", "u1" to "6.5", "u2" to "3.2"),
        )
        assertEquals(220.3, loop.primary.number.value, 1e-9)
    }

    @Test
    fun `the three ways of measuring a sine agree whichever one you start from`() {
        // 230 V RMS is 325.27 V peak, and the same three numbers must come back
        // whether the reader has the RMS, the peak or the average.
        val fromRms = solveByKey("rms_and_peak", "from_rms", mapOf("u_rms" to "230"))
        assertEquals(230.0, fromRms.primary.number.value, 1e-9)
        assertEquals(325.2691, fromRms.secondary[0].number.value, 1e-4)
        assertEquals(207.0728, fromRms.secondary[1].number.value, 1e-4)
        assertEquals("form factor", 1.11072, fromRms.secondary[2].number.value, 1e-5)
        assertEquals("crest factor", 1.41421, fromRms.secondary[3].number.value, 1e-5)

        val fromPeak = solveByKey("rms_and_peak", "from_peak", mapOf("u_peak" to "325.2691"))
        assertEquals(230.0, fromPeak.primary.number.value, 1e-4)

        val fromAverage = solveByKey("rms_and_peak", "from_average", mapOf("u_avg" to "207.0728"))
        assertEquals(230.0, fromAverage.primary.number.value, 1e-4)
    }

    @Test
    fun `reactance runs the opposite way for a capacitor and an inductor`() {
        val capacitive = solveByKey(
            "capacitive_reactance", "reactance",
            mapOf("f" to "50", "c" to "100", "u" to "230"),
        )
        assertEquals(31.8310, capacitive.primary.number.value, 1e-4)
        assertEquals(7.2257, capacitive.secondary[0].number.value, 1e-4)

        val inductive = solveByKey(
            "inductive_reactance", "reactance",
            mapOf("f" to "50", "l" to "100", "u" to "230"),
        )
        assertEquals(31.4159, inductive.primary.number.value, 1e-4)

        // Doubling the frequency halves one and doubles the other. Getting these
        // the same way round is the single thing the topic is for.
        val capacitiveFast = solveByKey(
            "capacitive_reactance", "reactance",
            mapOf("f" to "100", "c" to "100", "u" to "230"),
        )
        val inductiveFast = solveByKey(
            "inductive_reactance", "reactance",
            mapOf("f" to "100", "l" to "100", "u" to "230"),
        )
        assertEquals(0.5, capacitiveFast.primary.number.value / capacitive.primary.number.value, 1e-9)
        assertEquals(2.0, inductiveFast.primary.number.value / inductive.primary.number.value, 1e-9)
    }

    @Test
    fun `the power triangle closes`() {
        val solved = solveByKey("ac_power_triangle", "triangle", mapOf("p" to "7.5", "pf" to "0.85"))
        assertEquals(8.8235, solved.primary.number.value, 1e-4)
        assertEquals(4.6481, solved.secondary[0].number.value, 1e-4)
        assertEquals(31.7883, solved.secondary[1].number.value, 1e-4)

        // S² = P² + Q², in kilo-units throughout.
        val apparent = solved.primary.number.value
        val reactive = solved.secondary[0].number.value
        assertEquals(apparent * apparent, 7.5 * 7.5 + reactive * reactive, 1e-6)
    }

    @Test
    fun `star divides the voltage and delta divides the current`() {
        val star = solveByKey(
            "three_phase_star_delta", "star",
            mapOf("u_line" to "400", "i_line" to "32", "pf" to "0.85"),
        )
        assertEquals("400 V star gives the familiar 230 V", 230.9401, star.primary.number.value, 1e-4)
        assertEquals("winding carries the line current", 32.0, star.secondary[0].number.value, 1e-9)

        val delta = solveByKey(
            "three_phase_star_delta", "delta",
            mapOf("u_line" to "400", "i_line" to "14", "pf" to "0.85"),
        )
        assertEquals("winding sees the line voltage", 400.0, delta.primary.number.value, 1e-9)
        assertEquals(8.0829, delta.secondary[0].number.value, 1e-4)
    }

    @Test
    fun `one time constant gets a capacitor to 63 percent and five settle it`() {
        val solved = solveByKey(
            "rc_time_constant", "charging",
            mapOf("r" to "100000", "c" to "10", "u" to "12", "t" to "1"),
        )
        assertEquals("100 kΩ × 10 µF is one second", 1.0, solved.primary.number.value, 1e-12)
        assertEquals(12.0 * (1 - Math.exp(-1.0)), solved.secondary[0].number.value, 1e-9)
        assertEquals(5.0, solved.secondary[1].number.value, 1e-12)
    }

    @Test
    fun `the two reactances subtract before anything is squared`() {
        // 200 mH against 100 µF at 50 Hz is net inductive by 31 Ω; 10 mH against
        // the same capacitance is net capacitive by 28.7 Ω. Adding the
        // reactances instead of subtracting them would put both near 94 Ω.
        val inductive = solveByKey(
            "rlc_impedance", "impedance",
            mapOf("r" to "10", "l" to "200", "c" to "100", "f" to "50", "u" to "230"),
        )
        assertEquals(32.5738, inductive.primary.number.value, 1e-4)
        assertEquals("current lags", 72.1218, inductive.secondary[0].number.value, 1e-4)

        val capacitive = solveByKey(
            "rlc_impedance", "impedance",
            mapOf("r" to "10", "l" to "10", "c" to "100", "f" to "50", "u" to "230"),
        )
        assertEquals(30.3823, capacitive.primary.number.value, 1e-4)
        assertEquals("current leads", -70.7834, capacitive.secondary[0].number.value, 1e-4)
    }

    @Test
    fun `equal reactances leave a purely resistive circuit`() {
        // The claim the topic is built on: cancel the reactances and the
        // impedance is the resistance, however large either reactance was.
        // 10 mH resonates with 253.303 µF at 100 Hz.
        val solved = solveByKey(
            "rlc_impedance", "impedance",
            mapOf("r" to "10", "l" to "10", "c" to "253.303", "f" to "100", "u" to "230"),
        )
        assertEquals(10.0, solved.primary.number.value, 1e-3)
        assertEquals(0.0, solved.secondary[0].number.value, 1e-2)
    }

    @Test
    fun `resonance sharpens as the resistance falls`() {
        val damped = solveByKey(
            "series_resonance", "resonance",
            mapOf("l" to "10", "c" to "1", "r" to "5"),
        )
        assertEquals(1591.5494, damped.primary.number.value, 1e-4)
        assertEquals("quality factor", 20.0, damped.secondary[0].number.value, 1e-9)
        assertEquals("bandwidth", 79.5775, damped.secondary[1].number.value, 1e-4)

        val sharp = solveByKey(
            "series_resonance", "resonance",
            mapOf("l" to "10", "c" to "1", "r" to "0.5"),
        )
        // Same components, a tenth of the resistance: same frequency, ten times
        // the Q, a tenth of the bandwidth.
        assertEquals(damped.primary.number.value, sharp.primary.number.value, 1e-9)
        assertEquals(200.0, sharp.secondary[0].number.value, 1e-9)
        assertEquals(damped.secondary[1].number.value / 10, sharp.secondary[1].number.value, 1e-9)
    }

    @Test
    fun `thevenin and norton give the same load current`() {
        // The two theorems describe one box. If they ever disagree, one of the
        // two pages is wrong and a reader has no way to tell which.
        val thevenin = solveByKey(
            "thevenin_equivalent", "equivalent",
            mapOf("u" to "12", "r1" to "1000", "r2" to "1000", "r_load" to "1000"),
        )
        assertEquals(6.0, thevenin.primary.number.value, 1e-9)
        assertEquals(500.0, thevenin.secondary[0].number.value, 1e-9)
        val theveninLoadCurrent = thevenin.secondary[1].number.value
        assertEquals(0.004, theveninLoadCurrent, 1e-12)

        val norton = solveByKey(
            "norton_equivalent", "equivalent",
            mapOf("u_th" to "6", "r_th" to "500", "r_load" to "1000"),
        )
        assertEquals(0.012, norton.primary.number.value, 1e-12)
        assertEquals(theveninLoadCurrent, norton.secondary[1].number.value, 1e-12)
    }

    @Test
    fun `the matched load takes the most power and wastes half of it`() {
        val matched = solveByKey(
            "max_power_transfer", "transfer",
            mapOf("u_th" to "12", "r_th" to "50", "r_load" to "50"),
        )
        // Delivered equals the theoretical maximum only at the match, and the
        // efficiency there is the number that disqualifies the theorem from
        // power work.
        assertEquals(0.72, matched.primary.number.value, 1e-12)
        assertEquals(0.72, matched.secondary[0].number.value, 1e-12)
        assertEquals(50.0, matched.secondary[2].number.value, 1e-12)

        val efficient = solveByKey(
            "max_power_transfer", "transfer",
            mapOf("u_th" to "12", "r_th" to "50", "r_load" to "500"),
        )
        assertTrue(
            "A ten-to-one load should take less power than the matched one",
            efficient.primary.number.value < matched.primary.number.value,
        )
        assertTrue(
            "…and deliver a far larger share of it",
            efficient.secondary[2].number.value > 90.0,
        )
    }

    @Test
    fun `star and delta convert back into each other`() {
        // Three equal star arms give a delta of three times the value, and
        // converting back has to land on the original.
        val toDelta = solveByKey(
            "star_delta_transform", "star_to_delta",
            mapOf("ra" to "10", "rb" to "10", "rc" to "10"),
        )
        assertEquals(30.0, toDelta.primary.number.value, 1e-9)

        val backToStar = solveByKey(
            "star_delta_transform", "delta_to_star",
            mapOf("rab" to "30", "rbc" to "30", "rca" to "30"),
        )
        assertEquals(10.0, backToStar.primary.number.value, 1e-9)

        // And the round trip holds for an uneven star too, which the equal case
        // could not have caught — every arm is the same number there.
        val uneven = solveByKey(
            "star_delta_transform", "star_to_delta",
            mapOf("ra" to "10", "rb" to "20", "rc" to "30"),
        )
        val ab = uneven.primary.number.value
        val bc = uneven.secondary[0].number.value
        val ca = uneven.secondary[1].number.value
        val back = solveByKey(
            "star_delta_transform", "delta_to_star",
            mapOf("rab" to "$ab", "rbc" to "$bc", "rca" to "$ca"),
        )
        assertEquals(10.0, back.primary.number.value, 1e-9)
        assertEquals(20.0, back.secondary[0].number.value, 1e-9)
        assertEquals(30.0, back.secondary[1].number.value, 1e-9)
    }

    // -- Plumbing -------------------------------------------------------------

    private fun forEachExample(check: (String, TheorySolution, Map<String, String>) -> Unit) {
        var seen = 0
        TheoryCatalog.all.forEach { topic ->
            topic.solutions.forEach { solution ->
                solution.examples.forEach { example ->
                    seen++
                    check("${topic.key}/${solution.key}/${example.key}", solution, example.values)
                }
            }
        }
        assertTrue("No examples in the catalog at all", seen > 0)
    }

    private fun solve(solution: TheorySolution, values: Map<String, String>): TheorySolutionResult {
        val parsed = solution.fields.mapNotNull { field ->
            val raw = values[field.key].orEmpty()
            if (field.optional && raw.isBlank()) null
            else field.key to requireNotNull(raw.toDoubleOrNull()) { "unparseable ${field.key}" }
        }.toMap()
        return solution.solve(TheoryInputs(parsed))
    }

    private fun solveByKey(
        topicKey: String,
        solutionKey: String,
        values: Map<String, String>,
    ): TheorySolutionResult {
        val topic = requireNotNull(TheoryCatalog.topicOrNull(topicKey)) { "no topic $topicKey" }
        val solution = requireNotNull(topic.solutions.firstOrNull { it.key == solutionKey }) {
            "no solution $solutionKey on $topicKey"
        }
        return solve(solution, values)
    }
    @Test
    fun `the magnetic examples stay where the model is valid`() {
        // The topic's own assumption note says permeability collapses above
        // about 1.5 T and the calculation overstates the flux badly there. An
        // example that lands past saturation demonstrates the one case the
        // topic tells the reader not to trust it in — which is worse than no
        // example, because it looks like a worked answer.
        //
        // The first version of this topic shipped at 6.7 T.
        val topic = TheoryCatalog.topicOrNull("magnetic_circuit")
        assertNotNull(topic)

        topic!!.solutions.forEach { solution ->
            solution.examples.forEach { example ->
                val inputs = TheoryInputs(
                    solution.fields.associate { field ->
                        field.key to (example.values[field.key] ?: field.default).toDouble()
                    },
                )
                val density = solution.solve(inputs).primary.number.value
                assertTrue(
                    "${example.key} reaches $density T, past saturation",
                    density <= SATURATION_TESLA,
                )
            }
        }
    }

    private companion object {
        /** Where silicon steel stops behaving linearly. */
        const val SATURATION_TESLA = 1.5
    }

}
