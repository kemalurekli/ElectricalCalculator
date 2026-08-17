package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One thread's messages, oldest first, opening post at the top. */
@HiltViewModel
class ForumThreadViewModel @Inject constructor(
    private val repository: ForumRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumPost>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumPost>>> = _uiState.asStateFlow()

    private var threadId: String? = null

    fun onOpen(id: String) {
        if (threadId == id) return
        threadId = id
        load()
    }

    fun onRefresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        val id = threadId ?: return
        viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            _uiState.value = when (val result = repository.posts(id)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(result.value.toImmutableList())

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}
