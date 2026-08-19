package com.kemalurekli.electricalcalculator.core.common.model

/**
 * Icon choice for a calculator, named as a concept rather than a drawing.
 *
 * The catalog names the *concept*; the design system maps each concept to an
 * `ImageVector`. That indirection is what keeps `androidx.compose` off the
 * domain classpath and lets the icon set be restyled without touching a single
 * catalog entry.
 *
 * It lives in `:core:common` rather than with the rest of the domain model
 * because it is the one piece of vocabulary both sides of that indirection have
 * to agree on, and `:core:designsystem` cannot depend on the domain without
 * inverting the layering.
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
    SELECTIVITY,
    MOTOR_STARTING,
    HARMONICS,
    EVSE,
}
