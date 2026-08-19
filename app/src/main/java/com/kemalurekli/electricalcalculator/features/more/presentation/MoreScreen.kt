package com.kemalurekli.electricalcalculator.features.more.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.navigation.TopLevelDestination
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout

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
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(R.string.more_title),
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
            items(
                items = TopLevelDestination.moreDestinations,
                key = { it.name },
            ) { destination ->
                ElecListItem(
                    title = stringResource(destination.titleRes),
                    description = stringResource(destination.subtitleRes),
                    icon = destination.icon,
                    onClick = { onNavigate(destination.route) },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = spacing.md),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MoreScreenPreview() {
    ElecToolkitTheme {
        MoreRoute(onNavigate = {})
    }
}
