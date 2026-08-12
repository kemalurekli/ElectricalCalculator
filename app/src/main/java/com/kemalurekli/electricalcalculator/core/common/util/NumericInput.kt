package com.kemalurekli.electricalcalculator.core.common.util

import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError

/**
 * Reusable validation for the numeric fields every calculator is built from.
 *
 * Each calculator declares the constraints its physics requires — a cable length
 * must be positive, a power factor must sit within 0..1 — and shares this one
 * implementation instead of re-deriving the checks per screen.
 */
object NumericInput {

    /**
     * Parses and validates a single numeric field.
     *
     * Checks run in the order a user would encounter them: presence, then
     * format, then engineering range. The first failure is returned so the UI
     * shows one actionable message rather than a stack of them.
     *
     * @param raw the exact text the user typed.
     * @param min inclusive lower bound, or `null` for unbounded.
     * @param max inclusive upper bound, or `null` for unbounded.
     * @param allowZero whether zero is a meaningful value for this quantity.
     * @param allowNegative whether negative values are meaningful.
     */
    fun validate(
        raw: String,
        min: Double? = null,
        max: Double? = null,
        allowZero: Boolean = false,
        allowNegative: Boolean = false,
    ): Outcome<Double> {
        if (raw.isBlank()) return Outcome.Failure(ValidationError.Required)

        val value = NumberFormatter.parseOrNull(raw)
            ?: return Outcome.Failure(ValidationError.NotANumber)

        if (!allowNegative && value < 0.0) {
            return Outcome.Failure(
                if (allowZero) ValidationError.MustNotBeNegative else ValidationError.MustBePositive,
            )
        }
        if (!allowZero && value == 0.0) {
            return Outcome.Failure(ValidationError.MustBePositive)
        }

        if (min != null && max != null && (value < min || value > max)) {
            return Outcome.Failure(ValidationError.OutOfRange(min, max))
        }
        if (min != null && value < min) {
            return Outcome.Failure(ValidationError.BelowMinimum(min))
        }
        if (max != null && value > max) {
            return Outcome.Failure(ValidationError.ExceedsMaximum(max))
        }

        return Outcome.Success(value)
    }

    /**
     * Filters keystrokes for a numeric field, returning the text that should be
     * committed to state.
     *
     * Applied as the user types so impossible input never reaches the field: at
     * most one separator, at most one leading sign. Rejected keystrokes leave
     * [current] unchanged rather than clearing it.
     */
    fun sanitise(current: String, proposed: String, allowNegative: Boolean = false): String {
        if (proposed.isEmpty()) return proposed

        val separatorCount = proposed.count { it == '.' || it == ',' }
        if (separatorCount > 1) return current

        val body = if (allowNegative) proposed.removePrefix("-") else proposed
        if (body.any { !it.isDigit() && it != '.' && it != ',' }) return current
        if (!allowNegative && proposed.startsWith("-")) return current

        return proposed
    }
}
