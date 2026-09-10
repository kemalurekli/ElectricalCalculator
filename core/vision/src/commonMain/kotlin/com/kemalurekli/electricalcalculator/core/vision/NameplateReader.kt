package com.kemalurekli.electricalcalculator.core.vision

/**
 * What was found on a motor nameplate, in SI units and nothing else.
 *
 * Every field is nullable and nothing is guessed. A plate that does not print
 * its efficiency leaves efficiency null, and the screen keeps whatever the
 * reader had — filling a field with a plausible default and calling it a
 * reading is how a scan becomes worse than typing.
 */
data class NameplateReading(
    val powerKilowatts: Double? = null,
    val powerHorsepower: Double? = null,
    val voltageVolts: Double? = null,
    val currentAmperes: Double? = null,
    val powerFactor: Double? = null,
    val efficiencyPercent: Double? = null,
    val frequencyHertz: Double? = null,
    val phases: Int? = null,
) {
    /** Whether anything at all was recognised. */
    val isEmpty: Boolean
        get() = powerKilowatts == null && powerHorsepower == null && voltageVolts == null &&
            currentAmperes == null && powerFactor == null && efficiencyPercent == null &&
            frequencyHertz == null && phases == null
}

/**
 * Turns the lines a text recogniser produced into figures.
 *
 * ### Why this is not the recogniser's problem
 *
 * A recogniser returns characters. A nameplate is a dense, unlabelled grid
 * where the same number means different things depending on a symbol beside it,
 * that symbol is often a glyph the recogniser has never been trained on, and
 * half the plates in the world print their decimals with a comma. Deciding what
 * `0,86` is belongs in code that can be read and tested, not in a callback.
 *
 * ### What it refuses to do
 *
 * It does not guess. A figure with no unit beside it is ignored, however
 * plausible: a plate reading `1450` is a speed, and a reader who finds 1450 V
 * in the voltage field trusts the feature less than one who finds it empty.
 */
object NameplateReader {

    fun read(lines: List<String>): NameplateReading {
        val text = lines.joinToString(separator = " ").normalise()
        return NameplateReading(
            powerKilowatts = text.value(KILOWATTS),
            powerHorsepower = text.value(HORSEPOWER),
            voltageVolts = text.pairedValue(VOLTS),
            currentAmperes = text.pairedValue(AMPERES),
            powerFactor = text.after(COS, POWER_FACTOR_RANGE),
            efficiencyPercent = text.after(ETA, EFFICIENCY_RANGE),
            frequencyHertz = text.value(HERTZ),
            phases = text.phases(),
        )
    }

    /**
     * Lower-cases and makes every decimal a point.
     *
     * Half the plates in the world print `0,86`, and reading that comma as a
     * thousands separator turns a power factor into eighty-six.
     *
     * It deliberately does *not* substitute away the symbols a recogniser
     * mangles. An earlier version replaced `cos 0` with `cos ` to undo a misread
     * `φ` — and ate the leading zero of `0.86` with it, which is the reading
     * itself. Skipping numbers until one is in range does that job without
     * touching the text.
     */
    private fun String.normalise(): String = lowercase()
        .replace(',', '.')
        .replace(Regex("\\s+"), " ")

    /** The first number this pattern claims, or null. */
    private fun String.value(pattern: Regex): Double? =
        pattern.find(this)?.groupValues?.get(1)?.toDoubleOrNull()

    /**
     * A plate prints `230/400 v` for a motor that runs in delta or in star, and
     * `13.5/7.8 a` for the currents that go with them. The higher voltage is
     * the star connection, which is what a three-phase supply in this app's
     * world provides — and its current is the *lower* of the pair, because the
     * same machine at a higher voltage draws less.
     */
    private fun String.pairedValue(pattern: Regex): Double? {
        val match = pattern.find(this) ?: return null
        val first = match.groupValues[1].toDoubleOrNull()
        val second = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        if (second == null) return first
        if (first == null) return second
        return if (pattern === VOLTS) maxOf(first, second) else minOf(first, second)
    }

    /**
     * The first number after [marker] that could be the thing being looked for.
     *
     * Written this way because of `φ`. A recogniser reads it as `0`, `o`, `p`
     * or nothing at all depending on the font and the light, and an earlier
     * version substituted those away before matching — which also ate the
     * leading zero of `0.86` and turned the reading into nothing. Skipping
     * numbers until one is in range needs no guess about what the symbol
     * became: a misread `φ` is a `0`, and a power factor is never zero.
     */
    private fun String.after(marker: Regex, range: ClosedRange<Double>): Double? {
        val start = marker.find(this)?.range?.last ?: return null
        return NUMBERS.findAll(substring(start + 1))
            .take(NUMBERS_CONSIDERED)
            .mapNotNull { it.value.toDoubleOrNull() }
            .firstOrNull { it in range }
    }

    /** `3~`, `3 ph`, `3-phase`, and the single-phase forms of each. */
    private fun String.phases(): Int? = PHASES.find(this)?.groupValues?.get(1)?.toIntOrNull()

    private val NUMBER = "(\\d+(?:\\.\\d+)?)"

    private val KILOWATTS = Regex("$NUMBER\\s*kw\\b")
    private val HORSEPOWER = Regex("$NUMBER\\s*(?:hp|bg|cv)\\b")
    private val VOLTS = Regex("$NUMBER(?:\\s*/\\s*$NUMBER)?\\s*v\\b")
    private val AMPERES = Regex("$NUMBER(?:\\s*/\\s*$NUMBER)?\\s*a\\b")
    private val COS = Regex("cos")
    private val ETA = Regex("(?:eff\\.?|η|\\bn\\b)")
    private val NUMBERS = Regex(NUMBER)
    private val HERTZ = Regex("$NUMBER\\s*hz\\b")
    private val PHASES = Regex("(\\d)\\s*(?:~|ph\\b|-?phase\\b|mot\\b)")

    /** A power factor is a cosine, and a motor's is never nought. */
    private val POWER_FACTOR_RANGE = 0.01..1.0

    /** Below 50 % is not a motor; above 100 % is not anything. */
    private val EFFICIENCY_RANGE = 50.0..100.0

    /**
     * How far past the marker to keep looking.
     *
     * Far enough to step over a misread symbol, near enough that the frequency
     * printed on the next line is never mistaken for the thing being read.
     */
    private const val NUMBERS_CONSIDERED = 2
}
