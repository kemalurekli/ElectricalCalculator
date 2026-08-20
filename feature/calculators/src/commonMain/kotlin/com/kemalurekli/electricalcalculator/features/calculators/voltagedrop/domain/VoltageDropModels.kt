package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain

import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A fully validated set of voltage-drop inputs.
 *
 * Constructing one is only possible from validated text, so the calculation
 * itself never has to defend against blank fields or unparseable numbers.
 *
 * @param systemVoltage nominal supply voltage in V; line-to-line for three phase.
 * @param loadCurrent design current in A.
 * @param lengthMetres one-way route length in m. The return path is accounted
 *   for by [SupplySystem.lengthMultiplier], so this is the distance to the load,
 *   not the total conductor length.
 * @param crossSectionMm2 conductor cross-sectional area in mm².
 * @param powerFactor load cos φ. Ignored on a DC supply.
 * @param conductorTemperatureC operating temperature in °C, used to correct
 *   resistivity.
 * @param parallelConductors conductors per phase. Running `n` in parallel
 *   divides the effective resistance by `n`.
 */
data class VoltageDropInput(
    val systemVoltage: Double,
    val loadCurrent: Double,
    val lengthMetres: Double,
    val crossSectionMm2: Double,
    val material: ConductorMaterial,
    val system: SupplySystem,
    val powerFactor: Double,
    val conductorTemperatureC: Double,
    val parallelConductors: Int,
)

/**
 * How the computed drop compares with the limits recommended by
 * IEC 60364-5-52 Annex G.
 */
enum class VoltageDropStatus {
    /** Within 3 %: acceptable for lighting and for power circuits. */
    WITHIN_LIGHTING_LIMIT,

    /** Between 3 % and 5 %: acceptable for power, but exceeds the lighting limit. */
    WITHIN_POWER_LIMIT,

    /** Above 5 %: exceeds both recommended limits. */
    EXCEEDS_LIMITS,
    ;

    companion object {
        const val LIGHTING_LIMIT_PERCENT = 3.0
        const val POWER_LIMIT_PERCENT = 5.0

        fun forPercentage(percent: Double): VoltageDropStatus = when {
            percent <= LIGHTING_LIMIT_PERCENT -> WITHIN_LIGHTING_LIMIT
            percent <= POWER_LIMIT_PERCENT -> WITHIN_POWER_LIMIT
            else -> EXCEEDS_LIMITS
        }
    }
}

/**
 * The outcome of a voltage-drop calculation.
 *
 * @param voltageDrop ΔU in V.
 * @param dropPercentage ΔU as a percentage of the nominal supply voltage.
 * @param voltageAtLoad the voltage remaining at the load terminals, in V.
 * @param conductorResistance one-way resistance of a single conductor run at
 *   the operating temperature, in Ω, after any parallel conductors.
 * @param powerLossWatts total I²R loss across every current-carrying conductor.
 */
data class VoltageDropResult(
    val voltageDrop: Double,
    val dropPercentage: Double,
    val voltageAtLoad: Double,
    val conductorResistance: Double,
    val powerLossWatts: Double,
    val status: VoltageDropStatus,
)
