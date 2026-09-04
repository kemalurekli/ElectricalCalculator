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
 * IEC 60364-5-54 Table A.54.4, reproduced as Table 54.3 in BS 7671: a
 * **protective conductor as a core in a cable, or bunched with other cables**.
 * That is the common case — the earth core inside a multicore cable.
 *
 * Its temperatures are what make it that case. The conductor is assumed to
 * start at its own operating temperature, because it is bundled with cores that
 * are already running warm: 70 °C for thermoplastic, rising to 160 °C, and
 * 90 °C for thermoset, rising to 250 °C. A conductor run **separately** starts
 * at 30 °C, has more temperature rise to spend and so earns a larger `k` —
 * 143 and 176 for copper, in Table A.54.2. Using the values here for that case
 * is conservative, but it is not the right table, and the calculator says so in
 * its notes rather than quietly applying the wrong one.
 *
 * ### Where this stops
 *
 * Above 300 mm² the standard drops `k` — copper on thermoplastic goes from 115
 * to 103 — because a conductor that large is not heated uniformly in the time
 * the equation assumes. This object does not model that step, and does not need
 * to for the sizes the app selects: the IEC 60228 ladder stops at 300 mm².
 * The protective-conductor field, though, is free text. A user who types 400
 * there gets an answer computed with the smaller-conductor `k`, which errs
 * toward too small a conductor — the direction that matters. Anyone sizing
 * above 300 mm² is past what this calculator was written for.
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

    /** Copper, thermoplastic insulation: 70 °C initial, 160 °C final. */
    const val COPPER_PVC = 115.0

    /** Copper, thermoset insulation: 90 °C initial, 250 °C final. */
    const val COPPER_XLPE = 143.0

    /** Aluminium, thermoplastic insulation: 70 °C initial, 160 °C final. */
    const val ALUMINIUM_PVC = 76.0

    /** Aluminium, thermoset insulation: 90 °C initial, 250 °C final. */
    const val ALUMINIUM_XLPE = 94.0

    private const val FIRST_STEP = 16.0
    private const val SECOND_STEP = 35.0
}
