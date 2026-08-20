package com.kemalurekli.electricalcalculator.features.forum.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure

/**
 * Not built yet.
 *
 * Two things are needed and neither is a port. Google sign-in on iOS goes
 * through its own SDK or `ASWebAuthenticationSession` with a separate OAuth
 * client id — Credential Manager does not exist here. And App Store guideline
 * 4.8 requires an equivalent option beside it, which means Sign in with Apple:
 * Supabase supports the provider, so the work is in this client and in the
 * dashboard.
 *
 * Until then `isConfigured` is false and the account section says so. The
 * forum is readable — the threads are public and the anon key is enough to
 * fetch them — and nothing offers a button that cannot work.
 */
@Composable
actual fun rememberForumSignIn(): ForumSignIn = remember { UnconfiguredSignIn }

private object UnconfiguredSignIn : ForumSignIn {
    override val isConfigured: Boolean = false

    override suspend fun requestIdToken(onlyPreviousAccounts: Boolean): Result<SignInCredential> =
        Result.failure(IllegalStateException("Sign-in is not available on iOS yet"))
}

actual fun Throwable.toSignInFailure(): ForumAuthFailure = ForumAuthFailure.NOT_CONFIGURED
