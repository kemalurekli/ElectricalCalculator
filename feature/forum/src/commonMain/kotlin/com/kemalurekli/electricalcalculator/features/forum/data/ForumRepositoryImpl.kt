package com.kemalurekli.electricalcalculator.features.forum.data

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportTarget
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant
import kotlin.time.Clock

class ForumRepositoryImpl(
    private val backend: ForumBackend,
    private val ioDispatcher: CoroutineDispatcher,
) : ForumRepository {

    override suspend fun categories(language: ForumLanguage) = query {
        it.postgrest.from(TABLE_CATEGORIES)
            // The thread count comes back with the row as an aggregate rather
            // than as six more requests, or as a counter column that would need
            // a trigger and could drift out of step with the threads it counts.
            .select(Columns.raw("id, key, title, description, threads:forum_threads(count)")) {
                filter {
                    eq("language", language.code)
                    eq("is_active", true)
                }
                order("position", Order.ASCENDING)
            }
            .decodeList<CategoryDto>()
            .map(CategoryDto::toDomain)
    }

    override suspend fun threads(
        categoryId: String,
        language: ForumLanguage,
        limit: Int,
        before: Instant?,
    ) = query {
        it.postgrest.from(TABLE_THREADS)
            // The author's name is embedded rather than fetched separately: a
            // list of twenty threads would otherwise be twenty-one requests to
            // render one line of text each.
            .select(Columns.raw(THREAD_COLUMNS)) {
                filter {
                    eq("category_id", categoryId)
                    eq("language", language.code)
                    eq("is_deleted", false)
                    if (before != null) lt("last_reply_at", before.toString())
                }
                order("last_reply_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ThreadDto>()
            .map(ThreadDto::toDomain)
    }

    override suspend fun posts(threadId: String, limit: Int, offset: Int) = query {
        it.postgrest.from(TABLE_POSTS)
            .select(Columns.raw(POST_COLUMNS)) {
                filter {
                    eq("thread_id", threadId)
                    eq("is_deleted", false)
                }
                // Oldest first, so the opening post is at the top, a reply
                // reads after whatever it is replying to, and message number
                // four is the fourth row whichever window it arrives in.
                order("created_at", Order.ASCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<PostDto>()
            .map(PostDto::toDomain)
            .withThanksBy(it)
    }

    /**
     * Marks which of these the signed-in reader has already thanked.
     *
     * One extra request for the whole page rather than a column on each post:
     * PostgREST can only embed the *rows* of forum_thanks, which for a popular
     * message means downloading every thanks to learn one boolean.
     *
     * Signed out, the answer stays null — not false. The button is not shown at
     * all in that case, and claiming "you have not thanked this" to someone who
     * has no account would be a different statement than the truth.
     */
    private suspend fun List<ForumPost>.withThanksBy(client: SupabaseClient): List<ForumPost> {
        val user = client.auth.currentUserOrNull()?.id ?: return this
        if (isEmpty()) return this

        val thanked = client.postgrest.from(TABLE_THANKS)
            .select(Columns.list("post_id")) {
                filter {
                    eq("user_id", user)
                    isIn("post_id", map { post -> post.id })
                }
            }
            .decodeList<ThanksDto>()
            .mapTo(mutableSetOf()) { row -> row.postId }

        return map { post -> post.copy(thankedByMe = post.id in thanked) }
    }

    override suspend fun createThread(
        categoryId: String,
        language: ForumLanguage,
        title: String,
        body: String,
    ) = query { client ->
        // One request, because a function body is one transaction. As two
        // inserts a dropped connection between them left a thread with no
        // message in it — titled, listed, opening onto nothing.
        client.postgrest.rpc(
            RPC_CREATE_THREAD,
            buildJsonObject {
                put("p_category_id", categoryId)
                put("p_language", language.code)
                put("p_title", title)
                put("p_body", body)
            },
        ).decodeAs<String>()
    }

    override suspend fun createReply(threadId: String, body: String) = query { client ->
        client.postgrest.from(TABLE_POSTS).insert(
            buildJsonObject {
                put("thread_id", threadId)
                put("author_id", client.requireUserId())
                put("body", body)
            },
        )
        Unit
    }

    override suspend fun updatePost(postId: String, body: String) = query { client ->
        client.postgrest.from(TABLE_POSTS).update(
            buildJsonObject {
                put("body", body)
                put("edited_at", Clock.System.now().toString())
            },
        ) { filter { eq("id", postId) } }
        Unit
    }

    override suspend fun deletePost(postId: String) = query { client ->
        client.postgrest.from(TABLE_POSTS)
            .delete {
                select(Columns.list("id"))
                filter { eq("id", postId) }
            }
            .decodeList<IdDto>()
            .requireDeleted()
    }

    override suspend fun deleteThread(threadId: String) = query { client ->
        // The posts go with it through the schema's cascade, and each one is
        // archived on its way out by the same trigger that handles a single
        // deletion. Nothing here has to walk the thread.
        client.postgrest.from(TABLE_THREADS)
            .delete {
                select(Columns.list("id"))
                filter { eq("id", threadId) }
            }
            .decodeList<IdDto>()
            .requireDeleted()
    }

    /**
     * Turns "the policy refused this" into a failure.
     *
     * A DELETE that no policy allows is not an error to PostgREST — it matches
     * no rows and reports success, so a refused deletion and a completed one
     * look identical. Asking for the deleted rows back is what tells them
     * apart, and without it the app navigates away from a thread that is still
     * there: exactly what happens when somebody replies a moment before the
     * delete lands, which is the case the reply_count rule exists for.
     */
    private fun List<IdDto>.requireDeleted() {
        if (isEmpty()) error("the server refused the deletion")
    }

    override suspend fun setThanks(postId: String, thanked: Boolean) = query { client ->
        val user = client.requireUserId()
        if (thanked) {
            client.postgrest.from(TABLE_THANKS).insert(
                buildJsonObject {
                    put("post_id", postId)
                    put("user_id", user)
                },
            )
        } else {
            client.postgrest.from(TABLE_THANKS).delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", user)
                }
            }
        }
        Unit
    }

    override suspend fun report(
        target: ForumReportTarget,
        targetId: String,
        reason: ForumReportReason,
        note: String,
    ) = query { client ->
        client.postgrest.from(TABLE_REPORTS).insert(
            buildJsonObject {
                put("reporter_id", client.requireUserId())
                put("target_type", target.key)
                put("target_id", targetId)
                // The key first so the queue can be sorted and counted without
                // reading prose, the note after for whatever the list misses.
                put("reason", listOf(reason.key, note.trim()).filter { it.isNotEmpty() }
                    .joinToString(" — ").take(REASON_MAX_LENGTH))
            },
        )
        Unit
    }

    override suspend fun block(userId: String) = query { client ->
        client.postgrest.from(TABLE_BLOCKS).insert(
            buildJsonObject {
                put("blocker_id", client.requireUserId())
                put("blocked_id", userId)
            },
        )
        Unit
    }

    override suspend fun unblock(userId: String) = query { client ->
        client.postgrest.from(TABLE_BLOCKS).delete {
            filter {
                eq("blocker_id", client.requireUserId())
                eq("blocked_id", userId)
            }
        }
        Unit
    }

    override suspend fun blockedUserIds() = query { client ->
        val user = client.auth.currentUserOrNull()?.id
            ?: return@query emptySet<String>()

        client.postgrest.from(TABLE_BLOCKS)
            .select(Columns.list("blocked_id")) { filter { eq("blocker_id", user) } }
            .decodeList<BlockDto>()
            .mapTo(mutableSetOf()) { it.blockedId }
    }

    /**
     * The signed-in user's id, for a call that has no meaning without one.
     *
     * The policies would refuse an anonymous write anyway; failing here turns
     * that into a clear error instead of a rejected request that looks like a
     * server fault.
     */
    private fun SupabaseClient.requireUserId(): String =
        auth.currentUserOrNull()?.id ?: error("this call needs a signed-in user")

    /**
     * Runs [block] against the backend, turning anything that goes wrong into a
     * reason the screen can put into words.
     *
     * The three failures need different sentences: an unconfigured build is not
     * the reader's problem, a dropped connection is worth a retry, and a server
     * error is worth neither. Collapsing them into one "something went wrong"
     * would leave the reader tapping refresh at a build that has no backend.
     */
    private suspend fun <T> query(block: suspend (SupabaseClient) -> T): ForumResult<T> {
        val client = when (backend) {
            is ForumBackend.Available -> backend.client
            ForumBackend.NotConfigured -> return ForumResult.Failure(ForumFailure.NOT_CONFIGURED)
        }

        return withContext(ioDispatcher) {
            try {
                ForumResult.Success(block(client))
            } catch (e: kotlinx.io.IOException) {
                // No route to the host, DNS failure, timeout — all of them mean
                // the request never got an answer, and all of them are worth
                // trying again from the same screen.
                ForumResult.Failure(ForumFailure.NO_CONNECTION)
            } catch (e: Exception) {
                // A rejected policy, a schema that has moved on, a malformed
                // row. The reader can do nothing about any of it.
                ForumResult.Failure(ForumFailure.SERVER_ERROR)
            }
        }
    }

    // -- Wire format ---------------------------------------------------------

    @Serializable
    private data class CategoryDto(
        val id: String,
        val key: String,
        val title: String,
        val description: String = "",
        /** PostgREST returns an aggregate embed as a one-element array. */
        val threads: List<CountDto> = emptyList(),
    ) {
        fun toDomain() = ForumCategory(
            id = id,
            key = key,
            title = title,
            description = description,
            threadCount = threads.firstOrNull()?.count ?: 0,
        )
    }

    @Serializable
    private data class CountDto(val count: Int = 0)

    @Serializable
    private data class IdDto(val id: String)

    @Serializable
    private data class ThanksDto(@SerialName("post_id") val postId: String)

    @Serializable
    private data class BlockDto(@SerialName("blocked_id") val blockedId: String)

    /** PostgREST returns an embedded row as an object, hence the nested type. */
    @Serializable
    private data class AuthorDto(
        @SerialName("display_name") val displayName: String = "",
        @SerialName("post_count") val postCount: Int = 0,
        @SerialName("thanks_received") val thanksReceived: Int = 0,
    )

    @Serializable
    private data class ThreadDto(
        val id: String,
        @SerialName("category_id") val categoryId: String,
        val title: String,
        @SerialName("author_id") val authorId: String,
        val author: AuthorDto? = null,
        @SerialName("created_at") val createdAt: String,
        @SerialName("last_reply_at") val lastReplyAt: String,
        @SerialName("reply_count") val replyCount: Int = 0,
        @SerialName("is_locked") val isLocked: Boolean = false,
    ) {
        fun toDomain() = ForumThread(
            id = id,
            categoryId = categoryId,
            title = title,
            authorId = authorId,
            authorName = author?.displayName.orEmpty(),
            createdAt = Instant.parse(createdAt),
            lastReplyAt = Instant.parse(lastReplyAt),
            replyCount = replyCount,
            isLocked = isLocked,
        )
    }

    @Serializable
    private data class PostDto(
        val id: String,
        @SerialName("thread_id") val threadId: String,
        @SerialName("author_id") val authorId: String,
        val author: AuthorDto? = null,
        val body: String,
        @SerialName("is_opening_post") val isOpeningPost: Boolean = false,
        @SerialName("created_at") val createdAt: String,
        @SerialName("edited_at") val editedAt: String? = null,
        @SerialName("thanks_count") val thanksCount: Int = 0,
    ) {
        fun toDomain() = ForumPost(
            id = id,
            threadId = threadId,
            authorId = authorId,
            authorName = author?.displayName.orEmpty(),
            authorPostCount = author?.postCount ?: 0,
            authorThanksReceived = author?.thanksReceived ?: 0,
            body = body,
            isOpeningPost = isOpeningPost,
            createdAt = Instant.parse(createdAt),
            editedAt = editedAt?.let(Instant::parse),
            thanksCount = thanksCount,
        )
    }

    private companion object {
        const val TAG = "ForumRepository"

        const val TABLE_CATEGORIES = "forum_categories"
        const val TABLE_THREADS = "forum_threads"
        const val TABLE_POSTS = "forum_posts"
        const val TABLE_THANKS = "forum_thanks"
        const val TABLE_REPORTS = "forum_reports"
        const val TABLE_BLOCKS = "forum_blocks"
        const val RPC_CREATE_THREAD = "forum_create_thread"

        /** The schema's `reason` check is `between 3 and 500`. */
        const val REASON_MAX_LENGTH = 500

        const val THREAD_COLUMNS =
            "id, category_id, title, author_id, created_at, last_reply_at, reply_count, " +
                "is_locked, author:forum_profiles!author_id(display_name)"

        const val POST_COLUMNS =
            "id, thread_id, author_id, body, is_opening_post, created_at, edited_at, " +
                "thanks_count, " +
                "author:forum_profiles!author_id(display_name, post_count, thanks_received)"
    }
}
