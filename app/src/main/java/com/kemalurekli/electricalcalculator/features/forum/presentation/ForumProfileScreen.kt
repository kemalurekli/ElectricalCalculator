package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDate
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTextField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile

@Composable
fun ForumProfileRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOwnProfile by viewModel.isOwnProfile.collectAsStateWithLifecycle()

    ForumProfileScreen(
        uiState = uiState,
        isOwnProfile = isOwnProfile,
        onRename = viewModel::onRename,
        onRetry = viewModel::load,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumProfileScreen(
    uiState: ForumScreenState<ForumProfile>,
    isOwnProfile: Boolean,
    onRename: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = ElecTheme.spacing
    var renaming by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.forum_profile_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ForumStateHost(
            state = uiState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
        ) { profile ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                )

                // Plain label-to-value rows, the same shape the calculator
                // result cards use. Deliberately not badges or levels: the
                // numbers say how much someone has helped, and dressing them
                // up as a game would change what they mean.
                ElecCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        StatLine(
                            label = stringResource(R.string.forum_profile_joined),
                            value = profile.joinedAt.formatAsDate(),
                        )
                        StatLine(
                            label = stringResource(R.string.forum_profile_posts),
                            value = profile.postCount.toString(),
                        )
                        StatLine(
                            label = stringResource(R.string.forum_profile_thanks),
                            value = profile.thanksReceived.toString(),
                        )
                    }
                }

                if (isOwnProfile) {
                    OutlinedButton(onClick = { renaming = true }) {
                        Text(stringResource(R.string.forum_profile_rename))
                    }
                }
            }

            if (renaming) {
                RenameDialog(
                    current = profile.displayName,
                    onConfirm = {
                        onRename(it)
                        renaming = false
                    },
                    onDismiss = { renaming = false },
                )
            }
        }
    }
}

/** Matches the `display_name` limit in the schema. */
private const val DISPLAY_NAME_MAX_LENGTH = 40

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Renaming, for the reader's own profile only.
 *
 * The name Google supplied is a starting point, not a decision. People post
 * under a trade name or an initial and are entitled to.
 */
@Composable
private fun RenameDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forum_profile_rename)) },
        text = {
            ElecTextField(
                value = value,
                onValueChange = { value = it },
                label = stringResource(R.string.forum_profile_display_name),
                maxLength = DISPLAY_NAME_MAX_LENGTH,
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
