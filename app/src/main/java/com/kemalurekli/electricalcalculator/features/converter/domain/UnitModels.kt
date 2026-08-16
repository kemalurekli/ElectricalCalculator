package com.kemalurekli.electricalcalculator.features.converter.domain

import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * How a unit relates to its category's base unit.
 *
 * Most units are a simple multiple, but two of the ones an electrician actually
 * needs are not, and a converter built only on multiplication would get both
 * wrong:
 *
 * - temperature is **affine**, not proportional — 20 °C is not twice 10 °C;
 * - American Wire Gauge is **logarithmic**, and runs backwards.
 *
 * Modelling the relation rather than a factor keeps every category going
 * through one conversion path instead of special-casing the awkward ones at the
 * call site.
 */
sealed interface UnitScale {

    /** Converts a reading in this unit to the category's base unit. */
    fun toBase(value: Double): Double

    /** Converts a value in the base unit back to this unit. */
    fun fromBase(base: Double): Double

    /**
     * `base = value · factor + offset`.
     *
     * With [offset] left at zero this is an ordinary scale factor, which covers
     * every metric prefix and every imperial unit here. The offset exists for
     * temperature: Celsius and Fahrenheit have their zero somewhere other than
     * absolute zero, and ignoring that turns a conversion into nonsense.
     */
    data class Affine(val factor: Double, val offset: Double = 0.0) : UnitScale {
        override fun toBase(value: Double): Double = value * factor + offset
        override fun fromBase(base: Double): Double = (base - offset) / factor
    }

    /**
     * American Wire Gauge, against a base of mm².
     *
     * AWG is a geometric series, defined so that 36 gauges span a diameter
     * ratio of 92:
     *
     * ```
     * d(mm) = 0.127 · 92^((36 − n) / 39)
     * ```
     *
     * Two consequences that regularly catch people out, and that fall out of
     * the formula rather than being special-cased here: the number gets
     * *smaller* as the conductor gets *bigger*, and sizes above 0 are written
     * 00, 000, 0000 — which are gauges −1, −2 and −3.
     *
     * The formula is continuous, so a conversion from mm² can land between two
     * gauges. That is a real answer to "what gauge is this area", not a wire
     * you can buy; the screen says so.
     */
    /**
     * Decibels against a fixed power reference.
     *
     * Not a scale factor, so it cannot be an [Affine]: 0 dBm is one milliwatt
     * and every 10 dB is a factor of ten, which makes the relationship
     * logarithmic. Expressing it here rather than as a special case in the
     * converter is what lets dBm sit in the same category as the milliwatt and
     * convert between them like any other pair.
     *
     * Zero and negative powers have no decibel value — the logarithm diverges —
     * so they come back as negative infinity, which the formatter shows as such
     * rather than as a number that looks meaningful.
     */
    data class Decibel(val referenceWatts: Double) : UnitScale {
        override fun toBase(value: Double): Double = referenceWatts * 10.0.pow(value / 10.0)

        override fun fromBase(base: Double): Double =
            10.0 * log10(base / referenceWatts)
    }

    data object AmericanWireGauge : UnitScale {
        override fun toBase(value: Double): Double {
            val diameterMm = REFERENCE_DIAMETER_MM * RATIO.pow((36.0 - value) / STEPS)
            return PI * diameterMm * diameterMm / 4.0
        }

        override fun fromBase(base: Double): Double {
            val diameterMm = sqrt(4.0 * base / PI)
            return 36.0 - STEPS * ln(diameterMm / REFERENCE_DIAMETER_MM) / ln(RATIO)
        }

        /** Diameter of AWG 36, the gauge the series is anchored on. */
        private const val REFERENCE_DIAMETER_MM = 0.127

        /** Diameter ratio between AWG 36 and AWG 0000. */
        private const val RATIO = 92.0

        /** Gauge steps spanned by that ratio. */
        private const val STEPS = 39.0
    }

    /**
     * A conductor diameter, against a base of mm² of cross-section.
     *
     * Lets a caliper reading be compared with a cross-section directly, which
     * is the measurement available when a cable has no legible printing left.
     *
     * @param millimetresPerUnit length of one unit of this diameter in mm, so
     *   the same relation serves both a metric and an imperial caliper.
     */
    data class ConductorDiameter(val millimetresPerUnit: Double = 1.0) : UnitScale {
        override fun toBase(value: Double): Double {
            val diameterMm = value * millimetresPerUnit
            return PI * diameterMm * diameterMm / 4.0
        }

        override fun fromBase(base: Double): Double =
            sqrt(4.0 * base / PI) / millimetresPerUnit
    }
}

/**
 * A unit the converter offers.
 *
 * @param key stable identifier, safe to persist. Never shown.
 * @param symbol what the user sees. Deliberately **not** translated: unit
 *   symbols are defined by SI and by the standards that use them, and an
 *   engineer reads "kΩ" or "kcmil" the same way in every language.
 * @param scale how the unit relates to its category's base unit.
 */
data class MeasurementUnit(
    val key: String,
    val symbol: String,
    val scale: UnitScale,
)

/**
 * A set of units that can be converted between one another.
 *
 * Categories are closed on purpose. Volt-amperes are absent from [POWER]
 * because apparent power is not convertible to watts without a power factor,
 * and silently treating 1 kVA as 1 kW would be exactly the kind of confident
 * wrong answer this app exists to avoid — the power calculator is where that
 * relationship belongs.
 *
 * @param key stable identifier, safe to persist.
 * @param baseUnitKey the unit every conversion in this category passes through.
 * @param defaultFromKey the pair the category opens on. Declared rather than
 *   taken from the list order, because the two orderings want different things:
 *   a dropdown reads best with the prefixes ascending, while the sensible
 *   opening pair is the conversion people actually came for — mm² to AWG, or
 *   °C to °F, not µV to mV.
 * @param defaultToKey see [defaultFromKey].
 */
data class UnitCategory(
    val key: String,
    val baseUnitKey: String,
    val defaultFromKey: String,
    val defaultToKey: String,
    val units: List<MeasurementUnit>,
) {
    /** The unit [key] names, or null when it belongs to another category. */
    fun unitOrNull(key: String): MeasurementUnit? = units.firstOrNull { it.key == key }

    /** The unit the category opens converting from. */
    val defaultFrom: MeasurementUnit get() = unitOrNull(defaultFromKey) ?: units.first()

    /** The unit the category opens converting to. */
    val defaultTo: MeasurementUnit
        get() = unitOrNull(defaultToKey) ?: units.getOrElse(1) { units.first() }
}
