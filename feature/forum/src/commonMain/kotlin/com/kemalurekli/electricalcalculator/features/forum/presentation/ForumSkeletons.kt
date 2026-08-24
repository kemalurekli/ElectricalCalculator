package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSkeletonCircle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSkeletonLine
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * The shape of a forum list, drawn before the list arrives.
 *
 * The forum is the only part of the app that waits on a network, and it is the
 * part a reader opens most often — so it is where a spinner on an empty page is
 * seen most. These stand in for the two lists behind that wait, and are built
 * from the same cards, sizes and insets as the real rows, so nothing moves when
 * the answer lands.
 */
@Composable
internal fun ForumCategoriesSkeleton(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(CATEGORY_PLACEHOLDER_COUNT) {
            ElecCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
            ) {
                Row(
                    modifier = Modifier.padding(spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ElecSkeletonCircle(size = CATEGORY_ICON_SIZE)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        ElecSkeletonLine(modifier = Modifier.fillMaxWidth(TITLE_FRACTION))
                        ElecSkeletonLine(modifier = Modifier.fillMaxWidth())
                        ElecSkeletonLine(modifier = Modifier.fillMaxWidth(COUNT_FRACTION))
                    }
                }
            }
        }
    }
}

/** As [ForumCategoriesSkeleton], for the list of threads inside a section. */
@Composable
internal fun ForumThreadsSkeleton(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(THREAD_PLACEHOLDER_COUNT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                ElecSkeletonCircle(size = THREAD_AVATAR_SIZE)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    ElecSkeletonLine(modifier = Modifier.fillMaxWidth())
                    ElecSkeletonLine(modifier = Modifier.fillMaxWidth(AUTHOR_FRACTION))
                }
            }
        }
    }
}

/** Enough to fill a phone, and no more — the rest would scroll into nothing. */
private const val CATEGORY_PLACEHOLDER_COUNT = 5
private const val THREAD_PLACEHOLDER_COUNT = 7

/**
 * Ragged widths rather than full ones. A stack of identical bars reads as a
 * table; text does not line up on the right, and neither should its stand-in.
 */
private const val TITLE_FRACTION = 0.55f
private const val COUNT_FRACTION = 0.25f
private const val AUTHOR_FRACTION = 0.6f

private val CATEGORY_ICON_SIZE = 48.dp
private val THREAD_AVATAR_SIZE = 40.dp
