package com.kemalurekli.electricalcalculator.features.converter.domain

/**
 * Every unit the converter knows, and what it is worth.
 *
 * ### Where the numbers come from
 *
 * Metric prefixes are exact by definition. The imperial and customary factors
 * are the internationally agreed exact values — the inch has been exactly
 * 25.4 mm and the pound exactly 0.45359237 kg since 1959 — so they are written
 * to full precision rather than rounded, and derived units are written as the
 * arithmetic that produces them wherever that is clearer than the decimal.
 *
 * Two definitions are ambiguous in general use and are pinned deliberately:
 *
 * - **Horsepower** exists in two sizes. Mechanical `hp` is 550 ft·lbf/s;
 *   metric `PS` is 75 kgf·m/s. They differ by about 1.4 %, which is enough to
 *   change a motor selection, so both are offered rather than one being picked.
 * - **Calorie** likewise. The International Table calorie (4.1868 J) is used
 *   here, to agree with the IT British thermal unit in the same category; the
 *   thermochemical calorie of 4.184 J is 0.07 % smaller.
 */
object UnitCatalog {

    // -- Electrical ----------------------------------------------------------------

    val voltage = UnitCategory(
        key = "voltage",
        baseUnitKey = "V",
        defaultFromKey = "V",
        defaultToKey = "kV",
        units = listOf(
            metric("uV", "µV", 1e-6),
            metric("mV", "mV", 1e-3),
            metric("V", "V", 1.0),
            metric("kV", "kV", 1e3),
            metric("MV", "MV", 1e6),
        ),
    )

    val current = UnitCategory(
        key = "current",
        baseUnitKey = "A",
        defaultFromKey = "A",
        defaultToKey = "mA",
        units = listOf(
            metric("uA", "µA", 1e-6),
            metric("mA", "mA", 1e-3),
            metric("A", "A", 1.0),
            metric("kA", "kA", 1e3),
        ),
    )

    val resistance = UnitCategory(
        key = "resistance",
        baseUnitKey = "ohm",
        defaultFromKey = "ohm",
        defaultToKey = "kohm",
        units = listOf(
            metric("uohm", "µΩ", 1e-6),
            metric("mohm", "mΩ", 1e-3),
            metric("ohm", "Ω", 1.0),
            metric("kohm", "kΩ", 1e3),
            metric("Mohm", "MΩ", 1e6),
        ),
    )

    val power = UnitCategory(
        key = "power",
        baseUnitKey = "W",
        defaultFromKey = "kW",
        defaultToKey = "hp",
        units = listOf(
            metric("mW", "mW", 1e-3),
            metric("W", "W", 1.0),
            metric("kW", "kW", 1e3),
            metric("MW", "MW", 1e6),
            // 550 ft·lbf/s, the mechanical horsepower.
            metric("hp", "hp", 745.6998715822702),
            // 75 kgf·m/s, the metric horsepower — 1.4 % smaller than hp.
            metric("PS", "PS", 735.49875),
            metric("btu_h", "BTU/h", 0.2930710701722222),
        ),
    )

    val energy = UnitCategory(
        key = "energy",
        baseUnitKey = "J",
        defaultFromKey = "kWh",
        defaultToKey = "MJ",
        units = listOf(
            metric("J", "J", 1.0),
            metric("kJ", "kJ", 1e3),
            metric("MJ", "MJ", 1e6),
            metric("Wh", "Wh", 3_600.0),
            metric("kWh", "kWh", 3.6e6),
            metric("MWh", "MWh", 3.6e9),
            metric("cal", "cal", 4.1868),
            metric("kcal", "kcal", 4_186.8),
            metric("BTU", "BTU", 1_055.05585262),
        ),
    )

    val frequency = UnitCategory(
        key = "frequency",
        baseUnitKey = "Hz",
        defaultFromKey = "Hz",
        defaultToKey = "rpm",
        units = listOf(
            metric("Hz", "Hz", 1.0),
            metric("kHz", "kHz", 1e3),
            metric("MHz", "MHz", 1e6),
            metric("GHz", "GHz", 1e9),
            // Included because a motor nameplate speaks in rev/min while the
            // supply it is fed from speaks in hertz.
            metric("rpm", "rpm", 1.0 / 60.0),
        ),
    )

    // -- Physical ------------------------------------------------------------------

