package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * Units a machine's rated power may be quoted in.
 *
 * The two horsepower definitions are genuinely different and are both in
 * everyday use: nameplates in North America use mechanical horsepower, while
 * European catalogues that quote "HP" or "PS" usually mean metric horsepower.
 * Treating them as interchangeable introduces a 1.4 % error, which is small but
 * entirely avoidable.
 *
 * @param wattsPerUnit exact conversion factor to watts.
 */
enum class PowerUnit(val wattsPerUnit: Double) {
    KILOWATT(1_000.0),

    /** Mechanical (imperial) horsepower, 550 ft·lbf/s. */
    HORSEPOWER(745.6998715822702),

    /** Metric horsepower, also written PS, BG or CV: 75 kgf·m/s. */
    METRIC_HORSEPOWER(735.49875),
    ;

    /** Converts a value expressed in this unit to watts. */
    fun toWatts(value: Double): Double = value * wattsPerUnit

    /** Converts watts to this unit. */
    fun fromWatts(watts: Double): Double = watts / wattsPerUnit
}
