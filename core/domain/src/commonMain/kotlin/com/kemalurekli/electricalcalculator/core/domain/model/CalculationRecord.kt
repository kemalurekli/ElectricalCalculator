package com.kemalurekli.electricalcalculator.core.domain.model

import kotlin.time.Instant

/**
 * A calculation the user has run, as stored in history.
 *
 * [inputs] holds the raw field values keyed by the calculator's own field keys.
 * Keeping the untouched input — rather than only the formatted result — is what
 * makes "recalculate" and "duplicate" possible: a stored record can be loaded
 * straight back into the calculator's form.
 *
 * @param id row identifier; [NO_ID] for a record that has not been saved yet.
 * @param title display name, which the user may rename.
 * @param summary headline result shown in the history list, already formatted.
 * @param results every output value keyed by result key, already formatted.
 */
data class CalculationRecord(
    val id: Long = NO_ID,
    val calculatorId: CalculatorId,
    val title: String,
    val summary: String,
    val inputs: Map<String, String>,
    val results: Map<String, String>,
    val createdAt: Instant,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}
