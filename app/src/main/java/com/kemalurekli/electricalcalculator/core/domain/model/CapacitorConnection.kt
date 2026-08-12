package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * How a three-phase capacitor bank is connected.
 *
 * The choice changes the capacitance needed for the same reactive power,
 * because it changes the voltage each capacitor sees:
 *
 * - **Delta**: each unit sits across the full line-to-line voltage, so
 *   `Q = 3 · U² · ωC` and the bank needs a third of the capacitance.
 * - **Star**: each unit sees `U/√3`, so `Q = U² · ωC` and three times the
 *   capacitance is required.
 *
 * Delta is the usual choice for low-voltage correction precisely because less
 * capacitance is cheaper — at the cost of a higher voltage rating per unit.
 *
 * @param capacitanceDivisor the `n` in `C = Q / (n · ω · U²)`, with U the
 *   line-to-line voltage.
 */
enum class CapacitorConnection(val capacitanceDivisor: Double) {
    DELTA(3.0),
    STAR(1.0),
}
