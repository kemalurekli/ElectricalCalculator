package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The forum's sections, in the language the app is showing.
 *
 * The language is read once when the screen opens rather than observed.
 * Changing it recreates the activity, which brings a new instance of this —
 * so observing would add a subscription that can never fire.
 */
class ForumCategoriesViewModel(
    private val repository: ForumRepository,
    private val languageRepository: AppLanguageRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumCategory>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumCategory>>> = _uiState.asStateFlow()

    val language: ForumLanguage = ForumLanguage.forApp(languageRepository.language.value)

    init {
        load()
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

            _uiState.value = when (val result = repository.categories(language)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(result.value.toImmutableList())

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}
