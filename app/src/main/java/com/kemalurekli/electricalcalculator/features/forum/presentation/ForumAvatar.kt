package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * A monogram, standing in for the person.
 *
 * The forum has no profile pictures and is not getting any — an image is
 * something to upload, fetch, moderate and pay for, and none of that earns its
 * place next to a question about cable sizing. But a wall of undifferentiated
 * text is genuinely hard to read: it is what makes a thread look like a log
 * file rather than a conversation, and it gives the eye nothing to follow when
 * scanning for who said what.
 *
 * An initial in a circle costs nothing, needs no network, and does the one job
 * a picture would have done here.
 *
 * The colour is derived from the author's id rather than stored, so the same
 * person is the same colour on every device and in every thread, and nobody had
 * to choose it.
 */
@Composable
fun ForumAvatar(
    name: String,
    userId: String,
    modifier: Modifier = Modifier,
    size: Int = DEFAULT_SIZE,
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val (background, foreground) = palette[colourIndex(userId, palette.size)]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background)
            // One node for the row, not two. The name is right beside this and
            // a screen reader announcing the initial as well just repeats it.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.monogram(),
            style = MaterialTheme.typography.titleSmall,
            color = foreground,
        )
    }
}

/**
 * The first letter, upper-cased in the name's own alphabet.
 *
 * `uppercase()` without a locale turns a Turkish "i" into "I" rather than "İ",
 * which is wrong in the language half this forum is written in.
 */
private fun String.monogram(): String =
    trim().firstOrNull()?.toString()?.uppercase(Locale.getDefault()) ?: "?"

/** Stable across devices, because it is derived rather than remembered. */
private fun colourIndex(userId: String, count: Int): Int {
    val hash = userId.fold(0) { acc, c -> acc * 31 + c.code }
    return ((hash % count) + count) % count
}

private const val DEFAULT_SIZE = 40
