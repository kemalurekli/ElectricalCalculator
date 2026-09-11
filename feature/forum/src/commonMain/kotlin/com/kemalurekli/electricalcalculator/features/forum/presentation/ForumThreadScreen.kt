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
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.features.forum.domain.authorLevel
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTextField
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_delete
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_edit
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_save
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.destination_forum
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_author_stats_inline
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_block
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_block_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_block_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_blocked
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_post_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_post_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_thread
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_thread_failed
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_thread_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_thread_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_level_chip
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_opening_post
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_post_actions
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_post_edited
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_posts_empty_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_posts_empty_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_reply_failed
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_reply_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_reply_send
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_reply_sent
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_sent
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thanks
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_actions
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_locked_notice
import androidx.compose.material3.OutlinedButton
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_to_reply
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun ForumThreadRoute(
    threadId: String,
    threadTitle: String,
    isLocked: Boolean = false,
    threadAuthorId: String = "",
    categoryTitle: String = "",
    replyCount: Int = 0,
    onNavigateBack: (() -> Unit)?,
    onOpenProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ForumThreadViewModel = koinViewModel(),
    rulesViewModel: ForumRulesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    var promptingSignIn by rememberSaveable { mutableStateOf(false) }
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val sendFailed by viewModel.sendFailed.collectAsStateWithLifecycle()
    val rulesAccepted by rulesViewModel.accepted.collectAsStateWithLifecycle()
    val sent by viewModel.sent.collectAsStateWithLifecycle()
    val threadDeleted by viewModel.threadDeleted.collectAsStateWithLifecycle()
    val deleteFailed by viewModel.deleteFailed.collectAsStateWithLifecycle()
    val reported by viewModel.reported.collectAsStateWithLifecycle()
    val blocked by viewModel.blocked.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()

    // The thread is gone, so this screen has nothing left to show.
    LaunchedEffect(threadDeleted) {
        if (threadDeleted) onNavigateBack?.invoke()
    }
    var showingRules by remember { mutableStateOf(false) }

    LaunchedEffect(threadId) { viewModel.onOpen(threadId, replyCount) }

    ForumThreadScreen(
        title = threadTitle,
        uiState = uiState,
        isLocked = isLocked,
        threadAuthorId = threadAuthorId,
        categoryTitle = categoryTitle,
        currentUserId = session.userId,
        onSignInToReply = { promptingSignIn = true },
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
        onGoToPage = viewModel::onGoToPage,
        onTopVisible = viewModel::onTopVisible,
        position = position,
        onDeleteFailureShown = viewModel::onDeleteFailureShown,
        onRetry = viewModel::onRefresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )

    if (promptingSignIn) {
        ForumSignInPrompt(
            reason = Res.string.forum_sign_in_to_reply,
            onDismiss = { promptingSignIn = false },
            // Nothing to carry on to: the composer this bar was standing in
            // for takes its place as soon as the session lands, with the
            // cursor where the reader was already looking.
            onSignedIn = { promptingSignIn = false },
        )
    }

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
    onSignInToReply: () -> Unit = {},
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
    onGoToPage: (Int) -> Unit = {},
    onTopVisible: (Int) -> Unit = {},
    position: ThreadPosition = ThreadPosition(),
    onDeleteThread: () -> Unit = {},
    onDeleteFailureShown: () -> Unit = {},
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmingThreadDelete by remember { mutableStateOf(false) }

    // Derived from what is loaded rather than from a count carried in the
    // route: the route's number was true when the list was drawn, and the
    // server is the one that decides anyway.
    val posts = (uiState as? ForumScreenState.Content)?.value.orEmpty()
    val canDeleteThread = currentUserId != null &&
        threadAuthorId == currentUserId &&
        posts.none { !it.isOpeningPost }

    val sentMessage = stringResource(Res.string.forum_reply_sent)

    // Keyed on the counter so the second reply confirms as clearly as the
    // first. Skips zero, which is the state before anything has been sent.
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(sent) {
        if (sent > 0) {
            // The reply is gone and the field is empty; leaving the keyboard up
            // over the thread hides the very message that was just sent.
            focusManager.clearFocus()
            keyboard?.hide()
            snackbarHostState.showSnackbar(sentMessage)
        }
    }

    val reportedMessage = stringResource(Res.string.forum_report_sent)
    LaunchedEffect(reported) {
        if (reported > 0) snackbarHostState.showSnackbar(reportedMessage)
    }

    val blockedMessage = stringResource(Res.string.forum_blocked)
    LaunchedEffect(blocked) {
        if (blocked > 0) snackbarHostState.showSnackbar(blockedMessage)
    }

    val deleteFailedMessage = stringResource(Res.string.forum_delete_thread_failed)
    LaunchedEffect(deleteFailed) {
        if (deleteFailed) {
            snackbarHostState.showSnackbar(deleteFailedMessage)
            onDeleteFailureShown()
        }
    }

    ElecScreenScaffold(
        title = categoryTitle.ifBlank { stringResource(Res.string.destination_forum) },
        modifier = modifier,
        onNavigateBack = onNavigateBack,
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
                                stringResource(Res.string.forum_thread_actions),
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(Res.string.forum_delete_thread),
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
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            // Only for signed-in readers. Someone who is not signed in is shown
            // nothing here rather than a box that rejects them on submit.
            if (isLocked) {
                // The insert policy refuses posts to a locked thread, so a box
                // here would take what somebody typed and lose it. Saying the
                // thread is closed is the only useful thing left to do.
                Surface(tonalElevation = 2.dp) {
                    Text(
                        text = stringResource(Res.string.forum_thread_locked_notice),
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
            } else {
                // Where the composer would be, so a reader can see that
                // replying is something this screen does before finding out
                // whether they are allowed to. Tapping asks them to sign in
                // and says why.
                SignedOutReplyBar(onClick = onSignInToReply)
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
                    title = stringResource(Res.string.forum_posts_empty_title),
                    message = stringResource(Res.string.forum_posts_empty_message),
                    icon = ElecIcons.Forum,
                )
            } else {
                val listState = rememberLazyListState()
                LoadMoreOnApproachingEnd(listState, posts.size, onLoadMore)

                // What the bar reports. The title is an item too, so the first
                // message is at index one and the reader's position is the
                // topmost message rather than the topmost row.
                LaunchedEffect(listState) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { onTopVisible((it - 1).coerceAtLeast(0)) }
                }

                // A jump changes the whole window, so the list has to be told
                // to go and look at it: after a jump to the end the reader
                // wants the bottom of what just arrived, and after a jump to
                // the start, the title.
                val scope = rememberCoroutineScope()

                Column(modifier = Modifier.fillMaxSize()) {
                    if (position.isPaged) {
                        ThreadPositionBar(
                            position = position,
                            // The window is replaced, so the list has to be
                            // told to look at the top of it — otherwise the
                            // reader lands at whatever offset they had scrolled
                            // to on the page they just left.
                            onGoToPage = { page ->
                                onGoToPage(page)
                                scope.launch { listState.scrollToItem(0) }
                            },
                        )
                    }

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
                                    start = spacing.screenHorizontal,
                                    end = spacing.screenHorizontal,
                                    top = spacing.lg,
                                    bottom = spacing.md,
                                ),
                            )
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
    }

    if (confirmingThreadDelete) {
        AlertDialog(
            onDismissRequest = { confirmingThreadDelete = false },
            title = { Text(stringResource(Res.string.forum_delete_thread_title)) },
            text = { Text(stringResource(Res.string.forum_delete_thread_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingThreadDelete = false
                        onDeleteThread()
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.forum_delete_thread),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingThreadDelete = false }) {
                    Text(stringResource(Res.string.action_cancel))
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

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            // Header. The author reads across one line instead of down a rail,
            // which gives the message the full width of the card — the left
            // column was costing a quarter of it and the message is the point.
            Row(verticalAlignment = Alignment.CenterVertically) {
                ForumAvatar(
                    name = post.authorName,
                    userId = post.authorId,
                    size = AVATAR_SIZE,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = spacing.md),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable(onClick = onOpenProfile),
                        )
                        Text(
                            // The caption that used to sit above the rank has
                            // nowhere to go in a single header row, so the
                            // chip carries the word itself. "1 Volt" alone is
                            // a puzzle; "Seviye: 1 Volt" is not.
                            text = stringResource(
                                Res.string.forum_level_chip,
                                stringResource(post.authorLevel.label()),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .padding(start = spacing.sm)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = spacing.sm, vertical = 2.dp),
                        )
                    }

                    Text(
                        text = stringResource(
                            Res.string.forum_author_stats_inline,
                            post.authorPostCount,
                            post.authorThanksReceived,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (post.isOpeningPost) {
                    Text(
                        text = stringResource(Res.string.forum_opening_post),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = spacing.sm),
                    )
                }
            }

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * LINE_HEIGHT_RATIO,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // What was said above the rule, what can be done about it below.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = post.createdAt.formatAsDateTime() +
                        if (post.editedAt != null) {
                            " · " + stringResource(Res.string.forum_post_edited)
                        } else {
                            ""
                        },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

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
                            imageVector = if (thanked) {
                                ElecIcons.ThanksFilled
                            } else {
                                ElecIcons.Thanks
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (post.thanksCount > 0) {
                                " ${post.thanksCount}"
                            } else {
                                " " + stringResource(Res.string.forum_thanks)
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                } else if (post.thanksCount > 0) {
                    Icon(
                        imageVector = ElecIcons.Thanks,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = " ${post.thanksCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (isOwn || canModerate) {
                    Box {
                        var menuOpen by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { menuOpen = true },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = ElecIcons.More,
                                contentDescription =
                                    stringResource(Res.string.forum_post_actions),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                        ) {
                            if (!isOwn) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.forum_report)) },
                                    onClick = {
                                        menuOpen = false
                                        reporting = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.forum_block)) },
                                    onClick = {
                                        menuOpen = false
                                        confirmingBlock = true
                                    },
                                )
                            }
                            if (isOwn) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.action_edit)) },
                                    onClick = {
                                        menuOpen = false
                                        editing = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(Res.string.action_delete),
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
    }

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
            title = { Text(stringResource(Res.string.forum_block_title, post.authorName)) },
            text = { Text(stringResource(Res.string.forum_block_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingBlock = false
                        onBlock()
                    },
                ) {
                    Text(stringResource(Res.string.forum_block))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingBlock = false }) {
                    Text(stringResource(Res.string.action_cancel))
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
            title = { Text(stringResource(Res.string.forum_delete_post_title)) },
            text = { Text(stringResource(Res.string.forum_delete_post_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

/**
 * The reply box, pinned under the thread.
 *
 * The send control lives inside the field rather than on a row of its own.
 * A separate button cost a whole line of a screen that is mostly keyboard once
 * anybody starts typing, and it sat far from the text it applies to.
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

    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.length <= POST_MAX_LENGTH) onDraftChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.forum_reply_hint)) },
                trailingIcon = {
                    IconButton(onClick = onSend, enabled = draft.isNotBlank() && !sending) {
                        Icon(
                            imageVector = ElecIcons.Send,
                            contentDescription = stringResource(Res.string.forum_reply_send),
                            tint = if (draft.isNotBlank() && !sending) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
                // Grows to four lines and then scrolls inside itself. Past that
                // it would be eating the thread it is a reply to.
                minLines = 1,
                maxLines = 4,
                shape = MaterialTheme.shapes.extraLarge,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )

            if (failed) {
                Text(
                    text = stringResource(Res.string.forum_reply_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = spacing.xs),
                )
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
        title = { Text(stringResource(Res.string.action_edit)) },
        text = {
            ElecTextField(
                value = value,
                onValueChange = { value = it },
                label = stringResource(Res.string.forum_reply_hint),
                maxLength = POST_MAX_LENGTH,
                minLines = 3,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank() && value.trim() != current,
            ) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

/** Matches the schema's `body` check: 2–8000. */
private const val POST_MAX_LENGTH = 8000

/** Roughly 1.5×, which is where long prose stops feeling packed. */
private const val LINE_HEIGHT_RATIO = 1.5f

/** Small enough to caption the name rather than compete with it. */
private const val AVATAR_SIZE = 32

/**
 * The composer's place, held by an invitation rather than by nothing.
 *
 * Built from the same `Surface` and insets as [ReplyComposer] so the bar does
 * not move when the reader signs in: what they tapped becomes what they type
 * in, in the same spot.
 */
@Composable
private fun SignedOutReplyBar(onClick: () -> Unit) {
    val spacing = ElecTheme.spacing

    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        ) {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.forum_reply_hint))
            }
        }
    }
}
