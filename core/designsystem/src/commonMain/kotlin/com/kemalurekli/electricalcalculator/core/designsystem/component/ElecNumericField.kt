package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle

/**
 * Numeric input used by every calculator form.
 *
 * ### Why this is not an OutlinedTextField
 *
 * It was one, and each field cost about 140dp: a box, a label floating on its
 * border, the value, and a line of help underneath. Four of them filled a phone
 * screen, so a form and the result it produces could never be looked at
 * together — which is the thing somebody actually wants to do, because the
 * whole point of the app is trying a number and seeing what it does.
 *
 * This is a row instead: the name on the left, the figure right-aligned in the
 * same tabular face every other number in the app is set in, its unit after it,
 * and a hairline under the lot. Every value in a form lands on the same
 * vertical line and every unit under the one above it, so a filled-in form
 * reads down its right edge like an instrument rather than across like a
 * questionnaire. It costs about 88dp with a hint and 60dp without.
 *
 * Prose still gets a box — [ElecTextField], for a project's name or a forum
 * post. A box says "write something here"; a rule says "this is a quantity, and
 * it has a value". They are different acts and they should not look the same.
 *
 * The whole row is the touch target, not just the digits, and the rule under it
 * carries the state: it thickens and takes the accent on focus, and turns to
 * the error colour when the value is refused.
 *
 * @param error the validation failure to display, or null when the value is
 *   acceptable. Supplied by the caller rather than computed here so the field
 *   stays stateless and the ViewModel decides *when* to validate — typically on
 *   submit, not on every keystroke.
 * @param errorMessage replaces the generic message for [error] when a
 *   calculator can say something more useful. "Smaller than the conductors
 *   themselves" tells the user what to do; "must be at least 11.28" does not.
 *   Ignored unless [error] is set, so the error state has one source of truth.
 * @param supportingText what to type, in a sentence. Kept — the explaining is
 *   most of what this app is for — but held tight under the row it belongs to
 *   and set quieter than the label, so a form of six fields does not read as
 *   six paragraphs.
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
    val spacing = ElecTheme.spacing
    val message = error?.let { errorMessage ?: it.asMessage() }

    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val ruleColour by animateColorAsState(
        targetValue = when {
            message != null -> MaterialTheme.colorScheme.error
            focused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "fieldRule",
    )
    val ruleWeight by animateDpAsState(
        targetValue = if (focused || message != null) RULE_ACTIVE else RULE_RESTING,
        label = "fieldRuleWeight",
    )

    // Everything a screen reader needs is on the editable node: the label, the
    // hint and the failure. The three Texts around it are cleared, because the
    // row would otherwise be read as four separate items — which is what the
    // old OutlinedTextField's own label and supporting-text slots were quietly
    // preventing.
    val announcement = listOfNotNull(label, message ?: supportingText).joinToString(". ")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ROW_HEIGHT)
                // The row is the target. A 110dp column of digits at the far
                // edge of the screen is a hard thing to hit, and the label is
                // the part the reader is already looking at.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusRequester.requestFocus() }
                .padding(top = spacing.sm, bottom = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = spacing.sm)
                    .clearAndSetSemantics {},
            )

            // A fixed column rather than one that grows with the number, so
            // every value in a form shares a right edge whatever is typed.
            Box(
                modifier = Modifier.width(VALUE_COLUMN),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = EMPTY_MARK,
                        style = NumericCompactTextStyle,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { proposed ->
                        onValueChange(NumericInput.sanitise(value, proposed, allowNegative))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focused = it.isFocused }
                        .semantics {
                            contentDescription = announcement
                            if (message != null) error(message)
                        },
                    textStyle = NumericCompactTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (allowNegative) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        },
                        imeAction = imeAction,
                    ),
                )
            }

            // The column is held whether or not there is a unit in it. A
            // dimensionless field — a power factor, a count — would otherwise
            // let its digits run to the edge and break the line the rest of
            // the form is aligned on.
            Text(
                text = unit.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // A minimum rather than a fixed width: "kvar" and "dm³" are
                // wider than "V", and a unit is never truncated.
                modifier = Modifier
                    .padding(start = spacing.sm)
                    .widthIn(min = UNIT_COLUMN)
                    .clearAndSetSemantics {},
            )
        }

        // Above the rule, not below it. The rule is what separates one field
        // from the next, so a sentence under it belongs to the field that
        // follows — which is the opposite of what it says.
        if (message != null) {
            Row(
                modifier = Modifier
                    .padding(bottom = spacing.xs)
                    .clearAndSetSemantics {},
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Icon(
                    imageVector = ElecIcons.StageFail,
                    contentDescription = null,
                    modifier = Modifier.size(ERROR_GLYPH),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = spacing.xs)
                    .clearAndSetSemantics {},
            )
        }

        HorizontalDivider(thickness = ruleWeight, color = ruleColour)
    }
}

/** Comfortably over the 48dp minimum, since the whole row is the target. */
private val ROW_HEIGHT = 52.dp

/** Seven digits and a separator in the mono face, with room to spare. */
private val VALUE_COLUMN = 104.dp

/** Wide enough for "kvar", which is the longest unit the app prints. */
private val UNIT_COLUMN = 34.dp

private val RULE_RESTING = 1.dp
private val RULE_ACTIVE = 2.dp
private val ERROR_GLYPH = 14.dp

/** An em dash, so an empty field still shows where its value will land. */
private const val EMPTY_MARK = "—"

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
                value = "",
                onValueChange = {},
                label = "Design current",
                unit = "A",
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
