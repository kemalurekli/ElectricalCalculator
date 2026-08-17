package com.kemalurekli.electricalcalculator.features.forum.domain

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import java.time.Instant
import java.util.Locale

/**
 * Which forum a reader sees.
 *
 * The forum is split strictly by language: a Turkish reader sees Turkish
 * categories and threads, an English reader sees English ones, and there is no
 * "show everything" switch. Two reasons, and the second is the load-bearing
 * one.
 *
 * Content nobody can read is noise, not choice. And a forum that looks empty
 * reads as abandoned — showing each reader only their own language keeps every
 * list as full as it can honestly be.
 */
enum class ForumLanguage(val code: String) {
    TURKISH("tr"),
    ENGLISH("en"),
    ;

    companion object {
        /**
         * The forum for the language the app is currently showing.
         *
         * [AppLanguage.SYSTEM] means the app follows the device, so the device
         * locale decides. Anything that is not Turkish falls to English, which
         * is the app's own fallback everywhere else.
         */
        fun forApp(language: AppLanguage, locale: Locale = Locale.getDefault()): ForumLanguage {
            val tag = language.languageTag ?: locale.language
            return if (tag.equals(TURKISH.code, ignoreCase = true)) TURKISH else ENGLISH
        }
    }
}

/** A section of the forum. Entered by hand in the dashboard, never by the app. */
data class ForumCategory(
    val id: String,
    val key: String,
    val title: String,
    val description: String,
    val threadCount: Int = 0,
)

/**
 * A question, as the thread list shows it.
 *
 * Carries the author's display name rather than an id: the list would otherwise
 * need a second round trip to render a single line of text.
 */
data class ForumThread(
    val id: String,
    val categoryId: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val createdAt: Instant,
    val lastReplyAt: Instant,
    val replyCount: Int,
    val isLocked: Boolean,
)

/**
 * One message. The thread's opening message is one of these too, flagged.
 *
 * [thankedByMe] is null until it is known — a signed-out reader is not "has not
 * thanked", they are "cannot thank", and the button has to tell those apart.
 */
data class ForumPost(
    val id: String,
    val threadId: String,
    val authorId: String,
    val authorName: String,
    val body: String,
    val isOpeningPost: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val thanksCount: Int,
    val thankedByMe: Boolean? = null,
)

/** A member, and the two numbers the forum makes public about them. */
data class ForumProfile(
    val id: String,
    val displayName: String,
    val joinedAt: Instant,
    val postCount: Int,
    val thanksReceived: Int,
)

/**
 * What came back, or why nothing did.
 *
 * The app's own `Outcome` type carries a `ValidationError` and describes a form
 * the user filled in wrongly. Nothing here is the user's fault, and the three
 * failures below need different words on screen, so they are their own type.
 */
sealed interface ForumResult<out T> {

    data class Success<T>(val value: T) : ForumResult<T>

    data class Failure(val reason: ForumFailure) : ForumResult<Nothing>
}

enum class ForumFailure {
    /** This build has no Supabase credentials. Not the reader's problem to solve. */
    NOT_CONFIGURED,

    /** The device is offline, or the request timed out. Worth a retry. */
    NO_CONNECTION,

    /** The server answered, and the answer was not one we can use. */
    SERVER_ERROR,
}

/** The value, or null when the call failed. */
fun <T> ForumResult<T>.getOrNull(): T? = (this as? ForumResult.Success)?.value
