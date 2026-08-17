package com.kemalurekli.electricalcalculator.features.forum.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.auth.GoogleCredentialProvider
import com.kemalurekli.electricalcalculator.features.forum.auth.toGoogleSignInFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Signing in, signing out, and leaving for good. */
@HiltViewModel
class ForumAccountViewModel @Inject constructor(
    private val authRepository: ForumAuthRepository,
    private val googleCredentials: GoogleCredentialProvider,
) : ViewModel() {

    val session: StateFlow<ForumSession> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ForumSession.Unknown)

    val canSignIn: Boolean get() = googleCredentials.isConfigured

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** The last thing that went wrong, for a one-shot message. Null when nothing has. */
    private val _failure = MutableStateFlow<ForumAuthFailure?>(null)
    val failure: StateFlow<ForumAuthFailure?> = _failure.asStateFlow()

    private val _deleteFailed = MutableStateFlow(false)
    val deleteFailed: StateFlow<Boolean> = _deleteFailed.asStateFlow()

    fun onSignIn(activityContext: Context) {
        if (_busy.value) return

        viewModelScope.launch {
            _busy.value = true
            _failure.value = null

            // Returning readers get a one-tap sheet of accounts they have used
            // here. Only when there are none do we widen it to every account on
            // the device — asking for all of them first would turn every return
            // into a full chooser.
            val credential = googleCredentials.requestIdToken(activityContext)
                .recoverCatching { first ->
                    if (first.toGoogleSignInFailure() == ForumAuthFailure.NO_ACCOUNT) {
                        googleCredentials
                            .requestIdToken(activityContext, filterByAuthorizedAccounts = false)
                            .getOrThrow()
                    } else {
                        throw first
                    }
                }

            credential
                .mapCatching { authRepository.signInWithGoogle(it.idToken, it.rawNonce).getOrThrow() }
                .onFailure { _failure.value = it.toGoogleSignInFailure() }

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
