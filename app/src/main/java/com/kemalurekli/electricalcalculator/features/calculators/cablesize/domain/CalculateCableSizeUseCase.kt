package com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain

import com.kemalurekli.electricalcalculator.core.domain.table.CorrectionFactors
import com.kemalurekli.electricalcalculator.core.domain.model.LoadedConductors
import javax.inject.Inject

/**
 * Selects the smallest standard conductor that satisfies a circuit.
 *
 * ### Two independent constraints
 *
 * A cable must be large enough to do two different things, and the required
 * size is the larger of the two:
 *
 * 1. **Carry the current without overheating.** The tabulated capacity is
 *    derated for ambient temperature and grouping, so the cable must satisfy
 *    `I_z ≥ I_b / (Ca · Cg)`.
 * 2. **Deliver usable voltage.** Rearranging the voltage-drop formula for area:
 *    `A ≥ k · I · ρ(θ) · L · cos φ / (ΔU_max · n)`
 *
 * Sizing on voltage drop alone can return a conductor that overheats; sizing on
 * ampacity alone can leave a long run with unusable voltage at the load. Both
 * are computed and both are reported, so the engineer can see which one binds.
 *
 * ### Assumptions
 *
 * Resistivity is evaluated at the insulation's maximum conductor temperature —
 * the worst case for voltage drop, since a hotter conductor is more resistive.
 * Reactance is neglected, as in the voltage drop calculator.
 */
class CalculateCableSizeUseCase @Inject constructor(
    private val ampacityTable: AmpacityTable,
    private val correctionFactors: CorrectionFactors,
) {

    operator fun invoke(input: CableSizeInput): CableSizeResult {
        val ambientFactor = correctionFactors.ambientFactor(
            input.ambientTemperatureC,
            input.insulation,
        )
        val groupingFactor = correctionFactors.groupingFactor(input.groupedCircuits)
        val deratingFactor = ambientFactor * groupingFactor

        // Current shared between conductors running in parallel.
        val currentPerConductor = input.designCurrent / input.parallelConductors
        val requiredCapacity = currentPerConductor / deratingFactor

        val conductors = LoadedConductors.forSystem(input.system)
        val capacityArea = smallestByCapacity(input, conductors, requiredCapacity)
        val dropArea = smallestByVoltageDrop(input)

        val recommended = listOfNotNull(capacityArea, dropArea)
            .takeIf { it.size == REQUIRED_CONSTRAINTS }
            ?.max()

        return CableSizeResult(
            recommendedAreaMm2 = recommended,
            currentCapacityAreaMm2 = capacityArea,
            voltageDropAreaMm2 = dropArea,
            governingConstraint = governingConstraint(capacityArea, dropArea),
            requiredCapacityAmps = requiredCapacity,
            deratedCapacityAmps = recommended
                ?.let { area ->
                    tabulatedCapacity(input, conductors, area)?.times(deratingFactor) ?: 0.0
                }
                ?: 0.0,
            ambientFactor = ambientFactor,
            groupingFactor = groupingFactor,
            voltageDropVolts = recommended?.let { voltageDropAt(input, it) } ?: 0.0,
            voltageDropPercent = recommended
                ?.let { voltageDropAt(input, it) / input.systemVoltage * PERCENT }
                ?: 0.0,
        )
    }

    /** Smallest tabulated size whose derated capacity covers the design current. */
    private fun smallestByCapacity(
        input: CableSizeInput,
        conductors: LoadedConductors,
        requiredCapacity: Double,
    ): Double? = ampacityTable.tabulatedSizes(input.material).firstOrNull { area ->
        val capacity = tabulatedCapacity(input, conductors, area)
        capacity != null && capacity >= requiredCapacity
    }

    /**
     * Smallest standard size that keeps the drop within the permitted limit.
     *
     * Solves `ΔU = k · I · ρ · L · cos φ / (A · n)` for A, then rounds up to a
     * manufactured size.
     */
    private fun smallestByVoltageDrop(input: CableSizeInput): Double? {
        val resistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)
        val maxDropVolts = input.systemVoltage * input.maxVoltageDropPercent / PERCENT

        val minimumArea = input.system.lengthMultiplier *
            input.designCurrent *
            resistivity *
            input.lengthMetres *
            input.powerFactorFor() /
            (maxDropVolts * input.parallelConductors)

        return ampacityTable.tabulatedSizes(input.material).firstOrNull { it >= minimumArea }
    }

    /** Actual drop at a chosen area, for reporting alongside the recommendation. */
    private fun voltageDropAt(input: CableSizeInput, areaMm2: Double): Double {
        val resistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)
        val resistance = resistivity * input.lengthMetres / (areaMm2 * input.parallelConductors)
        return input.system.lengthMultiplier *
            input.designCurrent *
            resistance *
            input.powerFactorFor()
    }

    private fun tabulatedCapacity(
        input: CableSizeInput,
        conductors: LoadedConductors,
        areaMm2: Double,
    ): Double? = ampacityTable.capacityAmps(
        areaMm2 = areaMm2,
        material = input.material,
        insulation = input.insulation,
        method = input.method,
        conductors = conductors,
    )

    /** Power factor applies only to AC; DC has no phase angle. */
    private fun CableSizeInput.powerFactorFor(): Double = if (system.isAc) powerFactor else 1.0

    private fun governingConstraint(
        capacityArea: Double?,
        dropArea: Double?,
    ): GoverningConstraint = when {
        capacityArea == null || dropArea == null -> GoverningConstraint.CURRENT_CAPACITY
        capacityArea > dropArea -> GoverningConstraint.CURRENT_CAPACITY
        dropArea > capacityArea -> GoverningConstraint.VOLTAGE_DROP
        else -> GoverningConstraint.BOTH
    }

    private companion object {
        const val PERCENT = 100.0

        /** Both the capacity and the drop constraint must resolve to a size. */
        const val REQUIRED_CONSTRAINTS = 2
    }
}
