package com.kemalurekli.electricalcalculator.features.converter

import com.kemalurekli.electricalcalculator.features.converter.domain.ConvertUnitUseCase
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCategory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.math.abs

class ConvertUnitUseCaseTest {

    private val convert = ConvertUnitUseCase()

    private fun UnitCategory.unit(key: String) =
        requireNotNull(unitOrNull(key)) { "$key is not in ${this.key}" }

    private fun convert(category: UnitCategory, value: Double, from: String, to: String) =
        convert(value, category.unit(from), category.unit(to))

    // -- Electrical --------------------------------------------------------------------

    @Test
    fun `metric prefixes scale by powers of a thousand`() {
        assertEquals(11_000.0, convert(UnitCatalog.voltage, 11.0, "kV", "V"), 1e-9)
        assertEquals(0.4, convert(UnitCatalog.voltage, 400.0, "V", "kV"), 1e-12)
        assertEquals(1_500.0, convert(UnitCatalog.current, 1.5, "A", "mA"), 1e-9)
        assertEquals(0.047, convert(UnitCatalog.resistance, 47.0, "ohm", "kohm"), 1e-12)
    }

    @Test
    fun `the two horsepowers are not the same size`() {
        // 1.4 % apart — enough to change a motor selection, which is why both
        // are offered instead of one being chosen for the user.
        val mechanical = convert(UnitCatalog.power, 1.0, "hp", "W")
        val metric = convert(UnitCatalog.power, 1.0, "PS", "W")

        assertEquals(745.699872, mechanical, 1e-6)
        assertEquals(735.49875, metric, 1e-9)
        assertTrue(mechanical > metric)
        assertEquals(0.0139, mechanical / metric - 1.0, 1e-4)
    }

    @Test
    fun `a five and a half kilowatt motor is about seven and a half horsepower`() {
        assertEquals(7.376, convert(UnitCatalog.power, 5.5, "kW", "hp"), 1e-3)
    }

    @Test
    fun `a kilowatt-hour is three point six megajoules`() {
        assertEquals(3.6e6, convert(UnitCatalog.energy, 1.0, "kWh", "J"), 1e-6)
        assertEquals(3.6, convert(UnitCatalog.energy, 1.0, "kWh", "MJ"), 1e-12)
    }

    @Test
    fun `revolutions per minute convert to hertz`() {
        // A 4-pole motor at 50 Hz turns near 1500 rpm; the supply frequency and
        // the nameplate speed are the same quantity in different units.
        assertEquals(25.0, convert(UnitCatalog.frequency, 1_500.0, "rpm", "Hz"), 1e-12)
        assertEquals(3_000.0, convert(UnitCatalog.frequency, 50.0, "Hz", "rpm"), 1e-9)
    }

    // -- Temperature, where a factor alone would be wrong ---------------------------------

    @Test
    fun `water freezes and boils at the expected numbers`() {
        assertEquals(32.0, convert(UnitCatalog.temperature, 0.0, "C", "F"), 1e-9)
        assertEquals(212.0, convert(UnitCatalog.temperature, 100.0, "C", "F"), 1e-9)
        assertEquals(273.15, convert(UnitCatalog.temperature, 0.0, "C", "K"), 1e-9)
        assertEquals(373.15, convert(UnitCatalog.temperature, 100.0, "C", "K"), 1e-9)
    }

    @Test
    fun `the scales cross at minus forty`() {
        // The one temperature that reads the same on both scales.
        assertEquals(-40.0, convert(UnitCatalog.temperature, -40.0, "C", "F"), 1e-9)
    }

    @Test
    fun `absolute zero is absolute zero in every scale`() {
        assertEquals(0.0, convert(UnitCatalog.temperature, -273.15, "C", "K"), 1e-9)
        assertEquals(0.0, convert(UnitCatalog.temperature, -459.67, "F", "K"), 1e-9)
        assertEquals(0.0, convert(UnitCatalog.temperature, 0.0, "R", "K"), 1e-12)
    }

    @Test
    fun `temperature is not proportional`() {
        // The mistake a factor-only converter makes: twice the Celsius reading
        // is nowhere near twice the Fahrenheit reading.
        val ten = convert(UnitCatalog.temperature, 10.0, "C", "F")
        val twenty = convert(UnitCatalog.temperature, 20.0, "C", "F")

        assertEquals(50.0, ten, 1e-9)
        assertEquals(68.0, twenty, 1e-9)
        assertTrue(twenty < 2 * ten)
    }

    @Test
    fun `a PVC and an XLPE conductor rating in Fahrenheit`() {
        assertEquals(158.0, convert(UnitCatalog.temperature, 70.0, "C", "F"), 1e-9)
        assertEquals(194.0, convert(UnitCatalog.temperature, 90.0, "C", "F"), 1e-9)
    }

