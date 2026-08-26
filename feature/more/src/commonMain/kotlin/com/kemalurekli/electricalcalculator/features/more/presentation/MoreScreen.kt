package com.kemalurekli.electricalcalculator.features.more.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecAccent
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListDivider
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecSpacing
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.navigation.TopLevelDestination
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.feature.more.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.more.generated.resources.more_group_shelves
import com.kemalurekli.electricalcalculator.feature.more.generated.resources.more_group_yours
import com.kemalurekli.electricalcalculator.feature.more.generated.resources.more_title

/**
 * Everything that is not a tab.
 *
 * A flat list rather than a second grid of cards. The dashboard's tiles are for
 * a screen you land on and browse; this is a screen you arrive at knowing what
 * you came for, and a list of named rows is faster to scan than a field of
 * tiles at that job. It is also the shape both platforms use for a More tab,
 * which matters because this same code renders on iOS.
 *
 * The rows come from [TopLevelDestination.moreDestinations] rather than being
 * listed here, so a section added to the app appears in exactly one place —
 * either as a tab or here, never in neither and never in both.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreRoute(
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination> = TopLevelDestination.moreDestinations,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.more_title),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = spacing.xxl,
            ),
        ) {
            // Grouped, because eight unrelated rows in one column make the
            // reader scan all eight to find the one. The split is the one the
            // destinations already declare through their accent — the shelves
            // the app ships against the two built from the reader's own
            // activity — rather than a second opinion invented here.
            //
            // Settings has neither heading nor group: it is the one entry that
            // configures the app rather than doing work in it, and it sits at
            // the bottom on its own the way it does everywhere else.
            val groups = listOf(
                Res.string.more_group_shelves to destinations.filter { it.accent == ElecAccent.PRIMARY },
                Res.string.more_group_yours to destinations.filter { it.accent == ElecAccent.TERTIARY },
            )

            groups.forEachIndexed { group, (heading, rows) ->
                if (rows.isEmpty()) return@forEachIndexed
                item(key = "header-$group") {
                    ElecSectionHeader(
                        title = stringResource(heading),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }
                itemsIndexed(
                    items = rows,
                    key = { _, destination -> destination.name },
                ) { index, destination ->
                    MoreRow(destination, spacing, onNavigate)
                    if (index < rows.lastIndex) ElecListDivider()
                }
            }

            destinations.filter { it.accent == ElecAccent.NEUTRAL }.forEach { destination ->
                item(key = destination.name) {
                    // A gap the width of a heading, so it reads as its own
                    // group without a heading naming a group of one.
                    Spacer(Modifier.height(spacing.sectionGap))
                    MoreRow(destination, spacing, onNavigate)
                }
            }
        }
    }
}

/** One row of the More list. */
@Composable
private fun MoreRow(
    destination: TopLevelDestination,
    spacing: ElecSpacing,
    onNavigate: (Route) -> Unit,
) {
    ElecListItem(
        title = stringResource(destination.title),
        description = stringResource(destination.subtitle),
        icon = destination.icon,
        onClick = { onNavigate(destination.route) },
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = spacing.md),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MoreScreenPreview() {
    ElecToolkitTheme {
        MoreRoute(onNavigate = {})
    }
}
