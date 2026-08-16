package com.kemalurekli.electricalcalculator.features.design.domain

import com.kemalurekli.electricalcalculator.core.domain.model.LoadedConductors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.AdiabaticFactors
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultInput
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultResult
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult
import javax.inject.Inject

/**
 * Designs one circuit from end to end, and says what decided it.
 *
 * ### Why this exists when four calculators already do the parts
 *
 * They do the parts, and the parts disagree. Sizing by current capacity gives
 * one answer, the voltage drop another, the earth fault loop a third; the
 * design is the largest of them, and the app's own field note
 * (`fn_volt_drop_bites_before_ampacity`) already tells the reader that doing
 * these in the wrong order means sizing the cable twice. Today the reader does
 * the ordering by hand, copying a cross-section between four screens and
 * re-running whichever stage the last change invalidated.
 *
 * What no single calculator can answer, and what an engineer actually asks, is
 * *which* requirement decided the size. That determines what to do next: a
 * drop-bound run can be shortened or given a larger allowance, an
 * ampacity-bound one cannot, and a loop-bound one usually wants a bigger
 * protective conductor rather than a bigger cable.
 *
 * ### How the size is chosen
 *
 * By walking the tabulated sizes upward and stopping at the first that passes
 * every stage, rather than by taking the largest of four independently computed
 * answers. The stages are not independent: the loop impedance depends on the
 * cross-section, which the ampacity stage has just chosen, and enlarging the
 * cable to fix a drop also lowers Zs. Walking the list once is both simpler and
 * correct, and it makes "which constraint bound it" fall out for free — it is
 * whatever rejected the size immediately below the one that won.
 *
 * ### Why the coordination rule uses In and not Ib
 *
 * IEC 60364-4-43 asks for `Ib ≤ In ≤ Iz`. The cable has to carry the *device*,
 * not the load: a 22 A load on a 25 A breaker needs a cable good for 25 A,
 * because 24 A is a current the breaker will happily pass forever. The voltage
 * drop stage, by contrast, is judged at Ib — the drop that actually occurs.
 * Mixing those two up is the most common way to get a plausible wrong answer
 * out of this chain, so they are computed from different currents on purpose.
 */
