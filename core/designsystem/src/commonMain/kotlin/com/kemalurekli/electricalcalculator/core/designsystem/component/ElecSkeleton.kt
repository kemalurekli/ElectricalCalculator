package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A block standing in for content that has not arrived yet.
 *
 * The app's loading state was a spinner in the middle of an empty page, which
 * says "working" and nothing else. Where the shape of what is coming is already
 * known — a list of forum sections, a list of threads — drawing that shape says
 * how much is coming and where it will be, and the page does not jump when it
 * lands.
 *
 * Deliberately still. `docs/design-language.md` leaves motion undecided, and a
 * shimmer is a whole animation vocabulary arriving through the back door for the
 * sake of a state that should be brief. Tone alone is enough to read as absent.
 *
 * Hidden from accessibility: a screen reader is already told the screen is
 * loading by the state it replaces, and eight nameless blocks are noise.
 */
@Composable
fun ElecSkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(
        modifier = modifier
            .semantics { hideFromAccessibility() }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}

/** A line of placeholder text. Width is the caller's, height is the type's. */
@Composable
fun ElecSkeletonLine(modifier: Modifier = Modifier, height: Dp = SKELETON_LINE_HEIGHT) {
    ElecSkeletonBlock(modifier = modifier.height(height))
}

/** The round placeholder that stands in for an avatar or a section glyph. */
@Composable
fun ElecSkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    ElecSkeletonBlock(modifier = modifier.size(size), shape = CircleShape)
}

/** Matches the cap height of the body styles the lines stand in for. */
private val SKELETON_LINE_HEIGHT = 14.dp