    // -- Wire gauge, where the relation is logarithmic and runs backwards -------------------

    @Test
    fun `standard AWG sizes reproduce their published cross-sections`() {
        // The published table, reproduced from the defining formula rather than
        // transcribed. If the formula drifts, these fail.
        assertEquals(0.2047, convert(UnitCatalog.wireGauge, 24.0, "awg", "mm2"), 1e-4)
        assertEquals(2.081, convert(UnitCatalog.wireGauge, 14.0, "awg", "mm2"), 1e-3)
        assertEquals(5.261, convert(UnitCatalog.wireGauge, 10.0, "awg", "mm2"), 1e-3)
        assertEquals(21.15, convert(UnitCatalog.wireGauge, 4.0, "awg", "mm2"), 1e-2)
        assertEquals(53.48, convert(UnitCatalog.wireGauge, 0.0, "awg", "mm2"), 1e-2)
    }

    @Test
    fun `the sizes above zero are negative gauges`() {
        // 00, 000 and 0000 are gauges −1, −2 and −3. 4/0 is 107.2 mm².
        assertEquals(67.43, convert(UnitCatalog.wireGauge, -1.0, "awg", "mm2"), 1e-2)
        assertEquals(85.03, convert(UnitCatalog.wireGauge, -2.0, "awg", "mm2"), 1e-2)
        assertEquals(107.2, convert(UnitCatalog.wireGauge, -3.0, "awg", "mm2"), 1e-1)
    }

    @Test
    fun `a larger gauge number is a smaller conductor`() {
        val thin = convert(UnitCatalog.wireGauge, 20.0, "awg", "mm2")
        val thick = convert(UnitCatalog.wireGauge, 6.0, "awg", "mm2")

        assertTrue(thick > thin)
    }

    @Test
    fun `three gauges up doubles the area`() {
        // The property the 92-over-39-steps definition was chosen to give.
        val ten = convert(UnitCatalog.wireGauge, 10.0, "awg", "mm2")
        val seven = convert(UnitCatalog.wireGauge, 7.0, "awg", "mm2")

        assertEquals(2.0, seven / ten, 1e-2)
    }

    @Test
    fun `a metric cross-section converts back to a gauge`() {
        // 2.5 mm² sits between AWG 14 and 13 — a real answer to "what gauge is
        // this", not a wire that can be bought.
        val gauge = convert(UnitCatalog.wireGauge, 2.5, "mm2", "awg")

        assertTrue(gauge < 14.0)
        assertTrue(gauge > 13.0)
        assertEquals(13.209, gauge, 1e-3)
    }

    @Test
    fun `kcmil agrees with the gauge table`() {
        // 4/0 AWG is 211.6 kcmil, the figure an American datasheet quotes.
        assertEquals(211.6, convert(UnitCatalog.wireGauge, -3.0, "awg", "kcmil"), 1e-1)
        assertEquals(107.2, convert(UnitCatalog.wireGauge, 211.6, "kcmil", "mm2"), 1e-1)
    }

    @Test
    fun `a caliper reading converts to a cross-section`() {
        // A 2 mm conductor is π mm² of copper.
        assertEquals(3.141593, convert(UnitCatalog.wireGauge, 2.0, "dia_mm", "mm2"), 1e-6)
        assertEquals(2.0, convert(UnitCatalog.wireGauge, 3.141592653589793, "mm2", "dia_mm"), 1e-9)
    }

    @Test
    fun `an imperial caliper reads the same conductor`() {
        // AWG 10 is 0.1019 in in diameter.
        assertEquals(0.1019, convert(UnitCatalog.wireGauge, 10.0, "awg", "dia_in"), 1e-4)
        assertEquals(2.588, convert(UnitCatalog.wireGauge, 10.0, "awg", "dia_mm"), 1e-3)
    }

    // -- Physical --------------------------------------------------------------------------

    @Test
    fun `the inch and the pound are exact by definition`() {
        assertEquals(25.4, convert(UnitCatalog.length, 1.0, "in", "mm"), 1e-12)
        assertEquals(0.45359237, convert(UnitCatalog.mass, 1.0, "lb", "kg"), 1e-12)
        assertEquals(1_609.344, convert(UnitCatalog.length, 1.0, "mi", "m"), 1e-9)
    }

    @Test
    fun `pressure units agree with the standard atmosphere`() {
        assertEquals(1_013.25, convert(UnitCatalog.pressure, 1.0, "atm", "mbar"), 1e-9)
        assertEquals(14.6959, convert(UnitCatalog.pressure, 1.0, "atm", "psi"), 1e-4)
        // Not exactly 760: that identity defines the *torr*, while mmHg here
        // is the conventional millimetre of mercury of 133.322387415 Pa. The
        // two differ in the seventh figure, and the constant is the honest one.
        assertEquals(760.0, convert(UnitCatalog.pressure, 1.0, "atm", "mmHg"), 1e-3)
    }

