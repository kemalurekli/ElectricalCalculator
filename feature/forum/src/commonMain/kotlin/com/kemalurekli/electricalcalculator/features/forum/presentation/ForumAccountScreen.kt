package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecIconBadge
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItemDefaults
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSkeletonLine
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_danger
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_signed_out
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_signed_out_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_account_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_blocked_section
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

    when (val current = session) {
        // Saying "not signed in" here and then replacing it with a name a
        // moment later would be the row correcting itself, so it does not
        // guess. It draws the shape of what is coming instead: an empty row
        // with an icon floating in it reads as a card that failed, and this
        // one reads as a card that is still arriving.
        is ForumSession.Unknown -> ForumAccountRowLoading(modifier = modifier)

        else -> ElecListItem(
            // The heading above already says "Forum account"; repeating it here
            // would spend the row on a word the reader has just read. It says
            // who instead, which is the thing the list could not otherwise tell
            // them.
            title = when (current) {
                is ForumSession.SignedIn -> current.profile.displayName
                else -> stringResource(Res.string.forum_account_signed_out)
            },
            description = when (current) {
                is ForumSession.SignedIn -> current.email.orEmpty()
                else -> stringResource(Res.string.forum_account_signed_out_hint)
            },
            icon = ElecIcons.Forum,
            onClick = onOpen,
            modifier = modifier,
        )
    }
}

/**
 * [ForumAccountRow] before the stored session has been read.
 *
 * Laid out from the same parts as the row it stands in for — the list item's
 * own inset, the same gap after the icon badge, the same gap between the two
 * lines — so the card is the height it will keep and nothing below it moves
 * when the name lands. The line heights come from the type scale rather than
 * from a number typed here, which is what keeps that true if the scale changes.
 *
 * Not clickable. The destination is the same either way, but a row with nothing
 * legible on it is not something to invite a tap on.
 */
@Composable
private fun ForumAccountRowLoading(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ElecListItemDefaults.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        ElecIconBadge(icon = ElecIcons.Forum)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            ElecSkeletonLine(
                modifier = Modifier.fillMaxWidth(LOADING_TITLE_WIDTH),
                height = with(density) { typography.titleSmall.lineHeight.toDp() },
            )
            ElecSkeletonLine(
                modifier = Modifier.fillMaxWidth(LOADING_DESCRIPTION_WIDTH),
                height = with(density) { typography.bodySmall.lineHeight.toDp() },
            )
        }
    }
}

// A display name is short and an email is long, and the placeholder says so.
// Two blocks of equal width would stand in for a shape the row never takes.
private const val LOADING_TITLE_WIDTH = 0.35f
private const val LOADING_DESCRIPTION_WIDTH = 0.7f

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
