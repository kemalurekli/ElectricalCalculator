package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingInput
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

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
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> = persistentListOf(
    CalculationStep(
        labelRes = R.string.ms_step_inrush,
        formula = "Ist = In · LRC · k",
        substitution = "${f(input.fullLoadCurrentAmps, locale)} × " +
            "${f(input.lockedRotorMultiple, locale)} × ${f(input.method.currentFactor, locale)}",
        result = "${f(result.startingCurrentAmps, locale)} A",
    ),
    CalculationStep(
        labelRes = R.string.ms_step_starting_power,
        formula = "Sst = √3 · U · Ist",
        substitution = "1.732 × ${f(input.supplyVoltage, locale)} × " +
            f(result.startingCurrentAmps, locale),
        result = "${f(result.startingKva, locale)} kVA",
    ),
    CalculationStep(
        labelRes = R.string.ms_step_short_circuit_power,
        formula = "Ssc = St / (uk / 100)",
        substitution = "${f(input.transformerKva, locale)} / " +
            "(${f(input.transformerImpedancePercent, locale)} / 100)",
        result = if (result.shortCircuitKva.isFinite()) {
            "${f(result.shortCircuitKva, locale)} kVA"
        } else {
            "∞"
        },
    ),
    CalculationStep(
        labelRes = R.string.ms_step_dip,
        formula = "ΔU% = Sst / (Ssc + Sst)",
        substitution = if (result.shortCircuitKva.isFinite()) {
            "${f(result.startingKva, locale)} / " +
                "(${f(result.shortCircuitKva, locale)} + ${f(result.startingKva, locale)})"
        } else {
            "${f(result.startingKva, locale)} / ∞"
        },
        result = "${f(result.dipPercent, locale)} %",
    ),
)

private fun f(value: Double, locale: Locale) =
    NumberFormatter.format(value, decimals = 2, symbols = locale.toNumberSymbols())
