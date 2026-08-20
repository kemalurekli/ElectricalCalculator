package com.kemalurekli.electricalcalculator.features.favorites.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One pinned thing, resolved into what the list needs to draw it.
 *
 * [calculatorIcon] is set only for calculators, which carry their own icon in
 * the catalog. The other shelves each have one icon, chosen by [FavoriteItem.kind].
 */
@Immutable
data class FavoriteRow(
    val item: FavoriteItem,
    val title: String,
    val description: String,
    val calculatorIcon: CalculatorIcon? = null,
)

@Immutable
data class FavoritesUiState(
    val rows: ImmutableList<FavoriteRow> = persistentListOf(),
    /**
     * Distinguishes "nothing pinned" from "not read yet", so the empty state
     * does not flash on screen while the first database emission is in flight.
     */
    val isLoading: Boolean = true,
)

/**
 * Everything the user has pinned, across every shelf.
 *
 * A favourite is stored as a kind and a key, so this is where the key is turned
 * back into something readable. An item whose key no longer resolves — a topic
 * retired between releases — is dropped from the list rather than rendered as a
 * blank row; the pin stays in the database in case a downgrade brings it back.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val catalog: CalculatorCatalog,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.observeAll()
        .map { items ->
            FavoritesUiState(
                rows = items.mapNotNull(::resolve).toImmutableList(),
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = FavoritesUiState(),
        )

    fun onToggleFavorite(item: FavoriteItem) {
        viewModelScope.launch { favoritesRepository.toggle(item) }
    }

    private fun resolve(item: FavoriteItem): FavoriteRow? = when (item.kind) {
        FavoriteKind.CALCULATOR -> catalog.all.firstOrNull { it.key == item.key }?.let {
            FavoriteRow(
                item = item,
                title = stringResolver.get(it.title),
                description = stringResolver.get(it.description),
                calculatorIcon = it.icon,
            )
        }

        FavoriteKind.REFERENCE -> ReferenceCatalog.topicOrNull(item.key)?.let {
            FavoriteRow(item, stringResolver.get(it.title), stringResolver.get(it.description))
        }

        FavoriteKind.GLOSSARY -> GlossaryCatalog.termOrNull(item.key)?.let {
            FavoriteRow(item, stringResolver.get(it.term), stringResolver.get(it.definition))
        }

        FavoriteKind.FIELD_NOTE -> FieldNoteCatalog.noteOrNull(item.key)?.let {
            FavoriteRow(item, stringResolver.get(it.title), stringResolver.get(it.body))
        }

        FavoriteKind.THEORY -> TheoryCatalog.topicOrNull(item.key)?.let {
            FavoriteRow(item, stringResolver.get(it.title), stringResolver.get(it.summary))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** The catalog descriptor's key, which the favourite stores. */
private val com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorDescriptor.key: String
    get() = id.key
