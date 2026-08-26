package com.kemalurekli.electricalcalculator.core.designsystem.component

import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_examples
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_examples_hint

/**
 * The example scenarios a calculator can be loaded with.
 *
 * Open by default, because the reader who needs it most is the one who has just
 * arrived at an unfamiliar calculator and does not yet know there is anything to
 * expand. Two or three short chips cost one line and answer that.
 *
 * It closes once [hasResult] — and only then. A reader looking at their own
 * figure has plainly found the calculator, and the card sits between that figure
 * and the fields they would go back to change. Nothing is hidden that was not
 * already answered; the heading stays, and one tap brings it back.
 *
 * A tap either way is remembered for as long as the screen is, so the rule never
 * overrides a reader who has said otherwise.
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
    hasResult: Boolean = false,
) {
    if (examples.isEmpty()) return

    val spacing = ElecTheme.spacing
    // Null means "follow the rule"; a value means the reader has decided.
    var chosen by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val expanded = chosen ?: !hasResult

    ElecCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { chosen = !expanded }
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.calculator_examples),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = if (expanded) ElecIcons.Collapse else ElecIcons.Expand,
                    // The row it sits on is one target and already named; a
                    // screen reader gets the state from the expanded semantics.
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Text(
                    text = stringResource(Res.string.calculator_examples_hint),
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
                            label = { Text(stringResource(example.title)) },
                        )
                    }
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
                WorkedExample<Unit>("a", Res.string.calculator_examples) { it },
                WorkedExample<Unit>("b", Res.string.calculator_examples_hint) { it },
            ),
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
