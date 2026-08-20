package com.kemalurekli.electricalcalculator.features.calculators.battery.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryInput
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_c_rate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_energy
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_full_discharge
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_ideal
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_rated_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_runtime
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_step_usable_capacity

/**
 * Turns a battery runtime calculation into the worked solution behind it.
 *
 * The Peukert step and the naive one are shown side by side on purpose. `t =
 * C / I` is the calculation almost everyone does in their head, and on a bank
 * discharged faster than its rating it is optimistic by a margin that matters —
 * a UPS sized on it runs out before the generator starts. Putting the two lines
 * next to each other turns that from a claim into something the reader can see.
 *
 * The rated current is its own step because it is the quantity the exponent acts
 * on: Peukert's law compares the *actual* discharge rate against the rate the
 * nameplate was measured at, and the nameplate rate is not on the nameplate.
 */
internal fun explainBatteryRuntime(
    input: BatteryInput,
    result: BatteryResult,
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, symbols)

    val ratedCurrent = input.capacityAh / input.ratedDischargeHours
    val fullDischargeHours = result.runtimeHours / input.depthOfDischarge

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        label = Res.string.bt_step_current,
        formula = "I = P / (U · η)",
        substitution = "${n(input.loadPowerWatts)} / (${n(input.bankVoltage)} × " +
            "${n(input.systemEfficiency, RATIO_DECIMALS)})",
        result = "${n(result.dischargeCurrentAmps)} A",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_rated_current,
        formula = "I_r = C / H",
        substitution = "${n(input.capacityAh)} / ${n(input.ratedDischargeHours)}",
        result = "${n(ratedCurrent)} A",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_c_rate,
        formula = "C_rate = I / C",
        substitution = "${n(result.dischargeCurrentAmps)} / ${n(input.capacityAh)}",
        result = "${n(result.cRate, RATIO_DECIMALS)} C",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_full_discharge,
        formula = "t_full = H · (I_r / I)^k",
        substitution = "${n(input.ratedDischargeHours)} × (${n(ratedCurrent)} / " +
            "${n(result.dischargeCurrentAmps)})^${n(input.peukertExponent, RATIO_DECIMALS)}",
        result = "${n(fullDischargeHours)} h",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_runtime,
        formula = "t = t_full · DoD",
        substitution = "${n(fullDischargeHours)} × " +
            n(input.depthOfDischarge, RATIO_DECIMALS),
        result = "${n(result.runtimeHours)} h",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_ideal,
        formula = "t_ideal = C / I · DoD",
        substitution = "${n(input.capacityAh)} / ${n(result.dischargeCurrentAmps)} × " +
            n(input.depthOfDischarge, RATIO_DECIMALS),
        result = "${n(result.idealRuntimeHours)} h",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_usable_capacity,
        formula = "C_u = C · DoD",
        substitution = "${n(input.capacityAh)} × ${n(input.depthOfDischarge, RATIO_DECIMALS)}",
        result = "${n(result.usableCapacityAh)} Ah",
    )

    steps += CalculationStep(
        label = Res.string.bt_step_energy,
        formula = "E = P · t",
        substitution = "${n(input.loadPowerWatts)} × ${n(result.runtimeHours)}",
        result = "${n(result.energyDeliveredWh)} Wh",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
