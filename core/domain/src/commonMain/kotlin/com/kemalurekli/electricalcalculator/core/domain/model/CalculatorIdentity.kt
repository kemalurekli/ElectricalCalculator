package com.kemalurekli.electricalcalculator.core.domain.model

/*
 * Split out of CalculatorDescriptor.kt when the domain became a multiplatform
 * module. These two enums are pure vocabulary and travel; the descriptor they
 * sat beside carries Android string ids for every calculator's title and stays
 * in `:app` until the calculator screens move and take their strings with them.
 *
 * The package is unchanged, so nothing that referenced them needed editing.
 */

/**
 * Stable identifier for a calculator.
 *
 * The [key] is what gets written to the database for history and favourites, so
 * these strings are part of the app's persisted contract: rename an enum
 * constant freely, but never change a key without a migration.
 */
enum class CalculatorId(val key: String) {
    VOLTAGE_DROP("voltage_drop"),
    CABLE_SIZE("cable_size"),
    TRANSFORMER_CURRENT("transformer_current"),
    MOTOR_CURRENT("motor_current"),
    POWER("power"),
    POWER_FACTOR_CORRECTION("power_factor_correction"),
    BATTERY_RUNTIME("battery_runtime"),
    SHORT_CIRCUIT("short_circuit"),
    EARTH_FAULT_LOOP("earth_fault_loop"),
    CABLE_WEIGHT("cable_weight"),
    CONDUIT_FILL("conduit_fill"),
    CABLE_TRAY_FILL("cable_tray_fill"),
    LIGHTING_LUMEN("lighting_lumen"),
    SOLAR_STRING("solar_string"),
    NEUTRAL_CURRENT("neutral_current"),
    ENERGY_COST("energy_cost"),
    SELECTIVITY("selectivity"),
    MOTOR_STARTING("motor_starting"),
    HARMONICS("harmonics"),
    EVSE("evse"),
    ;

    companion object {
        private val byKey = entries.associateBy(CalculatorId::key)

        /** Resolves a persisted key, returning `null` for keys this build does not know. */
        fun fromKeyOrNull(key: String): CalculatorId? = byKey[key]
    }
}

/** Grouping used to section the calculator list. */
enum class CalculatorCategory {
    POWER_AND_LOAD,
    PROTECTION,
    CABLE_AND_CONDUIT,
    MACHINES,
    ENERGY_STORAGE,
    LIGHTING,
    RENEWABLES,
}
