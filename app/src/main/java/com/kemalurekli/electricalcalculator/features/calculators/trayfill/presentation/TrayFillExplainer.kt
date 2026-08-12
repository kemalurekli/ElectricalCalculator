package com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillInput
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a cable tray calculation into the worked solution behind it.
 *
 * The two arrangements get different solutions because they are different
 * calculations, not two views of one. A single layer is bounded by width and a
 * stack by area, and the worked lines say so: the width branch never mentions a
 * fill percentage, which is exactly the number that would mislead — a tray can
 * be 20 % full by area with no room left across it.
 *
 * In the width sum, `N − 1` is written out rather than folded into a total.
 * Counting one gap per cable instead of one between them is the standard slip,
 * and it quietly demands a wider tray than the installation needs.
 */
internal fun explainTrayFill(
    input: TrayFillInput,
    result: TrayFillResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> = when (result) {
    is TrayFillResult.SingleLayer -> explainSingleLayer(input, result, locale)
    is TrayFillResult.MultiLayer -> explainMultiLayer(input, result, locale)
}

private fun explainSingleLayer(
    input: TrayFillInput,
    result: TrayFillResult.SingleLayer,
    locale: Locale,
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val gaps = (result.cableCount - 1).coerceAtLeast(0)

    return persistentListOf(
        CalculationStep(
            labelRes = R.string.tf_step_required_width,
            formula = "W_req = Σ(n · d) + s · (N − 1)",
            substitution = input.cables.joinToString(" + ") { entry ->
                "${entry.quantity} × ${n(entry.diameterMm)}"
            } + " + ${n(input.clearSpacingMm)} × $gaps",
            result = "${n(result.requiredWidthMm)} mm",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_spare_width,
            formula = "W_spare = W − W_req",
            substitution = "${n(result.trayWidthMm)} − ${n(result.requiredWidthMm)}",
            result = "${n(result.spareWidthMm)} mm",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_width_used,
            formula = "W_req / W · 100",
            substitution = "${n(result.requiredWidthMm)} / ${n(result.trayWidthMm)} × 100",
            result = "${n(result.widthUsedFraction * PERCENT)} %",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_additional_cable,
            // One more cable brings one more gap with it, so the spacing comes
            // out of the spare width before the cable itself is measured.
            formula = "d_add = W_spare − s",
            substitution = "${n(result.spareWidthMm)} − ${n(input.clearSpacingMm)}",
            result = "${n(result.largestAdditionalCableMm)} mm",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_occupied_depth,
            formula = "h = A_c / W",
            substitution = "${n(result.cableAreaMm2)} / ${n(result.trayWidthMm)}",
            result = "${n(result.occupiedDepthMm)} mm",
        ),
    )
}

private fun explainMultiLayer(
    input: TrayFillInput,
    result: TrayFillResult.MultiLayer,
    locale: Locale,
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val largestDiameter = input.cables.maxOfOrNull { it.diameterMm } ?: 0.0
    val perLayer = if (largestDiameter > 0.0) {
        (input.trayWidthMm / largestDiameter).toInt()
    } else {
        0
    }

    val steps = mutableListOf(
        CalculationStep(
            labelRes = R.string.tf_step_tray_area,
            formula = "A = W · H",
            substitution = "${n(input.trayWidthMm)} × ${n(input.trayDepthMm)}",
            result = "${n(result.trayAreaMm2)} mm²",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_cable_area,
            formula = "A_c = Σ(n · π · d² / 4)",
            substitution = input.cables.joinToString(" + ") { entry ->
                "${entry.quantity} × π × ${n(entry.diameterMm)}² / 4"
            },
            result = "${n(result.cableAreaMm2)} mm²",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_fill,
            formula = "fill = A_c / A · 100",
            substitution = "${n(result.cableAreaMm2)} / ${n(result.trayAreaMm2)} × 100",
            result = "${n(result.fillFraction * PERCENT)} %",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_spare_area,
            formula = "A_spare = A · fill_max − A_c",
            substitution = "${n(result.trayAreaMm2)} × " +
                "${n(result.permittedFraction, RATIO_DECIMALS)} − ${n(result.cableAreaMm2)}",
            result = "${n(result.spareAreaMm2)} mm²",
        ),
        CalculationStep(
            labelRes = R.string.tf_step_occupied_depth,
            formula = "h = A_c / W",
            substitution = "${n(result.cableAreaMm2)} / ${n(input.trayWidthMm)}",
            result = "${n(result.occupiedDepthMm)} mm",
        ),
    )

    // A stack is only as shallow as its thickest cable allows, so the layer
    // count is meaningless without one to measure against.
    if (perLayer > 0) {
        steps += CalculationStep(
            labelRes = R.string.tf_step_layers,
            formula = "layers = ⌈N / ⌊W / d_max⌋⌉",
            substitution = "⌈${result.cableCount} / ⌊${n(input.trayWidthMm)} / " +
                "${n(largestDiameter)}⌋⌉",
            result = "${result.estimatedLayers}",
        )
    }

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val PERCENT = 100.0
