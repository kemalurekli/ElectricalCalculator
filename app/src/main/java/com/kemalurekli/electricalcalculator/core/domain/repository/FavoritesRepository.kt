package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.asFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Tracks what the user has pinned.
 *
 * Favourites are surfaced on the home screen, so this is read on the startup
 * path; implementations must emit an initial value without blocking.
 *
 * The calculator-shaped methods are kept as conveniences over the general ones.
 * Sixteen calculator view models call them, and widening their call sites to
 * carry a kind they can only ever pass one value for would be noise.
 */
interface FavoritesRepository {

    /** Everything pinned, in the order it was pinned. */
    fun observeAll(): Flow<List<FavoriteItem>>

    /** Whether [item] is currently pinned. */
    fun observeIsFavorite(item: FavoriteItem): Flow<Boolean>

    /** Pins [item] if absent, unpins it if present. Returns the new state. */
    suspend fun toggle(item: FavoriteItem): Boolean

    suspend fun setFavorite(item: FavoriteItem, isFavorite: Boolean)

    // -- Calculators -----------------------------------------------------------

    /** The pinned calculators, in the order they were pinned. */
    fun observeFavorites(): Flow<List<CalculatorId>> =
        observeAll().map { items ->
            // Unknown keys are skipped rather than crashing: they occur after a
            // downgrade or once a calculator is retired from the catalog.
            items.mapNotNull { CalculatorId.fromKeyOrNull(it.key) }
        }

    fun observeIsFavorite(calculatorId: CalculatorId): Flow<Boolean> =
        observeIsFavorite(calculatorId.asFavorite())

    suspend fun toggle(calculatorId: CalculatorId): Boolean = toggle(calculatorId.asFavorite())

    suspend fun setFavorite(calculatorId: CalculatorId, isFavorite: Boolean) =
        setFavorite(calculatorId.asFavorite(), isFavorite)
}
