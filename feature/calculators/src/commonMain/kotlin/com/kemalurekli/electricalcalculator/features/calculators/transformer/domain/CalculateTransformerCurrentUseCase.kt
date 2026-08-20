package com.kemalurekli.electricalcalculator.features.calculators.transformer.domain


/**
 * Computes transformer full load and short-circuit currents.
 *
 * ### Method
 *
 * Full load current on either winding, with `k` the phase factor (1 for single
 * phase, √3 for three phase against the line-to-line voltage):
 *
 * ```
 * I = S / (k · U)          [A],  S in VA
 * ```
 *
 * The short-circuit impedance from the nameplate gives the prospective fault
 * current at the secondary terminals. `u_k` is defined as the percentage of
 * rated primary voltage needed to circulate rated current with the secondary
 * shorted, so:
 *
 * ```
 * I_sc = I₂ · 100 / u_k    [A]
 * S_sc = S · 100 / u_k     [kVA]
 * ```
 *
 * ### Assumptions
 *
 * The short-circuit figures assume an **infinite upstream network** — that the
 * supply behind the transformer has zero impedance. Real networks add
 * impedance, so the true fault current is lower. That makes this the
 * conservative value for selecting switchgear breaking capacity, which is what
 * it is normally used for, but it is not the figure to use when checking that a
 * protective device will actually trip. The distinction is surfaced in the
 * calculator's engineering notes.
 */
class CalculateTransformerCurrentUseCase() {

    operator fun invoke(input: TransformerInput): TransformerResult {
        val ratingVa = input.ratingKva * VA_PER_KVA
        val phaseFactor = input.system.powerPhaseFactor

        val primaryCurrent = ratingVa / (phaseFactor * input.primaryVoltage)
        val secondaryCurrent = ratingVa / (phaseFactor * input.secondaryVoltage)

        val impedanceMultiplier = PERCENT / input.impedanceVoltagePercent

        return TransformerResult(
            primaryCurrent = primaryCurrent,
            secondaryCurrent = secondaryCurrent,
            voltageRatio = input.primaryVoltage / input.secondaryVoltage,
            secondaryShortCircuitCurrent = secondaryCurrent * impedanceMultiplier,
            shortCircuitPowerKva = input.ratingKva * impedanceMultiplier,
        )
    }

    private companion object {
        const val VA_PER_KVA = 1_000.0
        const val PERCENT = 100.0
    }
}
