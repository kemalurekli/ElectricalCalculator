package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * Conductor materials and their electrical constants.
 *
 * Values follow IEC 60228 / IEC 60287 for annealed copper and aluminium
 * conductors. They live in `core` rather than inside one calculator because
 * voltage drop, cable sizing and cable weight all resolve the same constants —
 * and a discrepancy between them would produce results that silently disagree.
 *
 * @param resistivityAt20C volume resistivity ρ₂₀ in Ω·mm²/m. Expressed per
 *   mm² of cross-section and per metre of length, which is the form the cable
 *   formulas use directly: `R = ρ · L / A`.
 * @param temperatureCoefficient α₂₀ in 1/K, the fractional change in resistance
 *   per kelvin above 20 °C.
 * @param densityKgPerDm3 used by the cable weight calculator.
 */
enum class ConductorMaterial(
    val resistivityAt20C: Double,
    val temperatureCoefficient: Double,
    val densityKgPerDm3: Double,
) {
    /** Annealed copper: ρ₂₀ = 1/58 Ω·mm²/m, the IEC reference conductivity. */
    COPPER(
        resistivityAt20C = 0.017241,
        temperatureCoefficient = 0.00393,
        densityKgPerDm3 = 8.89,
    ),

    /** Aluminium: ρ₂₀ = 1/35.4 Ω·mm²/m. */
    ALUMINIUM(
        resistivityAt20C = 0.028264,
        temperatureCoefficient = 0.00403,
        densityKgPerDm3 = 2.70,
    ),
    ;

    /**
     * Resistivity corrected to an operating temperature.
     *
     * `ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]`
     *
     * This correction is not optional in practice: a copper conductor at its
     * 70 °C PVC rating is about 20 % more resistive than at 20 °C, and ignoring
     * it under-reports voltage drop by the same margin.
     *
     * @param temperatureCelsius conductor operating temperature θ in °C.
     */
    fun resistivityAt(temperatureCelsius: Double): Double =
        resistivityAt20C * (1.0 + temperatureCoefficient * (temperatureCelsius - REFERENCE_TEMPERATURE_C))

    companion object {
        /** The temperature the tabulated constants are referenced to. */
        const val REFERENCE_TEMPERATURE_C = 20.0
    }
}
