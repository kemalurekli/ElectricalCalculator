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
import kotlin.time.Instant
import kotlinx.coroutines.flow.update
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

    /**
     * Whether the window reaches the opening post.
     *
     * True on an ordinary read, which starts there. False after a jump to the
     * end, which is the case this exists for: the reader is looking at the last
     * page of a thread whose beginning has never been fetched, and scrolling up
     * has to go and get it.
     */
    private var startReached = true
    private var loadingMore = false

    /**
     * How far into the thread the top of the window sits, counting from zero.
     *
     * Kept because it cannot be derived: after a jump to the end the window is
     * the last twenty-five messages of three hundred, and nothing in the list
     * itself says so.
     */
    private val _position = MutableStateFlow(ThreadPosition())
    val position: StateFlow<ThreadPosition> = _position.asStateFlow()

    fun onOpen(id: String, replyCount: Int = 0) {
        if (threadId == id) return
        threadId = id
        // The opening post is a message too, so a thread with four replies has
        // five of them.
        _position.value = ThreadPosition(total = replyCount + 1)
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
                    show(current.value + result.value, keepingStart = true)
                }
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    /**
     * The page before the one on screen, prepended.
     *
     * Only ever needed after a jump to the end. A thread read from the top
     * already has everything above it, which is why [startReached] starts true
     * and this returns immediately in the ordinary case.
     */
    fun onLoadOlder() {
        val id = threadId ?: return
        val current = _uiState.value as? ForumScreenState.Content ?: return
        if (startReached || loadingMore) return

        loadingMore = true
        viewModelScope.launch {
            val cursor = current.value.firstOrNull()?.createdAt
            when (val result = repository.posts(id, before = cursor)) {
                is ForumResult.Success -> {
                    startReached = result.value.size < ForumRepository.DEFAULT_PAGE_SIZE
                    // The window grew upwards, so the top of it moved back
                    // towards the beginning by exactly what arrived.
                    _position.update { it.copy(windowStart = it.windowStart - result.value.size) }
                    show(result.value + current.value, keepingStart = true)
                }
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    /**
     * The last page of the thread, in one request.
     *
     * Replaces the window rather than extending it. Keeping what was already
     * loaded and adding the end would leave a hole in the middle that the list
     * would draw as if it were continuous — two messages a week apart looking
     * like a reply to each other.
     */
    fun onJumpToEnd() {
        val id = threadId ?: return
        if (loadingMore) return

        loadingMore = true
        viewModelScope.launch {
            when (val result = repository.posts(id, before = FAR_FUTURE)) {
                is ForumResult.Success -> {
                    endReached = true
                    startReached = result.value.size < ForumRepository.DEFAULT_PAGE_SIZE
                    _position.update {
                        it.copy(windowStart = (it.total - result.value.size).coerceAtLeast(0))
                    }
                    show(result.value, keepingStart = true)
                }
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    /** Back to the opening post, which is an ordinary read from the top. */
    fun onJumpToStart() {
        if (loadingMore) return
        load()
    }

    /** Tracks where in the thread the reader is looking. */
    fun onTopVisible(index: Int) {
        _position.update { it.copy(offset = index) }
    }

    private fun show(posts: List<ForumPost>, keepingStart: Boolean) {
        if (!keepingStart) _position.update { it.copy(windowStart = 0) }
        _position.update { it.copy(loaded = posts.size) }
        _uiState.value = ForumScreenState.Content(posts.toImmutableList())
    }

    private fun load(refreshing: Boolean = false) {
        val id = threadId ?: return
        endReached = false
        startReached = true
        _position.update { it.copy(windowStart = 0, offset = 0) }
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
                is ForumResult.Success -> {
                    val visible = result.value.filterNot { it.authorId in hidden }
                    endReached = result.value.size < ForumRepository.DEFAULT_PAGE_SIZE
                    _position.update { it.copy(loaded = visible.size) }
                    ForumScreenState.Content(visible.toImmutableList())
                }

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/**
 * Where the reader is in a thread, and how long the thread is.
 *
 * [total] comes from the thread row the list already loaded, so opening a
 * thread costs no extra request to find out. It is a count of replies the
 * server maintains and can be a message or two out of step with what is
 * actually fetchable — a reply landing while the thread is being read, a
 * deletion not yet reflected — so it is never allowed to be smaller than what
 * has been loaded, and it is shown as a position rather than as an address.
 */
data class ThreadPosition(
    /** Messages before the top of the loaded window. */
    val windowStart: Int = 0,
    /** How far down the window the reader has scrolled. */
    val offset: Int = 0,
    /** How many messages the window holds. */
    val loaded: Int = 0,
    /** Every message in the thread, as far as anybody knows. */
    val total: Int = 0,
) {
    /** One-based, for a reader who does not count from zero. */
    val current: Int get() = (windowStart + offset + 1).coerceAtMost(size)

    val size: Int get() = maxOf(total, windowStart + loaded)

    /** Nothing to navigate when the whole thread is on one page. */
    val isPaged: Boolean get() = size > ForumRepository.DEFAULT_PAGE_SIZE
}

/**
 * Later than any message can have been written.
 *
 * "Everything before now" would race the clock: a reply posted in the same
 * second is written with the server's time, not the phone's, and a phone a few
 * seconds fast would ask for messages older than one that already exists.
 */
private val FAR_FUTURE = Instant.fromEpochSeconds(4_102_444_800)
