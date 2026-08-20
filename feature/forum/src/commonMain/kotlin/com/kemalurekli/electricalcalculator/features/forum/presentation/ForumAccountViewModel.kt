package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.auth.ForumSignIn
import com.kemalurekli.electricalcalculator.features.forum.auth.toSignInFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Signing in, signing out, and leaving for good. */
class ForumAccountViewModel(
    private val authRepository: ForumAuthRepository,
) : ViewModel() {

    val session: StateFlow<ForumSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ForumSession.Unknown)

    /** Asked of the capability the screen holds, because it is per-platform. */
    fun canSignIn(signIn: ForumSignIn): Boolean = signIn.isConfigured

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** The last thing that went wrong, for a one-shot message. Null when nothing has. */
    private val _failure = MutableStateFlow<ForumAuthFailure?>(null)
    val failure: StateFlow<ForumAuthFailure?> = _failure.asStateFlow()

    private val _deleteFailed = MutableStateFlow(false)
    val deleteFailed: StateFlow<Boolean> = _deleteFailed.asStateFlow()

    fun onSignIn(signIn: ForumSignIn) {
        if (_busy.value) return

        viewModelScope.launch {
            _busy.value = true
            _failure.value = null

            // Returning readers get a one-tap sheet of accounts they have used
            // here. Only when there are none do we widen it to every account on
            // the device — asking for all of them first would turn every return
            // into a full chooser.
            val credential = signIn.requestIdToken()
                .recoverCatching { first ->
                    if (first.toSignInFailure() == ForumAuthFailure.NO_ACCOUNT) {
                        signIn.requestIdToken(onlyPreviousAccounts = false).getOrThrow()
                    } else {
                        throw first
                    }
                }

            credential
                .mapCatching { authRepository.signInWithGoogle(it.idToken, it.rawNonce).getOrThrow() }
                .onFailure { _failure.value = it.toSignInFailure() }

            _busy.value = false
        }
    }

    fun onSignOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun onDeleteAccount() {
        if (_busy.value) return

        viewModelScope.launch {
            _busy.value = true
            _deleteFailed.value = authRepository.deleteAccount() is ForumResult.Failure
            _busy.value = false
        }
    }

    fun onFailureShown() {
        _failure.value = null
        _deleteFailed.value = false
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
