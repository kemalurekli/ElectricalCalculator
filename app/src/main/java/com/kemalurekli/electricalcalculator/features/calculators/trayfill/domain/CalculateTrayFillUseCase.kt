package com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain

import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.ceil

/**
 * Checks whether a cable tray will carry a bundle.
 *
 * ### Single layer — width
 *
 * ```
 * W_req = Σ(n · d) + s · (N − 1)
 * ```
 *
 * `N − 1` gaps, not `N`: the spacing sits *between* cables, and counting an
 * extra gap would quietly demand a wider tray than the installation needs.
 *
 * ### Multiple layers — area
 *
 * ```
 * A_tray  = W · H
 * fill    = Σ(n · π · d² / 4) / A_tray
 * ```
 *
 * ### Why the two are separate calculations
 *
 * A tray of 100 mm × 60 mm carrying four 20 mm cables in one layer is 21 % full
 * by area — and completely full by width. Reporting only the area figure would
 * declare four fifths of the tray still available when nothing more can be laid
 * in it. Which constraint binds depends on how the cables are installed, so the
 * arrangement is an input, not a presentation detail.
 */
class CalculateTrayFillUseCase @Inject constructor() {

    operator fun invoke(input: TrayFillInput): TrayFillResult {
        val cableCount = input.cables.sumOf { it.quantity }
        val cableArea = input.cables.sumOf { circleArea(it.diameterMm) * it.quantity }

        return when (input.arrangement) {
            TrayArrangement.SINGLE_LAYER -> singleLayer(input, cableCount, cableArea)
            TrayArrangement.MULTI_LAYER -> multiLayer(input, cableCount, cableArea)
        }
    }

    private fun singleLayer(
        input: TrayFillInput,
        cableCount: Int,
        cableArea: Double,
    ): TrayFillResult.SingleLayer {
        val diameterSum = input.cables.sumOf { it.diameterMm * it.quantity }
        val gaps = (cableCount - 1).coerceAtLeast(0)
        val requiredWidth = diameterSum + input.clearSpacingMm * gaps
        val spareWidth = input.trayWidthMm - requiredWidth

        // One more cable brings one more gap with it, so the spacing has to
        // come out of the spare width before the cable itself is measured.
        val additional = (spareWidth - input.clearSpacingMm).coerceAtLeast(0.0)

        return TrayFillResult.SingleLayer(
            cableCount = cableCount,
            cableAreaMm2 = cableArea,
            trayWidthMm = input.trayWidthMm,
            requiredWidthMm = requiredWidth,
            spareWidthMm = spareWidth,
            widthUsedFraction = requiredWidth / input.trayWidthMm,
            largestAdditionalCableMm = additional,
            occupiedDepthMm = cableArea / input.trayWidthMm,
        )
    }

    private fun multiLayer(
        input: TrayFillInput,
        cableCount: Int,
        cableArea: Double,
    ): TrayFillResult.MultiLayer {
        val trayArea = input.trayWidthMm * input.trayDepthMm
        val permittedArea = trayArea * input.permittedFillFraction

        // Layers are counted with the largest cable as the layer height: a
        // stack is only as shallow as its thickest cable allows.
        val largestDiameter = input.cables.maxOfOrNull { it.diameterMm } ?: 0.0
        val perLayer = if (largestDiameter > 0.0) {
            (input.trayWidthMm / largestDiameter).toInt()
        } else {
            0
        }
        val layers = if (perLayer > 0) ceil(cableCount.toDouble() / perLayer).toInt() else 0

        return TrayFillResult.MultiLayer(
            cableCount = cableCount,
            cableAreaMm2 = cableArea,
            trayAreaMm2 = trayArea,
            fillFraction = cableArea / trayArea,
            permittedFraction = input.permittedFillFraction,
            spareAreaMm2 = permittedArea - cableArea,
            occupiedDepthMm = cableArea / input.trayWidthMm,
            estimatedLayers = layers,
        )
    }

    private fun circleArea(diameter: Double): Double = PI * diameter * diameter / 4.0
}
