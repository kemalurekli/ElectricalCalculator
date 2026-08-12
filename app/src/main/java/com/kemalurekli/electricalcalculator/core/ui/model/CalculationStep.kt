package com.kemalurekli.electricalcalculator.core.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/**
 * One line of a worked solution.
 *
 * The calculators already show the formula and the answer. What has been
 * missing is the step between them — the substitution — which is the part that
 * turns a result into something the reader can check, reproduce on paper, and
 * learn from.
 *
 * ```
 * I = P / (k · U · cos φ)      ← formula
 *   = 5.500 / (1,732 × 400 × 0,85)   ← substitution
 *   = 9,34 A                   ← result
 * ```
 *
 * ### Why the strings arrive pre-formatted
 *
 * Every number here has already been through
 * [com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter], so a
 * Turkish reader sees `1.732 × 400` with the separators they expect. Building
 * that in the domain would put display concerns in the use case; building it in
 * the screen would put the formula's structure in Compose. It is built in a
 * feature-level explainer instead — presentation code, but plain Kotlin, so it
 * stays unit-testable with an explicit locale.
 *
 * @param labelRes what this step establishes, translated.
 * @param formula the symbolic form.
 * @param substitution the same expression with the user's numbers in it.
 * @param result the value this step produces, with its unit.
 */
@Immutable
data class CalculationStep(
    @StringRes val labelRes: Int,
    val formula: String,
    val substitution: String,
    val result: String,
)
