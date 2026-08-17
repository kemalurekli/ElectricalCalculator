package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread

@Composable
fun ForumThreadsRoute(
    categoryId: String,
    categoryTitle: String,
    onThreadClick: (ForumThread) -> Unit,
    onNewThread: (language: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumThreadsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) { viewModel.onOpen(categoryId) }

    ForumThreadsScreen(
        title = categoryTitle,
        uiState = uiState,
        onThreadClick = onThreadClick,
        onNewThread = { onNewThread(viewModel.language.code) },
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
    canWrite: Boolean = false,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // The title came with the route, so the bar is right from the first
            // frame rather than filling in after the request returns.
            ElecTopAppBar(
                title = title,
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            // Offered only to someone who can actually post. A button that
            // opens a form and then refuses it at the end wastes the typing.
            if (canWrite) {
                ExtendedFloatingActionButton(
                    onClick = onNewThread,
                    icon = { Icon(ElecIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.forum_new_thread)) },
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
                    title = stringResource(R.string.forum_threads_empty_title),
                    message = stringResource(R.string.forum_threads_empty_message),
                    icon = ElecIcons.Forum,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(threads, key = { it.id }) { thread ->
                        ElecListItem(
                            title = thread.title,
                            description = thread.subtitle(),
                            icon = ElecIcons.Forum,
                            onClick = { onThreadClick(thread) },
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
@Composable
private fun ForumThread.subtitle(): String {
    val replies = when (replyCount) {
        0 -> stringResource(R.string.forum_thread_replies_none)
        1 -> stringResource(R.string.forum_thread_replies_one)
        else -> stringResource(R.string.forum_thread_replies, replyCount)
    }
    val locked = if (isLocked) " · " + stringResource(R.string.forum_thread_locked) else ""
    return "$authorName · $replies · ${lastReplyAt.formatAsDateTime()}$locked"
}
