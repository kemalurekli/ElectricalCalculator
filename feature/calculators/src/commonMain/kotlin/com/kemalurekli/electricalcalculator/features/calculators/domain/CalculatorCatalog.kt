package com.kemalurekli.electricalcalculator.features.calculators.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorDescriptor
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_battery_runtime_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_battery_runtime_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_size_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_size_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_tray_fill_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_tray_fill_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_weight_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_weight_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_conduit_fill_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_conduit_fill_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_earth_fault_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_earth_fault_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_energy_cost_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_energy_cost_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_evse_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_evse_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_harmonics_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_harmonics_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_lighting_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_lighting_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_current_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_current_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_starting_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_starting_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_neutral_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_neutral_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_power_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_power_factor_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_power_factor_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_power_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_selectivity_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_selectivity_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_short_circuit_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_short_circuit_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_solar_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_solar_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_transformer_current_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_transformer_current_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_voltage_drop_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_voltage_drop_title

/**
 * The single source of truth for which calculators exist.
 *
 * The home dashboard, calculator list, search and favourites all read from here,
 * so shipping a new calculator means adding one [CalculatorDescriptor] plus its
 * screen — no other registration step, and no list that can drift out of sync.
 *
 * Injected rather than exposed as a top-level `val` so tests can substitute a
 * smaller catalog, and so a future build could assemble it from a remote
 * standards database without changing any consumer.
 */
class CalculatorCatalog() {

