package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The example scenarios a calculator can be loaded with.
 *
 * Always visible rather than collapsed behind a chevron: the reader who needs it
 * most is the one who has just opened an unfamiliar calculator and does not yet
 * know there is anything to expand. Two or three short chips cost one line and
 * answer that.
 *
 * [SuggestionChip] rather than [androidx.compose.material3.AssistChip] — these
 * offer a starting point rather than perform an action on what is already there,
 * which is the distinction Material draws between the two.
 */
@Composable
fun <S> ElecExamplesCard(
    examples: ImmutableList<WorkedExample<S>>,
    onSelect: (WorkedExample<S>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (examples.isEmpty()) return

    val spacing = ElecTheme.spacing

    ElecCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.calculator_examples),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.calculator_examples_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                examples.forEach { example ->
                    SuggestionChip(
                        onClick = { onSelect(example) },
                        label = { Text(stringResource(example.titleRes)) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElecExamplesCardPreview() {
    ElecToolkitTheme {
        ElecExamplesCard(
            examples = persistentListOf(
                WorkedExample<Unit>("a", R.string.calculator_examples) { it },
                WorkedExample<Unit>("b", R.string.calculator_examples_hint) { it },
            ),
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
