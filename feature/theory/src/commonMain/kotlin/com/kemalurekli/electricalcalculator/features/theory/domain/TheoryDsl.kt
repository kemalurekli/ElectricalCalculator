package com.kemalurekli.electricalcalculator.features.theory.domain

import org.jetbrains.compose.resources.StringResource

/**
 * Shorthand for writing solvers.
 *
 * A derivation written out in full constructor calls buries the arithmetic in
 * type names — the interesting part of a step is `I = U / R`, not that a
 * `TheoryNumber` is being constructed around it. These three functions exist so
 * that a solver body reads as the maths it is.
 *
 * Deliberately not part of the public model: they are an authoring convenience
 * for `TheorySolvers.kt` and nothing else consumes them.
 */

/** A bare number, as it appears inside a substitution. */
internal fun n(
    value: Double,
    unit: String = "",
    decimals: Int = 2,
    style: NumberStyle = NumberStyle.FIXED,
) = TheoryNumber(value = value, unit = unit, decimals = decimals, style = style)

/** A number the app reports under a label, in significant figures. */
internal fun sig(
    value: Double,
    unit: String = "",
    digits: Int = 4,
) = TheoryNumber(value = value, unit = unit, decimals = digits, style = NumberStyle.SIGNIFICANT)

/** A number whose magnitude varies enough that it needs an SI prefix. */
internal fun si(
    value: Double,
    unit: String,
    digits: Int = 4,
) = TheoryNumber(value = value, unit = unit, decimals = digits, style = NumberStyle.SI_PREFIX)

/** A reported quantity: a label and a number. */
internal fun q(
    label: StringResource,
    value: Double,
    unit: String = "",
    decimals: Int = 2,
    style: NumberStyle = NumberStyle.FIXED,
) = TheoryQuantity(label, TheoryNumber(value, unit, decimals, style))

/** A reported quantity built from an already-shaped number. */
internal fun q(label: StringResource, number: TheoryNumber) = TheoryQuantity(label, number)

/**
 * One line of a derivation.
 *
 * The `vararg operands` sit between the substitution and the result so a call
 * reads in the order the line is read: what it establishes, the symbolic form,
 * the shape of the substitution, the numbers that go in it, the answer.
 */
internal fun step(
    label: StringResource,
    formula: String,
    substitution: String,
    vararg operands: TheoryNumber,
    result: TheoryNumber,
) = TheoryStep(
    label = label,
    formula = formula,
    substitution = substitution,
    operands = operands.toList(),
    result = result,
)
