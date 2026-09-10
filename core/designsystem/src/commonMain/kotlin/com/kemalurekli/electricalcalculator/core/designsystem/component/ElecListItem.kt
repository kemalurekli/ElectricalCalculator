package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

/**
 * Row used by the calculator, reference and history lists.
 *
 * The favourite toggle is a separate node from the row itself, so a screen
 * reader offers "open" and "add to favorites" as distinct actions rather than
 * one ambiguous target.
 *
 * ### What goes in the leading slot
 *
 * The icon used to be required, and three lists paid for that: references drew
 * twenty identical books, theory twenty identical sigmas, projects a column of
 * folders. A glyph that is the same on every row is not an icon, it is an
 * indent — it costs 56dp of every row and tells the reader nothing they did not
 * already know from the heading above.
 *
 * So the [icon] is optional now, and a list that has nothing to say there says
 * nothing: the titles move left and the rows get shorter.
 *
 * Where a row does carry one, it is a bare glyph rather than the rounded tinted
 * square [ElecIconBadge] draws. The square is what a card gets — on the
 * dashboard the tint is doing work, since it says which of the app's three
 * territories a section belongs to. A row is a doorway, and twenty coloured
 * plaques down the left of a list is the single most template-looking thing a
 * list can do. The slot keeps its width so every title still starts at the
 * same x.
 *
 * A property worth reading goes in [badge] at the trailing edge instead of the
 * leading one, so that every title in the list still starts at the same place.
 * A leading pill would be as wide as its longest word — "Intermediate" against
 * "Basic" — and leave the column ragged.
 *
 * @param badge short label at the trailing edge, such as a theory topic's level.
 * @param isFavorite when null, no favourite affordance is shown — used by lists
 *   where pinning does not apply.
 * @param caption a third line under the description, quieter than it. For a
 *   provenance rather than a sentence: which standard a reference is transcribed
 *   from, which is what a reader picks between when the titles all sound equally
 *   plausible.
 * @param contentPadding inset around the row. Defaults to the standard list
 *   inset; callers whose container already applies a screen inset (such as the
 *   home grid) pass zero horizontal padding to avoid doubling it.
 */
@Composable
fun ElecListItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badge: String? = null,
    caption: String? = null,
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
        if (icon != null) {
            Box(
                modifier = Modifier.size(ElecListItemDefaults.LeadingSlot),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    // The title beside it already names the row.
                    contentDescription = null,
                    modifier = Modifier.size(ElecListItemDefaults.LeadingGlyph),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

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
            if (caption != null) {
                Text(
                    text = caption,
                    // Quieter than the description above it, not louder. It is
                    // there to be found when a reader is choosing between two
                    // plausible titles, not to be read on the way past.
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = spacing.xxs),
                )
            }
        }

        if (badge != null) {
            ElecPillBadge(text = badge)
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
                            Res.string.action_favorite_remove
                        } else {
                            Res.string.action_favorite_add
                        },
                    ),
                    // Smaller than the icon default. The 48dp touch target is
                    // set on the button above, so the glyph is free to be sized
                    // for its importance instead of for reachability — a pin
                    // toggle is secondary to the row it sits on, and at the
                    // default size a column of filled stars outshouts every
                    // title beside it.
                    modifier = Modifier.size(20.dp),
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        // Dimmed. Twenty-two identical outlines down the right
                        // edge of the calculator index read as a second column
                        // of content rather than as a control that is off. At
                        // 0.7 it is still 3.9:1 on the light surface and 5.7:1
                        // on the dark, so it stays a control you can see.
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = UNSET_STAR_ALPHA)
                    },
                )
            }
        }
    }
}

/**
 * The hairline between two rows.
 *
 * Drawn *between* rows and never after the last one in a group: a rule under
 * the final item is a line under nothing, and where a section heading follows
 * it, it fences the heading to the group above rather than the one below.
 *
 * It exists because the lists disagreed. Some drew a divider, some did not, and
 * the ones that did not were the ones that had a leading glyph on every row
 * holding the rows apart. Take the glyph away — as references and theory now do
 * — and the rows run together.
 */
@Composable
fun ElecListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/** How present an unset favourite star is against the row it sits on. */
private const val UNSET_STAR_ALPHA = 0.7f

/** Defaults for [ElecListItem]. */
object ElecListItemDefaults {
    val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

    /**
     * The leading slot keeps the width the tinted badge used to occupy, so
     * dropping the badge did not move every title in the app four pixels left.
     */
    val LeadingSlot: Dp = 40.dp

    /** The glyph inside it, at the size Material draws an icon unaided. */
    val LeadingGlyph: Dp = 24.dp
}

/**
 * Section label separating groups within a list.
 *
 * Rendered in the ordinary text colour rather than the primary accent. A
 * heading is structure, not emphasis: it should be found when the eye is
 * looking for it and recede when it is not. Tinting every heading with the
 * brand colour puts a dozen high-contrast blue anchors on a screen and makes
 * the labels compete with the content they are supposed to organise.
 */
@Composable
fun ElecSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
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
