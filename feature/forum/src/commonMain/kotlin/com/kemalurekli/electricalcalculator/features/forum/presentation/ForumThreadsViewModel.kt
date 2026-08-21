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
     * Whether the server has run out of older threads.
     *
     * A page that comes back short is the last one — asking again would spend a
     * request to be told the same thing, and the list would keep asking every
     * time the reader reached the bottom.
     */
    private var endReached = false
    private var loadingMore = false

    /**
     * The load in flight, so a second one replaces it rather than racing it.
     *
     * Without this the screen issued the same page three times: re-entering
     * composition restarts the load, and a next-page request already on its way
     * carried a cursor computed from the list as it was before either landed.
     */
    private var loadJob: Job? = null

    /** Does nothing when the category is already loaded, so the screen's
     *  `LaunchedEffect` can be keyed on its argument without refetching. */
    fun onOpen(id: String) {
        // Reloads even for the category already held. The guard that used to
        // sit here was protecting against recompositions, but the caller is a
        // LaunchedEffect keyed on the id — it already fires once per entry into
        // composition, and the entry that matters is the one after coming back
        // from a thread. Skipping that left a thread the reader had just
        // deleted still sitting in the list.
        categoryId = id
        load(refreshing = uiState.value is ForumScreenState.Content)
    }

    fun onRefresh() = load(refreshing = true)

    /**
     * Fetches the next page and appends it.
     *
     * The cursor is the oldest loaded thread's last-reply time rather than an
     * offset. Offsets skip and repeat rows when something is posted while the
     * reader is scrolling, which is exactly when a forum list moves.
     */
    fun onLoadMore() {
        val id = categoryId ?: return
        val current = _uiState.value as? ForumScreenState.Content ?: return
        if (endReached || loadingMore) return

        loadingMore = true
        loadJob = viewModelScope.launch {
            val cursor = current.value.lastOrNull()?.lastReplyAt
            when (val result = repository.threads(id, language.value, before = cursor)) {
                is ForumResult.Success -> {
                    endReached = result.value.size < ForumRepository.DEFAULT_PAGE_SIZE
                    _uiState.value = ForumScreenState.Content(
                        (current.value + result.value).toImmutableList(),
                    )
                }
                // Keeps what is already on screen. A failed next page is not a
                // reason to throw away the page the reader is reading.
                is ForumResult.Failure -> Unit
            }
            loadingMore = false
        }
    }

    private fun load(refreshing: Boolean = false) {
        val id = categoryId ?: return
        endReached = false
        loadingMore = false
        // Whatever was being fetched was for the list as it used to be.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val current = _uiState.value
            if (refreshing && current is ForumScreenState.Content) {
                _uiState.value = current.copy(isRefreshing = true)
            } else {
                _uiState.value = ForumScreenState.Loading
            }

            _uiState.value = when (val result = repository.threads(id, language.value)) {
                is ForumResult.Success ->
                    ForumScreenState.Content(result.value.toImmutableList())

                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }
}
