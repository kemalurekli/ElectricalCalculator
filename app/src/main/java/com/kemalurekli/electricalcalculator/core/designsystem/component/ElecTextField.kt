package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/**
 * Free text input — the forum's counterpart to [ElecNumericField].
 *
 * The calculators only ever take numbers, so until the forum arrived the app
 * had no reason for this. It is a separate component rather than a flag on
 * [ElecNumericField] because that field's whole purpose is filtering keystrokes
 * through `NumericInput.sanitise`, and a text field that can switch that off is
 * a text field whose most important behaviour is optional.
 *
 * @param maxLength characters accepted. Enforced on input rather than reported
 *   afterwards: a title that is silently truncated on the server is worse than
 *   one that visibly stops growing.
 * @param minLines set above one for a body field, which makes it multi-line and
 *   drops the IME's action key in favour of a newline.
 * @param maxLines the height ceiling. A multi-line field with no ceiling grows
 *   with every newline typed until whatever sits below it — a send button, say —
 *   has been pushed off the screen.
 * @param showCounter display "used / limit" under the field. Worth it where the
 *   limit is close enough to matter: silently refusing the next keystroke tells
 *   the writer their keyboard is broken.
 */
@Composable
fun ElecTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    maxLength: Int? = null,
    minLines: Int = 1,
    maxLines: Int = if (minLines == 1) 1 else minLines + MULTILINE_GROWTH,
    showCounter: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    val counter = maxLength?.takeIf { showCounter }?.let { "${value.length} / $it" }
    OutlinedTextField(
        value = value,
        onValueChange = { proposed ->
            if (maxLength == null || proposed.length <= maxLength) onValueChange(proposed)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        supportingText = (supportingText ?: counter)?.let { helper ->
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = helper, style = MaterialTheme.typography.bodySmall)
                    if (counter != null && supportingText != null) {
                        Text(text = counter, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        isError = isError,
        singleLine = minLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = if (minLines == 1) imeAction else ImeAction.Default,
        ),
    )
}

/**
 * How much a multi-line field may grow past its minimum before it scrolls
 * internally instead of pushing the screen around.
 */
private const val MULTILINE_GROWTH = 4
