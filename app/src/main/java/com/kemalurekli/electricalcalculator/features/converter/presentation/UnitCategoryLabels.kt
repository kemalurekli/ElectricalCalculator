package com.kemalurekli.electricalcalculator.features.converter.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCategory

/**
 * Category labels are the one translated part of the converter — the unit
 * symbols themselves are language-neutral by design.
 */
fun UnitCategory.categoryLabelRes(): Int = when (key) {
    "voltage" -> R.string.unit_category_voltage
    "current" -> R.string.unit_category_current
    "resistance" -> R.string.unit_category_resistance
    "power" -> R.string.unit_category_power
    "energy" -> R.string.unit_category_energy
    "frequency" -> R.string.unit_category_frequency
    "wire_gauge" -> R.string.unit_category_wire_gauge
    "temperature" -> R.string.unit_category_temperature
    "length" -> R.string.unit_category_length
    "area" -> R.string.unit_category_area
    "volume" -> R.string.unit_category_volume
    "mass" -> R.string.unit_category_mass
    "pressure" -> R.string.unit_category_pressure
    "capacitance" -> R.string.unit_category_capacitance
    "inductance" -> R.string.unit_category_inductance
    "signal_power" -> R.string.unit_category_signal_power
    "torque" -> R.string.unit_category_torque
    "illuminance" -> R.string.unit_category_illuminance
    "angle" -> R.string.unit_category_angle
    else -> error("No label for unit category $key")
}
