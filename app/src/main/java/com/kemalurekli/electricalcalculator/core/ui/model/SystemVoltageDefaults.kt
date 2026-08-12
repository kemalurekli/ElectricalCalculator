package com.kemalurekli.electricalcalculator.core.ui.model

import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * Pre-filled system voltages, and the rule for when to move them.
 *
 * ### Why a calculator should arrive with a voltage already in it
 *
 * A form that opens empty makes the user type the same 400 every time. The
 * supply voltage is the most predictable quantity in the app — every calculator
 * opens on three phase, and three phase in an IEC installation means 400 V — so
 * leaving it blank is asking for work rather than for information.
 *
 * The same reasoning already governs efficiency, power factor, ambient
 * temperature, grouping, depth of discharge and the fill limits: an accepted
 * norm is offered, and the user overrides it when their job differs.
 *
 * ### Why the caller tracks whether the field was edited
 *
 * The obvious shortcut is to move the voltage whenever it still *looks* like a
 * default — but 230 and 400 are also perfectly ordinary things to type. A
 * 230 V three-phase delta system is real, and a user who entered it by hand
 * would have it silently replaced by 400 the moment they touched the supply
 * selector.
 *
 * So each form carries a flag saying whether the user has been in the field,
 * and only an untouched field ever moves. That makes "what the user typed is
 * never overwritten" true rather than nearly true.
 *
 * DC is deliberately absent. Direct-current systems run at 12, 24, 48, 110 and
 * 400 V depending on what they are, with no norm worth guessing at, so
 * switching to DC leaves whatever is in the field for the user to correct.
 */
object SystemVoltageDefaults {

    /** Line-to-line on an IEC 60038 low-voltage system. */
    const val THREE_PHASE = "400"

    /** Line-to-neutral on the same system. */
    const val SINGLE_PHASE = "230"

    /** The voltage a form should open with for [system], or null for DC. */
    fun forSystem(system: SupplySystem): String? = when (system) {
        SupplySystem.THREE_PHASE_AC -> THREE_PHASE
        SupplySystem.SINGLE_PHASE_AC -> SINGLE_PHASE
        SupplySystem.DC -> null
    }

    /**
     * The voltage to show after a switch to [system].
     *
     * @param userEdited true once the user has typed in the field, after which
     *   the value is theirs and is returned unchanged.
     */
    fun follow(current: String, system: SupplySystem, userEdited: Boolean): String {
        if (userEdited) return current
        return forSystem(system) ?: current
    }
}
