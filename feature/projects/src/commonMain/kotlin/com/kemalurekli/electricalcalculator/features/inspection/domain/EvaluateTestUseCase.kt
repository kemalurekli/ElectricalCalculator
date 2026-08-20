package com.kemalurekli.electricalcalculator.features.inspection.domain

import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignResult

/**
 * A reading, judged against what the design expects of it.
 *
 * @param verdict what the app is willing to say.
 * @param limit the window the reading was judged against, or null when it was
 *   only recorded.
 * @param expected the figure the design predicts, where there is one. This is
 *   the column the whole feature exists for: a measured Zs of 1.4 Ω against a
 *   calculated 0.9 Ω is a loose termination, a wrong cross-section or an
 *   unexpected joint, and no amount of "pass" against the device maximum will
 *   tell the reader that.
 * @param ratio measured over expected, where both exist.
 */
data class TestEvaluation(
    val verdict: TestVerdict,
    val limit: TestLimit?,
    val expected: Double?,
    val ratio: Double?,
)

/**
 * Compares what was measured with what was designed.
 *
 * Takes the circuit's own design rather than a table of limits, because two of
 * the tests are only meaningful against it: the permitted loop impedance comes
 * from the protective device this circuit actually has, and the expected
 * continuity comes from the cross-section it was actually sized to.
 *
 * A circuit that has not been designed yet still records readings. An
 * inspector on site does not always have the design to hand, and refusing the
 * measurement until someone fills in a length would send them back to paper.
 */
class EvaluateTestUseCase() {

    operator fun invoke(test: CircuitTest, design: CircuitDesignResult?): TestEvaluation {
        if (test.kind == TestKind.POLARITY) {
            return TestEvaluation(
                verdict = when (test.passed) {
                    true -> TestVerdict.PASS
                    false -> TestVerdict.FAIL
                    null -> TestVerdict.RECORDED
                },
                limit = null,
                expected = null,
                ratio = null,
            )
        }

        val measured = NumberFormatter.parseOrNull(test.value.trim())
            ?: return TestEvaluation(TestVerdict.RECORDED, null, null, null)

        val limit = limitFor(test, design)
        val expected = expectedFor(test, design)

        return TestEvaluation(
            verdict = when {
                limit == null -> TestVerdict.RECORDED
                limit.accepts(measured) -> TestVerdict.PASS
                else -> TestVerdict.FAIL
            },
            limit = limit,
            expected = expected,
            // Guarded against a design that reports zero, which would otherwise
            // hand the screen an infinity to render.
            ratio = expected?.takeIf { it > 0.0 }?.let { measured / it },
        )
    }

    private fun limitFor(test: CircuitTest, design: CircuitDesignResult?): TestLimit? =
        when (test.kind) {
            TestKind.INSULATION -> InspectionLimits.insulation(test.insulationVoltage)

            TestKind.LOOP_IMPEDANCE -> design
                ?.takeIf { it.hasSolution }
                ?.maximumLoopImpedanceOhms
                ?.let(TestLimit::atMost)

            TestKind.RCD_AT_RATED, TestKind.RCD_AT_FIVE_TIMES ->
                InspectionLimits.rcd(test.rcdType, test.kind)

            // Continuity has no codified tolerance against the calculated
            // value, so it is reported rather than judged. See InspectionLimits.
            TestKind.CONTINUITY -> null

            TestKind.POLARITY -> null
        }

    /**
     * What the design says the reading should be.
     *
     * Continuity is compared against the loop's conductor resistance — Zs minus
     * the supply's own Ze, which is what R₁ + R₂ measures at the far end of the
     * circuit and nothing else in the chain reports directly.
     */
    private fun expectedFor(test: CircuitTest, design: CircuitDesignResult?): Double? {
        val solved = design?.takeIf { it.hasSolution } ?: return null
        return when (test.kind) {
            TestKind.CONTINUITY -> solved.conductorLoopOhms()
            TestKind.LOOP_IMPEDANCE -> solved.loopImpedanceOhms
            else -> null
        }
    }
}

/**
 * The part of Zs that belongs to this circuit's conductors.
 *
 * The chain reports the whole loop, which includes the supply's Ze. An R₁ + R₂
 * measurement is made across the circuit alone, so comparing it against the
 * full loop would make every reading look far too low.
 */
private fun CircuitDesignResult.conductorLoopOhms(): Double =
    loopImpedanceOhms - externalImpedanceOhms
