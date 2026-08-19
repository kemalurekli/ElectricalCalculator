package com.kemalurekli.electricalcalculator.features.references.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory

/**
 * The reference library index.
 *
 * Stateless: the catalog is compile-time data with nothing to load, observe or
 * fail at, so there is no ViewModel between it and the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferencesRoute(
    onTopicClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()
    // Grouped once here rather than per recomposition of the list: the catalog
    // is compile-time data, so the grouping never changes.
    val sections = remember { ReferenceCatalog.all.groupBy { it.category }.toList() }

    ElecScreenScaffold(
        title = stringResource(R.string.dashboard_references_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            // Grouped rather than flat: the library is past the point where
            // a single list is scannable. Same shape as the calculator
            // index, so the two read alike.
            sections.forEach { (category, categoryTopics) ->
                item(key = "header-${category.name}") {
                    ElecSectionHeader(title = stringResource(category.titleRes()))
                }
                items(
                    count = categoryTopics.size,
                    key = { index -> categoryTopics[index].key },
                ) { index ->
                    val topic = categoryTopics[index]
                    ElecListItem(
                        title = stringResource(topic.titleRes),
                        description = stringResource(topic.descriptionRes),
                        icon = ElecIcons.References,
                        onClick = { onTopicClick(topic.key) },
                    )
                }
            }
        }
    }
}

/** The localised section heading for a reference category. */
private fun ReferenceCategory.titleRes(): Int = when (this) {
    ReferenceCategory.PROTECTION_AND_EARTHING -> R.string.ref_category_protection
    ReferenceCategory.COMMISSIONING_AND_DIAGNOSIS -> R.string.ref_category_commissioning
    ReferenceCategory.SELECTION_GUIDES -> R.string.ref_category_selection
    ReferenceCategory.ENGINEERING_FOUNDATIONS -> R.string.ref_category_foundations
    ReferenceCategory.DRAWING_SYMBOLS -> R.string.ref_category_symbols
    ReferenceCategory.TABLES_AND_CODES -> R.string.ref_category_tables
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ReferencesRoutePreview() {
    ElecToolkitTheme {
        ReferencesRoute(onTopicClick = {}, onNavigateBack = {})
    }
}
