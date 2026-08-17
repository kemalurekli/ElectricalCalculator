package com.kemalurekli.electricalcalculator.features.forum.domain

/**
 * Who, if anyone, is signed in.
 *
 * Three states rather than a nullable profile, because "we have not looked
 * yet" and "nobody is signed in" call for different screens. Supabase restores
 * a stored session asynchronously on launch, so there is a real moment where
 * the answer is not known — and a sign-in button that flashes up for that
 * moment and then vanishes is worse than a blank space.
 */
sealed interface ForumSession {

    /** The stored session is still being read. Show nothing that could flicker. */
    data object Unknown : ForumSession

    data object SignedOut : ForumSession

    data class SignedIn(val profile: ForumProfile) : ForumSession

    /** The signed-in user's id, or `null` while unknown or signed out. */
    val userId: String?
        get() = (this as? SignedIn)?.profile?.id
}

/** Why a sign-in attempt did not produce a session. */
enum class ForumAuthFailure {

    /**
     * The reader dismissed the Google sheet. Not an error, and it must not be
     * reported as one — no message, no retry prompt.
     */
    CANCELLED,

    /**
     * There is no Google account on the device, or none that Credential Manager
     * would offer. Worth saying out loud, because the fix is outside the app.
     */
    NO_ACCOUNT,

    /** No usable connection. */
    NO_CONNECTION,

    /**
     * The build has no web client id, or Supabase rejected the token. Either
     * way it is a setup problem rather than something the reader can fix.
     */
    NOT_CONFIGURED,

    /** Anything else. */
    UNKNOWN,
}
