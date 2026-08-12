package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme

/**
 * One editable "cable diameter × quantity" row of a bundle.
 *
 * Shared by every containment calculator: a conduit and a cable tray are asked
 * the same question about their contents, and one implementation keeps the two
 * forms behaving identically as they are refined.
 *
 * Deliberately takes plain values rather than a state type, so each feature owns
 * its own row state and this stays a piece of layout rather than a piece of
 * architecture.
 *
 * @param canRemove false when this is the last row. The button stays visible but
 *   disabled, so the row does not change shape as rows come and go.
 * @param isLast drives the IME action: the final row completes the form.
 */
@Composable
fun ElecCableRow(
    diameter: String,
    quantity: String,
    onDiameterChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    diameterError: ValidationError? = null,
    quantityError: ValidationError? = null,
    canRemove: Boolean = true,
    isLast: Boolean = true,
) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        // Top rather than centre: a validation message grows one field only,
        // and the fields should stay aligned when it does.
        verticalAlignment = Alignment.Top,
    ) {
        ElecNumericField(
            value = diameter,
            onValueChange = onDiameterChange,
            label = stringResource(R.string.cable_row_diameter),
            modifier = Modifier.weight(DIAMETER_WEIGHT),
            unit = "mm",
            error = diameterError,
        )
        ElecNumericField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = stringResource(R.string.cable_row_quantity),
            modifier = Modifier.weight(QUANTITY_WEIGHT),
            error = quantityError,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        )
        IconButton(
            onClick = onRemove,
            enabled = canRemove,
            modifier = Modifier.padding(top = spacing.xs),
        ) {
            Icon(
                imageVector = ElecIcons.Remove,
                contentDescription = stringResource(R.string.cable_row_remove),
            )
        }
    }
}

private const val DIAMETER_WEIGHT = 2f
private const val QUANTITY_WEIGHT = 1f

@Preview(showBackground = true)
@Composable
private fun ElecCableRowPreview() {
    ElecToolkitTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ElecCableRow(
                diameter = "8.5",
                quantity = "3",
                onDiameterChange = {},
                onQuantityChange = {},
                onRemove = {},
                isLast = false,
            )
            ElecCableRow(
                diameter = "",
                quantity = "1",
                onDiameterChange = {},
                onQuantityChange = {},
                onRemove = {},
                diameterError = ValidationError.Required,
                canRemove = false,
            )
        }
    }
}
