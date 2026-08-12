package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain

/**
 * A validated set of unbalanced-neutral inputs.
 *
 * @param lineCurrents the three line currents, in the order L1, L2, L3.
 * @param thirdHarmonicPercent third-harmonic content of the line current, as a
 *   percentage of the fundamental. Zero for a purely linear load; 30–70 % is
 *   ordinary on a circuit feeding banks of switched-mode supplies.
 */
data class NeutralCurrentInput(
    val lineCurrents: Triple<Double, Double, Double>,
    val thirdHarmonicPercent: Double = 0.0,
) {
    val highestLineCurrent: Double
        get() = maxOf(lineCurrents.first, lineCurrents.second, lineCurrents.third)
}

/**
 * The outcome of a neutral current calculation.
 *
 * Two contributions come out, because they behave in opposite ways and adding
 * them by hand is where people go wrong:
 *
 * - [fundamentalNeutralAmps] is the *unbalance*. Perfectly balanced lines cancel
 *   in the neutral and it is zero.
 * - [triplenNeutralAmps] is the third harmonic. It does not cancel — the three
 *   arrive in phase — so it **adds up**, and a perfectly balanced circuit can
 *   still put three times one line's third-harmonic current into the neutral.
 *
 * @param neutralCurrentAmps the two combined in quadrature, since they are at
 *   different frequencies and do not add arithmetically.
 * @param neutralToHighestLineRatio the neutral as a fraction of the largest
 *   line current. Above 1 the neutral is the most heavily loaded conductor in
 *   the cable, which is the condition IEC 60364-5-52 derates for.
 */
data class NeutralCurrentResult(
    val fundamentalNeutralAmps: Double,
    val triplenNeutralAmps: Double,
    val neutralCurrentAmps: Double,
    val neutralToHighestLineRatio: Double,
) {
    /** True when the neutral carries more than any line conductor. */
    val neutralExceedsLines: Boolean get() = neutralToHighestLineRatio > 1.0
}
