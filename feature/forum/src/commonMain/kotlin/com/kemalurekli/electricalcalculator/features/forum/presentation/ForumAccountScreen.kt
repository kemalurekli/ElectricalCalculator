package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_danger
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_signed_out
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_account
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_account_confirm
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_account_failed
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_account_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_delete_account_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_apple
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_out
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_signed_out_summary
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_view_profile
import com.kemalurekli.electricalcalculator.features.forum.auth.SignInProvider
import com.kemalurekli.electricalcalculator.features.forum.auth.rememberForumSignIn
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_signed_out_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_blocked_section

/**
 * The reader's own account, on a screen of its own.
 *
 * It used to be a block inside settings, between the theme and the unit
 * system, and it had outgrown the place twice over: signing in is a form with
 * two steps, and signing out sits next to erasing everything you have written.
 * Settings is a list of rows; this was a control panel wearing one row's
 * clothes.
 *
 * Moving it out buys room the flow actually needed — and it puts the
 * destructive action at the bottom of its own screen rather than one thumb's
 * width from "sign out".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumAccountRoute(
    onNavigateBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumAccountViewModel = koinViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val failure by viewModel.failure.collectAsStateWithLifecycle()
    val deleteFailed by viewModel.deleteFailed.collectAsStateWithLifecycle()
    val signIn = rememberForumSignIn()
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    ElecScreenScaffold(
        title = stringResource(Res.string.forum_account_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xxl),
        ) {
            when (val current = session) {
                // Nothing while the stored session is being read, for the
                // reason it has always been nothing: a sign-in button that
                // appears and withdraws itself on every launch is worse than a
                // moment of empty space.
                is ForumSession.Unknown -> Unit

                is ForumSession.SignedOut -> AccountCard {
                    Text(
                        text = stringResource(Res.string.forum_signed_out_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = { viewModel.onSignIn(signIn) },
                        enabled = !busy && viewModel.canSignIn(signIn),
                        modifier = Modifier.fillMaxWidth(),
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
                    ForumEmailSignIn(viewModel)
                    failure?.message()?.let {
                        Text(
                            text = stringResource(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is ForumSession.SignedIn -> {
                    AccountCard {
                        Text(
                            text = current.profile.displayName,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        // The address, not only the name. Two accounts for one
                        // person is a real outcome of signing in one way on a
                        // phone and another on a tablet, and the name alone
                        // cannot tell them apart.
                        current.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedButton(
                            onClick = { onOpenProfile(current.profile.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.forum_view_profile))
                        }
                        Button(
                            onClick = viewModel::onSignOut,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.forum_sign_out))
                        }
                    }

                    // Whose posts this reader has chosen not to see. It was
                    // in settings, under its own heading between the units and
                    // the engineering defaults, where it had nothing to do
                    // with anything around it. Blocking is per account and
                    // means nothing without one, so it lives with the account.
                    ElecSectionHeader(title = stringResource(Res.string.forum_blocked_section))
                    AccountCard {
                        ForumBlockedSection()
                    }

                    // Its own section at the bottom, with its own heading. It
                    // was previously the third button in a stack of three, the
                    // same size and shape as "sign out", one slip away.
                    ElecSectionHeader(title = stringResource(Res.string.forum_account_danger))
                    AccountCard {
                        Text(
                            text = stringResource(Res.string.forum_delete_account_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { confirmingDelete = true },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = stringResource(Res.string.forum_delete_account),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        if (deleteFailed) {
                            Text(
                                text = stringResource(Res.string.forum_delete_account_failed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(Res.string.forum_delete_account_title)) },
            text = { Text(stringResource(Res.string.forum_delete_account_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.onDeleteAccount()
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.forum_delete_account_confirm),
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
 * The one row settings keeps.
 *
 * Reads the session so the row says which account, or that there is none —
 * a row that only ever said "Forum account" would make the reader open it to
 * find out something the list could have told them.
 */
@Composable
fun ForumAccountRow(
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumAccountViewModel = koinViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()

    val current = session
    ElecListItem(
        // The heading above already says "Forum account"; repeating it here
        // would spend the row on a word the reader has just read. It says who
        // instead, which is the thing the list could not otherwise tell them.
        title = when (current) {
            is ForumSession.SignedIn -> current.profile.displayName
            // Blank rather than "not signed in" while the stored session is
            // still being read, so the row does not correct itself.
            is ForumSession.Unknown -> ""
            is ForumSession.SignedOut -> stringResource(Res.string.forum_account_signed_out)
        },
        description = when (current) {
            is ForumSession.SignedIn -> current.email.orEmpty()
            is ForumSession.Unknown -> ""
            is ForumSession.SignedOut ->
                stringResource(Res.string.forum_account_signed_out_hint)
        },
        icon = ElecIcons.Forum,
        onClick = onOpen,
        modifier = modifier,
    )
}

/** The card the settings screen uses, so the two screens look like one app. */
@Composable
private fun AccountCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val spacing = ElecTheme.spacing
    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            content = content,
        )
    }
}
