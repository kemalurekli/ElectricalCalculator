package com.kemalurekli.electricalcalculator.testing

import com.kemalurekli.electricalcalculator.features.forum.auth.SignInCredential
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumPost
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportTarget
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

/**
 * A forum with no network behind it.
 *
 * The real one can only be exercised by tapping through a running app against a
 * live Supabase project, which is how every forum bug so far has been found —
 * one of them twice. This is the thing that lets a ViewModel's behaviour be
 * checked in milliseconds instead.
 *
 * Failures are opt-in per call ([failWith]) so a test can say "the server
 * refuses this" without any of the plumbing that would normally be needed to
 * make a server refuse something.
 */
class FakeForumRepository(
    var categories: List<ForumCategory> = emptyList(),
    threads: List<ForumThread> = emptyList(),
    posts: List<ForumPost> = emptyList(),
) : ForumRepository {

    var threads: MutableList<ForumThread> = threads.toMutableList()
    var posts: MutableList<ForumPost> = posts.toMutableList()

    /** When set, every call returns this instead of doing anything. */
    var failWith: ForumFailure? = null

    /** Every call made, in order, for tests that care about what was requested. */
    val calls = mutableListOf<String>()

    override suspend fun categories(language: ForumLanguage): ForumResult<List<ForumCategory>> =
        respond("categories($language)") { categories }

    override suspend fun threads(
        categoryId: String,
        language: ForumLanguage,
        limit: Int,
        before: Instant?,
    ): ForumResult<List<ForumThread>> =
        respond("threads($categoryId, limit=$limit, before=$before)") {
            threads.filter { it.categoryId == categoryId }
        }

    override suspend fun posts(
        threadId: String,
        limit: Int,
        after: Instant?,
    ): ForumResult<List<ForumPost>> =
        respond("posts($threadId, limit=$limit, after=$after)") {
            posts.filter { it.threadId == threadId }
        }

    override suspend fun createThread(
        categoryId: String,
        language: ForumLanguage,
        title: String,
        body: String,
    ): ForumResult<String> = respond("createThread($title)") {
        val id = "thread-${threads.size + 1}"
        threads += forumThread(id = id, categoryId = categoryId, title = title)
        posts += forumPost(id = "$id-opening", threadId = id, body = body, isOpeningPost = true)
        id
    }

    override suspend fun createReply(threadId: String, body: String): ForumResult<Unit> =
        respond("createReply($threadId)") {
            posts += forumPost(id = "post-${posts.size + 1}", threadId = threadId, body = body)
        }

    override suspend fun updatePost(postId: String, body: String): ForumResult<Unit> =
        respond("updatePost($postId)") {
            posts.replaceAll { if (it.id == postId) it.copy(body = body) else it }
        }

    override suspend fun deletePost(postId: String): ForumResult<Unit> =
        respond("deletePost($postId)") { posts.removeAll { it.id == postId } }

    override suspend fun deleteThread(threadId: String): ForumResult<Unit> =
        respond("deleteThread($threadId)") {
            // Mirrors the server's delete policy rather than trusting the
            // caller: the whole point of the rule is that it holds even when
            // the button was showing a moment ago.
            if (posts.any { it.threadId == threadId && !it.isOpeningPost }) {
                return ForumResult.Failure(ForumFailure.SERVER_ERROR)
            }
            threads.removeAll { it.id == threadId }
            posts.removeAll { it.threadId == threadId }
        }

    override suspend fun setThanks(postId: String, thanked: Boolean): ForumResult<Unit> =
        respond("setThanks($postId, $thanked)") {
            posts.replaceAll {
                if (it.id == postId) {
                    it.copy(
                        thankedByMe = thanked,
                        thanksCount = it.thanksCount + if (thanked) 1 else -1,
                    )
                } else {
                    it
                }
            }
        }

    /** Everything reported, so a test can assert what reached the queue. */
    val reports = mutableListOf<Triple<String, ForumReportReason, String>>()

    val blocked = mutableSetOf<String>()

    override suspend fun report(
        target: ForumReportTarget,
        targetId: String,
        reason: ForumReportReason,
        note: String,
    ): ForumResult<Unit> = respond("report($targetId, ${reason.key})") {
        reports += Triple(targetId, reason, note)
    }

    override suspend fun block(userId: String): ForumResult<Unit> =
        respond("block($userId)") { blocked += userId }

    override suspend fun unblock(userId: String): ForumResult<Unit> =
        respond("unblock($userId)") { blocked -= userId }

    override suspend fun blockedUserIds(): ForumResult<Set<String>> =
        respond("blockedUserIds()") { blocked.toSet() }

    private inline fun <T> respond(call: String, block: () -> T): ForumResult<T> {
        calls += call
        failWith?.let { return ForumResult.Failure(it) }
        return ForumResult.Success(block())
    }
}

/** A signed-in session that answers without a Supabase project behind it. */
class FakeForumAuthRepository(
    initial: ForumSession = ForumSession.SignedOut,
) : ForumAuthRepository {

    private val state = MutableStateFlow(initial)

    override val session: Flow<ForumSession> = state.asStateFlow()

    var failWith: ForumFailure? = null

    fun signIn(profile: ForumProfile = forumProfile()) {
        state.value = ForumSession.SignedIn(profile)
    }

    override suspend fun signIn(credential: SignInCredential): Result<Unit> {
        signIn()
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        state.value = ForumSession.SignedOut
    }

    override suspend fun profile(userId: String): ForumResult<ForumProfile> =
        failWith?.let { ForumResult.Failure(it) }
            ?: ForumResult.Success(forumProfile(id = userId))

    override suspend fun updateDisplayName(displayName: String): ForumResult<Unit> =
        failWith?.let { ForumResult.Failure(it) } ?: ForumResult.Success(Unit)

    override suspend fun deleteAccount(): ForumResult<Unit> =
        failWith?.let { ForumResult.Failure(it) } ?: ForumResult.Success(Unit)
}

// -- Builders, so a test names only the field it is about --------------------

private val FIXED_TIME: Instant = Instant.parse("2026-08-01T12:00:00Z")

fun forumThread(
    id: String = "thread-1",
    categoryId: String = "category-1",
    title: String = "Bir konu basligi",
    authorId: String = "user-1",
    replyCount: Int = 0,
    isLocked: Boolean = false,
) = ForumThread(
    id = id,
    categoryId = categoryId,
    title = title,
    authorId = authorId,
    authorName = "Test Yazar",
    createdAt = FIXED_TIME,
    lastReplyAt = FIXED_TIME,
    replyCount = replyCount,
    isLocked = isLocked,
)

fun forumPost(
    id: String = "post-1",
    threadId: String = "thread-1",
    authorId: String = "user-1",
    body: String = "Bir mesaj govdesi",
    isOpeningPost: Boolean = false,
    thanksCount: Int = 0,
    thankedByMe: Boolean? = null,
) = ForumPost(
    id = id,
    threadId = threadId,
    authorId = authorId,
    authorName = "Test Yazar",
    body = body,
    isOpeningPost = isOpeningPost,
    createdAt = FIXED_TIME,
    editedAt = null,
    thanksCount = thanksCount,
    thankedByMe = thankedByMe,
)

fun forumProfile(
    id: String = "user-1",
    displayName: String = "Test Yazar",
    postCount: Int = 0,
    thanksReceived: Int = 0,
) = ForumProfile(
    id = id,
    displayName = displayName,
    joinedAt = FIXED_TIME,
    postCount = postCount,
    thanksReceived = thanksReceived,
)
