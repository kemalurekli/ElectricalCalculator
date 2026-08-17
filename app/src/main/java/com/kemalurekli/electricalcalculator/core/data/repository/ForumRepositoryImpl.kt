package com.kemalurekli.electricalcalculator.core.data.repository

import android.util.Log
import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.data.network.di.ForumBackend
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepositoryImpl @Inject constructor(
    private val backend: ForumBackend,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ForumRepository {

    override suspend fun categories(language: ForumLanguage) = query {
        it.postgrest.from(TABLE_CATEGORIES)
            .select(Columns.list("id", "key", "title", "description")) {
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

    override suspend fun posts(threadId: String, limit: Int, after: Instant?) = query {
        it.postgrest.from(TABLE_POSTS)
            .select(Columns.raw(POST_COLUMNS)) {
                filter {
                    eq("thread_id", threadId)
                    eq("is_deleted", false)
                    if (after != null) gt("created_at", after.toString())
                }
                // Oldest first, so the opening post is at the top and a reply
                // reads after whatever it is replying to.
                order("created_at", Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<PostDto>()
            .map(PostDto::toDomain)
    }

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
            } catch (e: IOException) {
                // No route to the host, DNS failure, timeout — all of them mean
                // the request never got an answer, and all of them are worth
                // trying again from the same screen.
                Log.i(TAG, "Forum request could not reach the server", e)
                ForumResult.Failure(ForumFailure.NO_CONNECTION)
            } catch (e: Exception) {
                // A rejected policy, a schema that has moved on, a malformed
                // row. The reader can do nothing about any of it.
                Log.e(TAG, "Forum request failed", e)
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
    ) {
        fun toDomain() = ForumCategory(id, key, title, description)
    }

    /** PostgREST returns an embedded row as an object, hence the nested type. */
    @Serializable
    private data class AuthorDto(
        @SerialName("display_name") val displayName: String = "",
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

        const val THREAD_COLUMNS =
            "id, category_id, title, author_id, created_at, last_reply_at, reply_count, " +
                "is_locked, author:forum_profiles!author_id(display_name)"

        const val POST_COLUMNS =
            "id, thread_id, author_id, body, is_opening_post, created_at, edited_at, " +
                "thanks_count, author:forum_profiles!author_id(display_name)"
    }
}
