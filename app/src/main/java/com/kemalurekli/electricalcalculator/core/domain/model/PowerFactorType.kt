package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * Whether the current lags or leads the voltage.
 *
 * `cos φ` alone cannot say: 0.85 lagging and 0.85 leading are the same
 * magnitude but opposite reactive power. Getting the direction wrong flips the
 * sign of Q, which turns a capacitor bank that should be added into one that
 * should be removed.
 *
 * @param reactiveSign the sign applied to reactive power. Inductive load
 *   consumes reactive power (positive by convention); capacitive load supplies
 *   it (negative).
 */
enum class PowerFactorType(val reactiveSign: Double) {
    /** Inductive — motors, transformers, chokes. The usual industrial case. */
    LAGGING(1.0),

    /** Capacitive — capacitor banks, lightly loaded long cables. */
    LEADING(-1.0),
}
