package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericTextStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** How a result reads against its engineering limits. */
enum class ResultTone { NEUTRAL, SUCCESS, WARNING, ERROR }

/** One secondary value shown beneath the headline figure. */
@Immutable
data class ResultRow(
    val label: String,
    val value: String,
    val unit: String,
)

/**
 * The headline output of a calculator.
 *
 * The primary figure uses the tabular numeric style so digits do not shift as
 * the value changes, and the whole card is a polite live region: when a
 * recalculation replaces the result, a screen reader announces the new value
 * instead of leaving the user to discover it.
 *
 * @param tone colours the figure by whether it passes its limits, using the
 *   design system's semantic palette rather than raw colours at the call site.
 * @param statusMessage plain-language verdict, e.g. "Within the 3 % lighting
 *   limit". Shown only when [tone] is not [ResultTone.NEUTRAL].
 */
@Composable
fun ElecResultCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    tone: ResultTone = ResultTone.NEUTRAL,
    statusMessage: String? = null,
    secondaryRows: ImmutableList<ResultRow> = persistentListOf(),
) {
    val spacing = ElecTheme.spacing
    val semantic = ElecTheme.semanticColors

    val accent = when (tone) {
        ResultTone.NEUTRAL -> MaterialTheme.colorScheme.primary
        ResultTone.SUCCESS -> semantic.success
        ResultTone.WARNING -> semantic.warning
        ResultTone.ERROR -> MaterialTheme.colorScheme.error
    }
    val container = when (tone) {
        ResultTone.NEUTRAL -> MaterialTheme.colorScheme.primaryContainer
        ResultTone.SUCCESS -> semantic.successContainer
        ResultTone.WARNING -> semantic.warningContainer
        ResultTone.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = when (tone) {
        ResultTone.NEUTRAL -> MaterialTheme.colorScheme.onPrimaryContainer
        ResultTone.SUCCESS -> semantic.onSuccessContainer
        ResultTone.WARNING -> semantic.onWarningContainer
        ResultTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }

    // Announced as one sentence; without this the figure and its unit are read
    // as separate fragments ("5.17", "V").
    val announcement = buildString {
        append(label)
        append(": ")
        append(value)
        append(' ')
        append(unit)
        statusMessage?.let { append(". ").append(it) }
        secondaryRows.forEach { append(". ").append(it.label).append(": ").append(it.value).append(' ').append(it.unit) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = announcement
            },
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = onContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics {},
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.clearAndSetSemantics {},
            ) {
                Text(text = value, style = NumericTextStyle, color = accent)
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            if (statusMessage != null) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }

            if (secondaryRows.isNotEmpty()) {
                HorizontalDivider(color = onContainer.copy(alpha = DIVIDER_ALPHA))
                secondaryRows.forEach { row ->
                    SecondaryResultRow(row)
                }
            }
        }
    }
}

@Composable
private fun SecondaryResultRow(row: ResultRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${row.value} ${row.unit}",
            style = NumericCompactTextStyle,
            textAlign = TextAlign.End,
            // A weight that does not fill: a figure takes the width it needs
            // and no more, but a row whose value is a phrase rather than a
            // number — "Type A, alongside the charger's own 6 mA detection" —
            // is capped at half the row instead of starving the label down to
            // one letter per line.
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

private const val DIVIDER_ALPHA = 0.2f

@Preview(showBackground = true)
@Composable
private fun ElecResultCardPreview() {
    ElecToolkitTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElecResultCard(
                label = "Voltage drop",
                value = "5.17",
                unit = "V",
                tone = ResultTone.SUCCESS,
                statusMessage = "2.25 % — within the 3 % lighting limit",
                secondaryRows = persistentListOf(
                    ResultRow("Voltage at load", "224.83", "V"),
                    ResultRow("Conductor resistance", "0.1293", "Ω"),
                    ResultRow("Power loss", "103.4", "W"),
                ),
            )
            ElecResultCard(
                label = "Voltage drop",
                value = "18.4",
                unit = "V",
                tone = ResultTone.ERROR,
                statusMessage = "8.00 % — exceeds the 5 % limit",
            )
        }
    }
}

/** Colour helper so callers outside this file can tint text to match a tone. */
@Composable
fun ResultTone.accentColor(): Color = when (this) {
    ResultTone.NEUTRAL -> MaterialTheme.colorScheme.primary
    ResultTone.SUCCESS -> ElecTheme.semanticColors.success
    ResultTone.WARNING -> ElecTheme.semanticColors.warning
    ResultTone.ERROR -> MaterialTheme.colorScheme.error
}