    @Test
    fun `a US gallon is smaller than a UK one`() {
        val us = convert(UnitCatalog.volume, 1.0, "gal_us", "L")
        val uk = convert(UnitCatalog.volume, 1.0, "gal_uk", "L")

        assertEquals(3.785412, us, 1e-6)
        assertEquals(4.54609, uk, 1e-6)
        assertTrue(uk > us)
    }

    @Test
    fun `a circular mil is the same quantity in both categories`() {
        // cmil appears under area and under wire gauge; the two must agree or
        // the app contradicts itself depending on which screen is open.
        val viaArea = convert(UnitCatalog.area, 1_000.0, "cmil", "mm2")
        val viaGauge = convert(UnitCatalog.wireGauge, 1_000.0, "cmil", "mm2")

        assertEquals(viaArea, viaGauge, 1e-12)
        assertEquals(0.506707, viaArea, 1e-6)
    }

    // -- Properties every category must hold --------------------------------------------------

    @Test
    fun `converting a unit to itself changes nothing`() {
        UnitCatalog.all.forEach { category ->
            category.units.forEach { unit ->
                assertEquals(
                    42.0,
                    convert(42.0, unit, unit),
                    1e-9,
                    "${category.key}/${unit.key} is not its own identity",
                )
            }
        }
    }

    @Test
    fun `every conversion round-trips`() {
        UnitCatalog.all.forEach { category ->
            val base = category.unit(category.baseUnitKey)
            category.units.forEach { unit ->
                val there = convert(7.5, base, unit)
                val back = convert(there, unit, base)
                assertEquals(
                    7.5,
                    back,
                    // Relative, because a category's units span many decades.
                    abs(7.5) * 1e-9,
                    "${category.key}/${unit.key} does not round-trip",
                )
            }
        }
    }

    @Test
    fun `every category declares a base unit it actually contains`() {
        UnitCatalog.all.forEach { category ->
            assertNotNull(
                category.unitOrNull(category.baseUnitKey),
                "${category.key} has no unit named ${category.baseUnitKey}",
            )
        }
    }

    @Test
    fun `the base unit of every category is unity`() {
        // A base whose own factor is not 1 would make every figure in the
        // category quietly wrong by that factor.
        UnitCatalog.all.forEach { category ->
            val base = category.unit(category.baseUnitKey)
            assertEquals(
                1.0,
                base.scale.toBase(1.0),
                1e-12,
                "${category.key} base ${base.key} is not unity",
            )
        }
    }

    @Test
    fun `every category opens on two units it contains`() {
        UnitCatalog.all.forEach { category ->
            assertNotNull(
                category.unitOrNull(category.defaultFromKey),
                "${category.key} defaults from ${category.defaultFromKey}, which it lacks",
            )
            assertNotNull(
                category.unitOrNull(category.defaultToKey),
                "${category.key} defaults to ${category.defaultToKey}, which it lacks",
            )
            assertTrue(
                category.defaultFromKey != category.defaultToKey,
                "${category.key} opens converting a unit to itself",
            )
        }
    }

    @Test
    fun `unit keys are unique within a category`() {
        UnitCatalog.all.forEach { category ->
            val keys = category.units.map { it.key }
            assertEquals(keys.size, keys.distinct().size, "${category.key} has duplicate unit keys")
        }
    }

    @Test
    fun `category keys are unique`() {
        val keys = UnitCatalog.all.map { it.key }

        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `every category offers something to convert between`() {
        UnitCatalog.all.forEach { category ->
            assertTrue(category.units.size >= 2, "${category.key} has fewer than two units")
        }
    }

    // -- Converting to every unit at once -------------------------------------------------------

    @Test
    fun `converting to all skips the unit converted from`() {
        val from = UnitCatalog.voltage.unit("kV")

        val all = convert.toAll(11.0, from, UnitCatalog.voltage)

        assertEquals(UnitCatalog.voltage.units.size - 1, all.size)
        assertTrue(all.none { it.unit.key == "kV" })
    }

    @Test
    fun `converting to all agrees with converting one at a time`() {
        val from = UnitCatalog.wireGauge.unit("mm2")

        val all = convert.toAll(16.0, from, UnitCatalog.wireGauge)

        all.forEach { converted ->
            assertEquals(
                convert(16.0, from, converted.unit),
                converted.value,
                1e-12,
                converted.unit.key,
            )
        }
    }
}
