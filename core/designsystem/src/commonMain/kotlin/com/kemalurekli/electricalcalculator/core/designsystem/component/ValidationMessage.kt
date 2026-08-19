package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_below_minimum
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_exceeds_maximum
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_must_be_positive
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_must_not_be_negative
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_not_a_number
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_out_of_range
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.validation_required

/**
 * Renders a [ValidationError] as a localised message.
 *
 * This is the single boundary where domain errors become user-facing text. The
 * domain stays free of string resources, and adding a language means editing
 * `strings.xml` only.
 */
@Composable
fun ValidationError.asMessage(): String = when (this) {
    ValidationError.Required -> stringResource(Res.string.validation_required)
    ValidationError.NotANumber -> stringResource(Res.string.validation_not_a_number)
    ValidationError.MustBePositive -> stringResource(Res.string.validation_must_be_positive)
    ValidationError.MustNotBeNegative -> stringResource(Res.string.validation_must_not_be_negative)

    is ValidationError.OutOfRange -> stringResource(
        Res.string.validation_out_of_range,
        formatBound(min),
        formatBound(max),
    )

    is ValidationError.ExceedsMaximum -> stringResource(
        Res.string.validation_exceeds_maximum,
        formatBound(max),
    )

    is ValidationError.BelowMinimum -> stringResource(
        Res.string.validation_below_minimum,
        formatBound(min),
    )
}

/**
 * Formats a numeric bound for the active locale.
 *
 * Bounds are shown with the same separator convention as the input field, so a
 * message never asks for "0.8" while the keyboard produces "0,8".
 *
 * This used to read `LocalConfiguration`, which is Android-only. The separators
 * now come from [currentNumberSymbols], which each platform answers for itself
 * — and which is all the locale was ever consulted for here.
 */
private fun formatBound(value: Double): String =
    NumberFormatter.formatSignificant(
        value = value,
        significantDigits = 4,
        symbols = currentNumberSymbols(),
    )
