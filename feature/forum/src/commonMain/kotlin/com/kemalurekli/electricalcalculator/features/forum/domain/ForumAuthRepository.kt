package com.kemalurekli.electricalcalculator.features.forum.domain

import com.kemalurekli.electricalcalculator.features.forum.auth.SignInCredential
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in reader: who they are, and how they stop being signed in.
 *
 * Split from [ForumRepository] because the two answer to different things.
 * Reading the forum needs no account and must keep working without one; this
 * interface is only consulted when something is about to be written, or when
 * the reader asks about themselves.
 */
interface ForumAuthRepository {

    /**
     * The current session, re-emitting on sign-in, sign-out and token refresh.
     *
     * Starts at [ForumSession.Unknown] and stays there until the stored session
     * has been read, so nothing renders a sign-in button it is about to retract.
     */
    val session: Flow<ForumSession>

    /**
     * Exchanges a Google ID token for a Supabase session.
     *
     * The token comes from Credential Manager; this call does not show any UI.
     * The two are kept apart because obtaining a credential needs an Activity
     * and this does not, which is what lets the repository stay testable.
     */
    /**
     * Exchanges an identity token for a session.
     *
     * [provider] rather than one method per provider: Supabase takes it as
     * a parameter, and the two platforms differ only in which one they can
     * obtain a token from.
     */
    suspend fun signIn(credential: SignInCredential): Result<Unit>

    suspend fun signOut()

    /** A profile by id — the reader's own, or anyone whose name was tapped. */
    suspend fun profile(userId: String): ForumResult<ForumProfile>

    /**
     * Renames the signed-in reader.
     *
     * The name the provider supplied is only a starting point; people post under
     * something else and are entitled to.
     */
    suspend fun updateDisplayName(displayName: String): ForumResult<Unit>

    /**
     * Erases the account: profile, threads and posts, then the auth user.
     *
     * Play requires an in-app route to this for any app that holds an account,
     * and it must be real deletion rather than a support request. Runs as a
     * `security definer` function on the server, because the anon key cannot
     * touch `auth.users` and must not be able to.
     */
    suspend fun deleteAccount(): ForumResult<Unit>
}

/** Maps a sign-in throwable onto the reasons the UI knows how to speak about. */
fun Throwable.toForumAuthFailure(): ForumAuthFailure = when (this) {
    is kotlinx.io.IOException -> ForumAuthFailure.NO_CONNECTION
    else -> ForumAuthFailure.UNKNOWN
}
