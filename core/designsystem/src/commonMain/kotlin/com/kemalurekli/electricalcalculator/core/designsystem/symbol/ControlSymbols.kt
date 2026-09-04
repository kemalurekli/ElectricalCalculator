package com.kemalurekli.electricalcalculator.core.designsystem.symbol

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The symbols that appear on a control and measurement schematic.
 *
 * Drawn to the geometric description in IEC 60617; see [symbol] for why they are
 * drawn rather than shipped as artwork.
 *
 * Contacts are the part worth being careful about. A normally-open and a
 * normally-closed contact differ by a single stroke, and on a real drawing that
 * stroke is the difference between a machine that starts and one that will not
 * stop.
 */
object ControlSymbols {

    // -- Contacts -----------------------------------------------------------------

    val ContactNormallyOpen: ImageVector = symbol("contact_no") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
    }

    val ContactNormallyClosed: ImageVector = symbol("contact_nc") {
        terminalsVertical(inset = 14f)
        line(MID, 34f, MID + 11f, 15f)
        // The bar across the top contact is the whole difference.
        line(MID + 11f, 14f, MID + 11f, 22f)
    }

    val ContactChangeover: ImageVector = symbol("contact_changeover") {
        line(MID, TOP, MID, 14f)
        line(MID - 8f, 34f, MID - 8f, BOTTOM)
        line(MID + 8f, 34f, MID + 8f, BOTTOM)
        line(MID, 14f, MID + 8f, 34f)
    }

    val ContactDelayedClosing: ImageVector = symbol("contact_delay_close") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        arc(MID - 7f, 24f, MID + 7f, 24f, radius = 7f, positive = true)
    }

    val ContactDelayedOpening: ImageVector = symbol("contact_delay_open") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        arc(MID - 7f, 24f, MID + 7f, 24f, radius = 7f, positive = false)
    }

    // -- Operators ----------------------------------------------------------------

    val PushButtonMake: ImageVector = symbol("button_make") {
        terminalsVertical(inset = 16f)
        line(MID, 16f, MID + 11f, 32f)
        line(MID + 6f, 24f, MID + 18f, 24f)
        line(MID + 18f, 20f, MID + 18f, 28f)
    }

    val PushButtonBreak: ImageVector = symbol("button_break") {
        terminalsVertical(inset = 16f)
        line(MID, 33f, MID + 11f, 17f)
        line(MID + 11f, 16f, MID + 11f, 23f)
        line(MID + 6f, 26f, MID + 18f, 26f)
    }

    val EmergencyStop: ImageVector = symbol("emergency_stop") {
        terminalsVertical(inset = 16f)
        line(MID, 33f, MID + 11f, 17f)
        line(MID + 11f, 16f, MID + 11f, 23f)
        circle(radius = 5f, cx = MID + 15f, cy = 26f)
    }

    val LimitSwitch: ImageVector = symbol("limit_switch") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        line(MID + 11f, 26f, MID + 18f, 22f)
    }

    val SelectorSwitch: ImageVector = symbol("selector") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        line(MID + 4f, 20f, MID + 16f, 14f)
        line(MID + 10f, 17f, MID + 10f, 11f)
    }

    // -- Coils and relays ---------------------------------------------------------

    val Coil: ImageVector = symbol("coil") {
        terminalsVertical(inset = 14f)
        box(halfWidth = 12f, halfHeight = 8f)
    }

    val CoilTimedOn: ImageVector = symbol("coil_timed_on") {
        terminalsVertical(inset = 14f)
        box(halfWidth = 12f, halfHeight = 8f)
        line(MID + 4f, MID - 8f, MID + 12f, MID)
        line(MID + 12f, MID, MID + 4f, MID + 8f)
    }

    val CoilTimedOff: ImageVector = symbol("coil_timed_off") {
        terminalsVertical(inset = 14f)
        box(halfWidth = 12f, halfHeight = 8f)
        line(MID - 4f, MID - 8f, MID - 12f, MID)
        line(MID - 12f, MID, MID - 4f, MID + 8f)
    }

    val ThermalOverload: ImageVector = symbol("thermal_overload") {
        terminalsVertical(inset = 14f)
        box(halfWidth = 10f, halfHeight = 9f)
        line(MID - 6f, MID + 4f, MID + 6f, MID + 4f)
        line(MID + 6f, MID + 4f, MID + 6f, MID - 4f)
    }

    // -- Indication and measurement -----------------------------------------------

    val IndicatorLamp: ImageVector = symbol("indicator_lamp") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 10f)
        cross(halfSize = 7f)
    }

    val Horn: ImageVector = symbol("horn") {
        line(LEFT, MID, 16f, MID)
        moveTo(16f, MID - 11f)
        lineTo(16f, MID + 11f)
        lineTo(32f, MID)
        close()
    }

    val Ammeter: ImageVector = symbol("ammeter") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 12f)
        line(MID - 5f, MID + 6f, MID, MID - 6f)
        line(MID, MID - 6f, MID + 5f, MID + 6f)
        line(MID - 3f, MID + 1f, MID + 3f, MID + 1f)
    }

    val Voltmeter: ImageVector = symbol("voltmeter") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 12f)
        line(MID - 5f, MID - 6f, MID, MID + 6f)
        line(MID, MID + 6f, MID + 5f, MID - 6f)
    }

    val EnergyMeter: ImageVector = symbol("energy_meter") {
        terminalsHorizontal(inset = 14f)
        box(halfWidth = 13f, halfHeight = 11f)
        // Wh, not M. What goes in the box is the unit the meter registers, and
        // an M in a box on a drawing is a motor.
        line(MID - 10f, MID - 5f, MID - 8f, MID + 5f)
        line(MID - 8f, MID + 5f, MID - 5f, MID - 1f)
        line(MID - 5f, MID - 1f, MID - 2f, MID + 5f)
        line(MID - 2f, MID + 5f, MID, MID - 5f)
        line(MID + 4f, MID - 6f, MID + 4f, MID + 5f)
        arc(MID + 4f, MID - 1f, MID + 9f, MID + 1f, radius = 3f, positive = true)
        line(MID + 9f, MID + 1f, MID + 9f, MID + 5f)
    }

    val Sensor: ImageVector = symbol("sensor") {
        line(LEFT, MID, 14f, MID)
        moveTo(14f, MID - 10f)
        lineTo(34f, MID - 10f)
        lineTo(34f, MID + 10f)
        lineTo(14f, MID + 10f)
        close()
        line(20f, MID - 4f, 28f, MID + 4f)
        arrowHead(28f, MID + 4f, 0.71f, 0.71f, size = 4f)
    }

    val ProgrammableController: ImageVector = symbol("plc") {
        box(halfWidth = 14f, halfHeight = 16f)
        line(LEFT, MID - 8f, MID - 14f, MID - 8f)
        line(LEFT, MID + 8f, MID - 14f, MID + 8f)
        line(MID + 14f, MID - 8f, RIGHT, MID - 8f)
        line(MID + 14f, MID + 8f, RIGHT, MID + 8f)
    }
}
