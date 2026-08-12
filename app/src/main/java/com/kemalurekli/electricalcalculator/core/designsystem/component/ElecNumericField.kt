package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme

/**
 * Numeric input used by every calculator form.
 *
 * Centralising it fixes two things in one place for every calculator:
 * keystrokes are filtered through [NumericInput.sanitise], so characters that
 * could never form a number never reach the caller's state; and the unit symbol
 * and validation message are rendered through the text field's own suffix and
 * supporting-text slots, which keeps them inside the field's accessibility node.
 *
 * @param error the validation failure to display, or null when the value is
 *   acceptable. Supplied by the caller rather than computed here so the field
 *   stays stateless and the ViewModel decides *when* to validate — typically on
 *   submit, not on every keystroke.
 * @param errorMessage replaces the generic message for [error] when a
 *   calculator can say something more useful. "Smaller than the conductors
 *   themselves" tells the user what to do; "must be at least 11.28" does not.
 *   Ignored unless [error] is set, so the error state has one source of truth.
 */
@Composable
fun ElecNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    error: ValidationError? = null,
    errorMessage: String? = null,
    supportingText: String? = null,
    allowNegative: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    val message = error?.let { errorMessage ?: it.asMessage() }

    // Only one line of helper text is shown: an error replaces the hint, so the
    // field's height stays stable as validation state changes.
    val helper = message ?: supportingText

    OutlinedTextField(
        value = value,
        onValueChange = { proposed ->
            onValueChange(NumericInput.sanitise(value, proposed, allowNegative))
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        suffix = unit?.let { { Text(text = it) } },
        // The supporting-text slot rather than a Text of our own: Material
        // wires it into the field's own semantics node, so TalkBack announces
        // the error when focus lands instead of leaving it as a stray label.
        supportingText = helper?.let {
            { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        },
        isError = error != null,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowNegative) {
                KeyboardType.Number
            } else {
                KeyboardType.Decimal
            },
            imeAction = imeAction,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ElecNumericFieldPreview() {
    ElecToolkitTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ElecNumericField(
                value = "230",
                onValueChange = {},
                label = "System voltage",
                unit = "V",
                supportingText = "Line-to-line for three phase",
            )
            ElecNumericField(
                value = "0",
                onValueChange = {},
                label = "Cable length",
                unit = "m",
                error = ValidationError.MustBePositive,
            )
        }
    }
}
