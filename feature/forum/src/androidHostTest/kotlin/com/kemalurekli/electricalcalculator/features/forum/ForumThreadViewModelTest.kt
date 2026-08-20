package com.kemalurekli.electricalcalculator.features.forum

import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumScreenState
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.testing.FakeForumAuthRepository
import com.kemalurekli.electricalcalculator.testing.FakeForumRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import com.kemalurekli.electricalcalculator.testing.forumPost
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Rule
import kotlin.test.Test

/**
 * The behaviour that could only be checked by tapping through a running app
 * against a live backend — which is how every forum bug so far was found.
 */
class ForumThreadViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repository: FakeForumRepository,
        auth: FakeForumAuthRepository = FakeForumAuthRepository(),
    ) = ForumThreadViewModel(repository, auth)

    @Test
    fun `deleting the last reply-free thread reports it gone`() = runTest {
        val repository = FakeForumRepository(
            posts = listOf(forumPost(id = "opening", isOpeningPost = true)),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onDeleteThread()
        advanceUntilIdle()

        assertTrue(viewModel.threadDeleted.value, "the screen has to leave a thread that no longer exists")
        assertTrue(repository.threads.isEmpty())
    }

    @Test
    fun `a thread with replies refuses to be deleted`() = runTest {
        // The replies are other people's work. The server enforces this; the
        // ViewModel must survive the refusal rather than navigate away from a
        // thread that is still there.
        val repository = FakeForumRepository(
            posts = listOf(
                forumPost(id = "opening", isOpeningPost = true),
                forumPost(id = "reply", authorId = "someone-else"),
            ),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onDeleteThread()
        advanceUntilIdle()

        assertFalse(viewModel.threadDeleted.value, "the thread is still there, so the screen must stay")
        assertTrue(viewModel.deleteFailed.value, "and the refusal has to be visible")
    }

    @Test
    fun `deleting a post removes it rather than hiding it`() = runTest {
        val repository = FakeForumRepository(
            posts = listOf(
                forumPost(id = "opening", isOpeningPost = true),
                forumPost(id = "reply"),
            ),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onDeletePost("reply")
        advanceUntilIdle()

        assertEquals(listOf("opening"), repository.posts.map { it.id })
    }

    @Test
    fun `a failed reply keeps the draft`() = runTest {
        // Losing what somebody just typed is the one outcome worth avoiding at
        // any cost, so the field is cleared on success and only on success.
        val repository = FakeForumRepository(
            posts = listOf(forumPost(id = "opening", isOpeningPost = true)),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()
        viewModel.onDraftChange("uzun uzun yazilmis bir cevap")

        repository.failWith = ForumFailure.NO_CONNECTION
        viewModel.onSendReply()
        advanceUntilIdle()

        assertEquals("uzun uzun yazilmis bir cevap", viewModel.draft.value)
        assertTrue(viewModel.sendFailed.value)
    }

    @Test
    fun `a delivered reply clears the draft and confirms once`() = runTest {
        val repository = FakeForumRepository(
            posts = listOf(forumPost(id = "opening", isOpeningPost = true)),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()
        viewModel.onDraftChange("cevap")

        viewModel.onSendReply()
        advanceUntilIdle()

        assertEquals("", viewModel.draft.value)
        assertEquals(1, viewModel.sent.value)

        // A counter, not a flag: the second reply has to confirm as clearly as
        // the first, which a boolean already set to true would not.
        viewModel.onDraftChange("ikinci cevap")
        viewModel.onSendReply()
        advanceUntilIdle()
        assertEquals(2, viewModel.sent.value)
    }

    @Test
    fun `blocking someone hides their messages from this reader`() = runTest {
        // One-sided and local: the rows are untouched and everyone else still
        // sees them, which is what makes it this reader's decision rather than
        // a punishment applied to the author.
        val repository = FakeForumRepository(
            posts = listOf(
                forumPost(id = "opening", isOpeningPost = true),
                forumPost(id = "theirs", authorId = "loud-person"),
            ),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onBlock("loud-person")
        advanceUntilIdle()

        val shown = (viewModel.uiState.value as ForumScreenState.Content).value
        assertEquals(listOf("opening"), shown.map { it.id })
        assertEquals(2, repository.posts.size)
    }

    @Test
    fun `a report reaches the queue with its reason`() = runTest {
        val repository = FakeForumRepository(
            posts = listOf(forumPost(id = "opening", isOpeningPost = true)),
        )
        val viewModel = viewModel(repository)
        viewModel.onOpen("thread-1")
        advanceUntilIdle()

        viewModel.onReport("opening", ForumReportReason.UNSAFE_ADVICE, "topraklamasiz oneri")
        advanceUntilIdle()

        assertEquals(1, repository.reports.size)
        assertEquals(ForumReportReason.UNSAFE_ADVICE, repository.reports.single().second)
        assertEquals(1, viewModel.reported.value)
    }

    @Test
    fun `a dropped connection surfaces as an error state`() = runTest {
        val repository = FakeForumRepository()
        repository.failWith = ForumFailure.NO_CONNECTION
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(ForumScreenState.Loading, awaitItem())
            viewModel.onOpen("thread-1")
            advanceUntilIdle()
            assertEquals(
                ForumScreenState.Error(ForumFailure.NO_CONNECTION),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
