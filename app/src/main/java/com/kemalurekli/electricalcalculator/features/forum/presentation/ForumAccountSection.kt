package com.kemalurekli.electricalcalculator.features.forum.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession

/**
 * The forum account, as it appears in Settings.
 *
 * It lives here rather than in the settings feature because everything it
 * touches is the forum's. Settings drops it in and passes nothing: the section
 * holds its own ViewModel, so adding sign-in did not lengthen `SettingsScreen`'s
 * parameter list by six callbacks for a feature the other settings know nothing
 * about.
 */
@Composable
fun ForumAccountSection(
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumAccountViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val failure by viewModel.failure.collectAsStateWithLifecycle()
    val deleteFailed by viewModel.deleteFailed.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing
    val context = LocalContext.current
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        when (val current = session) {
            // Nothing at all while the stored session is being read. A sign-in
            // button that appears and then withdraws itself on every launch is
            // worse than a moment of empty space.
            is ForumSession.Unknown -> Unit

            is ForumSession.SignedOut -> {
                Text(
                    text = stringResource(R.string.forum_signed_out_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { context.findActivity()?.let(viewModel::onSignIn) },
                    enabled = !busy && viewModel.canSignIn,
                ) {
                    Text(stringResource(R.string.forum_sign_in))
                }
            }

            is ForumSession.SignedIn -> {
                Text(
                    text = stringResource(
                        R.string.forum_signed_in_as,
                        current.profile.displayName,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = { onOpenProfile(current.profile.id) }) {
                    Text(stringResource(R.string.forum_view_profile))
                }
                OutlinedButton(onClick = viewModel::onSignOut, enabled = !busy) {
                    Text(stringResource(R.string.forum_sign_out))
                }
                OutlinedButton(
                    onClick = { confirmingDelete = true },
                    enabled = !busy,
                ) {
                    Text(
                        text = stringResource(R.string.forum_delete_account),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        failure?.messageRes()?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (deleteFailed) {
            Text(
                text = stringResource(R.string.forum_delete_account_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.forum_delete_account_title)) },
            text = { Text(stringResource(R.string.forum_delete_account_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.onDeleteAccount()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.forum_delete_account_confirm),
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
 * The message for a failed sign-in, or null when there is nothing to say.
 *
 * Cancelling returns null on purpose. Dismissing the Google sheet is a
 * decision, and reporting it back as an error tells the reader they did
 * something wrong when they did not.
 */
private fun ForumAuthFailure.messageRes(): Int? = when (this) {
    ForumAuthFailure.CANCELLED -> null
    ForumAuthFailure.NO_ACCOUNT -> R.string.forum_sign_in_failed_no_account
    ForumAuthFailure.NO_CONNECTION -> R.string.forum_sign_in_failed_offline
    ForumAuthFailure.NOT_CONFIGURED -> R.string.forum_sign_in_failed_unconfigured
    ForumAuthFailure.UNKNOWN -> R.string.forum_sign_in_failed
}

/**
 * Credential Manager needs the Activity, not the application context — it has
 * to put a sheet on top of something.
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
