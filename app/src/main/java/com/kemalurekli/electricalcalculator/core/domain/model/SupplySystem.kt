package com.kemalurekli.electricalcalculator.core.domain.model

import kotlin.math.sqrt

/**
 * The supply arrangement.
 *
 * Two different multipliers hang off this enum, and they are **not** the same
 * number for single phase. Mixing them up is an easy and expensive mistake, so
 * each is named for the physics it encodes:
 *
 * - [lengthMultiplier] is a *cable* quantity — how far the current travels and
 *   which voltage a drop is referenced against.
 * - [powerPhaseFactor] is a *power* quantity — the `k` in `S = k · U · I`.
 *
 * A single-phase circuit has `lengthMultiplier = 2` (out along the line, back
 * along the neutral) but `powerPhaseFactor = 1` (`S = U · I`).
 *
 * @param lengthMultiplier factor applied to route length in voltage-drop
 *   calculations. Two for DC and single phase, because the drop accumulates
 *   over both the outgoing and returning conductor; √3 for three phase, where a
 *   balanced load carries no neutral current and the result is referenced to
 *   the line-to-line voltage.
 * @param powerPhaseFactor the `k` relating apparent power to line current:
 *   `S = k · U · I`. One for DC and single phase; √3 for three phase against
 *   the line-to-line voltage.
 */
enum class SupplySystem(
    val lengthMultiplier: Double,
    val powerPhaseFactor: Double,
) {
    DC(lengthMultiplier = 2.0, powerPhaseFactor = 1.0),
    SINGLE_PHASE_AC(lengthMultiplier = 2.0, powerPhaseFactor = 1.0),
    THREE_PHASE_AC(lengthMultiplier = sqrt(3.0), powerPhaseFactor = sqrt(3.0)),
    ;

    /** Power factor only has meaning on an AC supply. */
    val isAc: Boolean get() = this != DC

    /**
     * Number of current-carrying conductors that dissipate loss.
     *
     * Two for a DC or single-phase circuit (line and return); three for a
     * balanced three-phase circuit, whose neutral carries no current.
     */
    val lossConductorCount: Int get() = if (this == THREE_PHASE_AC) 3 else 2

    companion object {
        /** The arrangements that exist on an AC machine such as a transformer. */
        val acEntries: List<SupplySystem> get() = entries.filter { it.isAc }
    }
}
