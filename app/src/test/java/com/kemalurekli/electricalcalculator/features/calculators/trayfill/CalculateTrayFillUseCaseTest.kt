package com.kemalurekli.electricalcalculator.features.calculators.trayfill

import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillInput
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTrayFillUseCaseTest {

    private val calculate = CalculateTrayFillUseCase()

    private fun input(
        width: Double = 300.0,
        depth: Double = 100.0,
        cables: List<CableBundleEntry> = listOf(CableBundleEntry(20.5, 6)),
        arrangement: TrayArrangement = TrayArrangement.SINGLE_LAYER,
        spacing: Double = 0.0,
        limit: Double = 0.40,
    ) = TrayFillInput(
        trayWidthMm = width,
        trayDepthMm = depth,
        cables = cables,
        arrangement = arrangement,
        clearSpacingMm = spacing,
        permittedFillFraction = limit,
    )

    private fun single(input: TrayFillInput) = calculate(input) as TrayFillResult.SingleLayer
    private fun multi(input: TrayFillInput) = calculate(input) as TrayFillResult.MultiLayer

    // -- Single layer, judged on width -------------------------------------------------

    @Test
    fun `worked example - six 20_5 mm cables touching in a 300 mm tray`() {
        val result = single(input())

        assertEquals(123.0, result.requiredWidthMm, 1e-9)
        assertEquals(177.0, result.spareWidthMm, 1e-9)
        assertEquals(0.41, result.widthUsedFraction, 1e-9)
        assertEquals(6, result.cableCount)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `touching cables need only the sum of their diameters`() {
        val result = single(input(cables = listOf(CableBundleEntry(20.0, 5))))

        assertEquals(100.0, result.requiredWidthMm, 1e-9)
    }

    @Test
    fun `spacing is counted between cables, not after every one`() {
        // Six cables leave five gaps. Charging a sixth gap would demand a wider
        // tray than the installation actually needs.
        val result = single(input(spacing = 20.5))

        assertEquals(123.0 + 5 * 20.5, result.requiredWidthMm, 1e-9)
        assertEquals(225.5, result.requiredWidthMm, 1e-9)
    }

    @Test
    fun `a single cable needs no spacing at all`() {
        val result = single(input(cables = listOf(CableBundleEntry(20.0, 1)), spacing = 50.0))

        assertEquals(20.0, result.requiredWidthMm, 1e-9)
    }

    @Test
    fun `spacing one diameter apart roughly doubles the width needed`() {
        // The trade an engineer makes to get the better grouping factor.
        val touching = single(input()).requiredWidthMm
        val spaced = single(input(spacing = 20.5)).requiredWidthMm

        assertTrue(spaced > touching)
        assertEquals(225.5 / 123.0, spaced / touching, 1e-9)
    }

    @Test
    fun `cables of different sizes are summed by diameter across entries`() {
        val result = single(
            input(cables = listOf(CableBundleEntry(20.5, 4), CableBundleEntry(11.9, 6))),
        )

        assertEquals(153.4, result.requiredWidthMm, 1e-9)
        assertEquals(10, result.cableCount)
    }

    @Test
    fun `a bundle wider than the tray is rejected with the shortfall`() {
        val result = single(input(width = 100.0, cables = listOf(CableBundleEntry(20.5, 6))))

        assertFalse(result.isWithinLimit)
        assertEquals(-23.0, result.spareWidthMm, 1e-9)
    }

    @Test
    fun `a bundle exactly the tray's width fits`() {
        val result = single(input(width = 123.0))

        assertTrue(result.isWithinLimit)
        assertEquals(0.0, result.spareWidthMm, 1e-9)
        assertEquals(1.0, result.widthUsedFraction, 1e-9)
    }

    @Test
    fun `the largest additional cable allows for the gap it brings`() {
        // 74.5 mm of spare width, but a new cable also needs a 20.5 mm gap
        // before it, so only 54 mm of that is cable.
        val result = single(input(spacing = 20.5))

        assertEquals(74.5, result.spareWidthMm, 1e-9)
        assertEquals(54.0, result.largestAdditionalCableMm, 1e-9)
    }

    @Test
    fun `with no spacing the spare width is entirely available`() {
        val result = single(input())

        assertEquals(result.spareWidthMm, result.largestAdditionalCableMm, 1e-9)
    }

    @Test
    fun `a full tray admits nothing more`() {
        val result = single(input(width = 123.0))

        assertEquals(0.0, result.largestAdditionalCableMm, 1e-9)
    }

    @Test
    fun `a tray that cannot take the bundle never reports room for more`() {
        val result = single(input(width = 100.0))

        assertFalse(result.isWithinLimit)
        assertEquals(0.0, result.largestAdditionalCableMm, 1e-9)
    }

    // -- Multiple layers, judged on area ---------------------------------------------------

    @Test
    fun `worked example - thirty control cables stacked in a 300 by 100 tray`() {
        val result = multi(
            input(
                cables = listOf(CableBundleEntry(11.9, 30)),
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertEquals(30_000.0, result.trayAreaMm2, 1e-9)
        assertEquals(3336.607018, result.cableAreaMm2, 1e-6)
        assertEquals(0.11122, result.fillFraction, 1e-6)
        assertEquals(8663.392982, result.spareAreaMm2, 1e-6)
        assertTrue(result.isWithinLimit)
    }

    @Test
    fun `the permitted fraction is applied to the tray's cross-section`() {
        val result = multi(
            input(
                cables = listOf(CableBundleEntry(11.9, 30)),
                arrangement = TrayArrangement.MULTI_LAYER,
                limit = 0.50,
            ),
        )

        assertEquals(0.50, result.permittedFraction, 1e-9)
        assertEquals(30_000.0 * 0.50 - 3336.607018, result.spareAreaMm2, 1e-6)
    }

    @Test
    fun `an over-filled tray reports how much has to come out`() {
        val result = multi(
            input(
                width = 100.0,
                depth = 50.0,
                cables = listOf(CableBundleEntry(20.0, 10)),
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertFalse(result.isWithinLimit)
        assertTrue(result.spareAreaMm2 < 0.0)
    }

    @Test
    fun `a tray close to its limit is flagged without being a failure`() {
        val result = multi(
            input(
                width = 100.0,
                depth = 100.0,
                cables = listOf(CableBundleEntry(20.0, 12)),
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertTrue(result.isWithinLimit)
        assertTrue(result.isNearLimit)
    }

    @Test
    fun `layers are counted with the largest cable as the layer height`() {
        // 300 mm across takes 25 cables of 11.9 mm per layer, so 30 needs two.
        val result = multi(
            input(
                cables = listOf(CableBundleEntry(11.9, 30)),
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertEquals(2, result.estimatedLayers)
    }

    @Test
    fun `a bundle that fits across the tray is a single layer`() {
        val result = multi(
            input(
                cables = listOf(CableBundleEntry(20.0, 4)),
                width = 100.0,
                depth = 60.0,
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertEquals(1, result.estimatedLayers)
    }

    // -- Why the arrangement is an input, not a display choice --------------------------------

    @Test
    fun `a tray can be nearly empty by area and full by width`() {
        // The case that makes a single area percentage misleading: four 20 mm
        // cables across a 100 mm tray take 80 % of its width while occupying
        // only 21 % of its cross-section.
        val cables = listOf(CableBundleEntry(20.0, 4))
        val byWidth = single(input(width = 100.0, depth = 60.0, cables = cables))
        val byArea = multi(
            input(
                width = 100.0,
                depth = 60.0,
                cables = cables,
                arrangement = TrayArrangement.MULTI_LAYER,
            ),
        )

        assertEquals(0.80, byWidth.widthUsedFraction, 1e-9)
        assertEquals(0.20944, byArea.fillFraction, 1e-6)
        assertTrue(byWidth.widthUsedFraction > byArea.fillFraction * 3)
    }

    @Test
    fun `the cable area is the same whichever question is asked`() {
        val cables = listOf(CableBundleEntry(20.0, 4))
        val byWidth = single(input(cables = cables))
        val byArea = multi(input(cables = cables, arrangement = TrayArrangement.MULTI_LAYER))

        assertEquals(byWidth.cableAreaMm2, byArea.cableAreaMm2, 1e-12)
        assertEquals(byWidth.cableCount, byArea.cableCount)
    }

    @Test
    fun `occupied depth is reported for a single layer too`() {
        // So a single-layer design can still be checked against a tray whose
        // side rail is shallower than the cables are thick.
        val result = single(input(width = 100.0, cables = listOf(CableBundleEntry(20.0, 4))))

        assertEquals(12.566371, result.occupiedDepthMm, 1e-6)
    }
}
