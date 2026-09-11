package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.testing.FakeForumAuthRepository
import com.kemalurekli.electricalcalculator.testing.FakeForumRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import com.kemalurekli.electricalcalculator.testing.forumPost
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import kotlin.test.Test

/**
 * Reading a thread in both directions, by page.
 *
 * Every list was once silently capped at one page: the repository took a cursor
 * and no ViewModel passed one, so a long thread simply stopped. It then took a
 * cursor that only pointed forwards, so the end of a long thread was twelve
 * requests away and page four could not be named at all.
 *
 * Posts are addressed by offset, and safely: they are ordered by when they were
 * written and only ever appended, so a reply landing while somebody reads
 * shifts nothing already loaded.
 *
 * The list of threads is addressed by offset too, for a different reason. It
 * does reorder itself every time anybody posts anywhere — but it holds one page
 * and replaces it, so there is no accumulated window for a shifted row to
 * duplicate or skip. Offsets are unsafe for infinite scroll, not for a pager;
 * see [ForumThreadListPagingTest].
 */
class ForumPagingTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun thread(messages: Int) = List(messages) { index ->
        forumPost(id = "post-$index", isOpeningPost = index == 0)
    }

    private fun viewModel(repository: FakeForumRepository, replies: Int = 0) =
        ForumThreadViewModel(repository, FakeForumAuthRepository()).apply {
            onOpen("thread-1", replyCount = replies)
        }

    @Test
    fun `scrolling past the end of a page carries on into the next`() = runTest {
        val repository = FakeForumRepository(posts = thread(35))
        val model = viewModel(repository, replies = 34)
        advanceUntilIdle()

        model.onLoadMore()
        advanceUntilIdle()

        // The window grew rather than being replaced: a page boundary is an
        // address, not a wall, and a reader scrolling through one should not
        // have to ask.
        assertEquals(ForumRepository.POSTS_PER_PAGE * 2, model.position.value.loaded)
        assertEquals(0, model.position.value.windowStart)
    }

    @Test
    fun `the end of a long thread is one request away`() = runTest {
        val repository = FakeForumRepository(posts = thread(95))
        val model = viewModel(repository, replies = 94)
        advanceUntilIdle()
        val opening = repository.calls.count { it.startsWith("posts(") }

        model.onJumpToEnd()
        advanceUntilIdle()

        assertEquals(
            opening + 1,
            repository.calls.count { it.startsWith("posts(") },
            "jumping to the end must not walk there",
        )
        assertEquals(10, model.position.value.page)
        assertEquals(90, model.position.value.windowStart)
    }

    @Test
    fun `a page asked for by number is the page that arrives`() = runTest {
        val repository = FakeForumRepository(posts = thread(95))
        val model = viewModel(repository, replies = 94)
        advanceUntilIdle()

        model.onGoToPage(4)
        advanceUntilIdle()

        assertEquals(4, model.position.value.page)
        assertEquals(30, model.position.value.windowStart)
        assertTrue(
            repository.calls.last().contains("offset=30"),
            "the fourth page is the fourth ten of the thread, got ${repository.calls.last()}",
        )
    }

    @Test
    fun `a jump replaces the window rather than extending it`() = runTest {
        val repository = FakeForumRepository(posts = thread(95))
        val model = viewModel(repository, replies = 94)
        advanceUntilIdle()
        model.onLoadMore()
        advanceUntilIdle()

        model.onGoToPage(7)
        advanceUntilIdle()

        // Keeping the first twenty and adding the seventh page would leave a
        // hole the list draws as continuous, with two messages forty apart
        // looking like a reply to each other.
        assertEquals(60, model.position.value.windowStart)
        assertEquals(ForumRepository.POSTS_PER_PAGE, model.position.value.loaded)
    }

    @Test
    fun `a jump does not walk itself back to the first page`() = runTest {
        // The list sits at its own top after a jump. While it also fetched
        // upwards whenever it was there, that fired at once and again on every
        // arrival, and page ten crawled back to page one without a finger
        // touching it. Backwards is an address now; only forwards is a gesture.
        val repository = FakeForumRepository(posts = thread(95))
        val model = viewModel(repository, replies = 94)
        advanceUntilIdle()

        model.onJumpToEnd()
        advanceUntilIdle()
        model.onLoadMore()
        advanceUntilIdle()

        assertEquals(10, model.position.value.page)
        assertEquals(90, model.position.value.windowStart)
    }

    @Test
    fun `neither end asks for what is not there`() = runTest {
        val repository = FakeForumRepository(posts = thread(8))
        val model = viewModel(repository, replies = 7)
        advanceUntilIdle()
        val opening = repository.calls.count { it.startsWith("posts(") }

        // A thread shorter than a page has nothing below it to fetch.
        model.onLoadMore()
        advanceUntilIdle()

        assertEquals(opening, repository.calls.count { it.startsWith("posts(") })
        assertTrue(!model.position.value.isPaged, "one page is not worth a pager")
    }
}
