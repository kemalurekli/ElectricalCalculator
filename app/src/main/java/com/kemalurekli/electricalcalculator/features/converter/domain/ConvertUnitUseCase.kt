package com.kemalurekli.electricalcalculator.features.converter.domain

import javax.inject.Inject

/**
 * Converts a value between two units of the same category.
 *
 * Everything goes through the category's base unit rather than a table of
 * pairwise factors. With `n` units a pairwise table needs `n²` entries that can
 * disagree with one another; going through a base needs `n`, and round-tripping
 * A → base → A is exact by construction.
 */
class ConvertUnitUseCase @Inject constructor() {

    /** Converts [value] from [from] to [to]. */
    operator fun invoke(value: Double, from: MeasurementUnit, to: MeasurementUnit): Double =
        to.scale.fromBase(from.scale.toBase(value))

    /**
     * Converts [value] into every unit of [category] except [from] itself.
     *
     * Offered alongside the single conversion because the question "how big is
     * this" usually has more than one answer worth seeing — a cross-section is
     * a gauge *and* a diameter *and* a kcmil figure, and reading them together
     * is how the three number systems stop being separate.
     */
    fun toAll(
        value: Double,
        from: MeasurementUnit,
        category: UnitCategory,
    ): List<ConvertedValue> = category.units
        .filter { it.key != from.key }
        .map { ConvertedValue(unit = it, value = invoke(value, from, it)) }
}

/** One unit and what the input works out to in it. */
data class ConvertedValue(
    val unit: MeasurementUnit,
    val value: Double,
)
