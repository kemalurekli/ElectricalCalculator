package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumScreenState
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsViewModel
import com.kemalurekli.electricalcalculator.testing.FakeForumAuthRepository
import com.kemalurekli.electricalcalculator.testing.FakeForumRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import com.kemalurekli.electricalcalculator.testing.forumThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A category index, twenty threads at a time.
 *
 * It used to be infinite scroll on a cursor, and the cursor was the right
 * answer to the question that was being asked then: a list that *keeps* the
 * pages it has loaded cannot use offsets, because a reply anywhere lifts its
 * thread to the top and pushes every row below it down — so the same thread
 * arrives twice, or never.
 *
 * This list keeps one page and throws it away to get another, which makes that
 * whole class of bug unreachable and offsets the simple answer. What a shifted
 * row means here is that the board moved, which is what it did.
 */
class ForumThreadListPagingTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private class FakeLanguages : AppLanguageRepository {
        private val state = MutableStateFlow(AppLanguage.TURKISH)
        override val language: StateFlow<AppLanguage> = state.asStateFlow()
        override fun setLanguage(language: AppLanguage) { state.value = language }
    }

    /** Only the pins matter here, and nothing in these tests sets one. */
    private class FakePreferences : UserPreferencesRepository {
        private val state = MutableStateFlow(UserPreferences.Default)
        override val preferences: Flow<UserPreferences> = state.asStateFlow()
        override suspend fun setThemeMode(themeMode: ThemeMode) = Unit
        override suspend fun setUnitSystem(unitSystem: UnitSystem) = Unit
        override suspend fun setDisclaimerAccepted(accepted: Boolean) = Unit
        override suspend fun setThreadPinned(threadId: String, pinned: Boolean) = Unit
        override suspend fun setForumRulesAccepted(accepted: Boolean) = Unit
        override suspend fun setEngineeringDefaults(defaults: EngineeringDefaults) = Unit
        override suspend fun seedEngineeringDefaults(regionCode: String) = Unit
    }

    private fun board(threads: Int) = List(threads) { index ->
        forumThread(id = "thread-$index", categoryId = "category-1")
    }

    private fun viewModel(repository: FakeForumRepository, threadCount: Int) =
        ForumThreadsViewModel(
            repository,
            FakeLanguages(),
            FakeForumAuthRepository(),
            FakePreferences(),
        ).apply { onOpen("category-1", threadCount) }

    private fun ForumThreadsViewModel.rows() =
        (uiState.value as ForumScreenState.Content).value

    @Test
    fun `a page asked for by number is the page that arrives`() = runTest {
        val repository = FakeForumRepository(threads = board(45))
        val model = viewModel(repository, threadCount = 45)
        advanceUntilIdle()

        model.onGoToPage(2)
        advanceUntilIdle()

        assertEquals(2, model.position.value.page)
        assertTrue(
            repository.calls.last().contains("offset=20"),
            "the second page starts at the twenty-first thread, got ${repository.calls.last()}",
        )
        assertEquals("thread-20", model.rows().first().id)
    }

    @Test
    fun `the page replaces the list rather than growing it`() = runTest {
        // The difference from a thread, where pages accumulate: nobody reads a
        // board index straight through, so page two is instead of page one.
        val repository = FakeForumRepository(threads = board(45))
        val model = viewModel(repository, threadCount = 45)
        advanceUntilIdle()

        model.onGoToPage(2)
        advanceUntilIdle()

        assertEquals(ForumRepository.THREADS_PER_PAGE, model.rows().size)
    }

    @Test
    fun `the count on the category row draws the pager before the request lands`() = runTest {
        // The pager exists on the first frame because the row the reader tapped
        // already said how many threads there are. Waiting for the server would
        // pop a control into the bottom of the list a moment after it settled.
        val model = viewModel(FakeForumRepository(threads = board(45)), threadCount = 45)

        assertEquals(3, model.position.value.pageCount)
        advanceUntilIdle()
    }

    @Test
    fun `a short page overrules the count the category claimed`() = runTest {
        // Counts the server maintains drift. A page that comes back with room
        // to spare is the last page, and is allowed to say so — otherwise the
        // pager offers a page four that sends the reader to an empty screen.
        val repository = FakeForumRepository(threads = board(25))
        val model = viewModel(repository, threadCount = 70)
        advanceUntilIdle()

        model.onGoToPage(2)
        advanceUntilIdle()

        assertEquals(2, model.position.value.pageCount)
        assertEquals(5, model.rows().size)
    }

    @Test
    fun `a full page is enough to know there is another one`() = runTest {
        // Nothing has told this list how long the category is — the reader came
        // in from a search or a restored back stack, with a count of zero.
        val repository = FakeForumRepository(threads = board(45))
        val model = viewModel(repository, threadCount = 0)
        advanceUntilIdle()

        assertTrue(model.position.value.isPaged, "twenty threads mean there may be a twenty-first")
        model.onGoToPage(2)
        advanceUntilIdle()

        assertEquals(2, model.position.value.page)
        assertEquals(3, model.position.value.pageCount)
    }

    @Test
    fun `a category that fits on one page has no pager`() = runTest {
        val repository = FakeForumRepository(threads = board(7))
        val model = viewModel(repository, threadCount = 7)
        advanceUntilIdle()

        assertFalse(model.position.value.isPaged, "one page is not worth a pager")
    }

    @Test
    fun `neither end asks for what is not there`() = runTest {
        val repository = FakeForumRepository(threads = board(45))
        val model = viewModel(repository, threadCount = 45)
        advanceUntilIdle()
        val opening = repository.calls.count { it.startsWith("threads(") }

        model.onGoToPage(0)
        model.onGoToPage(9)
        advanceUntilIdle()

        assertEquals(3, model.position.value.page, "a page past the end is the last page")
        assertEquals(
            opening + 1,
            repository.calls.count { it.startsWith("threads(") },
            "asking for the page already showing must not spend a request",
        )
    }

    @Test
    fun `coming back to the category reloads the page being read`() = runTest {
        // Not page one: a reader who opened a thread from page three and backed
        // out of it is still on page three.
        val repository = FakeForumRepository(threads = board(45))
        val model = viewModel(repository, threadCount = 45)
        advanceUntilIdle()
        model.onGoToPage(3)
        advanceUntilIdle()

        model.onRefresh()
        advanceUntilIdle()

        assertEquals(3, model.position.value.page)
        assertTrue(repository.calls.last().contains("offset=40"))
    }
}
