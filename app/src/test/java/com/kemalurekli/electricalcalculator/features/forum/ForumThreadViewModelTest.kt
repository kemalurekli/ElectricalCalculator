package com.kemalurekli.electricalcalculator.features.forum

import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumScreenState
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.testing.FakeForumAuthRepository
import com.kemalurekli.electricalcalculator.testing.FakeForumRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import com.kemalurekli.electricalcalculator.testing.forumPost
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

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

        assertTrue("the screen has to leave a thread that no longer exists", viewModel.threadDeleted.value)
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

        assertFalse("the thread is still there, so the screen must stay", viewModel.threadDeleted.value)
        assertTrue("and the refusal has to be visible", viewModel.deleteFailed.value)
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