class DesignCircuitUseCase @Inject constructor(
    private val ampacityTable: AmpacityTable,
    private val correctionFactors: CorrectionFactors,
    private val calculateVoltageDrop: CalculateVoltageDropUseCase,
    private val calculateEarthFault: CalculateEarthFaultUseCase,
) {

    operator fun invoke(input: CircuitDesignInput): CircuitDesignResult {
        val designCurrent = designCurrent(input)
        val deviceRating = ProtectiveDeviceRatings.smallestAtLeast(designCurrent)
            ?: return CircuitDesignResult.beyondDeviceRange(designCurrent)

        val ambientFactor = correctionFactors.ambientFactor(
            input.ambientTemperatureC,
            input.insulation,
        )
        val groupingFactor = correctionFactors.groupingFactor(input.groupedCircuits)
        val derating = ambientFactor * groupingFactor
        val conductors = LoadedConductors.forSystem(input.system)

        // The last size that failed, and why. When a size finally passes, this
        // holds the reason the one below it did not — which is the answer.
        var rejectedBy = BindingConstraint.NONE
        var lastAttempt: Attempt? = null

        ampacityTable.tabulatedSizes(input.material).forEach { area ->
            val attempt = evaluate(input, designCurrent, area, deviceRating, derating, conductors)
                ?: return@forEach
            lastAttempt = attempt

            val failed = attempt.stages.firstOrNull { !it.passes }
            if (failed != null) {
                rejectedBy = failed.constraint
                return@forEach
            }
            return attempt.toResult(
                designCurrent,
                deviceRating,
                rejectedBy,
                input.externalImpedanceOhms,
            )
        }

        // Nothing passed. The largest size is still worth reporting: it says how
        // far off the design is, which "no solution" on its own does not.
        val furthest = lastAttempt
            ?: return CircuitDesignResult.beyondDeviceRange(designCurrent)
                .copy(deviceRatingAmps = deviceRating, failure = DesignFailure.NO_TABULATED_SIZE)
        return furthest
            .toResult(designCurrent, deviceRating, rejectedBy, input.externalImpedanceOhms)
            .copy(crossSectionMm2 = null, failure = DesignFailure.NO_TABULATED_SIZE)
    }

    /** Ib. Power is converted through the system's own phase factor. */
    private fun designCurrent(input: CircuitDesignInput): Double = when (val load = input.load) {
        is CircuitLoad.Current -> load.amps
        is CircuitLoad.Power -> {
            // Active power to line current: P = k · U · I · cos φ, with cos φ
            // fixed at one on DC where it has no meaning.
            val powerFactor = if (input.system.isAc) input.powerFactor else 1.0
            load.watts / (input.system.powerPhaseFactor * input.systemVoltage * powerFactor)
        }
    }

    /** Everything one candidate cross-section produces. */
    private data class Attempt(
        val areaMm2: Double,
        val protectiveAreaMm2: Double,
        val deratedCapacityAmps: Double,
        val drop: VoltageDropResult,
        val loop: EarthFaultResult,
        val stages: List<DesignStage>,
    )

    /** Null when the size is not tabulated for this cable — aluminium below 2.5 mm². */
    private fun evaluate(
        input: CircuitDesignInput,
        designCurrentAmps: Double,
        areaMm2: Double,
        deviceRatingAmps: Double,
        derating: Double,
        conductors: LoadedConductors,
    ): Attempt? {
        val tabulated = ampacityTable.capacityAmps(
            areaMm2 = areaMm2,
            material = input.material,
            insulation = input.insulation,
            method = input.method,
            conductors = conductors,
        ) ?: return null

        val capacity = tabulated * derating * input.parallelConductors
        val protectiveArea = AdiabaticFactors.tabulatedProtectiveSection(areaMm2)

        val drop = calculateVoltageDrop(
            VoltageDropInput(
                systemVoltage = input.systemVoltage,
                loadCurrent = designCurrentAmps,
                lengthMetres = input.lengthMetres,
                crossSectionMm2 = areaMm2,
                material = input.material,
                system = input.system,
                powerFactor = input.powerFactor,
                conductorTemperatureC = input.insulation.maxConductorTemperatureC,
                parallelConductors = input.parallelConductors,
            ),
        )

        val loop = calculateEarthFault(
            EarthFaultInput(
                externalImpedanceOhms = input.externalImpedanceOhms,
                phaseVoltage = phaseVoltage(input),
                lengthMetres = input.lengthMetres,
                lineCrossSectionMm2 = areaMm2,
                protectiveCrossSectionMm2 = protectiveArea,
                parallelConductors = input.parallelConductors,
                material = input.material,
                insulation = input.insulation,
                deviceType = input.deviceType,
                deviceRatingAmps = deviceRatingAmps,
                clearingTimeSeconds = input.disconnectionTimeSeconds,
            ),
        )

        return Attempt(
            areaMm2 = areaMm2,
            protectiveAreaMm2 = protectiveArea,
            deratedCapacityAmps = capacity,
            drop = drop,
            loop = loop,
            // Declared in the order an engineer works them, so a reader
            // following the chain sees it unfold the way they would do it.
            stages = listOf(
                DesignStage(
                    constraint = BindingConstraint.CURRENT_CAPACITY,
                    passes = capacity >= deviceRatingAmps,
                    value = capacity,
                    limit = deviceRatingAmps,
                ),
                DesignStage(
                    constraint = BindingConstraint.VOLTAGE_DROP,
                    passes = drop.dropPercentage <= input.maxVoltageDropPercent,
                    value = drop.dropPercentage,
                    limit = input.maxVoltageDropPercent,
                ),
                DesignStage(
                    constraint = BindingConstraint.EARTH_FAULT_LOOP,
                    passes = loop.disconnectsInTime,
                    value = loop.loopImpedanceOhms,
                    limit = loop.maximumPermittedOhms,
                ),
                DesignStage(
                    constraint = BindingConstraint.PROTECTIVE_CONDUCTOR,
                    passes = loop.protectiveConductorWithstands,
                    value = protectiveArea,
                    limit = loop.adiabaticMinimumMm2,
                ),
            ),
        )
    }

    /**
     * The voltage across the fault loop, which is line-to-neutral.
     *
     * An earth fault on a three-phase circuit is a line-to-earth fault, driven
     * by U/√3 rather than by the line-to-line voltage the circuit is named
     * after. Feeding 400 V into the loop calculation would understate Zs by the
     * same √3 and pass circuits that do not disconnect.
     */
    private fun phaseVoltage(input: CircuitDesignInput): Double =
        input.systemVoltage / input.system.powerPhaseFactor

    private fun Attempt.toResult(
        designCurrentAmps: Double,
        deviceRatingAmps: Double,
        bindingConstraint: BindingConstraint,
        externalImpedanceOhms: Double,
    ) = CircuitDesignResult(
        designCurrentAmps = designCurrentAmps,
        deviceRatingAmps = deviceRatingAmps,
        crossSectionMm2 = areaMm2,
        protectiveCrossSectionMm2 = protectiveAreaMm2,
        bindingConstraint = bindingConstraint,
        deratedCapacityAmps = deratedCapacityAmps,
        voltageDropPercent = drop.dropPercentage,
        voltageDropVolts = drop.voltageDrop,
        loopImpedanceOhms = loop.loopImpedanceOhms,
        maximumLoopImpedanceOhms = loop.maximumPermittedOhms,
        externalImpedanceOhms = externalImpedanceOhms,
        stages = stages,
    )
}
