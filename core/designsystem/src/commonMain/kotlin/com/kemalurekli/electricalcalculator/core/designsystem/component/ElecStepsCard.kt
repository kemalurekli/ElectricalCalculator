package com.kemalurekli.electricalcalculator.core.designsystem.component

import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.FormulaTextStyle
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_steps
import com.kemalurekli.electricalcalculator.core.designsystem.theme.elecRevealEnter
import com.kemalurekli.electricalcalculator.core.designsystem.theme.elecRevealExit

/**
 * The worked solution: how the answer was reached, line by line.
 *
 * Collapsed by default, like the formula card. A user who trusts the result
 * should not have to scroll past the arithmetic to reach the inputs; a user who
 * does not trust it — or who is learning — is one tap away from every
 * substitution.
 *
 * Each step's expression can be wider than a phone, so the expression block
 * scrolls horizontally rather than wrapping mid-formula, which would break the
 * alignment that makes it readable.
 */
@Composable
fun ElecStepsCard(
    steps: ImmutableList<CalculationStep>,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    if (steps.isEmpty()) return

    val spacing = ElecTheme.spacing
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    ElecCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.calculator_steps),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = if (expanded) ElecIcons.Collapse else ElecIcons.Expand,
                    // The row is the button and carries the label; the chevron
                    // is decorative.
                    contentDescription = null,
                    modifier = Modifier.size(CHEVRON_SIZE),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = elecRevealEnter,
                exit = elecRevealExit,
            ) {
                StepsBody(
                    steps = steps,
                    modifier = Modifier.padding(
                        start = spacing.lg,
                        end = spacing.lg,
                        bottom = spacing.lg,
                    ),
                )
            }
        }
    }
}

/**
 * The worked solution on its own, with no header and no collapsing.
 *
 * Shared with [ElecExplainerCard], which supplies its own header in the form
 * of a tab. Kept here beside [StepBlock] so the two move together.
 */
@Composable
internal fun StepsBody(
    steps: ImmutableList<CalculationStep>,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        steps.forEachIndexed { index, step ->
            if (index > 0) HorizontalDivider()
            StepBlock(step)
        }
    }
}

@Composable
private fun StepBlock(step: CalculationStep) {
    val spacing = ElecTheme.spacing
    val label = stringResource(step.label)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Read as one phrase. Three separate nodes per step would make a
            // six-step solution eighteen swipes to get through.
            .clearAndSetSemantics {
                contentDescription = "$label. ${step.formula}. ${step.substitution}. ${step.result}"
            },
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CORNER),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            // Horizontal scroll rather than wrapping: a formula broken across
            // lines at an arbitrary point is harder to read than one that
            // scrolls.
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = step.formula,
                    style = FormulaTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "= ${step.substitution}",
                    style = FormulaTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "= ${step.result}",
                    style = FormulaTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private val CHEVRON_SIZE = 20.dp
private val CORNER = 10.dp

@Preview(showBackground = true)
@Composable
private fun ElecStepsCardPreview() {
    ElecToolkitTheme {
        ElecStepsCard(
            steps = persistentListOf(
                CalculationStep(
                    label = Res.string.calculator_steps,
                    formula = "P_in = P_out / η",
                    substitution = "5.500 / 0,89",
                    result = "6.179,78 W",
                ),
                CalculationStep(
                    label = Res.string.calculator_steps,
                    formula = "I = P_in / (k · U · cos φ)",
                    substitution = "6.179,78 / (1,732 × 400 × 0,85)",
                    result = "10,50 A",
                ),
            ),
            modifier = Modifier.padding(16.dp),
            initiallyExpanded = true,
        )
    }
}
