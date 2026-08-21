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

    /** The address is not one the server will send to. */
    INVALID_EMAIL,

    /**
     * The code was wrong, or it was right an hour ago.
     *
     * One case rather than two on purpose. The server does not distinguish
     * them in a way worth passing on, and telling someone their code was
     * *correct but late* is an answer only an attacker benefits from.
     */
    INVALID_CODE,

    /**
     * Too many codes asked for, too quickly.
     *
     * Supabase rate-limits both per address and per project, and answers 429.
     * Worth its own case because it is the one failure here that is cured by
     * waiting rather than by trying something different.
     */
    TOO_MANY_REQUESTS,

    /** Anything else. */
    UNKNOWN,
}

/**
 * Why something was reported.
 *
 * A fixed list rather than a free-text box. Free text produces a queue nobody
 * can triage, and it asks the reporter to compose a sentence at the moment they
 * are annoyed — which is when people write things a moderator cannot act on.
 * The note is optional and comes after.
 */
enum class ForumReportReason(val key: String) {

    /** Advertising, link spam, repetition. */
    SPAM("spam"),

    /** Abuse, harassment, or anything aimed at a person rather than a question. */
    ABUSE("abuse"),

    /**
     * An answer that would be unsafe to follow.
     *
     * This forum is about mains electricity, so it needs a reason the others do
     * not cover: a confidently wrong answer here is not merely unhelpful.
     */
    UNSAFE_ADVICE("unsafe_advice"),

    /** Someone's personal details, or anything else that should not be public. */
    PRIVACY("privacy"),

    OTHER("other"),
}

/** What a report points at. Matches the `target_type` check in the schema. */
enum class ForumReportTarget(val key: String) {
    THREAD("thread"),
    POST("post"),
}
