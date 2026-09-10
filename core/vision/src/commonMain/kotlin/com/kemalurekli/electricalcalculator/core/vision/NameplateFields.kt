package com.kemalurekli.electricalcalculator.core.vision

/**
 * A reading as the reader is shown it, before anything is applied.
 *
 * The screen has to name what was found — "400 V", "7,5 kW" — and a screen
 * that builds those strings itself would have twenty-two chances to disagree
 * with the next one. What each figure *is* belongs here; how it is written
 * belongs to whatever formats numbers for the reader.
 */
data class NameplateField(val kind: NameplateFieldKind, val value: Double)

enum class NameplateFieldKind {
    POWER_KILOWATTS,
    POWER_HORSEPOWER,
    VOLTAGE,
    CURRENT,
    POWER_FACTOR,
    EFFICIENCY,
    FREQUENCY,
    PHASES,
}

/**
 * What was found, in the order a plate prints it.
 *
 * Ordered rather than a map, because the list is read aloud on a confirmation
 * sheet and "7,5 kW, 400 V, cos φ 0,86" is the order somebody who has held a
 * plate expects to hear.
 */
fun NameplateReading.fields(): List<NameplateField> = buildList {
    powerKilowatts?.let { add(NameplateField(NameplateFieldKind.POWER_KILOWATTS, it)) }
    powerHorsepower?.let { add(NameplateField(NameplateFieldKind.POWER_HORSEPOWER, it)) }
    voltageVolts?.let { add(NameplateField(NameplateFieldKind.VOLTAGE, it)) }
    currentAmperes?.let { add(NameplateField(NameplateFieldKind.CURRENT, it)) }
    powerFactor?.let { add(NameplateField(NameplateFieldKind.POWER_FACTOR, it)) }
    efficiencyPercent?.let { add(NameplateField(NameplateFieldKind.EFFICIENCY, it)) }
    frequencyHertz?.let { add(NameplateField(NameplateFieldKind.FREQUENCY, it)) }
    phases?.let { add(NameplateField(NameplateFieldKind.PHASES, it.toDouble())) }
}
