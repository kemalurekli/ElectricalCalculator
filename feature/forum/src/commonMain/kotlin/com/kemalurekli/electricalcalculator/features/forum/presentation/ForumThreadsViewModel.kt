package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One category's threads, most recently active first. */
class ForumThreadsViewModel(
    private val repository: ForumRepository,
    languageRepository: AppLanguageRepository,
    authRepository: ForumAuthRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    /**
     * The reader's pinned threads, kept on this device.
     *
     * Held separately from the list rather than folded into it, so pinning
     * re-sorts what is already loaded instead of costing a request.
     */
    val pinned: StateFlow<Set<String>> = preferences.preferences
        .map { it.pinnedThreadIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptySet())

    fun onTogglePin(threadId: String) {
        viewModelScope.launch {
            preferences.setThreadPinned(threadId, threadId !in pinned.value)
        }
    }

    val session: StateFlow<ForumSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ForumSession.Unknown)

    private val _uiState =
        MutableStateFlow<ForumScreenState<ImmutableList<ForumThread>>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ImmutableList<ForumThread>>> = _uiState.asStateFlow()

    /**
     * Observed rather than captured, for the reason spelled out in
     * [ForumCategoriesViewModel]: this instance outlives a language change on
     * both platforms.
     *
     * A category belongs to one language's board, so when this changes the
     * category being shown no longer exists. Reloading would only prove that —
     * the screen watches this and leaves instead.
     */
    val language: StateFlow<ForumLanguage> = languageRepository.language
        .map(ForumLanguage::forApp)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ForumLanguage.forApp(languageRepository.language.value),
        )

    private var categoryId: String? = null

    /**
     * Which page is on screen, and how many there are.
     *
     * Unlike a thread, this list holds **one page at a time** and replaces it.
     * That is the whole of the difference, and it is why an offset is safe
     * here: a list that accumulates pages as the reader scrolls has to survive
     * rows moving underneath it — a reply anywhere lifts its thread to the top
     * and pushes the rest down, so the same thread arrives twice or not at all.
     * A list that throws its page away and asks for another one cannot have
     * that bug; a shifted row just means the reader sees the board as it is
     * now, which is what "page 3" means everywhere else on the web.
     */
    private val _position = MutableStateFlow(ThreadListPosition())
    val position: StateFlow<ThreadListPosition> = _position.asStateFlow()

    /**
     * The load in flight, so a second one replaces it rather than racing it.
     *
     * Without this the screen issued the same page three times: re-entering
     * composition restarts the load, and pressing the pager twice quickly used
     * to leave whichever request happened to land last on screen.
     */
    private var loadJob: Job? = null

    /**
     * @param threadCount what the category row said, so the pager can show a
     *   page count on the first frame instead of appearing once the request
     *   lands. It is a count the server maintains and may be a thread or two
     *   out of date, so a page that comes back short overrules it.
     */
    fun onOpen(id: String, threadCount: Int = 0) {
        // Reloads even for the category already held. The guard that used to
        // sit here was protecting against recompositions, but the caller is a
        // LaunchedEffect keyed on the id — it already fires once per entry into
        // composition, and the entry that matters is the one after coming back
        // from a thread. Skipping that left a thread the reader had just
        // deleted still sitting in the list.
        categoryId = id
        _position.value = ThreadListPosition(total = threadCount)
        load(page = 1, refreshing = uiState.value is ForumScreenState.Content)
    }

    /** Reloads the page being read, not the first one. */
    fun onRefresh() = load(page = _position.value.page, refreshing = true)

    fun onGoToPage(page: Int) {
        val target = page.coerceIn(1, _position.value.pageCount)
        if (target == _position.value.page) return
        load(page = target, refreshing = true)
    }

    private fun load(page: Int, refreshing: Boolean = false) {
        val id = categoryId ?: return
        // Whatever was being fetched was for a page the reader has left.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            val offset = (page - 1) * ForumRepository.THREADS_PER_PAGE
            val result = repository.threads(id, language.value, offset = offset)
            _uiState.value = when (result) {
                is ForumResult.Success -> {
                    _position.value = _position.value.after(page, result.value.size)
                    ForumScreenState.Content(result.value.toImmutableList())
                }

                // Leaves the position alone: the page the reader asked for is
                // still the page they asked for, and the pager is how they get
                // back off it.
                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}

/**
 * Which page of a category is showing, and how many there are.
 *
 * [total] is a running best guess rather than a fact. It starts from the count
 * on the category row and is corrected by what the server actually sends: a
 * short page is the last page and settles the count exactly, while a full one
 * only proves there is at least one more.
 */
data class ThreadListPosition(
    /** One-based, for a reader who does not count from zero. */
    val page: Int = 1,
    /** Threads in the category, as far as anybody knows. */
    val total: Int = 0,
) {
    val pageCount: Int
        get() = ((total + ForumRepository.THREADS_PER_PAGE - 1) / ForumRepository.THREADS_PER_PAGE)
            .coerceAtLeast(1)

    /** Nothing to navigate when the category fits on one page, and most do. */
    val isPaged: Boolean get() = pageCount > 1

    /** What the server just told us, folded into what we thought. */
    internal fun after(page: Int, loaded: Int): ThreadListPosition {
        val before = (page - 1) * ForumRepository.THREADS_PER_PAGE
        val total = if (loaded < ForumRepository.THREADS_PER_PAGE) {
            // The end of the board. Authoritative, and allowed to shrink the
            // count — threads get deleted, and a pager offering a page that no
            // longer exists sends the reader to an empty screen.
            before + loaded
        } else {
            // A full page proves there is more, but not how much. Claiming one
            // extra thread is what turns the next-page control on; the page
            // after that will say whether it was the truth.
            maxOf(this.total, before + loaded + 1)
        }
        return copy(page = page, total = total)
    }
}
