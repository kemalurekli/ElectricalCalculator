package com.kemalurekli.electricalcalculator.core.domain.model

import java.util.Locale

/**
 * The values every calculator opens with.
 *
 * ### Why these are a preference and not constants
 *
 * An installation has one supply, one frequency and — for most of a job — one
 * cable type. Typing 400, 50 and "copper" into every form is the single thing
 * that makes a calculator feel like a toy: the app is asking for information it
 * was told an hour ago. Held here, the answer is given once and every form
 * inherits it.
 *
 * ### Why they are seeded from the locale and then left alone
 *
 * A first-run guess is worth making — 230/400 V at 50 Hz is right for most of
 * the world and 120/208 V at 60 Hz for North America — but it stays a guess.
 * The moment the user edits one of these, or even accepts it, the value is
 * theirs; nothing in the app may derive it from the environment again.
 *
 * That matters most for language. Someone reading the app in English may well
 * be commissioning a 400 V board, and someone who switches to Turkish for
 * readability has not moved country. So [seedFor] runs exactly once in the
 * lifetime of an install, and a later language change does not call it.
 * Re-seeding is a deliberate action the user takes in Settings.
 *
 * ### Why voltages are strings
 *
 * They go straight into text fields and come straight back out. Parsing on the
 * way in and formatting on the way out would round-trip through the locale's
 * decimal separator and turn "400" into "400,0" for a Turkish reader.
 */
data class EngineeringDefaults(
    /** Line-to-neutral, in volts. */
    val singlePhaseVoltage: String = IEC_SINGLE_PHASE,

    /** Line-to-line, in volts. */
    val threePhaseVoltage: String = IEC_THREE_PHASE,

    /** Supply frequency, in hertz. */
    val frequency: String = IEC_FREQUENCY,

    /** Ambient air temperature used for derating, in °C. */
    val ambientTemperature: String = IEC_AMBIENT,

    val material: ConductorMaterial = ConductorMaterial.COPPER,

    val insulation: CableInsulation = CableInsulation.PVC,

    val installationMethod: InstallationMethod = InstallationMethod.C_CLIPPED_DIRECT,
) {

    /**
     * The voltage a form should open with for [system], or null for DC.
     *
     * Direct-current systems run at 12, 24, 48, 110 and 400 V depending on what
     * they are, with no norm worth guessing at, so DC is deliberately absent.
     */
    fun voltageFor(system: SupplySystem): String? = when (system) {
        SupplySystem.THREE_PHASE_AC -> threePhaseVoltage
        SupplySystem.SINGLE_PHASE_AC -> singlePhaseVoltage
        SupplySystem.DC -> null
    }

    companion object {
        /** IEC 60038 low voltage: 230 V line-to-neutral, 400 V line-to-line. */
        const val IEC_SINGLE_PHASE = "230"
        const val IEC_THREE_PHASE = "400"
        const val IEC_FREQUENCY = "50"

        /** The reference ambient the ampacity tables are built on. */
        const val IEC_AMBIENT = "30"

        /** North American low voltage: 120 V line-to-neutral, 208 V wye. */
        const val NA_SINGLE_PHASE = "120"
        const val NA_THREE_PHASE = "208"
        const val NA_FREQUENCY = "60"

        val Default = EngineeringDefaults()

        /**
         * Countries whose low-voltage supply differs enough to be worth guessing at.
         *
         * Deliberately short. A full world table of supply voltages is exactly
         * the kind of transcribed data this app has no way to verify, and being
         * wrong about Japan or Brazil is worse than offering the majority value
         * and letting the user correct it in one tap. These two are the large
         * markets where 230 V would be wrong for every reader rather than some.
         */
        private val NORTH_AMERICAN = setOf("US", "CA")

        /**
         * The values a fresh install starts from.
         *
         * Reads the *region*, not the language: English is spoken at 230 V in
         * Britain and at 120 V in the United States, so the language subtag
         * carries no information here. On Android [Locale.getDefault] already
         * reflects the per-app locale when one is set and the device locale
         * otherwise, which is what makes "the language the user chose" the
         * thing being read.
         *
         * A locale with no region — plain `en`, or `tr` — falls to the IEC
         * values, which is the correct answer for the majority of the world.
         */
        fun seedFor(locale: Locale): EngineeringDefaults =
            if (locale.country.uppercase(Locale.ROOT) in NORTH_AMERICAN) {
                EngineeringDefaults(
                    singlePhaseVoltage = NA_SINGLE_PHASE,
                    threePhaseVoltage = NA_THREE_PHASE,
                    frequency = NA_FREQUENCY,
                )
            } else {
                Default
            }
    }
}
