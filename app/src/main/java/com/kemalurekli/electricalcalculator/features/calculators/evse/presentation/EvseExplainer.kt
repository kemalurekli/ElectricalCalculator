package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseInput
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

/** Three lines: one point, the bank, and what is left after simultaneity. */
internal fun explainEvse(
    input: EvseInput,
    result: EvseResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> = persistentListOf(
    CalculationStep(
        labelRes = R.string.ev_step_per_point,
        formula = "P = k · U · I",
        substitution = "${f(input.connection.phaseFactor, locale)} × " +
            "${f(input.supplyVoltage, locale)} × ${f(input.ratedCurrentPerPoint, locale)}",
        result = "${f(result.powerPerPointKw, locale)} kW",
    ),
    CalculationStep(
        labelRes = R.string.ev_step_connected,
        formula = "Itot = n · I",
        substitution = "${input.pointCount} × ${f(input.ratedCurrentPerPoint, locale)}",
        result = "${f(result.totalConnectedAmps, locale)} A",
    ),
    CalculationStep(
        labelRes = R.string.ev_step_design,
        formula = "Ib = Itot · c",
        substitution = "${f(result.totalConnectedAmps, locale)} × " +
            f(input.simultaneityFactor, locale),
        result = "${f(result.designCurrentAmps, locale)} A",
    ),
)

private fun f(value: Double, locale: Locale) =
    NumberFormatter.format(value, decimals = 2, locale = locale)
