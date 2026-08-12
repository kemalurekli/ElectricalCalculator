package com.kemalurekli.electricalcalculator.core.designsystem.symbol

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The North American form of the symbols that differ from IEC.
 *
 * Only the ones that actually differ are here. A great many symbols are the same
 * in both conventions — a motor is a circle with an M, an earth is an earth —
 * and putting those side by side would pad the comparison with rows that teach
 * nothing.
 *
 * The resistor is the one everybody knows about. The others are the ones that
 * cost time: an ANSI fuse and an IEC fuse look nothing alike, and an ANSI
 * circuit breaker looks like something else entirely to an IEC eye.
 *
 * Drawn to the geometric description in ANSI/IEEE 315, on the same 48 × 48 grid
 * as the IEC set so the two sit level in a comparison.
 */
object AnsiSymbols {

    /** The zigzag everyone recognises, against IEC's plain box. */
    val Resistor: ImageVector = symbol("ansi_resistor") {
        terminalsHorizontal(inset = 12f)
        moveTo(12f, MID)
        lineTo(15f, MID - 7f)
        lineTo(21f, MID + 7f)
        lineTo(27f, MID - 7f)
        lineTo(33f, MID + 7f)
        lineTo(36f, MID)
    }

    val Inductor: ImageVector = symbol("ansi_inductor") {
        terminalsHorizontal(inset = 12f)
        coilHorizontal(startX = 12f, y = MID, count = 4, bumpWidth = 6f)
    }

    /** A slender loop, against IEC's box with a line through it. */
    val Fuse: ImageVector = symbol("ansi_fuse") {
        terminalsHorizontal(inset = 12f)
        arc(12f, MID, 36f, MID, radius = 14f, positive = true)
        arc(12f, MID, 36f, MID, radius = 14f, positive = false)
    }

    /** A blade with a quarter-arc, against IEC's blade with crosses. */
    val CircuitBreaker: ImageVector = symbol("ansi_circuit_breaker") {
        terminalsVertical(inset = 14f)
        line(MID, 34f, MID + 10f, 16f)
        arc(MID + 10f, 16f, MID - 2f, 12f, radius = 9f, positive = false)
    }

    val Transformer: ImageVector = symbol("ansi_transformer") {
        line(MID - 14f, TOP, MID - 14f, 10f)
        line(MID - 14f, 38f, MID - 14f, BOTTOM)
        line(MID + 14f, TOP, MID + 14f, 10f)
        line(MID + 14f, 38f, MID + 14f, BOTTOM)
        coilVertical(x = MID - 14f, startY = 10f, count = 4, bumpHeight = 7f, rightwards = true)
        coilVertical(x = MID + 14f, startY = 10f, count = 4, bumpHeight = 7f, rightwards = false)
        line(MID - 3f, 10f, MID - 3f, 38f)
        line(MID + 3f, 10f, MID + 3f, 38f)
    }

    /** A circle with a loop inside, against IEC's circle with a cross. */
    val Lamp: ImageVector = symbol("ansi_lamp") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 10f)
        arc(MID - 7f, MID + 4f, MID, MID - 6f, radius = 7f, positive = true)
        arc(MID, MID - 6f, MID + 7f, MID + 4f, radius = 7f, positive = true)
    }

    val ContactNormallyOpen: ImageVector = symbol("ansi_contact_no") {
        terminalsHorizontal(inset = 14f)
        circle(radius = 2.5f, cx = 14f)
        circle(radius = 2.5f, cx = 34f)
        line(16f, MID, 33f, MID - 9f)
    }

    val Battery: ImageVector = symbol("ansi_battery") {
        terminalsHorizontal(inset = 16f)
        line(16f, MID - 11f, 16f, MID + 11f)
        line(23f, MID - 5f, 23f, MID + 5f)
        line(28f, MID - 11f, 28f, MID + 11f)
        line(34f, MID - 5f, 34f, MID + 5f)
    }

    val Capacitor: ImageVector = symbol("ansi_capacitor") {
        terminalsHorizontal(inset = 20f)
        line(20f, MID - 11f, 20f, MID + 11f)
        // The curved plate marks the polarised one, which IEC draws straight.
        arc(28f, MID - 11f, 28f, MID + 11f, radius = 14f, positive = false)
    }

    val Ground: ImageVector = symbol("ansi_ground") {
        line(MID, TOP, MID, 24f)
        line(MID - 12f, 24f, MID + 12f, 24f)
        // The rake, against IEC's stack of shortening bars.
        line(MID - 8f, 24f, MID - 12f, 34f)
        line(MID, 24f, MID - 4f, 34f)
        line(MID + 8f, 24f, MID + 4f, 34f)
    }
}
