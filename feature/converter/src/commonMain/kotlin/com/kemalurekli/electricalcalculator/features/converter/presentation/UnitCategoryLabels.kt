package com.kemalurekli.electricalcalculator.features.converter.presentation

import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCategory
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_angle
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_area
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_capacitance
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_current
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_energy
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_frequency
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_illuminance
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_inductance
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_length
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_mass
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_power
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_pressure
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_resistance
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_signal_power
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_temperature
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_torque
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_voltage
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_volume
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.unit_category_wire_gauge
import org.jetbrains.compose.resources.StringResource

/**
 * Category labels are the one translated part of the converter — the unit
 * symbols themselves are language-neutral by design.
 */
fun UnitCategory.categoryLabelRes(): StringResource = when (key) {
    "voltage" -> Res.string.unit_category_voltage
    "current" -> Res.string.unit_category_current
    "resistance" -> Res.string.unit_category_resistance
    "power" -> Res.string.unit_category_power
    "energy" -> Res.string.unit_category_energy
    "frequency" -> Res.string.unit_category_frequency
    "wire_gauge" -> Res.string.unit_category_wire_gauge
    "temperature" -> Res.string.unit_category_temperature
    "length" -> Res.string.unit_category_length
    "area" -> Res.string.unit_category_area
    "volume" -> Res.string.unit_category_volume
    "mass" -> Res.string.unit_category_mass
    "pressure" -> Res.string.unit_category_pressure
    "capacitance" -> Res.string.unit_category_capacitance
    "inductance" -> Res.string.unit_category_inductance
    "signal_power" -> Res.string.unit_category_signal_power
    "torque" -> Res.string.unit_category_torque
    "illuminance" -> Res.string.unit_category_illuminance
    "angle" -> Res.string.unit_category_angle
    else -> error("No label for unit category $key")
}
