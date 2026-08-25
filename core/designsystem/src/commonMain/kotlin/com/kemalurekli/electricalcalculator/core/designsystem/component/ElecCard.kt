package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme

/**
 * Base container for grouped content.
 *
 * Uses a tonal surface with no elevation rather than a shadow: at the density
 * of an engineering dashboard, stacked shadows read as noise, while tonal
 * separation stays legible in both light and dark.
 *
 * Tone alone is not enough to carry the edge, though. In the light scheme the
 * page sits at `surface` (#FDFBFF) and the next tonal step up is only about two
 * per cent brighter, so a card drawn on tone alone has no findable boundary and
 * reads as a smudge rather than a container. The hairline outline is what makes
 * the shape deliberate; the tone still does the grouping.
 */
@Composable
fun ElecCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val shape = MaterialTheme.shapes.large
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, border = border) { content() }
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
        ) { content() }
    }
}

/**
 * Large entry card used by the home dashboard.
 *
 * The whole card is one accessibility node reading "title. subtitle", so a
 * screen-reader user hears a single coherent target instead of three
 * fragments — matching how the card behaves for touch.
 */
@Composable
fun ElecDashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: ElecAccent = ElecAccent.PRIMARY,
    badge: String? = null,
) {
    val spacing = ElecTheme.spacing
    ElecCard(
        modifier = modifier.clearAndSetSemantics {
            // The badge carries live state ("3 pinned"), so it belongs in the
            // spoken description rather than being visual-only.
            contentDescription = listOfNotNull(title, badge, subtitle).joinToString(". ")
            role = Role.Button
        },
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElecIconBadge(icon = icon, accent = accent)
                if (badge != null) {
                    ElecPillBadge(text = badge)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                // Leading is tightened from the theme's 24sp. A tile this narrow
                // wraps most titles, and at the body-text leading the two halves
                // of "Electrical Calculators" sit far enough apart to read as
                // two separate labels rather than one wrapped one. The tracking
                // goes to zero for the same reason it exists at display sizes,
                // in reverse: it buys nothing here and costs a line break.
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = TITLE_LINE_HEIGHT,
                        letterSpacing = 0.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = MAX_TITLE_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = SUBTITLE_LINE_HEIGHT,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = MAX_SUBTITLE_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Two lines each.
 *
 * These were a deliberately generous three and four, so that a long translation
 * could take the room it needed rather than be cut. On a two-column grid that
 * traded one problem for a worse one: a card whose subtitle ran to three lines
 * stretched its whole row, and its neighbour became a mostly empty box with the
 * text stranded at the top. Uneven rows read as broken layout, where a trailing
 * ellipsis reads as a summary — which is all a tile's subtitle is. The full text
 * is on the destination it opens.
 */
private const val MAX_TITLE_LINES = 2
private const val MAX_SUBTITLE_LINES = 2

private val TITLE_LINE_HEIGHT = 20.sp
private val SUBTITLE_LINE_HEIGHT = 17.sp

/**
 * Tonal role for an icon badge.
 *
 * Sections are tinted by what they are for — tools, utilities, records,
 * configuration — rather than by arbitrary colour. Drawing only from the
 * Material tonal roles keeps the dashboard varied without it turning into a
 * colour chart, and it recolours correctly under a dynamic-colour theme.
 */
enum class ElecAccent { PRIMARY, SECONDARY, TERTIARY, NEUTRAL }

/**
 * Small pill carrying a short label — a count such as "3 pinned" on the
 * Favourites card, or a property such as a theory topic's level.
 *
 * Given a container of its own rather than being set as loose text. Bare text
 * floating in a card's top corner reads as something that failed to lay out;
 * the pill makes it read as a deliberate piece of status, and gives it a
 * boundary so it does not run into the title beneath it.
 */
@Composable
fun ElecPillBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/**
 * Rounded tinted square holding a leading icon.
 *
 * Marked as decorative (`contentDescription = null`) because the adjacent text
 * already names the item; announcing the icon too would duplicate it.
 */
@Composable
fun ElecIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    accent: ElecAccent = ElecAccent.PRIMARY,
) {
    val container = when (accent) {
        ElecAccent.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        ElecAccent.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        ElecAccent.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
        ElecAccent.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val onContainer = when (accent) {
        ElecAccent.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
        ElecAccent.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
        ElecAccent.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
        ElecAccent.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.size(containerSize),
        shape = RoundedCornerShape(percent = 30),
        color = container,
        contentColor = onContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(containerSize * 0.55f),
            )
        }
    }
}

@Preview(name = "Dashboard card", showBackground = true)
@Preview(name = "Dashboard card (dark)", showBackground = true)
@Composable
private fun ElecDashboardCardPreview() {
    ElecToolkitTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            ElecDashboardCard(
                title = "Electrical Calculators",
                subtitle = "Voltage drop, cable sizing, motor current and more",
                icon = ElecIcons.Calculators,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
