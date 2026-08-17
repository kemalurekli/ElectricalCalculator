package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    created?.let { id ->
        onThreadCreated(id, title)
        viewModel.onNavigated()
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
            ElecTextField(
                value = title,
                onValueChange = onTitleChange,
                label = stringResource(R.string.forum_new_thread_title_hint),
                maxLength = TITLE_MAX_LENGTH,
            )

            ElecTextField(
                value = body,
                onValueChange = onBodyChange,
                label = stringResource(R.string.forum_new_thread_body_hint),
                maxLength = BODY_MAX_LENGTH,
                minLines = 6,
            )

            if (failed) {
                Text(
                    text = stringResource(R.string.forum_new_thread_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onSend,
                enabled = title.isNotBlank() && body.isNotBlank() && !sending,
            ) {
                Text(stringResource(R.string.forum_new_thread_send))
            }
        }
    }
}

/** Matches the schema's limits. */
private const val TITLE_MAX_LENGTH = 140
private const val BODY_MAX_LENGTH = 5000
