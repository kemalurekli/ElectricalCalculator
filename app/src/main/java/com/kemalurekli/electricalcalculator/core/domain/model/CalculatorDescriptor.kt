package com.kemalurekli.electricalcalculator.core.domain.model

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

/**
 * Icon choice for a calculator, expressed as a domain-level enum.
 *
 * The domain layer names the *concept*; the design system maps each concept to
 * an `ImageVector`. That indirection is what keeps `androidx.compose` off the
 * domain classpath and lets the icon set be restyled without touching domain code.
 */
enum class CalculatorIcon {
    VOLTAGE_DROP,
    CABLE,
    TRANSFORMER,
    MOTOR,
    POWER,
    POWER_FACTOR,
    BATTERY,
    WEIGHT,
    CONDUIT,
    TRAY,
    FAULT,
    EARTH,
    LIGHTING,
    SOLAR,
    NEUTRAL,
    COST,
}

/**
 * Everything the app knows about a calculator without running it.
 *
 * Drives the calculator list, home dashboard, search index and favourites, so
 * that adding a calculator is a single entry in [
 *   com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
 * ] rather than an edit across several screens.
 *
 * @param titleRes user-visible name, as a string resource for translation.
 * @param descriptionRes one-line summary shown beneath the title.
 * @param searchKeywords extra terms matched during search, letting a user find
 *   "Voltage Drop" by typing "cable loss" or "IR".
 */
data class CalculatorDescriptor(
    val id: CalculatorId,
    val titleRes: Int,
    val descriptionRes: Int,
    val category: CalculatorCategory,
    val icon: CalculatorIcon,
    val searchKeywords: List<String> = emptyList(),
)
