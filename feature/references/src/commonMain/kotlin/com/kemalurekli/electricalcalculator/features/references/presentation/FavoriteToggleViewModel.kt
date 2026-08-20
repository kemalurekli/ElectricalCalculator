package com.kemalurekli.electricalcalculator.features.references.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Pinning for a screen that has nothing else to hold.
 *
 * The reference topics are compile-time data, so their screen is stateless by
 * design and says so. Pinning is the one thing on it that is not — it reads and
 * writes a database. Rather than give that screen a view model of its own and
 * blur the distinction, this holds the pin and nothing else, and any other
 * screen in the same position can reuse it.
 *
 * Calculators do *not* use this: they already hold a view model and observing
 * one more flow there costs nothing.
 *
 * It used to live in `core/ui` for any screen in the same position to pick up.
 * Nothing ever did, so it moved here with its one caller rather than becoming a
 * module of its own — it goes back down the moment a second screen needs it.
 */
class FavoriteToggleViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var item: FavoriteItem? = null
    private var watch: Job? = null

    /** Points this at [target]; restarts the watch when the screen changes item. */
    fun observe(target: FavoriteItem) {
        if (item == target) return
        item = target
        watch?.cancel()
        watch = viewModelScope.launch {
            favoritesRepository.observeIsFavorite(target).collect { _isFavorite.value = it }
        }
    }

    fun toggle() {
        val target = item ?: return
        viewModelScope.launch { favoritesRepository.toggle(target) }
    }
}
