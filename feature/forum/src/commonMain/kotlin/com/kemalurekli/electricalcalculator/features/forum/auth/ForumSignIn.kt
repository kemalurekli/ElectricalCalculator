package com.kemalurekli.electricalcalculator.features.forum.auth

import androidx.compose.runtime.Composable
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure

/** An identity token and the nonce it was minted with. */
data class SignInCredential(val idToken: String, val rawNonce: String)

/**
 * Signing in to the forum.
 *
 * ### Why this is a platform capability
 *
 * Everything else about the forum is portable — supabase-kt and Ktor are
 * multiplatform, and the threads, replies and reports are plain suspend calls.
 * Signing in is not. Android asks Credential Manager for a Google ID token, and
 * Credential Manager has no iOS counterpart; iOS needs the Google Sign-In SDK
 * or `ASWebAuthenticationSession`, a separate OAuth client id, and — because
 * App Store guideline 4.8 requires an equivalent option beside any third-party
 * sign-in — Sign in with Apple as well.
 *
 * ### Why it comes from composition
 *
 * Credential Manager needs the Activity, not the application context: it has to
 * show a sheet over what is on screen. iOS will need the presenting view
 * controller for the same reason. Both are available in composition and awkward
 * anywhere else, so the split is at the implementation.
 */
interface ForumSignIn {

    /**
     * Whether this build can sign anyone in at all.
     *
     * False on iOS until Sign in with Apple is built, and false on Android when
     * `GOOGLE_WEB_CLIENT_ID` is absent from `local.properties` — a fork or a CI
     * build without the key gets a readable forum rather than a broken button.
     */
    val isConfigured: Boolean

    /**
     * Asks for an identity token, showing whatever sheet the platform shows.
     *
     * @param onlyPreviousAccounts first ask for accounts already used here,
     *   because returning readers are the common case and the sheet is then one
     *   tap. The caller retries with false when there are none. Asking for every
     *   account up front turns a one-tap return into a full chooser every time.
     */
    suspend fun requestIdToken(onlyPreviousAccounts: Boolean = true): Result<SignInCredential>
}

/** How a failed sign-in should read to the user. */
expect fun Throwable.toSignInFailure(): ForumAuthFailure

@Composable
expect fun rememberForumSignIn(): ForumSignIn
