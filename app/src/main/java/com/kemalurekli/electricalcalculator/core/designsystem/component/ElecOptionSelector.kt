package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Labelled single-choice selector for a small, fixed set of options.
 *
 * Used for the discrete choices a calculator form is built from — supply
 * system, conductor material — where a dropdown would hide the alternatives
 * behind a tap and a radio group would cost far more vertical space.
 *
 * Generic over the option type so call sites pass their own enum directly and
 * the selection stays type-safe.
 *
 * @param optionLabel maps an option to its display text. Composable so callers
 *   can resolve the label from a string resource, which is what keeps the
 *   option enums themselves free of Android references.
 */
@Composable
fun <T> ElecOptionSelector(
    label: String,
    options: ImmutableList<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = spacing.xs),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(
                        text = optionLabel(option),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElecOptionSelectorPreview() {
    ElecToolkitTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElecOptionSelector(
                label = "Supply system",
                options = persistentListOf("DC", "1-phase", "3-phase"),
                selected = "3-phase",
                onSelect = {},
                optionLabel = { it },
            )
            ElecOptionSelector(
                label = "Conductor",
                options = persistentListOf("Copper", "Aluminium"),
                selected = "Copper",
                onSelect = {},
                optionLabel = { it },
            )
        }
    }
}
