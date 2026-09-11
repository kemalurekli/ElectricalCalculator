package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecMotion
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.elecSwap
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
 * ### Why this is not a SegmentedButton
 *
 * It was one, and Material's segmented button forces every option onto a single
 * row with `maxLines = 1`. In English that is fine. In Turkish it is not:
 * "Alüminyum" beside "Bakır", or three supply systems in a row, arrived on
 * screen as "Alümin…" — the reader is asked to choose between options the
 * layout has stopped naming. Ellipsis is acceptable for a summary and never for
 * a choice.
 *
 * Wrapping into a second row instead costs a line of height on the few forms
 * that need it and costs nothing on the rest. The connected-segment look does
 * not survive wrapping, so the options are separate pills — which also drops
 * the selected item's leading checkmark, the most distinctly Material thing in
 * any calculator form, and one more detail that would have had to be explained
 * away on iOS.
 *
 * @param optionLabel maps an option to its display text. Composable so callers
 *   can resolve the label from a string resource, which is what keeps the
 *   option enums themselves free of Android references.
 * @param explanation what the current choice means, in a sentence. Seven
 *   calculators used to draw this themselves as a loose `Text` under the
 *   selector, each with its own padding, and four of them changed it with the
 *   selection. Owning it here makes it part of the control rather than a grey
 *   sentence floating between two fields — and it crossfades when the choice
 *   changes, so the reader sees that the text answered them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ElecOptionSelector(
    label: String,
    options: ImmutableList<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    explanation: String? = null,
) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = spacing.xs),
        )
        // The pills and their explanation are one block, held closer together
        // than the label above them, so the sentence reads as belonging to the
        // choice rather than to the field that follows it.
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    // One group, so a screen reader announces "2 of 3" as the
                    // user moves between options rather than reading three
                    // unrelated buttons.
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                options.forEach { option ->
                    ElecOptionPill(
                        text = optionLabel(option),
                        selected = option == selected,
                        onSelect = { onSelect(option) },
                    )
                }
            }
            if (explanation != null) {
                AnimatedContent(
                    targetState = explanation,
                    transitionSpec = { elecSwap() },
                    label = "optionExplanation",
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = spacing.xs),
                    )
                }
            }
        }
    }
}

/**
 * One choice, as a pill.
 *
 * Internal rather than private so [ElecExplainerCard]'s tabs are drawn by the
 * same code as a calculator's choices. A tab row that merely resembled the
 * pills would drift the first time either was touched.
 */
@Composable
internal fun ElecOptionPill(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    // Animated so a tap reads as the same control changing rather than as two
    // controls swapping places.
    val container by animateColorAsState(
        animationSpec = ElecMotion.react(),
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "optionContainer",
    )
    val content by animateColorAsState(
        animationSpec = ElecMotion.react(),
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "optionContent",
    )

    Surface(
        // `selectable` rather than a Button, so the whole pill is one node with
        // the RadioButton role — which is what this is, however it looks.
        modifier = Modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onSelect,
        ),
        shape = RoundedCornerShape(percent = 50),
        color = container,
        contentColor = content,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            // No maxLines. A label long enough to wrap inside its own pill is
            // still readable; a label cut off is not.
            modifier = Modifier.padding(
                horizontal = spacing.lg,
                vertical = OPTION_VERTICAL_PADDING,
            ),
        )
    }
}

/**
 * Gives the pill a 48dp touch target at the default font scale, which is the
 * accessibility floor. Sized here rather than with a `heightIn` so that a label
 * which does wrap grows the pill instead of overflowing it.
 */
private val OPTION_VERTICAL_PADDING = 14.dp

@Preview(showBackground = true)
@Composable
private fun ElecOptionSelectorPreview() {
    ElecToolkitTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElecOptionSelector(
                label = "Besleme sistemi",
                options = persistentListOf("DC", "1 fazlı AC", "3 fazlı AC"),
                selected = "3 fazlı AC",
                onSelect = {},
                optionLabel = { it },
            )
            ElecOptionSelector(
                label = "İletken",
                options = persistentListOf("Bakır", "Alüminyum"),
                selected = "Bakır",
                onSelect = {},
                optionLabel = { it },
            )
            // The case that motivated the rewrite: four options whose labels
            // cannot share one row at any sensible font size.
            ElecOptionSelector(
                label = "Döşeme yöntemi",
                options = persistentListOf("B1", "B2", "C", "E"),
                selected = "B1",
                onSelect = {},
                optionLabel = { it },
            )
        }
    }
}
