package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme

/**
 * Row used by the calculator, reference and history lists.
 *
 * The favourite toggle is a separate node from the row itself, so a screen
 * reader offers "open" and "add to favorites" as distinct actions rather than
 * one ambiguous target.
 *
 * @param isFavorite when null, no favourite affordance is shown — used by lists
 *   where pinning does not apply.
 * @param contentPadding inset around the row. Defaults to the standard list
 *   inset; callers whose container already applies a screen inset (such as the
 *   home grid) pass zero horizontal padding to avoid doubling it.
 */
@Composable
fun ElecListItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean? = null,
    onToggleFavorite: () -> Unit = {},
    contentPadding: PaddingValues = ElecListItemDefaults.contentPadding,
) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        ElecIconBadge(icon = icon)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isFavorite != null) {
            IconButton(
                onClick = onToggleFavorite,
                // Guarantees the 48dp target the accessibility guidelines
                // require, independent of the icon's own size.
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) ElecIcons.FavoriteOn else ElecIcons.FavoriteOff,
                    contentDescription = stringResource(
                        if (isFavorite) {
                            R.string.action_favorite_remove
                        } else {
                            R.string.action_favorite_add
                        },
                    ),
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/** Defaults for [ElecListItem]. */
object ElecListItemDefaults {
    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
}

/** Section label separating groups within a list. */
@Composable
fun ElecSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.sm)
            .semantics { heading() },
    )
}

@Preview(showBackground = true)
@Composable
private fun ElecListItemPreview() {
    ElecToolkitTheme {
        Column {
            ElecSectionHeader(title = "Cable & Conduit")
            ElecListItem(
                title = "Voltage Drop",
                description = "Volt drop and percentage over a cable run",
                icon = ElecIcons.forCalculator(CalculatorIcon.VOLTAGE_DROP),
                onClick = {},
                isFavorite = true,
            )
        }
    }
}
