package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.foundation.clickable
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_locked
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_pinned
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_unpinned
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_threads_empty_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_threads_empty_title

@Composable
fun ForumThreadsRoute(
    categoryId: String,
    categoryTitle: String,
    onThreadClick: (ForumThread) -> Unit,
    onNewThread: (language: String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ForumThreadsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) { viewModel.onOpen(categoryId) }

    // A category belongs to one language's board. If the reader changes the
    // language while reading one, the category they are in stops existing —
    // it is not in the list behind them any more, and asking for its threads
    // in the new language returns none. Leaving is the honest answer; staying
    // would show an empty category the reader could not have got back to.
    val language by viewModel.language.collectAsStateWithLifecycle()
    val openedIn = remember { language }
    LaunchedEffect(language) {
        if (language != openedIn) onNavigateBack?.invoke()
    }

    ForumThreadsScreen(
        title = categoryTitle,
        uiState = uiState,
        onThreadClick = onThreadClick,
        onNewThread = { onNewThread(viewModel.language.value.code) },
        onLoadMore = viewModel::onLoadMore,
        pinned = pinned,
        onTogglePin = viewModel::onTogglePin,
        canWrite = session.userId != null,
        onRetry = viewModel::onRefresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumThreadsScreen(
    title: String,
    uiState: ForumScreenState<List<ForumThread>>,
    onThreadClick: (ForumThread) -> Unit,
    onNewThread: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    pinned: Set<String> = emptySet(),
    onTogglePin: (String) -> Unit = {},
    canWrite: Boolean = false,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pinnedMessage = stringResource(Res.string.forum_thread_pinned)
    val unpinnedMessage = stringResource(Res.string.forum_thread_unpinned)

    ElecScreenScaffold(
        title = title,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            // Offered only to someone who can actually post. A button that
            // opens a form and then refuses it at the end wastes the typing.
            if (canWrite) {
                ExtendedFloatingActionButton(
                    onClick = onNewThread,
                    icon = { Icon(ElecIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(Res.string.forum_new_thread)) },
                )
            }
        },
    ) { innerPadding ->
        ForumStateHost(
            state = uiState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
        ) { threads ->
            if (threads.isEmpty()) {
                ElecEmptyState(
                    title = stringResource(Res.string.forum_threads_empty_title),
                    message = stringResource(Res.string.forum_threads_empty_message),
                    icon = ElecIcons.Forum,
                )
            } else {
                // Pinned first, the rest in the order the server sent them.
                // A stable sort, so nothing else moves.
                val ordered = remember(threads, pinned) {
                    threads.sortedByDescending { it.id in pinned }
                }
                val listState = rememberLazyListState()
                LoadMoreOnApproachingEnd(listState, ordered.size, onLoadMore)

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(ordered, key = { it.id }) { thread ->
                        ForumThreadRow(
                            thread = thread,
                            isPinned = thread.id in pinned,
                            onClick = { onThreadClick(thread) },
                            onLongClick = {
                                val wasPinned = thread.id in pinned
                                onTogglePin(thread.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (wasPinned) unpinnedMessage else pinnedMessage,
                                    )
                                }
                            },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Who asked, when it last moved, and how much is there.
 *
 * One line rather than a row of chips: this list has to scan like the glossary,
 * and a thread with three separate badges reads as a notification, not an
 * index entry.
 */
/**
 * One thread in the list.
 *
 * No leading icon. Every row would carry the same one, which tells the reader
 * nothing and costs the width that the title needs — a thread list is read by
 * its titles, and the glossary's icons earn their place only because they
 * differ from row to row.
 *
 * The reply count sits on the right, where the eye can run down the column and
 * find the busy threads without reading a word.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ForumThreadRow(
    thread: ForumThread,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    // A list, not a stack of cards. A forum index is read by running down it,
    // and a card around every row turns each one into an island the eye has to
    // enter and leave. Pinned rows are tinted instead of raised.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isPinned) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = PINNED_TINT)
                } else {
                    Color.Transparent
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(
                horizontal = spacing.screenHorizontal,
                vertical = spacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForumAvatar(
            name = thread.authorName,
            userId = thread.authorId,
            size = 36,
            modifier = Modifier.padding(end = spacing.md),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = thread.subtitle(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }

        if (isPinned) {
            Icon(
                imageVector = ElecIcons.Pin,
                contentDescription = stringResource(Res.string.forum_thread_pinned),
                modifier = Modifier
                    .padding(start = spacing.sm)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        if (thread.isLocked) {
            Icon(
                imageVector = ElecIcons.Lock,
                contentDescription = stringResource(Res.string.forum_thread_locked),
                modifier = Modifier
                    .padding(start = spacing.sm)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // A count in a container rather than a bare digit at the margin. On
        // its own the number read as part of the date it sat beside.
        Box(
            modifier = Modifier
                .padding(start = spacing.md)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
        ) {
            Text(
                text = thread.replyCount.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Enough tint to mark the row, not enough to shout. */
private const val PINNED_TINT = 0.4f

/** The meta line: who asked, and when it last moved. */
@Composable
private fun ForumThread.subtitle(): String {
    return "$authorName · ${lastReplyAt.formatAsDateTime()}"
}

/**
 * Calls [onLoadMore] as the reader nears the end of [state]'s list.
 *
 * Three rows early rather than at the very last one, so the next page is on its
 * way before the scroll reaches the bottom and the list does not visibly stop.
 * `derivedStateOf` keeps this from recomposing on every pixel of scroll — the
 * question is only ever "are we close yet", and that answer changes rarely.
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
