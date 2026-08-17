package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread

/**
 * The forum, as the app reads it.
 *
 * ### Why these are suspend calls and not flows
 *
 * Every other repository here exposes a `Flow`, because everything else is
 * local: Room emits again when a row changes, and the screen is always looking
 * at the truth. A remote list has no such signal. A `Flow` would either be a
 * one-shot dressed as a stream, or a poll — and both would tell the reader that
 * the screen is live when it is not.
 *
 * So the forum asks once, shows what it got, and offers a refresh. That is
 * honest about what it knows.
 *
 * ### Why nothing here caches
 *
 * The rest of this app works on a plane. The forum cannot, and pretending
 * otherwise — showing yesterday's threads as though they were current — would
 * be a worse failure than an empty screen that says why it is empty.
 */
interface ForumRepository {

    /** The sections for a language, in the order the dashboard defines. */
    suspend fun categories(language: ForumLanguage): ForumResult<List<ForumCategory>>

    /**
     * Threads in a category, most recently active first.
     *
     * @param before pass the last thread's `lastReplyAt` to fetch the next
     *   page. Null starts from the top.
     */
    suspend fun threads(
        categoryId: String,
        language: ForumLanguage,
        limit: Int = DEFAULT_PAGE_SIZE,
        before: java.time.Instant? = null,
    ): ForumResult<List<ForumThread>>

    /** One thread's messages, oldest first, with the opening post at the top. */
    suspend fun posts(
        threadId: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        after: java.time.Instant? = null,
    ): ForumResult<List<ForumPost>>

    /**
     * Opens a thread, and returns its id so the caller can go straight to it.
     *
     * The title and the opening message are written together because a thread
     * with no message is not a question — the schema stores the opening post as
     * an ordinary row, so this is two inserts, and a failure between them would
     * leave an empty thread in a list somebody is reading.
     */
    suspend fun createThread(
        categoryId: String,
        language: ForumLanguage,
        title: String,
        body: String,
    ): ForumResult<String>

    suspend fun createReply(threadId: String, body: String): ForumResult<Unit>

    /** Edits one's own message. The server refuses anyone else's. */
    suspend fun updatePost(postId: String, body: String): ForumResult<Unit>

    /**
     * Removes one's own message, for good.
     *
     * The row goes rather than being flagged. A trigger copies the text, the
     * author and the remover into `forum_deleted_posts` first, so nothing that
     * moderation needs is lost — and once that archive existed, keeping a
     * hidden second copy in the live table preserved nothing.
     */
    suspend fun deletePost(postId: String): ForumResult<Unit>

    /**
     * Removes one's own thread, which is only possible while nobody has
     * answered it.
     *
     * The moment somebody replies the thread stops being one person's to
     * erase: the replies are other people's work, and taking the question away
     * would leave them answering nothing. The server enforces this — the
     * delete policy carries `reply_count = 0` — so the caller must be ready for
     * a refusal even when the button was showing.
     */
    suspend fun deleteThread(threadId: String): ForumResult<Unit>

    /**
     * Thanks a message, or takes it back.
     *
     * Nobody can thank their own — the policy refuses it, and the button is not
     * offered. Inflating one's own counter is otherwise a two-tap job.
     */
    suspend fun setThanks(postId: String, thanked: Boolean): ForumResult<Unit>

    companion object {
        /**
         * Enough to fill a phone screen twice over.
         *
         * Small enough that a slow connection returns something quickly, large
         * enough that most threads need no second request at all.
         */
        const val DEFAULT_PAGE_SIZE = 25
    }
}
