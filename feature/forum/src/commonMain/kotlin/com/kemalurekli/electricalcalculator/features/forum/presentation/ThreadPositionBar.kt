package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_position
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_to_end
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_thread_to_start

/**
 * Where the reader is in a thread, and the two ends of it.
 *
 * ### Why this is not a pager
 *
 * A web forum numbers its pages because a page is a thing that reloads. Here a
 * thread is read by scrolling, and "page 7" is not an address anybody uses for
 * it — a reader who wants to point at a message quotes the message. What they
 * do want, constantly, is the end: an active thread is opened to find out what
 * was said, and until now that cost twelve loads of scrolling through what they
 * had already read.
 *
 * So the middle of this bar does not ask the reader to choose a page. It tells
 * them where they are, which is the question a pager's numbers were a poor
 * answer to, and the two ends are the jumps that were actually missing.
 *
 * ### Why it is at the top
 *
 * The bottom of this screen is the reply box. A second bar down there would
 * put two controls in the one place a thumb rests, and the one that matters
 * more is the one you write with.
 *
 * ### When it is not there at all
 *
 * A thread that fits in a page has nothing to navigate, and most of them do.
 * [ThreadPosition.isPaged] is what keeps this off the twelve-reply threads that
 * make up a forum's ordinary day.
 */
@Composable
internal fun ThreadPositionBar(
    position: ThreadPosition,
    onJumpToStart: () -> Unit,
    onJumpToEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onJumpToStart) {
            Icon(
                imageVector = ElecIcons.ToStart,
                contentDescription = null,
                modifier = Modifier.size(GLYPH),
            )
            Text(
                text = stringResource(Res.string.forum_thread_to_start),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = spacing.xs),
            )
        }

        // The figures in the app's tabular face, like every other figure in it,
        // so the position does not jitter as the reader scrolls past ten.
        Text(
            text = stringResource(
                Res.string.forum_thread_position,
                position.current,
                position.size,
            ),
            style = NumericCompactTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TextButton(onClick = onJumpToEnd) {
            Text(
                text = stringResource(Res.string.forum_thread_to_end),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = spacing.xs),
            )
            Icon(
                imageVector = ElecIcons.ToEnd,
                contentDescription = null,
                modifier = Modifier.size(GLYPH),
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

private val GLYPH = 18.dp
