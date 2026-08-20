package com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A validated set of cable-sizing inputs.
 *
 * @param designCurrent the circuit design current I_b in A.
 * @param maxVoltageDropPercent permitted drop, as a percentage of [systemVoltage].
 *   IEC 60364-5-52 Annex G recommends 3 % for lighting and 5 % otherwise.
 * @param ambientTemperatureC ambient air temperature; the tables assume 30 °C.
 * @param groupedCircuits number of circuits bunched with this one, including it.
 * @param parallelConductors conductors per phase. Running `n` in parallel both
 *   divides the current each carries and divides the resistance of the run.
 */
data class CableSizeInput(
    val systemVoltage: Double,
    val designCurrent: Double,
    val lengthMetres: Double,
    val material: ConductorMaterial,
    val insulation: CableInsulation,
    val method: InstallationMethod,
    val system: SupplySystem,
    val powerFactor: Double,
    val maxVoltageDropPercent: Double,
    val ambientTemperatureC: Double,
    val groupedCircuits: Int,
    val parallelConductors: Int,
)

/** Which requirement decided the recommended size. */
enum class GoverningConstraint {
    /** The conductor would overheat at a smaller size. */
    CURRENT_CAPACITY,

    /** The conductor would carry current safely, but the drop would be too large. */
    VOLTAGE_DROP,

    /** Both constraints land on the same size. */
    BOTH,
}

/**
 * The outcome of a cable-sizing calculation.
 *
 * Both limiting sizes are reported, not just the answer. An engineer needs to
 * see *why* a size was chosen: a run that is voltage-drop limited can often be
 * fixed by shortening it or raising the permitted drop, while an
 * ampacity-limited one cannot.
 *
 * @param recommendedAreaMm2 the smallest standard size satisfying both
 *   constraints, or null when no tabulated size does.
 * @param requiredCapacityAmps the capacity the cable must have *before*
 *   derating: `I_b / (Ca · Cg)`.
 * @param deratedCapacityAmps the recommended size's tabulated capacity after
 *   both correction factors.
 */
data class CableSizeResult(
    val recommendedAreaMm2: Double?,
    val currentCapacityAreaMm2: Double?,
    val voltageDropAreaMm2: Double?,
    val governingConstraint: GoverningConstraint,
    val requiredCapacityAmps: Double,
    val deratedCapacityAmps: Double,
    val ambientFactor: Double,
    val groupingFactor: Double,
    val voltageDropVolts: Double,
    val voltageDropPercent: Double,
) {
    /** True when no standard size satisfies the inputs. */
    val hasSolution: Boolean get() = recommendedAreaMm2 != null
}
