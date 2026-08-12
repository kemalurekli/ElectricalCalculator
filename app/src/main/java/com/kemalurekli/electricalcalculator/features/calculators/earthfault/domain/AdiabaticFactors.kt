package com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial

/**
 * The `k` factor of the adiabatic equation, from IEC 60364-5-54.
 *
 * ```
 * S = √(I² · t) / k
 * ```
 *
 * `k` folds together the conductor's specific heat, resistivity and the
 * temperature rise it may take during a fault. It is **transcribed data**: it
 * cannot be derived from anything else the app knows, and no test can prove a
 * value right. What the tests here do guarantee is the shape — that copper
 * beats aluminium and that a thermoset insulation beats a thermoplastic one,
 * because it tolerates a higher final temperature.
 *
 * ### Which table these come from
 *
 * IEC 60364-5-54 Table 54.3: a **protective conductor incorporated in a cable
 * or bunched with other cables**, initial temperature 30 °C. That is the common
 * case — the earth core inside a multicore cable.
 *
 * A protective conductor run **separately** and not bunched is allowed a higher
 * final temperature and therefore a larger `k` (Table 54.2). Using these values
 * for that case is conservative, but it is not the right table, and the
 * calculator says so in its notes rather than quietly applying the wrong one.
 */
object AdiabaticFactors {

    /**
     * `k` for a protective conductor of [material] inside a cable insulated
     * with [insulation].
     */
    fun forProtectiveConductor(
        material: ConductorMaterial,
        insulation: CableInsulation,
    ): Double = when (material) {
        ConductorMaterial.COPPER -> when (insulation) {
            CableInsulation.PVC -> COPPER_PVC
            CableInsulation.XLPE -> COPPER_XLPE
        }

        ConductorMaterial.ALUMINIUM -> when (insulation) {
            CableInsulation.PVC -> ALUMINIUM_PVC
            CableInsulation.XLPE -> ALUMINIUM_XLPE
        }
    }

    /**
     * The protective conductor size the simple table rule of IEC 60364-5-54
     * gives for a line conductor of [lineCrossSectionMm2].
     *
     * ```
     * S ≤ 16          → S
     * 16 < S ≤ 35     → 16
     * S > 35          → S / 2
     * ```
     *
     * The standard permits either this route or the adiabatic calculation, so
     * both are reported and the designer chooses. The table is the quicker
     * answer and usually the larger conductor.
     */
    fun tabulatedProtectiveSection(lineCrossSectionMm2: Double): Double = when {
        lineCrossSectionMm2 <= FIRST_STEP -> lineCrossSectionMm2
        lineCrossSectionMm2 <= SECOND_STEP -> FIRST_STEP
        else -> lineCrossSectionMm2 / 2.0
    }

    /** Copper conductor, thermoplastic (PVC) insulation, 70 °C rated. */
    const val COPPER_PVC = 115.0

    /** Copper conductor, thermoset (XLPE/EPR) insulation, 90 °C rated. */
    const val COPPER_XLPE = 143.0

    /** Aluminium conductor, thermoplastic insulation. */
    const val ALUMINIUM_PVC = 76.0

    /** Aluminium conductor, thermoset insulation. */
    const val ALUMINIUM_XLPE = 94.0

    private const val FIRST_STEP = 16.0
    private const val SECOND_STEP = 35.0
}
