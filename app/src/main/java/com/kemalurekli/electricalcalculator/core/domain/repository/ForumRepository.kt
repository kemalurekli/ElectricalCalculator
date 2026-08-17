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
