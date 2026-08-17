package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One thread's messages, oldest first, opening post at the top. */
@HiltViewModel
class ForumThreadViewModel @Inject constructor(
    private val repository: ForumRepository,
    authRepository: ForumAuthRepository,
) : ViewModel() {

    val session: StateFlow<ForumSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ForumSession.Unknown)

    /** The reply being composed. Kept here so it survives a rotation. */
    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _sendFailed = MutableStateFlow(false)
    val sendFailed: StateFlow<Boolean> = _sendFailed.asStateFlow()

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

    fun onDraftChange(value: String) {
        _draft.value = value
    }

    fun onSendReply() {
        val id = threadId ?: return
        val body = _draft.value.trim()
        if (body.isEmpty() || _sending.value) return

        viewModelScope.launch {
            _sending.value = true
            when (repository.createReply(id, body)) {
                is ForumResult.Success -> {
                    // Cleared only on success. A failed send that wipes what
                    // somebody just typed is the one outcome worth avoiding at
                    // any cost.
                    _draft.value = ""
                    _sendFailed.value = false
                    load(refreshing = true)
                }

                is ForumResult.Failure -> _sendFailed.value = true
            }
            _sending.value = false
        }
    }

    fun onToggleThanks(post: ForumPost) {
        viewModelScope.launch {
            val thanked = post.thankedByMe == true
            if (repository.setThanks(post.id, !thanked) is ForumResult.Success) {
                load(refreshing = true)
            }
        }
    }

    fun onDeletePost(postId: String) {
        viewModelScope.launch {
            if (repository.deletePost(postId) is ForumResult.Success) load(refreshing = true)
        }
    }

    fun onEditPost(postId: String, body: String) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            if (repository.updatePost(postId, trimmed) is ForumResult.Success) {
                load(refreshing = true)
            }
        }
    }

    fun onSendFailureShown() {
        _sendFailed.value = false
    }

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

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
