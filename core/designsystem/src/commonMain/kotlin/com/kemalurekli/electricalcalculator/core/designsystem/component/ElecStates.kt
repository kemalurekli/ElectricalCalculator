package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_retry
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_empty_favorites_message
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_empty_favorites_title
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_error_title
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_loading

/**
 * Placeholder shown when a screen has nothing to show.
 *
 * One shape for "this list is empty", "the network is down" and "this build has
 * no backend", because to the reader those are the same event — they came for
 * something and it is not here — and three different-looking pages for one
 * event is how an app starts to feel assembled rather than made. The forum
 * carried its own near-copy of this until it was folded back in.
 *
 * The whole block is one accessibility node so it is announced as a single
 * sentence, and the icon is dropped from the tree as decoration.
 *
 * @param actionLabel with [onAction], shows a button. Offered only where doing
 *   the thing could plausibly change the answer: retrying a dropped connection
 *   is worth a tap, retrying a build with no server configured is not.
 */
@Composable
fun ElecEmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = ElecTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.xl)
            // Cleared *around* the action: a button inside a cleared node is a
            // button a screen reader cannot reach, so the description covers
            // the text and the button keeps its own node.
            .semantics(mergeDescendants = false) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "$title. $message"
            },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ICON_SIZE),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

/**
 * Failure state with a retry affordance.
 *
 * Marked as a polite live region so the message is announced when it replaces
 * loading content, rather than passing silently.
 */
@Composable
fun ElecErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.state_error_title),
) {
    val spacing = ElecTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.xl)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.action_retry))
        }
    }
}

/** The empty state's icon. Large enough to read as an illustration, not a glyph. */
private val ICON_SIZE = 48.dp

/** Centred progress indicator for content that is still resolving. */
@Composable
fun ElecLoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.state_loading)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = description },
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ElecEmptyStatePreview() {
    ElecToolkitTheme {
        ElecEmptyState(
            title = stringResource(Res.string.state_empty_favorites_title),
            message = stringResource(Res.string.state_empty_favorites_message),
            icon = ElecIcons.FavoriteOff,
        )
    }
}
