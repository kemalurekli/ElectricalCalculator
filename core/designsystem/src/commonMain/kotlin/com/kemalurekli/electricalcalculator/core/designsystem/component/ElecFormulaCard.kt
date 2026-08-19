package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.FormulaTextStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_notes_read_more

/** One symbol in a formula, with what it means and the unit it carries. */
@Immutable
data class FormulaVariable(
    val symbol: String,
    val meaning: String,
    val unit: String,
)

/**
 * Shows the equation a calculator applies, with every symbol defined.
 *
 * Collapsed by default: an engineer who trusts the tool should not have to
 * scroll past the derivation to reach the result, but one checking a number
 * against a standard needs it one tap away. The expanded state is remembered
 * with [rememberSaveable] so it survives rotation.
 */
@Composable
fun ElecFormulaCard(
    title: String,
    formula: String,
    variables: ImmutableList<FormulaVariable>,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    // The row itself is the button and carries the label, so
                    // the chevron is decorative.
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = spacing.lg,
                            end = spacing.lg,
                            bottom = spacing.lg,
                        ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = formula,
                            style = FormulaTextStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.md),
                        )
                    }

                    variables.forEach { variable ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        ) {
                            Text(
                                text = variable.symbol,
                                style = FormulaTextStyle,
                                color = MaterialTheme.colorScheme.primary,
                                // Fixed width so every definition's meaning
                                // starts on the same vertical line.
                                modifier = Modifier.width(44.dp),
                            )
                            Text(
                                text = variable.meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = variable.unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A reference topic a note points at, already named for the reader. */
@Immutable
data class NoteLink(val topicKey: String, val label: String)

/**
 * Collapsible block of engineering guidance — assumptions, standards, and the
 * conditions under which a result stops being valid.
 *
 * ### Why the notes end in links
 *
 * A note has room to say *that* a long run makes reactance matter, and no room
 * to say what reactance is. Until the reference library had somewhere to send
 * the reader, that was the end of it. Now each calculator names the topics its
 * caveats come from, so the note is a doorway rather than a dead end — which is
 * the difference between a calculator that warns you and one that teaches you.
 *
 * @param links reference topics worth opening next. Empty is normal.
 * @param onLinkClick opens a topic by its key.
 */
@Composable
fun ElecNotesCard(
    title: String,
    notes: ImmutableList<String>,
    modifier: Modifier = Modifier,
    links: ImmutableList<NoteLink> = persistentListOf(),
    onLinkClick: (String) -> Unit = {},
) {
    val spacing = ElecTheme.spacing
    var expanded by rememberSaveable { mutableStateOf(false) }

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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing.lg, end = spacing.lg, bottom = spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    notes.forEach { note ->
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (links.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.calculator_notes_read_more),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = spacing.xs),
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            links.forEach { link ->
                                AssistChip(
                                    onClick = { onLinkClick(link.topicKey) },
                                    label = { Text(link.label) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElecFormulaCardPreview() {
    ElecToolkitTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElecFormulaCard(
                title = "Formula",
                formula = "ΔU = k · I · R · cos φ\nR = ρ(θ) · L / (A · n)",
                variables = persistentListOf(
                    FormulaVariable("ΔU", "Voltage drop", "V"),
                    FormulaVariable("k", "2 for DC / 1-phase, √3 for 3-phase", "—"),
                    FormulaVariable("I", "Design current", "A"),
                    FormulaVariable("R", "Conductor resistance", "Ω"),
                ),
                initiallyExpanded = true,
            )
        }
    }
}
