package com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.LoadedConductors
import com.kemalurekli.electricalcalculator.core.domain.model.StandardCrossSection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Current-carrying capacity of cables, after IEC 60364-5-52 Annex B.
 *
 * ## Source
 *
 * Values are the tabulated capacities for reference installation methods at a
 * 30 °C ambient, single circuit:
 *
 * - Copper, PVC 70 °C — Table B.52.2
 * - Copper, XLPE/EPR 90 °C — Table B.52.4
 * - Aluminium, PVC 70 °C — Table B.52.3
 * - Aluminium, XLPE/EPR 90 °C — Table B.52.5
 *
 * ## ⚠ Verification required before release
 *
 * These figures are transcribed data, not derived results: unlike the voltage
 * drop formula, nothing in the code can prove them right. An undersized
 * conductor is a fire risk, so the table must be checked against the edition of
 * IEC 60364-5-52 (or the national adoption, e.g. TS HD 60364-5-52) that the app
 * claims to follow, and re-checked whenever that edition changes.
 *
 * The structure below is deliberately one row per cross-section so a reviewer
 * can read it side by side with the printed table. `AmpacityTableTest` locks
 * every value and additionally asserts the physical relationships the table must
 * satisfy — capacity rising with area, XLPE above PVC, copper above aluminium,
 * two loaded conductors above three — which catches a mistyped digit even
 * without the standard to hand.
 */
@Singleton
class AmpacityTable @Inject constructor() {

    /**
     * Tabulated capacity in amperes for [areaMm2] at 30 °C ambient, one circuit.
     *
     * Returns null when the combination is not tabulated — aluminium is not
     * manufactured below 2.5 mm², and callers must skip such sizes rather than
     * assume a value.
     */
    fun capacityAmps(
        areaMm2: Double,
        material: ConductorMaterial,
        insulation: CableInsulation,
        method: InstallationMethod,
        conductors: LoadedConductors,
    ): Double? {
        val column = columnIndex(method)
        val row = table(material, insulation, conductors)[areaMm2] ?: return null
        return row[column].takeIf { it > 0.0 }
    }

    /** Column order used by every row below: B1, B2, C, E. */
    private fun columnIndex(method: InstallationMethod): Int = when (method) {
        InstallationMethod.B1_CONDUIT_ON_WALL -> 0
        InstallationMethod.B2_MULTICORE_IN_CONDUIT -> 1
        InstallationMethod.C_CLIPPED_DIRECT -> 2
        InstallationMethod.E_FREE_AIR -> 3
    }

    private fun table(
        material: ConductorMaterial,
        insulation: CableInsulation,
        conductors: LoadedConductors,
    ): Map<Double, DoubleArray> = when {
        material == ConductorMaterial.COPPER && insulation == CableInsulation.PVC &&
            conductors == LoadedConductors.TWO -> COPPER_PVC_TWO

        material == ConductorMaterial.COPPER && insulation == CableInsulation.PVC -> COPPER_PVC_THREE

        material == ConductorMaterial.COPPER && conductors == LoadedConductors.TWO ->
            COPPER_XLPE_TWO

        material == ConductorMaterial.COPPER -> COPPER_XLPE_THREE

        insulation == CableInsulation.PVC && conductors == LoadedConductors.TWO ->
            ALUMINIUM_PVC_TWO

        insulation == CableInsulation.PVC -> ALUMINIUM_PVC_THREE

        conductors == LoadedConductors.TWO -> ALUMINIUM_XLPE_TWO

        else -> ALUMINIUM_XLPE_THREE
    }

    /** Sizes this table covers for a material, ascending. */
    fun tabulatedSizes(material: ConductorMaterial): List<Double> =
        StandardCrossSection.allMm2.filter { area ->
            table(material, CableInsulation.PVC, LoadedConductors.TWO).containsKey(area)
        }

