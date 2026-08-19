package com.kemalurekli.electricalcalculator.core.designsystem.symbol

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The symbols that appear on a single-line diagram.
 *
 * Drawn to the geometric description in IEC 60617; see [symbol] for why they are
 * drawn rather than shipped as artwork.
 *
 * Where two symbols differ by one detail — a switch-disconnector's arrow, a
 * circuit breaker's crosses — the difference is exactly what the catalogue's
 * distinguishing note is about, so the geometry is kept deliberately faithful on
 * those pairs even where a looser drawing would read more easily.
 */
object SingleLineSymbols {

    // -- Conductors and connections ----------------------------------------------

    val Conductor: ImageVector = symbol("conductor") {
        line(LEFT, MID, RIGHT, MID)
    }

    val Junction: ImageVector = symbol("junction") {
        line(LEFT, MID, RIGHT, MID)
        line(MID, MID, MID, BOTTOM)
        circle(radius = 3f)
    }

    val Crossing: ImageVector = symbol("crossing") {
        line(LEFT, MID, RIGHT, MID)
        line(MID, TOP, MID, BOTTOM)
    }

    val Earth: ImageVector = symbol("earth") {
        line(MID, TOP, MID, 26f)
        line(MID - 11f, 26f, MID + 11f, 26f)
        line(MID - 7f, 32f, MID + 7f, 32f)
        line(MID - 3f, 38f, MID + 3f, 38f)
    }

    val Busbar: ImageVector = symbol("busbar") {
        line(4f, MID - 4f, RIGHT - 4f, MID - 4f)
        line(4f, MID + 4f, RIGHT - 4f, MID + 4f)
        line(MID, MID + 4f, MID, BOTTOM)
    }

    val Terminal: ImageVector = symbol("terminal") {
        terminalsHorizontal(inset = 18f)
        circle(radius = 6f)
    }

    // -- Switching ----------------------------------------------------------------

