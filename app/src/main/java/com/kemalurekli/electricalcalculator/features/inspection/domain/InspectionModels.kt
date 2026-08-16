package com.kemalurekli.electricalcalculator.features.inspection.domain

/**
 * The tests a circuit is signed off against.
 *
 * The app has told readers what to measure since the commissioning reference
 * was written, and given them nowhere to write it down — while carrying a field
 * note called `record_the_reading_not_the_verdict`. This is where a reading
 * goes.
 *
 * Ordered as IEC 60364-6 orders them: dead tests before live ones. The sequence
 * is not tidiness — each step checks for a condition that makes the next one
 * dangerous.
 */
enum class TestKind {
    /** Continuity of the protective conductor, as R₁ + R₂. */
    CONTINUITY,

    /** Insulation resistance, dead, with electronics disconnected. */
    INSULATION,

    /** Every switch and device in the line conductor, never the neutral. */
    POLARITY,

    /** Earth fault loop impedance, live. */
    LOOP_IMPEDANCE,

    /** RCD operating time at the rated residual current. */
    RCD_AT_RATED,

    /** And at five times it, which is the test that catches a slow device. */
    RCD_AT_FIVE_TIMES,
}

/**
 * The voltage an insulation test was made at, and the resistance it must reach.
 *
 * IEC 60364-6 Table 6.1. The figures are duplicated from the commissioning
 * reference table, which shows them to readers as text; `InspectionLimitsTest`
 * fails the build if the two ever disagree, so there is still one truth even
 * though it is written twice.
 */
enum class InsulationTestVoltage(val volts: Int, val minimumMegohms: Double) {
    /** SELV and PELV circuits. */
    V250(volts = 250, minimumMegohms = 0.5),

    /** Up to and including 500 V. */
    V500(volts = 500, minimumMegohms = 1.0),

    /** Above 500 V. */
    V1000(volts = 1000, minimumMegohms = 1.0),
}

/**
 * Which kind of RCD, since the two are judged differently.
 *
 * A selective device is *meant* to be slow: it has a minimum as well as a
 * maximum, because a type S that trips as fast as a general device would
 * defeat the discrimination it exists to provide.
 */
enum class RcdType {
    GENERAL,
    SELECTIVE_S,
}

/** The window a reading has to fall inside, in the test's own unit. */
data class TestLimit(
    val minimum: Double?,
    val maximum: Double?,
) {
    fun accepts(value: Double): Boolean =
        (minimum == null || value >= minimum) && (maximum == null || value <= maximum)

    companion object {
        fun atMost(maximum: Double) = TestLimit(minimum = null, maximum = maximum)
        fun between(minimum: Double, maximum: Double) = TestLimit(minimum, maximum)
    }
}

/** What the app can say about a reading. */
enum class TestVerdict {
    PASS,
    FAIL,

    /**
     * Recorded, but not judged.
     *
     * Either there is no codified limit for the combination, or the comparison
     * is against a calculated expectation rather than a requirement. Saying so
     * is the point: an app that turns every reading into a verdict teaches
     * readers to trust verdicts it has no basis for.
     */
    RECORDED,
}

/**
 * The limits, in one place.
 *
 * ### Why the calculated comparisons are not verdicts
 *
 * Continuity is checked against what R₁ + R₂ *should* be for the length and
 * cross-section installed, and there is no standard tolerance on that
 * agreement — the useful output is the ratio, which an engineer reads. A
 * measured value twice the calculated one is a finding whatever a threshold
 * would have said about it.
 *
 * Loop impedance is different: the maximum is a real requirement derived from
 * the device, so that one is judged.
 */
object InspectionLimits {

    /** Minimum insulation resistance for the voltage the test was made at. */
    fun insulation(voltage: InsulationTestVoltage) = TestLimit(
        minimum = voltage.minimumMegohms,
        maximum = null,
    )

    /**
     * RCD operating time in milliseconds, or null where the app has no figure.
     *
     * A selective device at five times its rated current has no limit in the
     * commissioning table this app carries, and inventing one would be worse
     * than recording the reading and leaving the judgement to the person
     * holding the tester.
     */
    fun rcd(type: RcdType, kind: TestKind): TestLimit? = when (type) {
        RcdType.GENERAL -> when (kind) {
            TestKind.RCD_AT_RATED -> TestLimit.atMost(GENERAL_AT_RATED_MS)
            TestKind.RCD_AT_FIVE_TIMES -> TestLimit.atMost(GENERAL_AT_FIVE_TIMES_MS)
            else -> null
        }

        RcdType.SELECTIVE_S -> when (kind) {
            TestKind.RCD_AT_RATED -> TestLimit.between(SELECTIVE_MIN_MS, SELECTIVE_MAX_MS)
            else -> null
        }
    }

    /** General type, at IΔn. */
    const val GENERAL_AT_RATED_MS = 300.0

    /** General type, at 5 × IΔn — the test that catches a device that is merely slow. */
    const val GENERAL_AT_FIVE_TIMES_MS = 40.0

    /** Type S, at IΔn: a window, not a ceiling. */
    const val SELECTIVE_MIN_MS = 130.0
    const val SELECTIVE_MAX_MS = 500.0
}

/**
 * One measurement, as recorded against a circuit.
 *
 * [value] is the text the user typed, for the same reason every other numeric
 * field in this app keeps text: it comes from a keyboard and goes back to one,
 * and round-tripping through a `Double` rewrites it in the locale's decimal
 * separator.
 *
 * [passed] is stored for [TestKind.POLARITY] alone, which has no number to
 * record — the reading *is* the verdict.
 */
data class CircuitTest(
    val id: Long = NO_ID,
    val circuitId: Long,
    val kind: TestKind,
    val value: String = "",
    val passed: Boolean? = null,
    val insulationVoltage: InsulationTestVoltage = InsulationTestVoltage.V500,
    val rcdType: RcdType = RcdType.GENERAL,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}
