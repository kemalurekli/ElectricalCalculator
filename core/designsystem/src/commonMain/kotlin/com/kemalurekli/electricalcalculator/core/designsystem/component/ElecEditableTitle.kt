package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow

/**
 * The name of the thing the screen is about, edited where it is displayed.
 *
 * A project and a circuit are the two things in this app the user names, and
 * both used to hold the name in a text field halfway down a form while the bar
 * above showed "Untitled project" — the placeholder for a value sitting on the
 * same screen. Two places for one fact, and the one you read was not the one you
 * could change.
 *
 * Set at the bar's own size and weight, so it reads as a title until it is
 * touched. The keyboard's action is Done rather than Next: this is the whole
 * field, and there is nowhere to go next.
 *
 * @param label what the field is called, for a screen reader. Not drawn — the
 *   bar has no room for a caption and the placeholder covers the sighted case.
 */
@Composable
fun ElecEditableTitle(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.titleLarge
    val content = LocalContentColor.current

    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                // A bare text field in the bar has no name for a screen reader
                // to announce. The label the field used to carry in the form is
                // what it is called, so it stays — as the field's name rather
                // than as a caption nobody needed to see.
                .semantics { contentDescription = label },
            textStyle = style.copy(color = content),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
        )
    }
}
