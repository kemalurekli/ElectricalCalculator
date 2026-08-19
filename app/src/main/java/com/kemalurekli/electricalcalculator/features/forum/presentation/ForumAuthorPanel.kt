package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLevel

/**
 * Who is speaking, in a column beside what they said.
 *
 * The layout every forum has used for twenty years, and for a reason: the
 * person is a fixed, scannable column and the message is free to be whatever
 * length it is. It also gives standing somewhere to live — on a forum about
 * mains electricity, whether an answer comes from somebody with four posts or
 * four hundred is information the reader wants before they act on it.
 *
 * Fixed width rather than wrapped: the messages then share one left edge all
 * the way down the thread, which is what makes it scannable at all.
 */
@Composable
fun ForumAuthorPanel(
    name: String,
    userId: String,
    postCount: Int,
    thanksReceived: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val spacing = ElecTheme.spacing
    val level = ForumLevel.of(postCount, thanksReceived)

    Column(
        modifier = modifier
            .width(PANEL_WIDTH.dp)
            .clickable(onClick = onClick)
            .padding(end = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        ForumAvatar(name = name, userId = userId, size = AVATAR_SIZE)

        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // The rank reads as a caption, not a medal. It is derived from the two
        // numbers underneath it and says the same thing in a word.
        Text(
            text = stringResource(level.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = spacing.sm, vertical = 2.dp),
        )

        Text(
            text = stringResource(R.string.forum_author_stats, postCount, thanksReceived),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun ForumLevel.labelRes(): Int = when (this) {
    ForumLevel.NEWCOMER -> R.string.forum_level_newcomer
    ForumLevel.APPRENTICE -> R.string.forum_level_apprentice
    ForumLevel.JOURNEYMAN -> R.string.forum_level_journeyman
    ForumLevel.MASTER -> R.string.forum_level_master
    ForumLevel.EXPERT -> R.string.forum_level_expert
}

/** Wide enough for a rank chip and two lines of most names. */
private const val PANEL_WIDTH = 96
private const val AVATAR_SIZE = 44
