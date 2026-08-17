package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumProfileViewModel @Inject constructor(
    private val authRepository: ForumAuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: String = savedStateHandle.toRoute<Route.ForumProfile>().userId

    private val _uiState =
        MutableStateFlow<ForumScreenState<ForumProfile>>(ForumScreenState.Loading)
    val uiState: StateFlow<ForumScreenState<ForumProfile>> = _uiState.asStateFlow()

    /**
     * Whether this is the reader looking at themselves.
     *
     * The same screen serves both cases; only the actions differ. Deriving it
     * from the session rather than passing a flag through the route means it
     * cannot be wrong — a stale `true` here would offer a rename button that
     * renames somebody else.
     */
    val isOwnProfile: StateFlow<Boolean> = authRepository.session
        .map { it.userId == userId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    private val _renameError = MutableStateFlow(false)
    val renameError: StateFlow<Boolean> = _renameError.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ForumScreenState.Loading
            _uiState.value = when (val result = authRepository.profile(userId)) {
                is ForumResult.Success -> ForumScreenState.Content(result.value)
                is ForumResult.Failure -> ForumScreenState.Error(result.reason)
            }
        }
    }

    fun onRename(displayName: String) {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            when (authRepository.updateDisplayName(trimmed)) {
                is ForumResult.Success -> {
                    _renameError.value = false
                    // Re-read rather than patch the local copy: the server may
                    // have trimmed or rejected it, and the screen should show
                    // what is actually stored.
                    load()
                }

                is ForumResult.Failure -> _renameError.value = true
            }
        }
    }

    fun onRenameErrorShown() {
        _renameError.value = false
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** The reader's own session, for screens that need to know whether to offer writing. */
@HiltViewModel
class ForumSessionViewModel @Inject constructor(
    private val authRepository: ForumAuthRepository,
) : ViewModel() {

    val session: StateFlow<ForumSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ForumSession.Unknown)

    fun onSignedInWithGoogle(idToken: String, nonce: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.signInWithGoogle(idToken, nonce).isSuccess)
        }
    }

    fun onSignOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
