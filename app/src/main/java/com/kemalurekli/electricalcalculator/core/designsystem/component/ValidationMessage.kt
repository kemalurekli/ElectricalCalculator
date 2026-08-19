package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols

/**
 * Renders a [ValidationError] as a localised message.
 *
 * This is the single boundary where domain errors become user-facing text. The
 * domain stays free of string resources, and adding a language means editing
 * `strings.xml` only.
 */
@Composable
fun ValidationError.asMessage(): String = when (this) {
    ValidationError.Required -> stringResource(R.string.validation_required)
    ValidationError.NotANumber -> stringResource(R.string.validation_not_a_number)
    ValidationError.MustBePositive -> stringResource(R.string.validation_must_be_positive)
    ValidationError.MustNotBeNegative -> stringResource(R.string.validation_must_not_be_negative)

    is ValidationError.OutOfRange -> stringResource(
        R.string.validation_out_of_range,
        formatBound(min),
        formatBound(max),
    )

    is ValidationError.ExceedsMaximum -> stringResource(
        R.string.validation_exceeds_maximum,
        formatBound(max),
    )

    is ValidationError.BelowMinimum -> stringResource(
        R.string.validation_below_minimum,
        formatBound(min),
    )
}

/**
 * Formats a numeric bound for the active locale.
 *
 * Bounds are shown with the same separator convention as the input field, so a
 * message never asks for "0.8" while the keyboard produces "0,8".
 */
@Composable
private fun formatBound(value: Double): String {
    // LocalConfiguration, not LocalContext.resources: reading through the
    // composition local makes this recompose when the locale changes.
    val locale = LocalConfiguration.current.locales[0]
    return NumberFormatter.formatSignificant(value, significantDigits = 4, symbols = locale.toNumberSymbols())
}
