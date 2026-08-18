package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTextField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason

@Composable
fun ForumThreadRoute(
    threadId: String,
    threadTitle: String,
    isLocked: Boolean = false,
    threadAuthorId: String = "",
    categoryTitle: String = "",
    onNavigateBack: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ForumThreadViewModel = hiltViewModel(),
    rulesViewModel: ForumRulesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val sendFailed by viewModel.sendFailed.collectAsStateWithLifecycle()
    val rulesAccepted by rulesViewModel.accepted.collectAsStateWithLifecycle()
    val sent by viewModel.sent.collectAsStateWithLifecycle()
    val threadDeleted by viewModel.threadDeleted.collectAsStateWithLifecycle()
    val deleteFailed by viewModel.deleteFailed.collectAsStateWithLifecycle()
    val reported by viewModel.reported.collectAsStateWithLifecycle()
    val blocked by viewModel.blocked.collectAsStateWithLifecycle()

    // The thread is gone, so this screen has nothing left to show.
    LaunchedEffect(threadDeleted) {
        if (threadDeleted) onNavigateBack()
    }
    var showingRules by remember { mutableStateOf(false) }

    LaunchedEffect(threadId) { viewModel.onOpen(threadId) }

    ForumThreadScreen(
        title = threadTitle,
        uiState = uiState,
        isLocked = isLocked,
        threadAuthorId = threadAuthorId,
        categoryTitle = categoryTitle,
        currentUserId = session.userId,
        draft = draft,
        sending = sending,
        sendFailed = sendFailed,
        onDraftChange = viewModel::onDraftChange,
        // The rules come before the first post, not alongside it.
        onSendReply = {
            // `!= true` rather than `== false`: the preference is null until
            // DataStore has been read, and treating that as "already accepted"
            // would let a fast first reply through without the rules ever being
            // shown. Seeing them once more is harmless; skipping them is not.
            if (rulesAccepted != true) showingRules = true else viewModel.onSendReply()
        },
        onToggleThanks = viewModel::onToggleThanks,
        onEditPost = viewModel::onEditPost,
        onDeletePost = viewModel::onDeletePost,
        sent = sent,
        deleteFailed = deleteFailed,
        onDeleteThread = viewModel::onDeleteThread,
        reported = reported,
        blocked = blocked,
        onReport = { postId, reason, note -> viewModel.onReport(postId, reason, note) },
        onBlock = viewModel::onBlock,
        onOpenProfile = onOpenProfile,
        onLoadMore = viewModel::onLoadMore,
        onDeleteFailureShown = viewModel::onDeleteFailureShown,
        onRetry = viewModel::onRefresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )

    if (showingRules) {
        ForumRulesDialog(
            onAccept = {
                rulesViewModel.onAccept()
                showingRules = false
                viewModel.onSendReply()
            },
            onDismiss = { showingRules = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumThreadScreen(
    title: String,
    uiState: ForumScreenState<List<ForumPost>>,
    isLocked: Boolean = false,
    threadAuthorId: String = "",
    categoryTitle: String = "",
    currentUserId: String?,
    draft: String,
    sending: Boolean,
    sendFailed: Boolean,
    onDraftChange: (String) -> Unit,
    onSendReply: () -> Unit,
    onToggleThanks: (ForumPost) -> Unit,
    onEditPost: (String, String) -> Unit,
    onDeletePost: (String) -> Unit,
    sent: Int = 0,
    deleteFailed: Boolean = false,
    reported: Int = 0,
    blocked: Int = 0,
    onReport: (String, ForumReportReason, String) -> Unit = { _, _, _ -> },
    onBlock: (String) -> Unit = {},
    onOpenProfile: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onDeleteThread: () -> Unit = {},
    onDeleteFailureShown: () -> Unit = {},
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmingThreadDelete by remember { mutableStateOf(false) }

    // Derived from what is loaded rather than from a count carried in the
    // route: the route's number was true when the list was drawn, and the
    // server is the one that decides anyway.
    val posts = (uiState as? ForumScreenState.Content)?.value.orEmpty()
    val canDeleteThread = currentUserId != null &&
        threadAuthorId == currentUserId &&
        posts.none { !it.isOpeningPost }

    val sentMessage = stringResource(R.string.forum_reply_sent)

    // Keyed on the counter so the second reply confirms as clearly as the
    // first. Skips zero, which is the state before anything has been sent.
    LaunchedEffect(sent) {
        if (sent > 0) snackbarHostState.showSnackbar(sentMessage)
    }

    val reportedMessage = stringResource(R.string.forum_report_sent)
    LaunchedEffect(reported) {
        if (reported > 0) snackbarHostState.showSnackbar(reportedMessage)
    }

    val blockedMessage = stringResource(R.string.forum_blocked)
    LaunchedEffect(blocked) {
        if (blocked > 0) snackbarHostState.showSnackbar(blockedMessage)
    }

    val deleteFailedMessage = stringResource(R.string.forum_delete_thread_failed)
    LaunchedEffect(deleteFailed) {
        if (deleteFailed) {
            snackbarHostState.showSnackbar(deleteFailedMessage)
            onDeleteFailureShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The bottom is left to the reply box, which is a bottomBar and gets
        // no insets from the Scaffold. Claiming it here as well would pad the
        // list for a keyboard that the bar has already moved above.
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = categoryTitle.ifBlank { stringResource(R.string.destination_forum) },
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    // A thread action belongs to the thread, not to a message
                    // inside it. Hanging it off the opening post meant that
                    // deleting that post left the thread with no menu at all —
                    // the titled shell this was supposed to prevent.
                    if (canDeleteThread) {
                        Box {
                            var menuOpen by remember { mutableStateOf(false) }
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = ElecIcons.More,
                                    contentDescription =
                                        stringResource(R.string.forum_thread_actions),
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.forum_delete_thread),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        confirmingThreadDelete = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            // Only for signed-in readers. Someone who is not signed in is shown
            // nothing here rather than a box that rejects them on submit.
            if (isLocked) {
                // The insert policy refuses posts to a locked thread, so a box
                // here would take what somebody typed and lose it. Saying the
                // thread is closed is the only useful thing left to do.
                Surface(tonalElevation = 2.dp) {
                    Text(
                        text = stringResource(R.string.forum_thread_locked_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                            )
                            .padding(
                                horizontal = spacing.screenHorizontal,
                                vertical = spacing.lg,
                            ),
                    )
                }
            } else if (currentUserId != null) {
                ReplyComposer(
                    draft = draft,
                    sending = sending,
                    failed = sendFailed,
                    onDraftChange = onDraftChange,
                    onSend = onSendReply,
                )
            }
        },
    ) { innerPadding ->
        ForumStateHost(
            state = uiState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
        ) { posts ->
            if (posts.isEmpty()) {
                ElecEmptyState(
                    title = stringResource(R.string.forum_posts_empty_title),
                    message = stringResource(R.string.forum_posts_empty_message),
                    icon = ElecIcons.Forum,
                )
            } else {
                val listState = rememberLazyListState()
                LoadMoreOnApproachingEnd(listState, posts.size, onLoadMore)

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = spacing.xs,
                    ),
                ) {
                    // The title, at the size a title should be and free to
                    // use as many lines as it needs. In the app bar it was one
                    // line of a sentence with the rest replaced by an ellipsis.
                    item(key = "thread-title") {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(
                                horizontal = spacing.screenHorizontal,
                                vertical = spacing.md,
                            ),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            isOwn = post.authorId == currentUserId,
                            canModerate = currentUserId != null,
                            onReport = { reason, note -> onReport(post.id, reason, note) },
                            onBlock = { onBlock(post.authorId) },
                            onOpenProfile = { onOpenProfile(post.authorId) },
                            canThank = currentUserId != null && post.authorId != currentUserId,
                            onToggleThanks = { onToggleThanks(post) },
                            onEdit = { onEditPost(post.id, it) },
                            onDelete = { onDeletePost(post.id) },
                        )
                    }
                }
            }
        }
    }

    if (confirmingThreadDelete) {
        AlertDialog(
            onDismissRequest = { confirmingThreadDelete = false },
            title = { Text(stringResource(R.string.forum_delete_thread_title)) },
            text = { Text(stringResource(R.string.forum_delete_thread_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingThreadDelete = false
                        onDeleteThread()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.forum_delete_thread),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingThreadDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/**
 * One message.
 *
 * The opening post is labelled rather than styled differently. A card with its
 * own colour would read as an announcement; a small caption says the same thing
 * and keeps the thread one conversation.
 */
@Composable
private fun PostCard(
    post: ForumPost,
    isOwn: Boolean,
    canThank: Boolean,
    canModerate: Boolean = false,
    onReport: (ForumReportReason, String) -> Unit = { _, _ -> },
    onBlock: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onToggleThanks: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    var confirmingBlock by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = spacing.screenHorizontal,
                vertical = spacing.md,
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The profile screen has existed since sign-in shipped and was
            // reachable from exactly one place — your own, from Settings. The
            // name is where anyone would look for it.
            Text(
                text = post.authorName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clickable(onClick = onOpenProfile),
            )
            if (post.isOpeningPost) {
                Text(
                    text = stringResource(R.string.forum_opening_post),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = spacing.sm),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = post.createdAt.formatAsDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(text = post.body, style = MaterialTheme.typography.bodyMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thanking and the count are one control, not a button beside a
            // tally that says the same thing twice. Filled once you have
            // thanked, so the state is visible without reading the label.
            if (canThank) {
                val thanked = post.thankedByMe == true
                TextButton(
                    onClick = onToggleThanks,
                    contentPadding = PaddingValues(
                        horizontal = spacing.sm,
                        vertical = 0.dp,
                    ),
                ) {
                    Icon(
                        imageVector = if (thanked) ElecIcons.ThanksFilled else ElecIcons.Thanks,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = if (post.thanksCount > 0) {
                            " ${post.thanksCount}"
                        } else {
                            " " + stringResource(R.string.forum_thanks)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else if (post.thanksCount > 0) {
                // Somebody else's tally, with nothing to press.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ElecIcons.Thanks,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = " ${post.thanksCount}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (post.editedAt != null) {
                Text(
                    text = stringResource(R.string.forum_post_edited),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.sm),
                )
            }

            Spacer(Modifier.weight(1f))

            // Edit and delete behind one control. Two permanent text buttons
            // under every message you wrote turns your own thread into a row
            // of controls with the conversation squeezed between them.
            // The menu used to exist only on your own messages, which left a
            // reader looking at somebody else's with no control at all — and
            // the forum rules promise abusive content is removed.
            if (isOwn || canModerate) {
                Box {
                    var menuOpen by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = ElecIcons.More,
                            contentDescription = stringResource(R.string.forum_post_actions),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        if (!isOwn) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.forum_report)) },
                                onClick = {
                                    menuOpen = false
                                    reporting = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.forum_block)) },
                                onClick = {
                                    menuOpen = false
                                    confirmingBlock = true
                                },
                            )
                        }
                        if (isOwn) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = {
                                menuOpen = false
                                editing = true
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                menuOpen = false
                                confirmingDelete = true
                            },
                        )
                        }
                    }
                }
            }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    if (reporting) {
        ForumReportDialog(
            onConfirm = { reason, note ->
                reporting = false
                onReport(reason, note)
            },
            onDismiss = { reporting = false },
        )
    }

    if (confirmingBlock) {
        AlertDialog(
            onDismissRequest = { confirmingBlock = false },
            title = { Text(stringResource(R.string.forum_block_title, post.authorName)) },
            text = { Text(stringResource(R.string.forum_block_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingBlock = false
                        onBlock()
                    },
                ) {
                    Text(stringResource(R.string.forum_block))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingBlock = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (editing) {
        EditPostDialog(
            current = post.body,
            onConfirm = {
                onEdit(it)
                editing = false
            },
            onDismiss = { editing = false },
        )
    }


    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.forum_delete_post_title)) },
            text = { Text(stringResource(R.string.forum_delete_post_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/**
 * The reply box, pinned under the thread.
 *
 * A bottom bar rather than the last item in the list, because it has to stay
 * above the keyboard while somebody types into it.
 */
@Composable
private fun ReplyComposer(
    draft: String,
    sending: Boolean,
    failed: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Surface(tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // One inset, not two. safeDrawing's bottom is the larger of the
                // navigation bar and the keyboard; chaining imePadding and
                // navigationBarsPadding adds them together instead, leaving a
                // navigation-bar-sized gap under the keyboard.
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            ElecTextField(
                value = draft,
                onValueChange = onDraftChange,
                label = stringResource(R.string.forum_reply_hint),
                maxLength = POST_MAX_LENGTH,
                minLines = 2,
            )
            if (failed) {
                Text(
                    text = stringResource(R.string.forum_reply_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = onSend, enabled = draft.isNotBlank() && !sending) {
                    Text(stringResource(R.string.forum_reply_send))
                }
            }
        }
    }
}

@Composable
private fun EditPostDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_edit)) },
        text = {
            ElecTextField(
                value = value,
                onValueChange = { value = it },
                label = stringResource(R.string.forum_reply_hint),
                maxLength = POST_MAX_LENGTH,
                minLines = 3,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank() && value.trim() != current,
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** Matches the schema's `body` check: 2–8000. */
private const val POST_MAX_LENGTH = 8000
