package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
 * Shared by the two lists a forum has, which want the same five controls and
 * disagree only about where they sit.
 *
 * ### What a page is in a thread
 *
 * An address, not a container. The list of messages underneath is continuous
 * and a reader scrolls straight through a page boundary; this bar says where
 * they have got to and lets them go somewhere else. That is the difference
 * between a page number that helps — a place you can name, jump to and come
 * back to — and one that stops a read every ten messages to ask permission to
 * carry on.
 *
 * ### What a page is in a list of threads
 *
 * A container, and deliberately. Nobody reads a board index from top to bottom;
 * they scan twenty rows, and either one of them is the thread or the next
 * twenty are. So there the window holds exactly one page and the bar comes
 * after the last row, where a reader who has run out of rows is already
 * looking.
 *
 * ### Where it sits
 *
 * In a thread, at the top: the bottom of that screen is the reply box, and two
 * bars at the one place a thumb rests is one too many. In a list, at the
 * bottom: there is nothing else down there, and a pager above twenty rows
 * would be a control offered before the reader could want it.
 *
 * ### Why a list has three controls and a thread has five
 *
 * A thread can run to twenty pages and the last message is where an argument
 * got to, so jumping to either end is a thing a reader actually wants. A
 * category has two or three pages, and there a jump to the end is the step
 * either way with a different glyph — two controls earning nothing, drawn as
 * stacked chevrons that read as up and down rather than as first and last.
 *
 * ### When it is not there at all
 *
 * Whenever there is one page, which is most threads and most categories.
 */
@Composable
internal fun ForumPager(
    page: Int,
    pageCount: Int,
    onGoToPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
    rule: PagerRule = PagerRule.Below,
    ends: Boolean = true,
) {
    val spacing = ElecTheme.spacing
    var picking by remember { mutableStateOf(false) }

    if (rule == PagerRule.Above) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            if (ends) {
                Step(
                    icon = ElecIcons.ToStart,
                    label = stringResource(Res.string.forum_page_first),
                    enabled = page > 1,
                    onClick = { onGoToPage(1) },
                )
            }
            Step(
                icon = ElecIcons.Back,
                label = stringResource(Res.string.forum_page_previous),
                enabled = page > 1,
                onClick = { onGoToPage(page - 1) },
            )
        }

        // The figures in the app's tabular face, so the number does not jitter
        // as the reader scrolls past nine.
        TextButton(onClick = { picking = true }) {
            Text(
                text = stringResource(
                    Res.string.forum_page_of,
                    page,
                    pageCount,
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
                (1..pageCount).forEach { page ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.forum_page_number, page),
                                style = NumericCompactTextStyle,
                                color = if (page == page) {
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
                enabled = page < pageCount,
                onClick = { onGoToPage(page + 1) },
            )
            if (ends) {
                Step(
                    icon = ElecIcons.ToEnd,
                    label = stringResource(Res.string.forum_page_last),
                    enabled = page < pageCount,
                    onClick = { onGoToPage(pageCount) },
                )
            }
        }
    }

    if (rule == PagerRule.Below) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Which side the hairline goes, which is the side the list is on. */
internal enum class PagerRule { Above, Below }

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

/**
 * Calls [onLoadMore] as the reader nears the end of [state]'s list.
 *
 * Three rows early rather than at the very last one, so the next page is on its
 * way before the scroll reaches the bottom and the list does not visibly stop.
 * `derivedStateOf` keeps this from recomposing on every pixel of scroll — the
 * question is only ever "are we close yet", and that answer changes rarely.
 *
 * Used by threads, not by the list of threads: a thread reads forwards and
 * wants the next ten messages without being asked, while a board index is
 * scanned twenty rows at a time and stops there on purpose.
 */
@Composable
internal fun LoadMoreOnApproachingEnd(
    state: LazyListState,
    itemCount: Int,
    onLoadMore: () -> Unit,
) {
    val shouldLoad by remember(itemCount) {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            itemCount > 0 && last >= itemCount - LOAD_MORE_LEAD
        }
    }

    LaunchedEffect(shouldLoad, itemCount) {
        if (shouldLoad) onLoadMore()
    }
}

private const val LOAD_MORE_LEAD = 3
