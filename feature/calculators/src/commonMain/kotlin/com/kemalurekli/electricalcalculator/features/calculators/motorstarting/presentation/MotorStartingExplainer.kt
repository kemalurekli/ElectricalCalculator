package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingInput
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_step_dip
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_step_inrush
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_step_short_circuit_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_step_starting_power

/**
 * Four lines, in the order the quantities depend on each other.
 *
 * The inrush first, then what it is in kVA, then how strong the supply is, and
 * only then the dip — because the dip is a ratio of the two powers and reading
 * it before both are on screen tells the reader nothing they can check.
 */
internal fun explainMotorStarting(
    input: MotorStartingInput,
    result: MotorStartingResult,
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> = persistentListOf(
    CalculationStep(
        label = Res.string.ms_step_inrush,
        formula = "Ist = In · LRC · k",
        substitution = "${f(input.fullLoadCurrentAmps, symbols)} × " +
            "${f(input.lockedRotorMultiple, symbols)} × ${f(input.method.currentFactor, symbols)}",
        result = "${f(result.startingCurrentAmps, symbols)} A",
    ),
    CalculationStep(
        label = Res.string.ms_step_starting_power,
        formula = "Sst = √3 · U · Ist",
        substitution = "1.732 × ${f(input.supplyVoltage, symbols)} × " +
            f(result.startingCurrentAmps, symbols),
        result = "${f(result.startingKva, symbols)} kVA",
    ),
    CalculationStep(
        label = Res.string.ms_step_short_circuit_power,
        formula = "Ssc = St / (uk / 100)",
        substitution = "${f(input.transformerKva, symbols)} / " +
            "(${f(input.transformerImpedancePercent, symbols)} / 100)",
        result = if (result.shortCircuitKva.isFinite()) {
            "${f(result.shortCircuitKva, symbols)} kVA"
        } else {
            "∞"
        },
    ),
    CalculationStep(
        label = Res.string.ms_step_dip,
        formula = "ΔU% = Sst / (Ssc + Sst)",
        substitution = if (result.shortCircuitKva.isFinite()) {
            "${f(result.startingKva, symbols)} / " +
                "(${f(result.shortCircuitKva, symbols)} + ${f(result.startingKva, symbols)})"
        } else {
            "${f(result.startingKva, symbols)} / ∞"
        },
        result = "${f(result.dipPercent, symbols)} %",
    ),
)

private fun f(value: Double, symbols: NumberSymbols) =
    NumberFormatter.format(value, decimals = 2, symbols = symbols)
