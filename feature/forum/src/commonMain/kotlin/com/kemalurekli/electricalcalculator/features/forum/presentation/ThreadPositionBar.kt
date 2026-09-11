package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_first
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_last
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_next
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_number
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_of
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_page_previous

/**
 * The pager: both ends, one step either way, and a way to name a page.
 *
 * ### What a page is here
 *
 * An address, not a container. The list underneath is continuous and a reader
 * scrolls straight through a page boundary; this bar says where they have got
 * to and lets them go somewhere else. That is the difference between a page
 * number that helps — a place you can name, jump to and come back to — and one
 * that stops a read every ten messages to ask permission to carry on.
 *
 * ### Why it is at the top
 *
 * The bottom of this screen is the reply box. Two bars at the one place a thumb
 * rests is one too many, and the one that matters more is the one you write
 * with.
 *
 * ### When it is not there at all
 *
 * A thread that fits on one page has nothing to navigate, and most do.
 */
@Composable
internal fun ThreadPositionBar(
    position: ThreadPosition,
    onGoToPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    var picking by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Step(
                icon = ElecIcons.ToStart,
                label = stringResource(Res.string.forum_page_first),
                enabled = position.page > 1,
                onClick = { onGoToPage(1) },
            )
            Step(
                icon = ElecIcons.Back,
                label = stringResource(Res.string.forum_page_previous),
                enabled = position.page > 1,
                onClick = { onGoToPage(position.page - 1) },
            )
        }

        // The figures in the app's tabular face, so the number does not jitter
        // as the reader scrolls past nine.
        TextButton(onClick = { picking = true }) {
            Text(
                text = stringResource(
                    Res.string.forum_page_of,
                    position.page,
                    position.pageCount,
                ),
                style = NumericCompactTextStyle,
            )
            Icon(
                imageVector = ElecIcons.Expand,
                contentDescription = null,
                modifier = Modifier.size(CHEVRON),
            )

            DropdownMenu(
                expanded = picking,
                onDismissRequest = { picking = false },
                modifier = Modifier.heightIn(max = MENU_HEIGHT),
            ) {
                // Every page, because a picker that offers a window around the
                // current page cannot answer "take me to page ninety" — which
                // is the only question a picker is for.
                (1..position.pageCount).forEach { page ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.forum_page_number, page),
                                style = NumericCompactTextStyle,
                                color = if (page == position.page) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = {
                            picking = false
                            onGoToPage(page)
                        },
                    )
                }
            }
        }

        Row {
            Step(
                icon = ElecIcons.Forward,
                label = stringResource(Res.string.forum_page_next),
                enabled = position.page < position.pageCount,
                onClick = { onGoToPage(position.page + 1) },
            )
            Step(
                icon = ElecIcons.ToEnd,
                label = stringResource(Res.string.forum_page_last),
                enabled = position.page < position.pageCount,
                onClick = { onGoToPage(position.pageCount) },
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/**
 * One of the four jumps.
 *
 * Disabled rather than hidden at the ends: a row of controls that changes width
 * as the reader moves through a thread makes them chase the one they were
 * about to press.
 */
@Composable
private fun Step(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(GLYPH),
        )
    }
}

private val GLYPH = 20.dp
private val CHEVRON = 18.dp
private val MENU_HEIGHT = 320.dp
