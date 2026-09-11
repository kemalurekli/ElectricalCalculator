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

    /**
     * The window after the one on screen, appended.
     *
     * Scrolling still crosses pages: the bar is an address over a continuous
     * list, not a wall between chunks of it, so reaching the bottom of page
     * four carries on into page five rather than asking for permission.
     */
    fun onLoadMore() {
        val id = threadId ?: return
        val current = _uiState.value as? ForumScreenState.Content ?: return
        if (loadingMore) return
        val position = _position.value
        if (position.windowEnd >= position.size) return

        fetch(id, offset = position.windowEnd) { page ->
            show(current.value + page, windowStart = position.windowStart)
        }
    }

    /**
     * The page a reader asked for, by number, counting from one.
     *
     * Every way backwards goes through here — the pager's arrows, its ends and
     * its picker all name a page. Scrolling only ever goes forwards, and that
     * asymmetry is deliberate: a list that fetched upwards whenever it sat at
     * its own top walked itself back to page one the moment a jump put it
     * there, and a window that begins at page five has nothing above it for a
     * thumb to pull against anyway. Forwards is a gesture; backwards is an
     * address.
     */
    fun onGoToPage(page: Int) {
        val id = threadId ?: return
        if (loadingMore) return
        val target = page.coerceIn(1, _position.value.pageCount)
        val offset = (target - 1) * ForumRepository.POSTS_PER_PAGE

        fetch(id, offset = offset) { window ->
            // The window is replaced rather than extended. Keeping what was
            // loaded and adding a distant page would leave a hole the list
            // draws as continuous, with two messages a week apart looking like
            // a reply to each other.
            show(window, windowStart = offset, offsetInWindow = 0)
        }
    }

    fun onNextPage() = onGoToPage(_position.value.page + 1)

    fun onPreviousPage() = onGoToPage(_position.value.page - 1)

    fun onJumpToStart() = onGoToPage(1)

    fun onJumpToEnd() = onGoToPage(_position.value.pageCount)

    /** Tracks which message is at the top of the viewport. */
    fun onTopVisible(index: Int) {
        _position.update { it.copy(offsetInWindow = index) }
    }

    private fun fetch(
        id: String,
        offset: Int,
        limit: Int = ForumRepository.POSTS_PER_PAGE,
        onLoaded: (List<ForumPost>) -> Unit,
    ) {
        if (limit <= 0) return
        loadingMore = true
        viewModelScope.launch {
            when (val result = repository.posts(id, limit = limit, offset = offset)) {
                is ForumResult.Success -> onLoaded(result.value)
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    private fun show(
        posts: List<ForumPost>,
        windowStart: Int,
        offsetInWindow: Int = _position.value.offsetInWindow,
    ) {
        _position.update {
            it.copy(
                windowStart = windowStart,
                loaded = posts.size,
                offsetInWindow = offsetInWindow,
                // The thread is at least as long as what has been seen of it.
                total = maxOf(it.total, windowStart + posts.size),
            )
        }
        _uiState.value = ForumScreenState.Content(posts.toImmutableList())
    }

    private fun load(refreshing: Boolean = false) {
        val id = threadId ?: return
        _position.update { it.copy(windowStart = 0, offsetInWindow = 0) }
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
                    _position.update {
                        it.copy(loaded = visible.size, total = maxOf(it.total, visible.size))
                    }
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
 * ### Pages are an address, not a container
 *
 * Nothing here chops the list into tens. The window is whatever has been
 * loaded, a reader scrolls straight through a page boundary, and [page] is
 * arithmetic on where they have got to. That is the difference between a page
 * number that helps — a place you can name, jump to and come back to — and one
 * that interrupts a read every ten messages to ask for permission to continue.
 *
 * [total] starts from the thread row the list already loaded, so opening a
 * thread costs no extra request to learn its length. It is a count the server
 * maintains and can be a message or two out of step with what is actually
 * fetchable, so it is never allowed to be smaller than what has been seen.
 */
data class ThreadPosition(
    /** Messages before the top of the loaded window. */
    val windowStart: Int = 0,
    /** How far down the window the reader has scrolled. */
    val offsetInWindow: Int = 0,
    /** How many messages the window holds. */
    val loaded: Int = 0,
    /** Every message in the thread, as far as anybody knows. */
    val total: Int = 0,
) {
    /** Where the window ends, as an offset into the thread. */
    val windowEnd: Int get() = windowStart + loaded

    val size: Int get() = maxOf(total, windowEnd)

    /** One-based, for a reader who does not count from zero. */
    val message: Int get() = (windowStart + offsetInWindow + 1).coerceAtMost(size)

    val page: Int get() = (message - 1) / ForumRepository.POSTS_PER_PAGE + 1

    val pageCount: Int
        get() = ((size + ForumRepository.POSTS_PER_PAGE - 1) / ForumRepository.POSTS_PER_PAGE)
            .coerceAtLeast(1)

    /** Nothing to navigate when the whole thread is one page. */
    val isPaged: Boolean get() = pageCount > 1
}