    private companion object {

        /** Columns: B1, B2, C, E. A zero marks a combination the table omits. */
        private fun rows(vararg entries: Pair<Double, DoubleArray>) = linkedMapOf(*entries)

        // --- Copper, PVC 70 °C — IEC 60364-5-52 Table B.52.2 -----------------
        val COPPER_PVC_TWO = rows(
            //                  B1      B2      C       E
            1.5 to doubleArrayOf(17.5, 16.5, 19.5, 22.0),
            2.5 to doubleArrayOf(24.0, 23.0, 27.0, 30.0),
            4.0 to doubleArrayOf(32.0, 30.0, 36.0, 40.0),
            6.0 to doubleArrayOf(41.0, 38.0, 46.0, 51.0),
            10.0 to doubleArrayOf(57.0, 52.0, 63.0, 70.0),
            16.0 to doubleArrayOf(76.0, 69.0, 85.0, 94.0),
            25.0 to doubleArrayOf(101.0, 90.0, 112.0, 119.0),
            35.0 to doubleArrayOf(125.0, 111.0, 138.0, 148.0),
            50.0 to doubleArrayOf(151.0, 133.0, 168.0, 180.0),
            70.0 to doubleArrayOf(192.0, 168.0, 213.0, 232.0),
            95.0 to doubleArrayOf(232.0, 201.0, 258.0, 282.0),
            120.0 to doubleArrayOf(269.0, 232.0, 299.0, 328.0),
            150.0 to doubleArrayOf(300.0, 258.0, 344.0, 379.0),
            185.0 to doubleArrayOf(341.0, 294.0, 392.0, 434.0),
            240.0 to doubleArrayOf(400.0, 344.0, 461.0, 514.0),
            300.0 to doubleArrayOf(458.0, 394.0, 530.0, 593.0),
        )

        val COPPER_PVC_THREE = rows(
            1.5 to doubleArrayOf(15.5, 15.0, 17.5, 18.5),
            2.5 to doubleArrayOf(21.0, 20.0, 24.0, 25.0),
            4.0 to doubleArrayOf(28.0, 27.0, 32.0, 34.0),
            6.0 to doubleArrayOf(36.0, 34.0, 41.0, 43.0),
            10.0 to doubleArrayOf(50.0, 46.0, 57.0, 60.0),
            16.0 to doubleArrayOf(68.0, 62.0, 76.0, 80.0),
            25.0 to doubleArrayOf(89.0, 80.0, 96.0, 101.0),
            35.0 to doubleArrayOf(110.0, 99.0, 119.0, 126.0),
            50.0 to doubleArrayOf(134.0, 118.0, 144.0, 153.0),
            70.0 to doubleArrayOf(171.0, 149.0, 184.0, 196.0),
            95.0 to doubleArrayOf(207.0, 179.0, 223.0, 238.0),
            120.0 to doubleArrayOf(239.0, 206.0, 259.0, 276.0),
            150.0 to doubleArrayOf(275.0, 236.0, 299.0, 319.0),
            185.0 to doubleArrayOf(314.0, 268.0, 341.0, 364.0),
            240.0 to doubleArrayOf(370.0, 315.0, 403.0, 430.0),
            300.0 to doubleArrayOf(426.0, 360.0, 464.0, 497.0),
        )

        // --- Copper, XLPE 90 °C — IEC 60364-5-52 Table B.52.4 ----------------
        val COPPER_XLPE_TWO = rows(
            1.5 to doubleArrayOf(23.0, 22.0, 24.0, 26.0),
            2.5 to doubleArrayOf(31.0, 30.0, 33.0, 36.0),
            4.0 to doubleArrayOf(42.0, 40.0, 45.0, 49.0),
            6.0 to doubleArrayOf(54.0, 51.0, 58.0, 63.0),
            10.0 to doubleArrayOf(75.0, 69.0, 80.0, 86.0),
            16.0 to doubleArrayOf(100.0, 91.0, 107.0, 115.0),
            25.0 to doubleArrayOf(133.0, 119.0, 138.0, 149.0),
            35.0 to doubleArrayOf(164.0, 146.0, 171.0, 185.0),
            50.0 to doubleArrayOf(198.0, 175.0, 209.0, 225.0),
            70.0 to doubleArrayOf(253.0, 221.0, 269.0, 289.0),
            95.0 to doubleArrayOf(306.0, 265.0, 328.0, 352.0),
            120.0 to doubleArrayOf(354.0, 305.0, 382.0, 410.0),
            150.0 to doubleArrayOf(393.0, 334.0, 441.0, 473.0),
            185.0 to doubleArrayOf(449.0, 384.0, 506.0, 542.0),
            240.0 to doubleArrayOf(528.0, 459.0, 599.0, 641.0),
            300.0 to doubleArrayOf(603.0, 532.0, 693.0, 741.0),
        )

        val COPPER_XLPE_THREE = rows(
            1.5 to doubleArrayOf(20.0, 19.5, 22.0, 23.0),
            2.5 to doubleArrayOf(28.0, 26.0, 30.0, 32.0),
            4.0 to doubleArrayOf(37.0, 35.0, 40.0, 42.0),
            6.0 to doubleArrayOf(48.0, 44.0, 52.0, 54.0),
            10.0 to doubleArrayOf(66.0, 60.0, 71.0, 75.0),
            16.0 to doubleArrayOf(88.0, 80.0, 96.0, 100.0),
            25.0 to doubleArrayOf(117.0, 105.0, 119.0, 127.0),
            35.0 to doubleArrayOf(144.0, 128.0, 147.0, 158.0),
            50.0 to doubleArrayOf(175.0, 154.0, 179.0, 192.0),
            70.0 to doubleArrayOf(222.0, 194.0, 229.0, 246.0),
            95.0 to doubleArrayOf(269.0, 233.0, 278.0, 298.0),
            120.0 to doubleArrayOf(312.0, 268.0, 322.0, 346.0),
            150.0 to doubleArrayOf(342.0, 300.0, 371.0, 395.0),
            185.0 to doubleArrayOf(384.0, 340.0, 424.0, 450.0),
            240.0 to doubleArrayOf(450.0, 398.0, 500.0, 538.0),
            300.0 to doubleArrayOf(514.0, 455.0, 576.0, 621.0),
        )

        // --- Aluminium, PVC 70 °C — IEC 60364-5-52 Table B.52.3 --------------
        // Aluminium conductors are not made below 2.5 mm², so 1.5 is absent.
        val ALUMINIUM_PVC_TWO = rows(
            2.5 to doubleArrayOf(18.5, 17.5, 21.0, 23.0),
            4.0 to doubleArrayOf(25.0, 24.0, 28.0, 31.0),
            6.0 to doubleArrayOf(32.0, 30.0, 36.0, 39.0),
            10.0 to doubleArrayOf(44.0, 41.0, 49.0, 54.0),
            16.0 to doubleArrayOf(60.0, 55.0, 66.0, 73.0),
            25.0 to doubleArrayOf(79.0, 71.0, 83.0, 90.0),
            35.0 to doubleArrayOf(97.0, 87.0, 103.0, 112.0),
            50.0 to doubleArrayOf(118.0, 104.0, 125.0, 136.0),
            70.0 to doubleArrayOf(150.0, 131.0, 160.0, 174.0),
            95.0 to doubleArrayOf(181.0, 157.0, 195.0, 211.0),
            120.0 to doubleArrayOf(210.0, 180.0, 226.0, 245.0),
            150.0 to doubleArrayOf(234.0, 206.0, 261.0, 283.0),
            185.0 to doubleArrayOf(266.0, 233.0, 298.0, 323.0),
            240.0 to doubleArrayOf(312.0, 273.0, 352.0, 382.0),
            300.0 to doubleArrayOf(358.0, 313.0, 406.0, 440.0),
        )

        val ALUMINIUM_PVC_THREE = rows(
            2.5 to doubleArrayOf(16.5, 15.5, 18.5, 19.5),
            4.0 to doubleArrayOf(22.0, 21.0, 25.0, 26.0),
            6.0 to doubleArrayOf(28.0, 27.0, 32.0, 33.0),
            10.0 to doubleArrayOf(39.0, 36.0, 44.0, 46.0),
            16.0 to doubleArrayOf(53.0, 48.0, 59.0, 61.0),
            25.0 to doubleArrayOf(70.0, 62.0, 73.0, 78.0),
            35.0 to doubleArrayOf(86.0, 77.0, 90.0, 96.0),
            50.0 to doubleArrayOf(104.0, 92.0, 110.0, 117.0),
            70.0 to doubleArrayOf(133.0, 116.0, 140.0, 150.0),
            95.0 to doubleArrayOf(161.0, 139.0, 170.0, 183.0),
            120.0 to doubleArrayOf(186.0, 160.0, 197.0, 212.0),
            150.0 to doubleArrayOf(204.0, 176.0, 227.0, 245.0),
            185.0 to doubleArrayOf(230.0, 199.0, 259.0, 280.0),
            240.0 to doubleArrayOf(269.0, 232.0, 305.0, 330.0),
            300.0 to doubleArrayOf(306.0, 265.0, 351.0, 381.0),
        )

        // --- Aluminium, XLPE 90 °C — IEC 60364-5-52 Table B.52.5 -------------
        val ALUMINIUM_XLPE_TWO = rows(
            2.5 to doubleArrayOf(24.0, 22.0, 26.0, 28.0),
            4.0 to doubleArrayOf(32.0, 30.0, 35.0, 38.0),
            6.0 to doubleArrayOf(41.0, 38.0, 45.0, 49.0),
            10.0 to doubleArrayOf(57.0, 52.0, 62.0, 67.0),
            16.0 to doubleArrayOf(76.0, 69.0, 84.0, 91.0),
            25.0 to doubleArrayOf(101.0, 90.0, 101.0, 108.0),
            35.0 to doubleArrayOf(125.0, 111.0, 126.0, 135.0),
            50.0 to doubleArrayOf(151.0, 133.0, 154.0, 164.0),
            70.0 to doubleArrayOf(192.0, 168.0, 198.0, 211.0),
            95.0 to doubleArrayOf(232.0, 201.0, 241.0, 257.0),
            120.0 to doubleArrayOf(269.0, 232.0, 280.0, 300.0),
            150.0 to doubleArrayOf(300.0, 258.0, 324.0, 346.0),
            185.0 to doubleArrayOf(341.0, 294.0, 371.0, 397.0),
            240.0 to doubleArrayOf(400.0, 344.0, 439.0, 470.0),
            300.0 to doubleArrayOf(458.0, 394.0, 508.0, 543.0),
        )

        val ALUMINIUM_XLPE_THREE = rows(
            2.5 to doubleArrayOf(21.0, 20.0, 23.0, 24.0),
            4.0 to doubleArrayOf(28.0, 27.0, 31.0, 32.0),
            6.0 to doubleArrayOf(36.0, 34.0, 39.0, 41.0),
            10.0 to doubleArrayOf(49.0, 46.0, 54.0, 57.0),
            16.0 to doubleArrayOf(66.0, 62.0, 73.0, 76.0),
            25.0 to doubleArrayOf(83.0, 80.0, 90.0, 96.0),
            35.0 to doubleArrayOf(103.0, 99.0, 112.0, 119.0),
            50.0 to doubleArrayOf(125.0, 118.0, 136.0, 145.0),
            70.0 to doubleArrayOf(160.0, 149.0, 174.0, 186.0),
            95.0 to doubleArrayOf(195.0, 179.0, 211.0, 227.0),
            120.0 to doubleArrayOf(226.0, 206.0, 245.0, 263.0),
            150.0 to doubleArrayOf(261.0, 236.0, 283.0, 304.0),
            185.0 to doubleArrayOf(298.0, 268.0, 323.0, 347.0),
            240.0 to doubleArrayOf(352.0, 315.0, 382.0, 409.0),
            300.0 to doubleArrayOf(406.0, 360.0, 440.0, 471.0),
        )
    }
}