    val temperature = UnitCategory(
        key = "temperature",
        baseUnitKey = "K",
        defaultFromKey = "C",
        defaultToKey = "F",
        units = listOf(
            MeasurementUnit("C", "°C", UnitScale.Affine(factor = 1.0, offset = 273.15)),
            MeasurementUnit("F", "°F", UnitScale.Affine(factor = 5.0 / 9.0, offset = 255.3722222222222)),
            MeasurementUnit("K", "K", UnitScale.Affine(factor = 1.0)),
            MeasurementUnit("R", "°R", UnitScale.Affine(factor = 5.0 / 9.0)),
        ),
    )

    val length = UnitCategory(
        key = "length",
        baseUnitKey = "m",
        defaultFromKey = "m",
        defaultToKey = "ft",
        units = listOf(
            metric("mm", "mm", 1e-3),
            metric("cm", "cm", 1e-2),
            metric("m", "m", 1.0),
            metric("km", "km", 1e3),
            metric("in", "in", 0.0254),
            metric("ft", "ft", 0.3048),
            metric("yd", "yd", 0.9144),
            metric("mi", "mi", 1_609.344),
        ),
    )

    val area = UnitCategory(
        key = "area",
        baseUnitKey = "m2",
        defaultFromKey = "mm2",
        defaultToKey = "in2",
        units = listOf(
            metric("mm2", "mm²", 1e-6),
            metric("cm2", "cm²", 1e-4),
            metric("m2", "m²", 1.0),
            metric("in2", "in²", 0.00064516),
            metric("ft2", "ft²", 0.09290304),
            metric("cmil", "cmil", 5.067074790974978e-10),
        ),
    )

    val volume = UnitCategory(
        key = "volume",
        baseUnitKey = "m3",
        defaultFromKey = "L",
        defaultToKey = "gal_us",
        units = listOf(
            metric("mL", "mL", 1e-6),
            metric("L", "L", 1e-3),
            metric("m3", "m³", 1.0),
            metric("in3", "in³", 1.6387064e-5),
            metric("ft3", "ft³", 0.028316846592),
            metric("gal_us", "gal (US)", 0.003785411784),
            metric("gal_uk", "gal (UK)", 0.00454609),
        ),
    )

    val mass = UnitCategory(
        key = "mass",
        baseUnitKey = "kg",
        defaultFromKey = "kg",
        defaultToKey = "lb",
        units = listOf(
            metric("g", "g", 1e-3),
            metric("kg", "kg", 1.0),
            metric("t", "t", 1e3),
            metric("lb", "lb", 0.45359237),
            metric("oz", "oz", 0.028349523125),
        ),
    )

    val pressure = UnitCategory(
        key = "pressure",
        baseUnitKey = "Pa",
        defaultFromKey = "bar",
        defaultToKey = "psi",
        units = listOf(
            metric("Pa", "Pa", 1.0),
            metric("kPa", "kPa", 1e3),
            metric("MPa", "MPa", 1e6),
            metric("mbar", "mbar", 100.0),
            metric("bar", "bar", 1e5),
            metric("psi", "psi", 6_894.757293168361),
            metric("atm", "atm", 101_325.0),
            metric("mmHg", "mmHg", 133.322387415),
        ),
    )

    // -- Conductor sizing ------------------------------------------------------------

    /**
     * The category this app exists for: moving between the metric cross-section
     * a European drawing specifies, the gauge an American datasheet quotes, and
     * the diameter a pair of calipers reads.
     */
    val wireGauge = UnitCategory(
        key = "wire_gauge",
        baseUnitKey = "mm2",
        defaultFromKey = "mm2",
        defaultToKey = "awg",
        units = listOf(
            metric("mm2", "mm²", 1.0),
            MeasurementUnit("awg", "AWG", UnitScale.AmericanWireGauge),
            metric("kcmil", "kcmil", 0.5067074790974977),
            metric("cmil", "cmil", 0.0005067074790974977),
            MeasurementUnit("dia_mm", "mm ⌀", UnitScale.ConductorDiameter()),
            MeasurementUnit("dia_in", "in ⌀", UnitScale.ConductorDiameter(MM_PER_INCH)),
        ),
    )

    /** Every category, in the order the converter presents them. */
    val all: List<UnitCategory> = listOf(
        voltage,
        current,
        resistance,
        power,
        energy,
        frequency,
        wireGauge,
        temperature,
        length,
        area,
        volume,
        mass,
        pressure,
    )

    /** The category [key] names, or null if there is none. */
    fun categoryOrNull(key: String): UnitCategory? = all.firstOrNull { it.key == key }

    private fun metric(key: String, symbol: String, factor: Double) =
        MeasurementUnit(key, symbol, UnitScale.Affine(factor))

    private const val MM_PER_INCH = 25.4
}
