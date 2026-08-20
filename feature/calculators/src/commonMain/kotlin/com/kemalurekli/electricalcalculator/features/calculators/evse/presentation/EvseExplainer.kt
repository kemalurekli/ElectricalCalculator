package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseInput
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_step_connected
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_step_design
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_step_per_point

/** Three lines: one point, the bank, and what is left after simultaneity. */
internal fun explainEvse(
    input: EvseInput,
    result: EvseResult,
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> = persistentListOf(
    CalculationStep(
        label = Res.string.ev_step_per_point,
        formula = "P = k · U · I",
        substitution = "${f(input.connection.phaseFactor, symbols)} × " +
            "${f(input.supplyVoltage, symbols)} × ${f(input.ratedCurrentPerPoint, symbols)}",
        result = "${f(result.powerPerPointKw, symbols)} kW",
    ),
    CalculationStep(
        label = Res.string.ev_step_connected,
        formula = "Itot = n · I",
        substitution = "${input.pointCount} × ${f(input.ratedCurrentPerPoint, symbols)}",
        result = "${f(result.totalConnectedAmps, symbols)} A",
    ),
    CalculationStep(
        label = Res.string.ev_step_design,
        formula = "Ib = Itot · c",
        substitution = "${f(result.totalConnectedAmps, symbols)} × " +
            f(input.simultaneityFactor, symbols),
        result = "${f(result.designCurrentAmps, symbols)} A",
    ),
)

private fun f(value: Double, symbols: NumberSymbols) =
    NumberFormatter.format(value, decimals = 2, symbols = symbols)
