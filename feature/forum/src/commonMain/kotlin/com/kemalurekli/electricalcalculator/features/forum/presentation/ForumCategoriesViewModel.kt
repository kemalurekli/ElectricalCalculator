package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The forum's sections, in the language the app is showing.
 *
 * The language is observed, not read once. It used to be read once, on the
 * reasoning that changing it recreates the activity and so brings a new
 * instance of this — and that reasoning was wrong on both platforms. Android
 * restores a back stack entry under its original id after the recreation, so
 * the entry's ViewModels are handed back rather than rebuilt; iOS has no
 * activity to recreate at all.
 *
 * What the reader saw was a forum that kept the language it was first opened
 * in. Turkish sections under English chrome, each still claiming its Turkish
 * thread count, and every one of them empty when opened — because the screen
 * behind them *was* rebuilt, asked for English, and found nothing.
 */
class ForumCategoriesViewModel(
    private val repository: ForumRepository,
    private val languageRepository: AppLanguageRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumCategory>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumCategory>>> = _uiState.asStateFlow()

    val language: StateFlow<ForumLanguage> = languageRepository.language
        .map(ForumLanguage::forApp)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ForumLanguage.forApp(languageRepository.language.value),
        )

    init {
        // Collecting rather than calling `load()` once: the first emission is
        // the current language, so this is the initial load as well as the
        // reload, and there is no window where the two disagree.
        viewModelScope.launch {
            language.collect { load() }
        }
    }

    fun onRefresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                // Keep what is on screen while the new list is in flight; a
                // refresh that blanks the page loses the reader's place.
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            _uiState.value = when (val result = repository.categories(language.value)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(result.value.toImmutableList())

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}