    /** A plain isolator: a blade lifted off its contact, and nothing else. */
    val Disconnector: ImageVector = symbol("disconnector") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
    }

    /** A load-break switch: the disconnector plus the arc-quenching half-circle. */
    val SwitchDisconnector: ImageVector = symbol("switch_disconnector") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        arc(MID - 5f, 34f, MID + 5f, 34f, radius = 5f, positive = false)
    }

    /** A circuit breaker: the blade replaced by a cross on the contact. */
    val CircuitBreaker: ImageVector = symbol("circuit_breaker") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        cross(halfSize = 4f, cx = MID, cy = 14f)
    }

    val Contactor: ImageVector = symbol("contactor") {
        terminalsVertical(inset = 14f)
        line(MID, 14f, MID + 11f, 33f)
        arc(MID - 5f, 18f, MID + 5f, 18f, radius = 5f, positive = true)
    }

    val Fuse: ImageVector = symbol("fuse") {
        terminalsVertical(inset = 14f)
        box(halfWidth = 6f, halfHeight = 10f)
        line(MID, 14f, MID, 34f)
    }

    val FuseSwitch: ImageVector = symbol("fuse_switch") {
        terminalsVertical(inset = 12f)
        box(halfWidth = 6f, halfHeight = 8f, cy = MID + 6f)
        line(MID, MID - 2f, MID, MID + 14f)
        line(MID, 12f, MID + 10f, MID - 4f)
    }

    val MiniatureBreaker: ImageVector = symbol("mcb") {
        terminalsVertical(inset = 12f)
        box(halfWidth = 9f, halfHeight = 12f)
        line(MID - 5f, MID + 6f, MID + 5f, MID - 6f)
        cross(halfSize = 3f, cx = MID, cy = MID - 8f)
    }

    val ResidualCurrentDevice: ImageVector = symbol("rcd") {
        terminalsVertical(inset = 12f)
        box(halfWidth = 10f, halfHeight = 12f)
        circle(radius = 6f, cy = MID)
        line(MID - 10f, MID - 3f, MID + 10f, MID - 3f)
    }

    val ResidualCurrentBreakerOverload: ImageVector = symbol("rcbo") {
        terminalsVertical(inset = 12f)
        box(halfWidth = 10f, halfHeight = 13f)
        circle(radius = 5f, cy = MID + 4f)
        line(MID - 5f, MID - 4f, MID + 5f, MID - 10f)
        cross(halfSize = 3f, cx = MID, cy = MID - 11f)
    }

    // -- Machines and sources -----------------------------------------------------

    val Motor: ImageVector = symbol("motor") {
        terminalsVertical(inset = 10f)
        circle(radius = 14f)
        line(MID - 6f, MID + 5f, MID - 6f, MID - 5f)
        line(MID - 6f, MID - 5f, MID, MID + 3f)
        line(MID, MID + 3f, MID + 6f, MID - 5f)
        line(MID + 6f, MID - 5f, MID + 6f, MID + 5f)
    }

    val Generator: ImageVector = symbol("generator") {
        terminalsVertical(inset = 10f)
        circle(radius = 14f)
        line(MID + 5f, MID - 4f, MID - 1f, MID - 4f)
        arc(MID - 1f, MID - 4f, MID - 1f, MID + 4f, radius = 4f, positive = false)
        line(MID - 1f, MID + 4f, MID + 5f, MID + 4f)
    }

    val Battery: ImageVector = symbol("battery") {
        terminalsVertical(inset = 16f)
        line(MID - 10f, 18f, MID + 10f, 18f)
        line(MID - 5f, 23f, MID + 5f, 23f)
        line(MID - 10f, 27f, MID + 10f, 27f)
        line(MID - 5f, 32f, MID + 5f, 32f)
    }

    val Transformer: ImageVector = symbol("transformer") {
        line(MID, TOP, MID, 10f)
        line(MID, BOTTOM - 10f, MID, BOTTOM)
        circle(radius = 10f, cy = MID - 5f)
        circle(radius = 10f, cy = MID + 5f)
    }

    val Autotransformer: ImageVector = symbol("autotransformer") {
        terminalsVertical(inset = 10f)
        circle(radius = 13f)
        line(MID - 13f, MID, MID + 13f, MID)
    }

    val CurrentTransformer: ImageVector = symbol("current_transformer") {
        line(LEFT, MID, RIGHT, MID)
        circle(radius = 9f, cy = MID + 11f)
        line(MID, MID + 20f, MID, BOTTOM)
    }

    val VoltageTransformer: ImageVector = symbol("voltage_transformer") {
        line(MID, TOP, MID, 12f)
        circle(radius = 9f, cy = 21f)
        circle(radius = 9f, cy = 33f)
    }

    val SurgeProtectiveDevice: ImageVector = symbol("spd") {
        terminalsVertical(inset = 13f)
        box(halfWidth = 8f, halfHeight = 11f)
        line(MID - 5f, MID + 6f, MID + 4f, MID - 2f)
        arrowHead(MID + 4f, MID - 2f, 0.71f, -0.71f, size = 4f)
    }

    val Capacitor: ImageVector = symbol("capacitor") {
        terminalsVertical(inset = 20f)
        line(MID - 11f, 20f, MID + 11f, 20f)
        line(MID - 11f, 28f, MID + 11f, 28f)
    }

    val Resistor: ImageVector = symbol("resistor") {
        terminalsVertical(inset = 13f)
        box(halfWidth = 7f, halfHeight = 11f)
    }

    val Inductor: ImageVector = symbol("inductor") {
        line(MID, TOP, MID, 12f)
        coilVertical(x = MID, startY = 12f, count = 3, bumpHeight = 8f)
        line(MID, 36f, MID, BOTTOM)
    }

    val Rectifier: ImageVector = symbol("rectifier") {
        terminalsVertical(inset = 10f)
        box(halfWidth = 13f, halfHeight = 13f)
        line(MID - 13f, MID + 13f, MID + 13f, MID - 13f)
        line(MID - 8f, MID - 7f, MID - 2f, MID - 7f)
        line(MID + 2f, MID + 7f, MID + 8f, MID + 7f)
    }

    val Inverter: ImageVector = symbol("inverter") {
        terminalsVertical(inset = 10f)
        box(halfWidth = 13f, halfHeight = 13f)
        line(MID - 13f, MID + 13f, MID + 13f, MID - 13f)
        line(MID - 9f, MID - 7f, MID - 3f, MID - 7f)
        arc(MID + 1f, MID + 8f, MID + 5f, MID + 8f, radius = 2f, positive = true)
        arc(MID + 5f, MID + 8f, MID + 9f, MID + 8f, radius = 2f, positive = false)
    }

    val Drive: ImageVector = symbol("drive") {
        terminalsVertical(inset = 8f)
        box(halfWidth = 15f, halfHeight = 15f)
        line(MID - 9f, MID - 6f, MID - 1f, MID - 6f)
        arc(MID + 1f, MID + 6f, MID + 5f, MID + 6f, radius = 2f, positive = true)
        arc(MID + 5f, MID + 6f, MID + 9f, MID + 6f, radius = 2f, positive = false)
    }

    val Lamp: ImageVector = symbol("lamp") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 10f)
        cross(halfSize = 7f)
    }
}
