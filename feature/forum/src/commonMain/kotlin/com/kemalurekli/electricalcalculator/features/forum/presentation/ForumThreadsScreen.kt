package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_to_post
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_locked
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_pinned
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_unpinned
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_threads_empty_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_threads_empty_title
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForumThreadsRoute(
    categoryId: String,
    categoryTitle: String,
    threadCount: Int = 0,
    onThreadClick: (ForumThread) -> Unit,
    onNewThread: (language: String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ForumThreadsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) { viewModel.onOpen(categoryId, threadCount) }

    // A category belongs to one language's board. If the reader changes the
    // language while reading one, the category they are in stops existing —
    // it is not in the list behind them any more, and asking for its threads
    // in the new language returns none. Leaving is the honest answer; staying
    // would show an empty category the reader could not have got back to.
    var promptingSignIn by rememberSaveable { mutableStateOf(false) }
    val language by viewModel.language.collectAsStateWithLifecycle()
    val openedIn = remember { language }
    LaunchedEffect(language) {
        if (language != openedIn) onNavigateBack?.invoke()
    }

    ForumThreadsScreen(
        title = categoryTitle,
        uiState = uiState,
        onThreadClick = onThreadClick,
        onNewThread = {
            // Offered to everyone; what it does depends on who is asking.
            if (session.userId != null) {
                onNewThread(viewModel.language.value.code)
            } else {
                promptingSignIn = true
            }
        },
        position = position,
        onGoToPage = viewModel::onGoToPage,
        pinned = pinned,
        onTogglePin = viewModel::onTogglePin,
        onRetry = viewModel::onRefresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )

    if (promptingSignIn) {
        ForumSignInPrompt(
            reason = Res.string.forum_sign_in_to_post,
            onDismiss = { promptingSignIn = false },
            onSignedIn = {
                promptingSignIn = false
                onNewThread(viewModel.language.value.code)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumThreadsScreen(
    title: String,
    uiState: ForumScreenState<List<ForumThread>>,
    onThreadClick: (ForumThread) -> Unit,
    onNewThread: () -> Unit = {},
    position: ThreadListPosition = ThreadListPosition(),
    onGoToPage: (Int) -> Unit = {},
    pinned: Set<String> = emptySet(),
    onTogglePin: (String) -> Unit = {},
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
            // Shown whether or not the reader is signed in. It used to appear
            // only for those who could already post, which kept the screen
            // tidy and kept the possibility a secret: the one place that says
            // an account is possible was a section of the settings screen,
            // which someone here to read a thread has no reason to open. The
            // caller decides what the tap means.
            ExtendedFloatingActionButton(
                onClick = onNewThread,
                icon = { Icon(ElecIcons.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.forum_new_thread)) },
            )
        },
    ) { innerPadding ->
        ForumStateHost(
            state = uiState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
            loading = { ForumThreadsSkeleton() },
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
                //
                // Within the page, which is as far as this can reach: the pins
                // live on this device and the pages come from the server, so a
                // pinned thread sitting on page four rises to the top of page
                // four. Lifting it onto page one would mean fetching the
                // pinned threads by id alongside the page and is a separate
                // piece of work.
                val ordered = remember(threads, pinned) {
                    threads.sortedByDescending { it.id in pinned }
                }
                val listState = rememberLazyListState()

                // A page change replaces every row, so the list has to be told
                // to go and look at the top of the new one — otherwise page two
                // opens at whatever offset the reader had scrolled to on
                // page one.
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = spacing.fabClearance),
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

                    // After the twentieth row, not above the first. A reader
                    // arrives at a category to look at threads; a pager offered
                    // before they have seen one is a control for a problem they
                    // do not have yet. It scrolls away with the list for the
                    // same reason.
                    if (position.isPaged) {
                        item(key = "pager") {
                            ForumPager(
                                page = position.page,
                                pageCount = position.pageCount,
                                onGoToPage = { page ->
                                    onGoToPage(page)
                                    scope.launch { listState.scrollToItem(0) }
                                },
                                rule = PagerRule.Above,
                            )
                        }
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
