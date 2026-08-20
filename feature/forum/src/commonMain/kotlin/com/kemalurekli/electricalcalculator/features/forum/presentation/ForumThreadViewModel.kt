package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportTarget
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One thread's messages, oldest first, opening post at the top. */
class ForumThreadViewModel(
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

    /**
     * Bumped once per delivered reply, purely so the screen can confirm it.
     *
     * A counter rather than a boolean: two replies in a row are two separate
     * confirmations, and a flag that is already true the second time would show
     * nothing.
     */
    private val _sent = MutableStateFlow(0)
    val sent: StateFlow<Int> = _sent.asStateFlow()

    /**
     * Set once the thread itself is gone, so the screen knows to leave.
     *
     * The screen cannot stay on a thread that no longer exists, and it must not
     * decide to leave optimistically either — the server refuses the delete if
     * somebody replied in the meantime.
     */
    private val _threadDeleted = MutableStateFlow(false)
    val threadDeleted: StateFlow<Boolean> = _threadDeleted.asStateFlow()

    /** Bumped per filed report, so the screen can confirm each one. */
    private val _reported = MutableStateFlow(0)
    val reported: StateFlow<Int> = _reported.asStateFlow()

    private val _blocked = MutableStateFlow(0)
    val blocked: StateFlow<Int> = _blocked.asStateFlow()

    private val _deleteFailed = MutableStateFlow(false)
    val deleteFailed: StateFlow<Boolean> = _deleteFailed.asStateFlow()

    private val _sendFailed = MutableStateFlow(false)
    val sendFailed: StateFlow<Boolean> = _sendFailed.asStateFlow()

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumPost>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumPost>>> = _uiState.asStateFlow()

    private var threadId: String? = null
    private var endReached = false
    private var loadingMore = false

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
                    _sent.value += 1
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
            when (repository.deletePost(postId)) {
                is ForumResult.Success -> load(refreshing = true)
                is ForumResult.Failure -> _deleteFailed.value = true
            }
        }
    }

    fun onDeleteThread() {
        val id = threadId ?: return
        viewModelScope.launch {
            when (repository.deleteThread(id)) {
                is ForumResult.Success -> _threadDeleted.value = true
                // Most likely somebody replied between the menu opening and the
                // tap, which the server refuses. Saying so beats a dead button.
                is ForumResult.Failure -> _deleteFailed.value = true
            }
        }
    }

    fun onDeleteFailureShown() {
        _deleteFailed.value = false
    }

    fun onReport(
        targetId: String,
        reason: ForumReportReason,
        note: String,
        target: ForumReportTarget = ForumReportTarget.POST,
    ) {
        viewModelScope.launch {
            // The result is deliberately not distinguished for the reader. A
            // report either reaches the queue or it does not; telling them
            // anything about what happens next would tell them about the
            // person they reported.
            if (repository.report(target, targetId, reason, note) is ForumResult.Success) {
                _reported.value += 1
            }
        }
    }

    fun onBlock(userId: String) {
        viewModelScope.launch {
            if (repository.block(userId) is ForumResult.Success) {
                _blocked.value += 1
                load(refreshing = true)
            }
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

    /** The next page of replies, oldest first, appended to what is shown. */
    fun onLoadMore() {
        val id = threadId ?: return
        val current = _uiState.value as? ForumScreenState.Content ?: return
        if (endReached || loadingMore) return

        loadingMore = true
        viewModelScope.launch {
            val cursor = current.value.lastOrNull()?.createdAt
            when (val result = repository.posts(id, after = cursor)) {
                is ForumResult.Success -> {
                    endReached = result.value.size < ForumRepository.DEFAULT_PAGE_SIZE
                    _uiState.value = ForumScreenState.Content(
                        (current.value + result.value).toImmutableList(),
                    )
                }
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    private fun load(refreshing: Boolean = false) {
        val id = threadId ?: return
        endReached = false
        viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            // Blocking is enforced on the reader's side: the rows still exist
            // and are still visible to everyone else, which is what makes it a
            // decision this reader made rather than a punishment.
            val hidden = (repository.blockedUserIds() as? ForumResult.Success)?.value.orEmpty()

            _uiState.value = when (val result = repository.posts(id)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(
                        result.value
                            .filterNot { it.authorId in hidden }
                            .toImmutableList(),
                    )

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
