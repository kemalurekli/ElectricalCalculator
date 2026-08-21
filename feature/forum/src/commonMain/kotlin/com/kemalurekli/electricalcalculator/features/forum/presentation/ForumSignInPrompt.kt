package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_apple
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_signed_out_summary
import com.kemalurekli.electricalcalculator.features.forum.auth.SignInProvider
import com.kemalurekli.electricalcalculator.features.forum.auth.rememberForumSignIn
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_email
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement

/**
 * Asks a signed-out reader to sign in, at the moment they reached for
 * something that needs it.
 *
 * The forum shows the button to write before it knows whether the reader can.
 * Hiding it kept the app tidy and kept the possibility a secret: someone who
 * has only ever read has no way to learn that asking is an option, because the
 * only place that says so is a section of the settings screen.
 *
 * So [reason] is not decoration. The reader tapped a specific thing, and the
 * first line of this names it — "Sign in to reply", not "Sign in". A prompt
 * that appears without saying what prompted it is the part of this pattern
 * that turns it into a bait.
 *
 * Reuses [ForumAccountViewModel], which is the same sign-in the settings screen
 * runs. There is one way into an account, and it is worth exactly one
 * implementation.
 */
@Composable
fun ForumSignInPrompt(
    reason: StringResource,
    onDismiss: () -> Unit,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumAccountViewModel = koinViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val failure by viewModel.failure.collectAsStateWithLifecycle()
    val signIn = rememberForumSignIn()

    // The dialog opens small, offering the one-tap route. The email route is a
    // field and three buttons, and putting it in front of everyone would make
    // the common case read like a form.
    var usingEmail by rememberSaveable { mutableStateOf(false) }

    // The point of the prompt is what comes after it. Once the session lands,
    // the reader is taken on to what they were trying to do rather than back
    // to the screen they tapped from, having to find the button again.
    LaunchedEffect(session) {
        if (session is ForumSession.SignedIn) onSignedIn()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(reason)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ElecTheme.spacing.md)) {
                Text(
                    text = failure?.message()?.let { stringResource(it) }
                        ?: stringResource(Res.string.forum_signed_out_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (failure?.message() != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (usingEmail) {
                    ForumEmailSignIn(viewModel)
                } else {
                    TextButton(onClick = { usingEmail = true }) {
                        Text(stringResource(Res.string.forum_sign_in_email))
                    }
                }
            }
        },
        confirmButton = {
            if (!usingEmail) {
                TextButton(
                    onClick = { viewModel.onSignIn(signIn) },
                    enabled = !busy && viewModel.canSignIn(signIn),
                ) {
                    Text(
                        stringResource(
                            when (signIn.provider) {
                                SignInProvider.GOOGLE -> Res.string.forum_sign_in
                                SignInProvider.APPLE -> Res.string.forum_sign_in_apple
                            },
                        ),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}
