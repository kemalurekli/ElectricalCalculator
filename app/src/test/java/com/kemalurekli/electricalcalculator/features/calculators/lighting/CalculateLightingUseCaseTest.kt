package com.kemalurekli.electricalcalculator.features.calculators.lighting

import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.CalculateLightingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateLightingUseCaseTest {

    private val calculate = CalculateLightingUseCase()

    private fun input(
        lux: Double = 500.0,
        length: Double = 12.0,
        width: Double = 8.0,
        mountingHeight: Double = 2.2,
        flux: Double = 4_000.0,
        uf: Double = 0.6,
        mf: Double = 0.8,
    ) = LightingInput(
        targetIlluminanceLux = lux,
        roomLengthMetres = length,
        roomWidthMetres = width,
        mountingHeightMetres = mountingHeight,
        luminousFluxPerLuminaireLumens = flux,
        utilisationFactor = uf,
        maintenanceFactor = mf,
    )

    // -- Worked reference case --------------------------------------------------

    @Test
    fun `worked example - a 12 by 8 office at 500 lux`() {
        // A = 96 m², N = 500 × 96 / (4000 × 0,6 × 0,8) = 25
        val result = calculate(input())

        assertEquals(96.0, result.areaSquareMetres, 1e-9)
        assertEquals(25.0, result.exactLuminaireCount, 1e-9)
        assertEquals(25, result.luminaireCount)
        assertEquals(500.0, result.achievedIlluminanceLux, 1e-9)
    }

    @Test
    fun `the room index follows its own definition`() {
        // K = (L·W) / (Hm·(L+W)) = 96 / (2,2 × 20)
        val result = calculate(input())

        assertEquals(96.0 / (2.2 * 20.0), result.roomIndex, 1e-9)
        assertEquals(2.1818, result.roomIndex, 1e-4)
    }

    // -- Rounding ----------------------------------------------------------------

    @Test
    fun `the count is rounded up, never to nearest`() {
        // 4,2 fittings rounded down is a design that misses its own target by
        // 5 %, which is not a decision the calculator gets to make.
        val result = calculate(input(lux = 505.0))

        assertTrue(result.exactLuminaireCount > 25.0)
        assertEquals(26, result.luminaireCount)
    }

    @Test
    fun `rounding up raises the achieved illuminance above the target`() {
        val result = calculate(input(lux = 505.0))

        assertTrue(result.achievedIlluminanceLux > 505.0)
    }

    @Test
    fun `a room needing a fraction of a fitting still gets one`() {
        val result = calculate(input(lux = 5.0, length = 2.0, width = 2.0))

        assertEquals(1, result.luminaireCount)
    }

    // -- Physical behaviour --------------------------------------------------------

    @Test
    fun `doubling the target doubles the fittings`() {
        val base = calculate(input(lux = 300.0)).exactLuminaireCount
        val doubled = calculate(input(lux = 600.0)).exactLuminaireCount

        assertEquals(2.0, doubled / base, 1e-9)
    }

    @Test
    fun `a brighter luminaire needs proportionally fewer of them`() {
        val small = calculate(input(flux = 2_000.0)).exactLuminaireCount
        val large = calculate(input(flux = 4_000.0)).exactLuminaireCount

        assertEquals(2.0, small / large, 1e-9)
    }

    @Test
    fun `a worse maintenance factor needs more fittings`() {
        val clean = calculate(input(mf = 0.9)).exactLuminaireCount
        val dirty = calculate(input(mf = 0.6)).exactLuminaireCount

        assertTrue(dirty > clean)
    }

    @Test
    fun `mounting higher above the working plane lowers the room index`() {
        // K falls as the room gets proportionally taller, which is what sends
        // the reader to a different column of the utilisation table.
        val low = calculate(input(mountingHeight = 2.0)).roomIndex
        val high = calculate(input(mountingHeight = 4.0)).roomIndex

        assertTrue(high < low)
        assertEquals(2.0, low / high, 1e-9)
    }

    // -- The layout suggestion --------------------------------------------------------

    @Test
    fun `a composite count suggests the grid closest to square`() {
        // 24 fittings: 4 × 6 rather than 2 × 12 or 1 × 24.
        val result = calculate(input(lux = 480.0))

        assertEquals(24, result.luminaireCount)
        val (along, across) = requireNotNull(result.luminairesPerRowSuggestion)
        assertEquals(24, along * across)
        assertEquals(6, along)
        assertEquals(4, across)
    }

    @Test
    fun `the longer run of fittings goes along the longer wall`() {
        val wide = calculate(input(lux = 480.0, length = 12.0, width = 8.0))
        val tall = calculate(input(lux = 480.0, length = 8.0, width = 12.0))

        assertEquals(6 to 4, wide.luminairesPerRowSuggestion)
        assertEquals(4 to 6, tall.luminairesPerRowSuggestion)
    }

    @Test
    fun `a prime count suggests nothing rather than a single row`() {
        // 7 fittings in a 1 × 7 line is not a layout, it is arithmetic.
        val result = calculate(input(lux = 140.0))

        assertEquals(7, result.luminaireCount)
        assertNull(result.luminairesPerRowSuggestion)
    }
}
