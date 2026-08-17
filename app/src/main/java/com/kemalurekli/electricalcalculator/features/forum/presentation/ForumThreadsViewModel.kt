package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One category's threads, most recently active first. */
@HiltViewModel
class ForumThreadsViewModel @Inject constructor(
    private val repository: ForumRepository,
    languageRepository: AppLanguageRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumThread>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumThread>>> = _uiState.asStateFlow()

    private val language = ForumLanguage.forApp(languageRepository.language.value)
    private var categoryId: String? = null

    /** Does nothing when the category is already loaded, so the screen's
     *  `LaunchedEffect` can be keyed on its argument without refetching. */
    fun onOpen(id: String) {
        if (categoryId == id) return
        categoryId = id
        load()
    }

    fun onRefresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        val id = categoryId ?: return
        viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            _uiState.value = when (val result = repository.threads(id, language)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(result.value.toImmutableList())

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}
