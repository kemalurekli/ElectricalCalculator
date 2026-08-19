package com.kemalurekli.electricalcalculator.features.calculators.motor.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorInput
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a motor current calculation into the worked solution behind it.
 *
 * Lives beside the ViewModel rather than in the domain: the substitution
 * strings are display, and the use case should not be formatting numbers. It is
 * still plain Kotlin — no Compose, no Android beyond a string id — so it is
 * covered by ordinary unit tests with an explicit locale.
 *
 * The steps deliberately follow the order the physics does, because the whole
 * point is that the reader can follow it: nameplate output, then input power,
 * then current. The step that most often surprises people — dividing by the
 * efficiency before anything else — is therefore the first one they see.
 */
internal fun explainMotorCurrent(
    input: MotorInput,
    result: MotorResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val outputWatts = input.powerUnit.toWatts(input.ratedPower)
    val powerFactor = if (input.system.isAc) input.powerFactor else 1.0
    val phaseFactor = input.system.powerPhaseFactor

    val steps = mutableListOf<CalculationStep>()

    // Always shown: every unit the app offers has a conversion factor other
    // than one, and making it explicit is what separates the two horsepowers
    // — 745.7 W and 735.5 W — that otherwise look interchangeable.
    steps += CalculationStep(
        labelRes = R.string.mt_step_output,
        formula = "P_out = P_n · c",
        substitution = "${n(input.ratedPower)} × ${n(input.powerUnit.wattsPerUnit, UNIT_DECIMALS)}",
        result = "${n(outputWatts)} W",
    )

    steps += CalculationStep(
        labelRes = R.string.mt_step_input,
        formula = "P_in = P_out / η",
        substitution = "${n(outputWatts)} / ${n(input.efficiency, RATIO_DECIMALS)}",
        result = "${n(result.inputPowerWatts)} W",
    )

    steps += CalculationStep(
        labelRes = R.string.mt_step_current,
        formula = "I = P_in / (k · U · cos φ)",
        substitution = "${n(result.inputPowerWatts)} / (${n(phaseFactor, RATIO_DECIMALS)} × " +
            "${n(input.voltage)} × ${n(powerFactor, RATIO_DECIMALS)})",
        result = "${n(result.fullLoadCurrent)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.mt_step_starting,
        formula = "I_start = I · r",
        substitution = "${n(result.fullLoadCurrent)} × ${n(input.startingCurrentRatio, RATIO_DECIMALS)}",
        result = "${n(result.startingCurrent)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.mt_step_losses,
        formula = "P_loss = P_in − P_out",
        substitution = "${n(result.inputPowerWatts)} − ${n(outputWatts)}",
        result = "${n(result.lossesWatts)} W",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val UNIT_DECIMALS = 4
