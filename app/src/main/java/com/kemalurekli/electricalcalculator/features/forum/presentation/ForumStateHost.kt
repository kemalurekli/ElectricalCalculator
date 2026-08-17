package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure

/**
 * Loading, failed, or here it is — for all three forum screens.
 *
 * The three failures get three different pages because they ask for three
 * different things from the reader. A dropped connection is worth another tap;
 * a server that answered with something unreadable is not; a build with no
 * backend is not the reader's problem at all, so it does not offer a button
 * that cannot help.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ForumStateHost(
    state: ForumScreenState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = onRetry,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        ForumScreenState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ElecLoadingState()
        }

        // The Box is not decoration. Every caller passes the Scaffold's inner
        // padding in `modifier`, and a branch that forgets to apply it draws its
        // content from the top of the window — underneath the app bar, where the
        // first rows are invisible and untappable while the rest of the list
        // looks perfectly fine. All three branches consume it for that reason.
        //
        // The forum is the one part of this app that needs the network, so it is
        // also the one part that can be looking at something stale. Until now
        // the only way to ask again was the button on the no-connection page —
        // which, by definition, a loaded list never shows.
        is ForumScreenState.Content -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
        ) {
            content(state.value)
        }

        is ForumScreenState.Error -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ForumError(state.failure, onRetry)
        }
    }
}

@Composable
private fun ForumError(failure: ForumFailure, onRetry: () -> Unit) {
    val spacing = ElecTheme.spacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        ElecEmptyState(
            title = stringResource(failure.titleRes()),
            message = stringResource(failure.messageRes()),
            icon = ElecIcons.Forum,
        )
        // Only where trying again could plausibly change the answer.
        if (failure == ForumFailure.NO_CONNECTION) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.padding(horizontal = spacing.lg),
            ) {
                Text(stringResource(R.string.forum_retry))
            }
        }
    }
}

private fun ForumFailure.titleRes(): Int = when (this) {
    ForumFailure.NOT_CONFIGURED -> R.string.forum_error_unconfigured_title
    ForumFailure.NO_CONNECTION -> R.string.forum_error_offline_title
    ForumFailure.SERVER_ERROR -> R.string.forum_error_server_title
}

private fun ForumFailure.messageRes(): Int = when (this) {
    ForumFailure.NOT_CONFIGURED -> R.string.forum_error_unconfigured_message
    ForumFailure.NO_CONNECTION -> R.string.forum_error_offline_message
    ForumFailure.SERVER_ERROR -> R.string.forum_error_server_message
}
