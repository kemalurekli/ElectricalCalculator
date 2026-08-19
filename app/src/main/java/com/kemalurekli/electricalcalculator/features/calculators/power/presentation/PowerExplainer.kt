package com.kemalurekli.electricalcalculator.features.calculators.power.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerInput
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale
import kotlin.math.sqrt

/**
 * Turns a power calculation into the worked solution behind it.
 *
 * Apparent power comes first because everything else is derived from it, which
 * is the opposite of how most people remember the power triangle. Seeing S
 * computed from the measurable quantities — voltage and current — and then P
 * and Q falling out of it is the point of showing the work at all.
 *
 * On DC the reactive step is omitted rather than shown as zero: there is no
 * phase angle to speak of, and a line reading "Q = 0" implies there was a
 * calculation where there was none.
 */
internal fun explainPower(
    input: PowerInput,
    result: PowerResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val isAc = input.system.isAc
    val powerFactor = if (isAc) input.powerFactor else 1.0
    val phaseFactor = input.system.powerPhaseFactor

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.pw_step_apparent,
        formula = "S = k · U · I",
        substitution = "${n(phaseFactor, RATIO_DECIMALS)} × ${n(input.voltage)} × " +
            n(input.current),
        result = "${n(result.apparentPowerVa)} VA",
    )

    steps += CalculationStep(
        labelRes = R.string.pw_step_active,
        formula = "P = S · cos φ",
        substitution = "${n(result.apparentPowerVa)} × ${n(powerFactor, RATIO_DECIMALS)}",
        result = "${n(result.activePowerWatts)} W",
    )

    if (isAc) {
        val sinPhi = sqrt((1.0 - powerFactor * powerFactor).coerceAtLeast(0.0))
        steps += CalculationStep(
            labelRes = R.string.pw_step_reactive,
            formula = "Q = S · sin φ",
            substitution = "${n(result.apparentPowerVa)} × ${n(sinPhi, RATIO_DECIMALS)}",
            result = "${n(result.reactivePowerVar)} var",
        )

        steps += CalculationStep(
            labelRes = R.string.pw_step_angle,
            formula = "φ = arccos(cos φ)",
            substitution = "arccos(${n(powerFactor, RATIO_DECIMALS)})",
            result = "${n(result.phaseAngleDegrees)}°",
        )
    }

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
