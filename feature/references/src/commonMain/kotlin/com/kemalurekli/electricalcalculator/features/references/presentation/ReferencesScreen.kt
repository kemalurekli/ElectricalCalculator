package com.kemalurekli.electricalcalculator.features.references.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListDivider
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.dashboard_references_title
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_commissioning
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_foundations
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_protection
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_selection
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_symbols
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_category_tables

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
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()
    // Grouped once here rather than per recomposition of the list: the catalog
    // is compile-time data, so the grouping never changes.
    val sections = remember { ReferenceCatalog.all.groupBy { it.category }.toList() }

    ElecScreenScaffold(
        title = stringResource(Res.string.dashboard_references_title),
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
                    ElecSectionHeader(title = stringResource(category.title()))
                }
                items(
                    count = categoryTopics.size,
                    key = { index -> categoryTopics[index].key },
                ) { index ->
                    val topic = categoryTopics[index]
                    ElecListItem(
                        title = stringResource(topic.title),
                        description = stringResource(topic.description),
                        // The standard this page transcribes, in place of the
                        // book glyph that used to sit here twenty times over.
                        // Two reference titles can sound equally plausible; the
                        // designation underneath is what a reader picks between.
                        caption = stringResource(topic.source),
                        onClick = { onTopicClick(topic.key) },
                    )
                    if (index < categoryTopics.lastIndex) ElecListDivider()
                }
            }
        }
    }
}

/** The localised section heading for a reference category. */
private fun ReferenceCategory.title(): StringResource = when (this) {
    ReferenceCategory.PROTECTION_AND_EARTHING -> Res.string.ref_category_protection
    ReferenceCategory.COMMISSIONING_AND_DIAGNOSIS -> Res.string.ref_category_commissioning
    ReferenceCategory.SELECTION_GUIDES -> Res.string.ref_category_selection
    ReferenceCategory.ENGINEERING_FOUNDATIONS -> Res.string.ref_category_foundations
    ReferenceCategory.DRAWING_SYMBOLS -> Res.string.ref_category_symbols
    ReferenceCategory.TABLES_AND_CODES -> Res.string.ref_category_tables
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ReferencesRoutePreview() {
    ElecToolkitTheme {
        ReferencesRoute(onTopicClick = {}, onNavigateBack = {})
    }
}