    /** Every calculator, in the order they should appear within their category. */
    val all: List<CalculatorDescriptor> = listOf(
        CalculatorDescriptor(
            id = CalculatorId.VOLTAGE_DROP,
            title = Res.string.calculator_voltage_drop_title,
            description = Res.string.calculator_voltage_drop_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.VOLTAGE_DROP,
            searchKeywords = listOf("volt drop", "vd", "cable loss", "ir drop", "percentage drop"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.CABLE_SIZE,
            title = Res.string.calculator_cable_size_title,
            description = Res.string.calculator_cable_size_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.CABLE,
            searchKeywords = listOf("conductor", "cross section", "mm2", "awg", "wire gauge", "sizing"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.SHORT_CIRCUIT,
            title = Res.string.calculator_short_circuit_title,
            description = Res.string.calculator_short_circuit_description,
            category = CalculatorCategory.PROTECTION,
            icon = CalculatorIcon.FAULT,
            searchKeywords = listOf(
                "fault current", "prospective", "Ik", "Isc", "breaking capacity",
                "kısa devre", "arıza akımı", "kesme kapasitesi",
            ),
        ),
        CalculatorDescriptor(
            id = CalculatorId.EARTH_FAULT_LOOP,
            title = Res.string.calculator_earth_fault_title,
            description = Res.string.calculator_earth_fault_description,
            category = CalculatorCategory.PROTECTION,
            icon = CalculatorIcon.EARTH,
            searchKeywords = listOf(
                "Zs", "loop impedance", "disconnection", "adiabatic", "earth", "RCD", "MCB",
                "hata döngüsü", "açma süresi", "topraklama", "koruma iletkeni",
            ),
        ),
        CalculatorDescriptor(
            id = CalculatorId.CABLE_WEIGHT,
            title = Res.string.calculator_cable_weight_title,
            description = Res.string.calculator_cable_weight_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.WEIGHT,
            searchKeywords = listOf("mass", "kg", "drum", "reel", "copper weight"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.CONDUIT_FILL,
            title = Res.string.calculator_conduit_fill_title,
            description = Res.string.calculator_conduit_fill_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.CONDUIT,
            searchKeywords = listOf("pipe fill", "40 percent", "raceway", "trunking"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.CABLE_TRAY_FILL,
            title = Res.string.calculator_cable_tray_fill_title,
            description = Res.string.calculator_cable_tray_fill_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.TRAY,
            searchKeywords = listOf("ladder", "basket", "occupancy", "tray width"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.POWER,
            title = Res.string.calculator_power_title,
            description = Res.string.calculator_power_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.POWER,
            searchKeywords = listOf("kw", "kva", "kvar", "active", "reactive", "apparent"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.POWER_FACTOR_CORRECTION,
            title = Res.string.calculator_power_factor_title,
            description = Res.string.calculator_power_factor_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.POWER_FACTOR,
            searchKeywords = listOf("cos phi", "capacitor", "kvar", "pf correction", "compensation"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.TRANSFORMER_CURRENT,
            title = Res.string.calculator_transformer_current_title,
            description = Res.string.calculator_transformer_current_description,
            category = CalculatorCategory.MACHINES,
            icon = CalculatorIcon.TRANSFORMER,
            searchKeywords = listOf("flc", "full load", "kva", "primary", "secondary", "trafo"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.MOTOR_CURRENT,
            title = Res.string.calculator_motor_current_title,
            description = Res.string.calculator_motor_current_description,
            category = CalculatorCategory.MACHINES,
            icon = CalculatorIcon.MOTOR,
            searchKeywords = listOf("flc", "full load amps", "hp", "kw", "induction motor"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.BATTERY_RUNTIME,
            title = Res.string.calculator_battery_runtime_title,
            description = Res.string.calculator_battery_runtime_description,
            category = CalculatorCategory.ENERGY_STORAGE,
            icon = CalculatorIcon.BATTERY,
            searchKeywords = listOf("ups", "autonomy", "ah", "backup time", "discharge"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.LIGHTING_LUMEN,
            title = Res.string.calculator_lighting_title,
            description = Res.string.calculator_lighting_description,
            category = CalculatorCategory.LIGHTING,
            icon = CalculatorIcon.LIGHTING,
            searchKeywords = listOf("lux", "lumen", "luminaire", "illuminance", "room index"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.SOLAR_STRING,
            title = Res.string.calculator_solar_title,
            description = Res.string.calculator_solar_description,
            category = CalculatorCategory.RENEWABLES,
            icon = CalculatorIcon.SOLAR,
            searchKeywords = listOf("pv", "solar", "string", "mppt", "voc", "inverter"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.NEUTRAL_CURRENT,
            title = Res.string.calculator_neutral_title,
            description = Res.string.calculator_neutral_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.NEUTRAL,
            searchKeywords = listOf("neutral", "unbalance", "harmonic", "triplen"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.ENERGY_COST,
            title = Res.string.calculator_energy_cost_title,
            description = Res.string.calculator_energy_cost_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.COST,
            searchKeywords = listOf("cost", "kwh", "tariff", "payback", "energy", "bill"),
        ),
        CalculatorDescriptor(
            id = CalculatorId.SELECTIVITY,
            title = Res.string.calculator_selectivity_title,
            description = Res.string.calculator_selectivity_description,
            category = CalculatorCategory.PROTECTION,
            icon = CalculatorIcon.SELECTIVITY,
            searchKeywords = listOf(
                "selectivity", "discrimination", "coordination", "cascade",
                "selektivite", "seçicilik", "koordinasyon",
            ),
        ),
        CalculatorDescriptor(
            id = CalculatorId.MOTOR_STARTING,
            title = Res.string.calculator_motor_starting_title,
            description = Res.string.calculator_motor_starting_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.MOTOR_STARTING,
            searchKeywords = listOf(
                "starting", "inrush", "dip", "sag", "flicker", "star delta", "soft start",
                "yol alma", "kalkis", "cokme", "yildiz ucgen", "yumusak yol verme",
            ),
        ),
        CalculatorDescriptor(
            id = CalculatorId.HARMONICS,
            title = Res.string.calculator_harmonics_title,
            description = Res.string.calculator_harmonics_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.HARMONICS,
            searchKeywords = listOf(
                "harmonic", "thd", "distortion", "k factor", "triplen", "rms", "spectrum",
                "harmonik", "bozulma", "k faktoru", "spektrum",
            ),
        ),
        CalculatorDescriptor(
            id = CalculatorId.EVSE,
            title = Res.string.calculator_evse_title,
            description = Res.string.calculator_evse_description,
            category = CalculatorCategory.POWER_AND_LOAD,
            icon = CalculatorIcon.EVSE,
            searchKeywords = listOf(
                "ev", "charger", "charging", "evse", "type b", "rdc-dd", "wallbox",
                "sarj", "elektrikli arac", "sarj istasyonu",
            ),
        ),
    )

    private val byId: Map<CalculatorId, CalculatorDescriptor> = all.associateBy { it.id }

    /** Calculators grouped by category, preserving [all]'s ordering within each group. */
    val byCategory: Map<CalculatorCategory, List<CalculatorDescriptor>> =
        all.groupBy { it.category }

    /** The descriptor for [id]. Total, because every id has an entry in [all]. */
    operator fun get(id: CalculatorId): CalculatorDescriptor =
        requireNotNull(byId[id]) { "No descriptor registered for $id" }

    /** The descriptor for [id], or `null` if it is not registered. */
    fun findById(id: CalculatorId): CalculatorDescriptor? = byId[id]
}
