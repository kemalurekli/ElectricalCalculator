package com.kemalurekli.electricalcalculator.features.forum.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_offline_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_offline_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_server_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_server_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_unconfigured_message
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_error_unconfigured_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_retry

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

/**
 * The forum's three failures, drawn as the app's one empty state.
 *
 * This used to be its own Column with its own button placement — a second way
 * of saying "there is nothing here", differing from the shared one by a few
 * pixels of spacing that nobody had chosen. [ElecEmptyState] now takes an
 * optional action, which is all this needed from it.
 */
@Composable
private fun ForumError(failure: ForumFailure, onRetry: () -> Unit) {
    ElecEmptyState(
        title = stringResource(failure.title()),
        message = stringResource(failure.message()),
        icon = ElecIcons.Forum,
        // Only where trying again could plausibly change the answer. A dropped
        // connection is worth another tap; a build with no backend configured
        // is not the reader's problem and gets no button to press at it.
        actionLabel = stringResource(Res.string.forum_retry)
            .takeIf { failure == ForumFailure.NO_CONNECTION },
        onAction = onRetry.takeIf { failure == ForumFailure.NO_CONNECTION },
    )
}

private fun ForumFailure.title(): StringResource = when (this) {
    ForumFailure.NOT_CONFIGURED -> Res.string.forum_error_unconfigured_title
    ForumFailure.NO_CONNECTION -> Res.string.forum_error_offline_title
    ForumFailure.SERVER_ERROR -> Res.string.forum_error_server_title
}

private fun ForumFailure.message(): StringResource = when (this) {
    ForumFailure.NOT_CONFIGURED -> Res.string.forum_error_unconfigured_message
    ForumFailure.NO_CONNECTION -> Res.string.forum_error_offline_message
    ForumFailure.SERVER_ERROR -> Res.string.forum_error_server_message
}
