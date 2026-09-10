package com.kemalurekli.electricalcalculator.core.vision

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Real plates, as a recogniser hands them over.
 *
 * The lines are in the order and the shape text recognition actually produces:
 * a nameplate is a grid, the reader walks it row by row, and the symbols it is
 * least sure of are exactly the ones that carry the meaning — `φ` and `η`. Every
 * fixture here is a plate somebody could photograph, not a sentence written to
 * suit the parser.
 */
class NameplateReaderTest {

    @Test
    fun `a European three-phase plate`() {
        val reading = NameplateReader.read(
            listOf("3~ MOT", "7,5 kW", "230/400 V", "26,5/15,3 A", "cos φ 0,86", "50 Hz", "1450 r/min"),
        )

        assertEquals(7.5, reading.powerKilowatts)
        // The star connection, which is what a 400 V supply gives it.
        assertEquals(400.0, reading.voltageVolts)
        // And its current is the lower of the pair: the same machine at the
        // higher voltage draws less.
        assertEquals(15.3, reading.currentAmperes)
        assertEquals(0.86, reading.powerFactor)
        assertEquals(50.0, reading.frequencyHertz)
        assertEquals(3, reading.phases)
    }

    @Test
    fun `a comma is a decimal point`() {
        val reading = NameplateReader.read(listOf("11,0 kW", "0,88 cos"))

        assertEquals(11.0, reading.powerKilowatts)
    }

    @Test
    fun `phi comes back as whatever the recogniser guessed`() {
        // Three plates, one symbol, three readings of it. All three have to
        // land on the same number.
        listOf("cos φ 0,86", "cos 0 0,86", "cosф0,86").forEach { line ->
            assertEquals(0.86, NameplateReader.read(listOf(line)).powerFactor, line)
        }
    }

    @Test
    fun `a speed is not a voltage`() {
        // The failure this parser exists to avoid. `1450` is the only large
        // number on many plates and it is revolutions, not volts.
        val reading = NameplateReader.read(listOf("1450 r/min", "7,5 kW"))

        assertNull(reading.voltageVolts, "a figure with no unit beside it is not a reading")
        assertEquals(7.5, reading.powerKilowatts)
    }

    @Test
    fun `horsepower is read where horsepower is printed`() {
        assertEquals(10.0, NameplateReader.read(listOf("10 HP", "460 V")).powerHorsepower)
        assertNull(NameplateReader.read(listOf("10 HP")).powerKilowatts)
    }

    @Test
    fun `a single-phase plate`() {
        val reading = NameplateReader.read(listOf("1~ 230 V", "2,2 kW", "13,5 A", "50 Hz"))

        assertEquals(1, reading.phases)
        assertEquals(230.0, reading.voltageVolts)
        assertEquals(13.5, reading.currentAmperes)
    }

    @Test
    fun `an efficiency outside what a motor can be is refused`() {
        // OCR turns `η 89,5` into `n 895` often enough to matter, and a screen
        // that offers 895 % has told the reader it cannot read.
        assertNull(NameplateReader.read(listOf("n 895 %")).efficiencyPercent)
        assertEquals(89.5, NameplateReader.read(listOf("η 89,5 %")).efficiencyPercent)
    }

    @Test
    fun `a power factor above one is refused`() {
        assertNull(NameplateReader.read(listOf("cos 8,6")).powerFactor)
    }

    @Test
    fun `a photograph of a wall reads as nothing`() {
        val reading = NameplateReader.read(listOf("SIEMENS", "made in germany", "IP55", "IE3"))

        assertTrue(reading.isEmpty, "nothing on this plate is a figure this app can use")
    }

    @Test
    fun `an empty recognition is empty rather than a crash`() {
        assertTrue(NameplateReader.read(emptyList()).isEmpty)
    }
}
