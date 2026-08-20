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
 * Every list was silently capped at one page.
 *
 * The repository has taken a cursor since it was written and no ViewModel ever
 * passed one, so a thread with more than 25 messages simply stopped — no
 * indicator, no button, nothing to scroll to.
 */
class ForumPagingTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun fullPage() = List(ForumRepository.DEFAULT_PAGE_SIZE) { index ->
        forumPost(id = "post-$index", isOpeningPost = index == 0)
    }

    @Test
    fun `a full page asks for the next one with a cursor`() = runTest {
        val repository = FakeForumRepository(posts = fullPage())
        val viewModel = ForumThreadViewModel(repository, FakeForumAuthRepository())
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        // An `after` cursor rather than an offset: offsets skip and repeat rows
        // when somebody posts while the reader is scrolling.
        val paged = repository.calls.filter { it.startsWith("posts(") }
        assertTrue(paged.last().contains("after=") && !paged.last().contains("after=null"), "the second request must carry a cursor, got $paged")
    }

    @Test
    fun `a short page stops the asking`() = runTest {
        // Fewer rows than a page means the server has no more, and asking again
        // would spend a request every time the reader touched the bottom.
        val repository = FakeForumRepository(posts = listOf(forumPost(isOpeningPost = true)))
        val viewModel = ForumThreadViewModel(repository, FakeForumAuthRepository())
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()
        val afterFirst = repository.calls.count { it.startsWith("posts(") }

        viewModel.onLoadMore()
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(afterFirst, repository.calls.count { it.startsWith("posts(") })
    }
}
