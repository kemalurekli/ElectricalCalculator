package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle

/**
 * A past calculation, shown compactly on the dashboard.
 *
 * Denser than the history screen's card: the dashboard is a launchpad, so the
 * row carries only what identifies the run — what it was, the headline number,
 * and how long ago — with the result in the tabular numeric style so a column
 * of them stays aligned.
 *
 * Collapsed to a single accessibility node, because a screen-reader user wants
 * "Voltage drop, 5.36 volts, 2 minutes ago" as one item, not three fragments.
 */
@Composable
fun ElecRecentRow(
    title: String,
    summary: String,
    timestamp: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "$title. $summary. $timestamp"
                role = Role.Button
            }
            .clickable(onClick = onClick)
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        ElecIconBadge(icon = icon, containerSize = 36.dp, accent = ElecAccent.SECONDARY)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        Text(
            text = summary,
            style = NumericCompactTextStyle,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ElecRecentRowPreview() {
    ElecToolkitTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ElecRecentRow(
                title = "Voltage drop — 4 mm², 30 m",
                summary = "5.36 V",
                timestamp = "2 minutes ago",
                icon = ElecIcons.History,
                onClick = {},
            )
            ElecRecentRow(
                title = "Cable size — 25 A, 30 m",
                summary = "4 mm²",
                timestamp = "yesterday",
                icon = ElecIcons.History,
                onClick = {},
            )
        }
    }
}
