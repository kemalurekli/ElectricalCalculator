package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import kotlinx.coroutines.flow.Flow

/**
 * Tracks the calculators the user has pinned.
 *
 * Favourites are surfaced on the home screen, so this is read on the startup
 * path; implementations must emit an initial value without blocking.
 */
interface FavoritesRepository {

    /** The pinned calculators, in the order they were pinned. */
    fun observeFavorites(): Flow<List<CalculatorId>>

    /** Whether [calculatorId] is currently pinned. */
    fun observeIsFavorite(calculatorId: CalculatorId): Flow<Boolean>

    /** Pins [calculatorId] if absent, unpins it if present. Returns the new state. */
    suspend fun toggle(calculatorId: CalculatorId): Boolean

    suspend fun setFavorite(calculatorId: CalculatorId, isFavorite: Boolean)
}
