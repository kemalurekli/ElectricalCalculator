package com.kemalurekli.electricalcalculator.features.favorites.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_calculators
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_favorites
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_field_notes
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_glossary
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_references
import com.kemalurekli.electricalcalculator.feature.favorites.generated.resources.destination_theory
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_empty_favorites_message
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_empty_favorites_title

@Composable
fun FavoritesRoute(
    onOpenFavorite: (FavoriteItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        onToggleFavorite = viewModel::onToggleFavorite,
        onOpenFavorite = onOpenFavorite,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Everything the user has pinned.
 *
 * Grouped by shelf rather than shown as one flat list. A calculator and a
 * glossary term do different things when tapped, and a reader scanning for the
 * table they pinned last week should not have to read past four calculators to
 * find it. Sections appear only when they have something in them, so a user who
 * only ever pins calculators sees exactly what they saw before.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onToggleFavorite: (FavoriteItem) -> Unit,
    onOpenFavorite: (FavoriteItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_favorites),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> ElecLoadingState()

                uiState.rows.isEmpty() -> ElecEmptyState(
                    title = stringResource(DesignSystemRes.string.state_empty_favorites_title),
                    message = stringResource(DesignSystemRes.string.state_empty_favorites_message),
                    icon = ElecIcons.FavoriteOff,
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Declaration order of the enum, so the sections keep one
                    // order however the user pinned them.
                    FavoriteKind.entries.forEach { kind ->
                        val rows = uiState.rows.filter { it.item.kind == kind }
                        if (rows.isEmpty()) return@forEach

                        item(key = "header-${kind.name}") {
                            ElecSectionHeader(title = stringResource(kind.title()))
                        }
                        items(rows, key = { "${it.item.kind}-${it.item.key}" }) { row ->
                            ElecListItem(
                                title = row.title,
                                description = row.description,
                                icon = row.calculatorIcon
                                    ?.let(ElecIcons::forCalculator)
                                    ?: kind.icon(),
                                onClick = { onOpenFavorite(row.item) },
                                isFavorite = true,
                                onToggleFavorite = { onToggleFavorite(row.item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The heading a shelf's favourites render under. */
private fun FavoriteKind.title(): StringResource = when (this) {
    FavoriteKind.CALCULATOR -> Res.string.destination_calculators
    FavoriteKind.REFERENCE -> Res.string.destination_references
    FavoriteKind.GLOSSARY -> Res.string.destination_glossary
    FavoriteKind.FIELD_NOTE -> Res.string.destination_field_notes
    FavoriteKind.THEORY -> Res.string.destination_theory
}

/** Each shelf has one icon; only calculators carry their own. */
private fun FavoriteKind.icon() = when (this) {
    FavoriteKind.CALCULATOR -> ElecIcons.Calculators
    FavoriteKind.REFERENCE -> ElecIcons.References
    FavoriteKind.GLOSSARY -> ElecIcons.Glossary
    FavoriteKind.FIELD_NOTE -> ElecIcons.FieldNotes
    FavoriteKind.THEORY -> ElecIcons.Theory
}
