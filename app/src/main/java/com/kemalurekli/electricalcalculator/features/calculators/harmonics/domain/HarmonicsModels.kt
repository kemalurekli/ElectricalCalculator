package com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain

/**
 * One harmonic, as a meter reports it.
 *
 * @param order the multiple of the fundamental. Odd orders only: a symmetrical
 *   waveform has no even harmonics, and the even orders a meter does show are
 *   usually a sign of a half-wave rectifier or a failing device rather than a
 *   quantity to design against.
 * @param percentOfFundamental the magnitude relative to the fundamental, which
 *   is how every power-quality analyser presents a spectrum.
 */
data class HarmonicComponent(
    val order: Int,
    val percentOfFundamental: Double,
) {
    /**
     * Whether this order adds arithmetically in the neutral.
     *
     * Triplen harmonics — the multiples of three — are in phase in all three
     * lines of a balanced system, so they do not cancel at the star point. They
     * sum. Everything else does cancel, which is why a neutral can run hotter
     * than the lines it serves without any imbalance at all.
     */
    val isTriplen: Boolean get() = order % 3 == 0
}

/**
 * A spectrum measured on one line.
 *
 * @param balanced whether the three lines carry the same spectrum. The neutral
 *   result depends on it: triplen harmonics add in the neutral only when all
 *   three lines produce them together, which is the case for a floor of
 *   identical equipment and not for a mixed board.
 */
data class HarmonicsInput(
    val fundamentalAmps: Double,
    val components: List<HarmonicComponent>,
    val balanced: Boolean = true,
)

/**
 * What a distorted current does to the things carrying it.
 *
 * @param thdPercent total harmonic distortion of the current, against the
 *   fundamental. The figure a limit is usually written in.
 * @param rmsAmps the current a conductor actually heats up on. Always larger
 *   than the fundamental, and the number an ammeter reading true RMS shows.
 * @param neutralAmps what the neutral carries. Zero on an unbalanced input,
 *   because this cannot know the phase relationships that would make it
 *   anything else — see [HarmonicsInput.balanced].
 * @param kFactor the transformer K-factor, ANSI/UL style: the weighting that
 *   says how much extra eddy-current loss this spectrum causes. A K-13
 *   transformer is built for a spectrum of about this weight.
 * @param neutralExceedsLines the finding that surprises people. A neutral
 *   carrying more than its lines is not a fault; it is what triplen harmonics
 *   do, and it is why a neutral sized as "half the phase" burns.
 */
data class HarmonicsResult(
    val thdPercent: Double,
    val rmsAmps: Double,
    val neutralAmps: Double,
    val kFactor: Double,
    val dominantOrder: Int?,
) {
    val neutralExceedsLines: Boolean get() = neutralAmps > rmsAmps
}
