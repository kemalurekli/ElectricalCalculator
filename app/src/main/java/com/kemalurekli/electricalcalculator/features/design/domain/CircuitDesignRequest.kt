package com.kemalurekli.electricalcalculator.features.design.domain

import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType

/**
 * Turns a stored project and circuit into something the chain can design.
 *
 * Both sides keep their numbers as the text the user typed, so this is where
 * the text becomes arithmetic. It is deliberately all-or-nothing: a circuit
 * with a blank length is not a circuit designed at zero metres, it is a circuit
 * that is not finished yet, and the schedule says so rather than showing a
 * confident cross-section derived from a field nobody filled in.
 *
 * Returns null when any field the chain needs is missing or unreadable.
 */
fun designInputOrNull(project: Project, circuit: Circuit): CircuitDesignInput? {
    val voltage = project.systemVoltage.toNumberOrNull() ?: return null
    val ambient = project.ambientTemperatureC.toNumberOrNull() ?: return null
    val maxDrop = project.maxVoltageDropPercent.toNumberOrNull() ?: return null
    val ze = project.externalImpedanceOhms.toNumberOrNull() ?: return null

    val loadValue = circuit.load.toNumberOrNull() ?: return null
    val powerFactor = circuit.powerFactor.toNumberOrNull() ?: return null
    val length = circuit.lengthMetres.toNumberOrNull() ?: return null
    val grouped = circuit.groupedCircuits.toNumberOrNull()?.toInt() ?: return null
    val parallel = circuit.parallelConductors.toNumberOrNull()?.toInt() ?: return null
    val disconnection = circuit.disconnectionTimeSeconds.toNumberOrNull() ?: return null

    // A load of zero would sail through the chain and produce a 1.5 mm² answer
    // for a circuit that has not been specified.
    if (voltage <= 0.0 || loadValue <= 0.0 || length <= 0.0) return null
    if (grouped < 1 || parallel < 1) return null

    return CircuitDesignInput(
        load = when (circuit.loadKind) {
            CircuitLoadKind.CURRENT -> CircuitLoad.Current(loadValue)
            CircuitLoadKind.POWER -> CircuitLoad.Power(loadValue)
        },
        system = project.system,
        systemVoltage = voltage,
        powerFactor = powerFactor,
        lengthMetres = length,
        material = project.material,
        insulation = project.insulation,
        method = project.method,
        ambientTemperatureC = ambient,
        groupedCircuits = grouped,
        parallelConductors = parallel,
        maxVoltageDropPercent = maxDrop,
        deviceType = circuit.deviceType.toDeviceType(),
        externalImpedanceOhms = ze,
        disconnectionTimeSeconds = disconnection,
    )
}

/** Accepts either decimal separator, as every other field in the app does. */
private fun String.toNumberOrNull(): Double? = NumberFormatter.parseOrNull(trim())

/** A stored name that no longer resolves falls back to the commonest device. */
private fun String.toDeviceType(): ProtectiveDeviceType =
    ProtectiveDeviceType.entries.firstOrNull { it.name == this } ?: ProtectiveDeviceType.MCB_TYPE_B
