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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthErrorCode

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

    /** Which half of the email route the reader is on. */
    sealed interface EmailStep {
        /** Typing an address. */
        data object Address : EmailStep

        /** A code has been sent to [email]; typing it in. */
        data class Code(val email: String) : EmailStep
    }

    private val _emailStep = MutableStateFlow<EmailStep>(EmailStep.Address)
    val emailStep: StateFlow<EmailStep> = _emailStep.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value.trim()
        _failure.value = null
    }

    fun onCodeChange(value: String) {
        // Digits only, six of them. The field is the wrong place to learn that
        // a letter was never going to work.
        _code.value = value.filter(Char::isDigit).take(CODE_LENGTH)
        _failure.value = null
    }

    /** Back to the address, keeping it, for a typo spotted after sending. */
    fun onUseAnotherAddress() {
        _emailStep.value = EmailStep.Address
        _code.value = ""
        _failure.value = null
    }

    /**
     * Seconds until another code may be asked for, or zero.
     *
     * Counted down rather than left to the server to refuse. Supabase allows
     * one code per address per minute, and — the part that matters — thirty an
     * hour across the whole project. That second limit is shared, so somebody
     * tapping "send again" out of impatience is not spending their own
     * allowance, they are spending everyone's. A button that cannot be tapped
     * costs nothing and says what the wait is.
     */
    private val _resendIn = MutableStateFlow(0)
    val resendIn: StateFlow<Int> = _resendIn.asStateFlow()

    private var cooldownJob: Job? = null

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _resendIn.value = RESEND_COOLDOWN_SECONDS
            while (_resendIn.value > 0) {
                delay(1_000)
                _resendIn.value -= 1
            }
        }
    }

    fun onSendCode() {
        val address = _email.value
        if (_busy.value || _resendIn.value > 0) return

        // Checked here as well as by the server, because asking is rate
        // limited: a missing @ should not spend one of the codes this address
        // is allowed in an hour.
        if (!address.isPlausibleEmail()) {
            _failure.value = ForumAuthFailure.INVALID_EMAIL
            return
        }

        viewModelScope.launch {
            _busy.value = true
            _failure.value = null
            authRepository.requestEmailCode(address)
                .onSuccess {
                    _emailStep.value = EmailStep.Code(address)
                    _code.value = ""
                    startCooldown()
                }
                .onFailure { _failure.value = it.toEmailFailure() }
            _busy.value = false
        }
    }

    fun onVerifyCode() {
        val step = _emailStep.value as? EmailStep.Code ?: return
        if (_busy.value || _code.value.length < CODE_LENGTH) return

        viewModelScope.launch {
            _busy.value = true
            _failure.value = null
            authRepository.signInWithEmailCode(step.email, _code.value)
                .onSuccess {
                    // Nothing to announce: the session flow carries the news,
                    // and every screen watching it reacts on its own.
                    _emailStep.value = EmailStep.Address
                    _email.value = ""
                    _code.value = ""
                }
                .onFailure { _failure.value = it.toEmailFailure() }
            _busy.value = false
        }
    }

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
                .mapCatching { authRepository.signIn(it).getOrThrow() }
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
        const val CODE_LENGTH = 6

        /** Supabase's own per-address limit, so the app refuses before the server does. */
        const val RESEND_COOLDOWN_SECONDS = 60
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/**
 * Whether an address is worth spending a rate-limited request on.
 *
 * Deliberately loose. The one authority on whether an address exists is the
 * server that tries to reach it, and a client-side rule strict enough to be
 * interesting is a rule that rejects somebody's real address — the plus signs,
 * the new top-level domains, the apostrophes. This only catches what could not
 * possibly work.
 */
internal fun String.isPlausibleEmail(): Boolean {
    val at = indexOf('@')
    if (at <= 0 || at != lastIndexOf('@')) return false
    val domain = substring(at + 1)
    return domain.length >= 3 && '.' in domain.drop(1).dropLast(1) && none(Char::isWhitespace)
}

/**
 * How a failed email sign-in should read.
 *
 * Reads the error Supabase actually returned rather than the words in the
 * exception's message. The first version of this matched on the substring
 * "invalid", which meant every unrelated failure — a rejected key, a
 * misconfigured project, a 500 — arrived on screen as "the code is wrong, or
 * it has expired". That is the worst kind of error message: confident, wrong,
 * and it sends the reader to check the one thing that was fine.
 *
 * Anything not recognised is [ForumAuthFailure.UNKNOWN] and is printed with
 * its code, so the next unmapped case is a line in the log rather than another
 * confident lie.
 */
private fun Throwable.toEmailFailure(): ForumAuthFailure = when {
    this is kotlinx.io.IOException -> ForumAuthFailure.NO_CONNECTION

    this is AuthRestException -> when (errorCode) {
        AuthErrorCode.OtpExpired -> ForumAuthFailure.INVALID_CODE
        AuthErrorCode.OverEmailSendRateLimit,
        AuthErrorCode.OverRequestRateLimit,
        -> ForumAuthFailure.TOO_MANY_REQUESTS
        AuthErrorCode.ValidationFailed -> ForumAuthFailure.INVALID_EMAIL
        // The project is not set up for this, which is nothing the reader can
        // act on and must not be dressed as a mistyped code.
        AuthErrorCode.OtpDisabled,
        AuthErrorCode.EmailProviderDisabled,
        AuthErrorCode.SignupDisabled,
        -> ForumAuthFailure.NOT_CONFIGURED
        else -> unmapped(error, statusCode)
    }

    this is RestException -> unmapped(error, statusCode)

    else -> ForumAuthFailure.UNKNOWN
}

/**
 * A failure with no name of its own, recorded before it is generalised.
 *
 * The status is a last resort: a 401 or 403 on a code being verified really is
 * a code the server would not take, whatever it chose to call it.
 */
private fun unmapped(code: String, status: Int): ForumAuthFailure {
    println("VoltageBoard: unmapped auth failure — status=$status code=$code")
    return when (status) {
        401, 403 -> ForumAuthFailure.INVALID_CODE
        422 -> ForumAuthFailure.INVALID_EMAIL
        429 -> ForumAuthFailure.TOO_MANY_REQUESTS
        else -> ForumAuthFailure.UNKNOWN
    }
}
