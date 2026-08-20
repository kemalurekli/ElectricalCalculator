package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain

/**
 * How a motor is brought up to speed, and what that does to its inrush.
 *
 * @param currentFactor the share of the direct-on-line starting current the
 *   supply actually sees.
 * @param torqueFactor the share of the direct-on-line starting torque. Carried
 *   because it is the reason a gentler start is not free: torque falls with the
 *   square of voltage, so a method that halves the current quarters the pull,
 *   and a loaded machine may simply fail to turn.
 */
enum class StartingMethod(val currentFactor: Double, val torqueFactor: Double) {
    /** Full voltage, full inrush. */
    DIRECT_ON_LINE(currentFactor = 1.0, torqueFactor = 1.0),

    /**
     * Star-delta.
     *
     * Each winding sees U/√3, so the line current is a third of direct on line
     * — and so is the torque. The transition back to delta draws a second peak
     * that this does not model.
     */
    STAR_DELTA(currentFactor = 1.0 / 3.0, torqueFactor = 1.0 / 3.0),

    /**
     * Soft starter at a 50 % voltage ramp.
     *
     * Current falls with voltage and torque with its square, which is why a
     * soft start that looks gentle on paper stalls a conveyor.
     */
    SOFT_STARTER_50(currentFactor = 0.5, torqueFactor = 0.25),

    /** Autotransformer on the 65 % tap, the commonest of the three taps. */
    AUTOTRANSFORMER_65(currentFactor = 0.65 * 0.65, torqueFactor = 0.65 * 0.65),
}

/**
 * A motor starting against a transformer of finite strength.
 *
 * @param lockedRotorMultiple starting current as a multiple of full-load
 *   current, from the motor's own plate. Six is typical and seven or eight is
 *   not unusual; guessing costs the whole answer, which is why it is asked for.
 * @param transformerImpedancePercent the transformer's short-circuit impedance,
 *   uk. This is what decides how stiff the supply is: everything else being
 *   equal, a 4 % transformer dips half as much as an 8 % one.
 */
data class MotorStartingInput(
    val fullLoadCurrentAmps: Double,
    val lockedRotorMultiple: Double,
    val method: StartingMethod,
    val supplyVoltage: Double,
    val transformerKva: Double,
    val transformerImpedancePercent: Double,
)

/**
 * What the board sees while the motor runs up.
 *
 * @param dipPercent the drop at the transformer's terminals, as a percentage of
 *   nominal.
 * @param residualVoltage what is left during the start, in volts.
 * @param startingTorquePercent the torque available at that residual voltage,
 *   as a percentage of the motor's own direct-on-line starting torque. The
 *   number that decides whether the motor actually accelerates.
 */
data class MotorStartingResult(
    val startingCurrentAmps: Double,
    val startingKva: Double,
    val shortCircuitKva: Double,
    val dipPercent: Double,
    val residualVoltage: Double,
    val startingTorquePercent: Double,
) {
    /**
     * Whether a contactor coil is likely to drop out.
     *
     * Coils to IEC 60947 must hold at 85 % of rated voltage and are permitted
     * to release below 20 %. Real ones let go somewhere in between, so 80 % is
     * the point at which a dip stops being a lighting complaint and starts
     * being a plant that shuts itself down.
     */
    val risksContactorDropout: Boolean get() = dipPercent > CONTACTOR_DROPOUT_PERCENT

    /** Whether the dip is large enough to be seen in the lighting. */
    val visibleFlicker: Boolean get() = dipPercent > FLICKER_PERCENT

    companion object {
        const val CONTACTOR_DROPOUT_PERCENT = 20.0

        /**
         * A convention rather than a limit.
         *
         * Repetitive dips of a few per cent are what flicker standards are
         * written about; an isolated motor start is judged more loosely. Three
         * per cent is the point at which people start noticing.
         */
        const val FLICKER_PERCENT = 3.0
    }
}
