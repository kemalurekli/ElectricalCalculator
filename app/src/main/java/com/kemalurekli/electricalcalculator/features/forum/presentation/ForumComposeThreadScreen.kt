package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTextField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/** Opening a thread: a title, and the question itself. */
@Composable
fun ForumComposeThreadRoute(
    onThreadCreated: (threadId: String, title: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumComposeThreadViewModel = hiltViewModel(),
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val body by viewModel.body.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val failed by viewModel.failed.collectAsStateWithLifecycle()
    val created by viewModel.created.collectAsStateWithLifecycle()

    // Navigating on a state change rather than from the button's onClick: the
    // send is asynchronous, and a click handler that navigates optimistically
    // would open a thread that may not exist.
    //
    // Inside a LaunchedEffect because navigation is a side effect, and running
    // it straight from the composable body means it fires again on every
    // recomposition that happens before the state clears.
    LaunchedEffect(created) {
        created?.let { id ->
            onThreadCreated(id, title)
            viewModel.onNavigated()
        }
    }

    ForumComposeThreadScreen(
        title = title,
        body = body,
        sending = sending,
        failed = failed,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onSend = viewModel::onSend,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumComposeThreadScreen(
    title: String,
    body: String,
    sending: Boolean,
    failed: Boolean,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSend: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = ElecTheme.spacing

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.forum_new_thread_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    // In the bar rather than under the fields. At the bottom of
                    // a growing body field it moved down with every newline
                    // typed until it was off the screen — the writer had to
                    // dismiss the keyboard and scroll to find the way to post.
                    TextButton(
                        onClick = onSend,
                        enabled = title.trim().length >= TITLE_MIN_LENGTH &&
                            body.trim().length >= BODY_MIN_LENGTH &&
                            !sending,
                    ) {
                        Text(stringResource(R.string.forum_new_thread_send))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // No imePadding here. The Scaffold's safeDrawing insets
                // already include the keyboard, and adding it again counts the
                // keyboard twice — which pushes the field being typed into off
                // the top of the screen. The text field brings itself into view
                // within this scroll on its own.
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            // The schema requires a title of at least 5 characters and a body
            // of at least 2. Without this the send button enabled on any
            // non-blank input and the constraint refused it server-side, which
            // surfaced as a generic failure.
            val titleTooShort = title.isNotEmpty() && title.trim().length < TITLE_MIN_LENGTH

            ElecTextField(
                value = title,
                onValueChange = onTitleChange,
                label = stringResource(R.string.forum_new_thread_title_hint),
                maxLength = TITLE_MAX_LENGTH,
                showCounter = true,
                isError = titleTooShort,
                supportingText = if (titleTooShort) {
                    stringResource(R.string.forum_title_too_short, TITLE_MIN_LENGTH)
                } else {
                    null
                },
            )

            ElecTextField(
                value = body,
                onValueChange = onBodyChange,
                label = stringResource(R.string.forum_new_thread_body_hint),
                maxLength = BODY_MAX_LENGTH,
                showCounter = true,
                minLines = 6,
                // Stops growing well before it could push the screen around.
                // Past this it scrolls inside itself, like any long document.
                maxLines = 12,
            )

            if (failed) {
                Text(
                    text = stringResource(R.string.forum_new_thread_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

        }
    }
}

/** Matches the schema's checks: title 5–140, body 2–8000. */
private const val TITLE_MIN_LENGTH = 5
private const val TITLE_MAX_LENGTH = 140
private const val BODY_MIN_LENGTH = 2
private const val BODY_MAX_LENGTH = 8000
