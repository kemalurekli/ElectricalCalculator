package com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringInput
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

/**
 * Turns a string-sizing calculation into the worked solution behind it.
 *
 * The cold correction leads, because it is the step that turns a nameplate
 * 49,5 V module into a 54,2 V one and the step everybody skips. Seeing the
 * voltage go *up* as the temperature goes down is the whole idea, and a reader
 * who has watched it happen once will not size a string on the 25 °C figure
 * again.
 */
internal fun explainSolarString(
    input: SolarStringInput,
    result: SolarStringResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val stc = n(SolarStringInput.STC_TEMPERATURE_C, 0)

    return persistentListOf(
        CalculationStep(
            labelRes = R.string.ss_step_voc_cold,
            formula = "Voc(T) = Voc · (1 + β · (T − 25) / 100)",
            substitution = "${n(input.moduleVocVolts)} × (1 + " +
                "${n(input.voltageCoefficientPercentPerK, RATIO_DECIMALS)} × " +
                "(${n(input.minimumCellTemperatureC, 0)} − $stc) / 100)",
            result = "${n(result.vocAtMinimumTemperature)} V",
        ),
        CalculationStep(
            labelRes = R.string.ss_step_max_modules,
            formula = "n_max = ⌊V_inv / Voc(T_min)⌋",
            substitution = "⌊${n(input.inverterMaxDcVolts)} / " +
                "${n(result.vocAtMinimumTemperature)}⌋",
            result = "${result.maximumModules}",
        ),
        CalculationStep(
            labelRes = R.string.ss_step_string_voc,
            formula = "U_string = n_max · Voc(T_min)",
            substitution = "${result.maximumModules} × ${n(result.vocAtMinimumTemperature)}",
            result = "${n(result.stringVocAtMaximum)} V",
        ),
        CalculationStep(
            labelRes = R.string.ss_step_vmp_hot,
            formula = "Vmp(T) = Vmp · (1 + β · (T − 25) / 100)",
            substitution = "${n(input.moduleVmpVolts)} × (1 + " +
                "${n(input.voltageCoefficientPercentPerK, RATIO_DECIMALS)} × " +
                "(${n(input.maximumCellTemperatureC, 0)} − $stc) / 100)",
            result = "${n(result.vmpAtMaximumTemperature)} V",
        ),
        CalculationStep(
            labelRes = R.string.ss_step_min_modules,
            formula = "n_min = ⌈V_mppt / Vmp(T_max)⌉",
            substitution = "⌈${n(input.inverterMpptMinVolts)} / " +
                "${n(result.vmpAtMaximumTemperature)}⌉",
            result = "${result.minimumModules}",
        ),
    )
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
