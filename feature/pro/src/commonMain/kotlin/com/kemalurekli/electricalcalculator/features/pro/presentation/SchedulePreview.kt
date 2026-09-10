package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_caption
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_column_device
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_column_circuit
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_column_current
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_column_section
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_row_heat_pump
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_row_lighting
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_row_oven
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_row_sockets
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_section_lighting
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_section_sockets
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_section_oven
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_section_heat_pump
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_supply
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_title
import org.jetbrains.compose.resources.stringResource

/**
 * The document, drawn rather than described.
 *
 * A paywall that only lists what you get is asking to be believed. This is the
 * thing itself: the schedule's own heading, the supply it was designed against,
 * its columns, and its figures — set in the same monospaced face every number
 * in the app is set in, which is what makes it read as this app's output rather
 * than as a picture of a table.
 *
 * The cross-sections come from resources rather than being written here: a
 * decimal separator is the reader's, and "1,5" set in front of an English
 * reader is the kind of small wrongness this app is careful about everywhere
 * else.
 *
 * ### Why the rows are invented
 *
 * They are a depiction, not the reader's data. Rendering their actual schedule
 * would be more persuasive, but it needs the project carried on the route, and
 * a project with no circuits yet — which is most projects at the moment someone
 * first meets this screen — would have nothing to show. Obviously generic names
 * keep it honest: nobody will mistake "Lighting, Sockets, Oven" for their job.
 *
 * Read as one node by a screen reader. Nine separate cells would be nine swipes
 * through a picture.
 */
@Composable
internal fun SchedulePreview(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing
    val caption = stringResource(Res.string.pro_preview_caption)

    ElecCard(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = caption },
    ) {
        // Tighter than a card the reader is meant to work in. This one is
        // evidence, not content: it has to be legible enough to be believed
        // and small enough that the three things Pro actually gives are not
        // below the fold. All four circuits stay — the range from a 6 A
        // lighting way to a 32 A heat pump is most of what makes it look like
        // a real schedule rather than a mock-up.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.pro_preview_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(Res.string.pro_preview_supply),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = spacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                HeaderCell(stringResource(Res.string.pro_preview_column_circuit), CIRCUIT_WEIGHT)
                HeaderCell(stringResource(Res.string.pro_preview_column_current), FIGURE_WEIGHT)
                HeaderCell(stringResource(Res.string.pro_preview_column_section), FIGURE_WEIGHT)
                HeaderCell(stringResource(Res.string.pro_preview_column_device), FIGURE_WEIGHT)
            }

            ScheduleRow(
                stringResource(Res.string.pro_preview_row_lighting),
                "6",
                stringResource(Res.string.pro_preview_section_lighting),
                "10",
            )
            ScheduleRow(
                stringResource(Res.string.pro_preview_row_sockets),
                "16",
                stringResource(Res.string.pro_preview_section_sockets),
                "20",
            )
            ScheduleRow(
                stringResource(Res.string.pro_preview_row_oven),
                "25",
                stringResource(Res.string.pro_preview_section_oven),
                "32",
            )
            ScheduleRow(
                stringResource(Res.string.pro_preview_row_heat_pump),
                "32",
                stringResource(Res.string.pro_preview_section_heat_pump),
                "40",
            )
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (weight == CIRCUIT_WEIGHT) TextAlign.Start else TextAlign.End,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun ScheduleRow(circuit: String, current: String, section: String, device: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = circuit,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(CIRCUIT_WEIGHT),
        )
        // The figures, in the app's own numeric face. A schedule whose columns
        // do not line up is not a schedule, which is why this face exists.
        Figure(current)
        Figure(section)
        Figure(device)
    }
}

@Composable
private fun RowScope.Figure(value: String) {
    Text(
        text = value,
        style = NumericCompactTextStyle,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End,
        modifier = Modifier.weight(FIGURE_WEIGHT),
    )
}

/** The name column takes what the three figure columns leave. */
private const val CIRCUIT_WEIGHT = 1.6f
private const val FIGURE_WEIGHT = 1f
