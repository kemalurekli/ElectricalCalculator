package com.kemalurekli.electricalcalculator.core.designsystem.symbol

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The symbols that appear on an installation plan.
 *
 * A different drawing language from the single-line diagram: a plan shows where
 * things are in a room, so its symbols sit on a wall line and read as objects
 * rather than as a circuit. The wall stub at the bottom of most of them is not
 * decoration — it is what says "mounted on this wall" and orients the symbol.
 *
 * Drawn to the geometric description in IEC 60617; see [symbol] for why they are
 * drawn rather than shipped as artwork.
 */
object InstallationSymbols {

    // -- Socket outlets -------------------------------------------------------------

    val Socket: ImageVector = symbol("socket") {
        line(MID, BOTTOM, MID, 30f)
        arc(MID - 14f, 30f, MID + 14f, 30f, radius = 14f, positive = true)
        line(MID - 14f, 30f, MID + 14f, 30f)
    }

    val SocketEarthed: ImageVector = symbol("socket_earthed") {
        line(MID, BOTTOM, MID, 30f)
        arc(MID - 14f, 30f, MID + 14f, 30f, radius = 14f, positive = true)
        line(MID - 14f, 30f, MID + 14f, 30f)
        // The earth contact: a bar across the throat of the arc.
        line(MID - 6f, 22f, MID + 6f, 22f)
    }

    val SocketDouble: ImageVector = symbol("socket_double") {
        line(MID, BOTTOM, MID, 30f)
        arc(MID - 14f, 30f, MID + 14f, 30f, radius = 14f, positive = true)
        line(MID - 14f, 30f, MID + 14f, 30f)
        line(MID - 6f, 22f, MID + 6f, 22f)
        arc(MID - 9f, 30f, MID + 9f, 30f, radius = 9f, positive = true)
    }

    val SocketWeatherproof: ImageVector = symbol("socket_weatherproof") {
        line(MID, BOTTOM, MID, 30f)
        arc(MID - 13f, 30f, MID + 13f, 30f, radius = 13f, positive = true)
        line(MID - 13f, 30f, MID + 13f, 30f)
        line(MID - 6f, 23f, MID + 6f, 23f)
        // The dome that marks a protected fitting.
        arc(MID - 17f, 30f, MID + 17f, 30f, radius = 17f, positive = true)
    }

    val SocketSwitched: ImageVector = symbol("socket_switched") {
        line(MID, BOTTOM, MID, 32f)
        arc(MID - 12f, 32f, MID + 12f, 32f, radius = 12f, positive = true)
        line(MID - 12f, 32f, MID + 12f, 32f)
        line(MID - 5f, 25f, MID + 5f, 25f)
        line(MID + 12f, 32f, MID + 20f, 22f)
    }

    val DataOutlet: ImageVector = symbol("data_outlet") {
        line(MID, BOTTOM, MID, 32f)
        box(halfWidth = 12f, halfHeight = 9f, cy = 23f)
        line(MID - 5f, 19f, MID + 5f, 19f)
        line(MID - 5f, 26f, MID + 5f, 26f)
    }

    // -- Switches -------------------------------------------------------------------

    val SwitchOneWay: ImageVector = symbol("switch_one_way") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
    }

    val SwitchTwoWay: ImageVector = symbol("switch_two_way") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
        line(MID, 25f, MID - 13f, 13f)
    }

    val SwitchIntermediate: ImageVector = symbol("switch_intermediate") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
        line(MID, 25f, MID - 13f, 13f)
        line(MID - 13f, 20f, MID + 13f, 20f)
    }

    val SwitchTwoGang: ImageVector = symbol("switch_two_gang") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
        line(MID + 4f, 24f, MID + 17f, 18f)
    }

    val Dimmer: ImageVector = symbol("dimmer") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
        arc(MID - 12f, 22f, MID - 2f, 12f, radius = 12f, positive = true)
    }

    val PullSwitch: ImageVector = symbol("pull_switch") {
        line(MID, BOTTOM, MID, 30f)
        circle(radius = 5f, cy = 30f)
        line(MID, 25f, MID + 13f, 13f)
        line(MID + 13f, 13f, MID + 13f, 5f)
    }

    // -- Luminaires -----------------------------------------------------------------

    val Luminaire: ImageVector = symbol("luminaire") {
        circle(radius = 11f)
        cross(halfSize = 8f)
    }

    val LuminaireFluorescent: ImageVector = symbol("luminaire_fluorescent") {
        box(halfWidth = 20f, halfHeight = 6f)
        line(MID - 20f, MID, MID + 20f, MID)
    }

    val Downlight: ImageVector = symbol("downlight") {
        circle(radius = 11f)
        circle(radius = 4f)
    }

    val EmergencyLuminaire: ImageVector = symbol("emergency_luminaire") {
        circle(radius = 11f)
        cross(halfSize = 8f)
        // The bar is the maintained-supply mark.
        line(MID - 16f, MID + 16f, MID + 16f, MID + 16f)
    }

    val Floodlight: ImageVector = symbol("floodlight") {
        circle(radius = 9f, cx = 16f)
        line(25f, MID - 8f, 40f, MID - 14f)
        line(25f, MID + 8f, 40f, MID + 14f)
        line(40f, MID - 14f, 40f, MID + 14f)
    }

    val Exit: ImageVector = symbol("exit") {
        box(halfWidth = 16f, halfHeight = 10f)
        line(MID - 8f, MID, MID + 6f, MID)
        arrowHead(MID + 6f, MID, 1f, 0f, size = 4f)
    }

    // -- Distribution and containment -----------------------------------------------

    val JunctionBox: ImageVector = symbol("junction_box") {
        circle(radius = 11f)
        line(MID - 8f, MID - 8f, MID + 8f, MID + 8f)
    }

    val DistributionBoard: ImageVector = symbol("distribution_board") {
        box(halfWidth = 18f, halfHeight = 11f)
        line(MID - 18f, MID - 3f, MID + 18f, MID - 3f)
        line(MID - 9f, MID - 11f, MID - 9f, MID - 3f)
        line(MID + 9f, MID - 11f, MID + 9f, MID - 3f)
    }

    val Riser: ImageVector = symbol("riser") {
        circle(radius = 11f)
        line(MID, MID + 8f, MID, MID - 8f)
        arrowHead(MID, MID - 8f, 0f, -1f, size = 4f)
    }

    val Conduit: ImageVector = symbol("conduit") {
        line(LEFT, MID, RIGHT, MID)
        line(MID - 6f, MID - 6f, MID - 6f, MID + 6f)
        line(MID + 6f, MID - 6f, MID + 6f, MID + 6f)
    }

    // -- Auxiliary ------------------------------------------------------------------

    val Bell: ImageVector = symbol("bell") {
        line(MID, BOTTOM, MID, 32f)
        arc(MID - 13f, 32f, MID + 13f, 32f, radius = 13f, positive = true)
        line(MID - 13f, 32f, MID + 13f, 32f)
    }

    val Intercom: ImageVector = symbol("intercom") {
        box(halfWidth = 11f, halfHeight = 14f)
        circle(radius = 5f, cy = MID - 5f)
        line(MID - 6f, MID + 8f, MID + 6f, MID + 8f)
    }

    val Detector: ImageVector = symbol("detector") {
        circle(radius = 12f)
        line(MID - 6f, MID, MID + 6f, MID)
        line(MID, MID - 6f, MID, MID + 6f)
    }
}
