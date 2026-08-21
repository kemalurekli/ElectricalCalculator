package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesViewModel
import com.kemalurekli.electricalcalculator.testing.FakeForumRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The forum follows the language while it is open.
 *
 * It did not, and the way it failed was worse than not following: the sections
 * kept the language the screen was first opened in, still carrying their old
 * thread counts, while the screen behind each one was rebuilt and asked the
 * server for the new language. Every section said "6 threads" and every one of
 * them opened onto "No threads yet".
 *
 * The reasoning that allowed it was written down and wrong — that changing the
 * language recreates the activity and so replaces this ViewModel. Android hands
 * a restored back stack entry its original ViewModels, and iOS has no activity
 * to recreate. Both platforms kept the instance.
 */
class ForumLanguageChangeTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private class FakeLanguages(initial: AppLanguage) : AppLanguageRepository {
        private val state = MutableStateFlow(initial)
        override val language: StateFlow<AppLanguage> = state.asStateFlow()
        override fun setLanguage(language: AppLanguage) { state.value = language }
    }

    @Test
    fun `changing the language asks the server for the other board`() = runTest {
        val repository = FakeForumRepository(
            categories = listOf(ForumCategory("1", "wiring", "Tesisat", "", threadCount = 6)),
        )
        val languages = FakeLanguages(AppLanguage.TURKISH)
        ForumCategoriesViewModel(repository, languages)
        advanceUntilIdle()
        assertEquals(listOf("categories(TURKISH)"), repository.calls)

        languages.setLanguage(AppLanguage.ENGLISH)
        advanceUntilIdle()

        assertEquals(
            listOf("categories(TURKISH)", "categories(ENGLISH)"),
            repository.calls,
            "the sections on screen belong to the old board until they are fetched again",
        )
    }

    @Test
    fun `the language the screen reports follows the setting`() = runTest {
        // The compose-thread route is built from this, so a stale value would
        // file a new question under the board the reader has just left.
        val languages = FakeLanguages(AppLanguage.TURKISH)
        val viewModel = ForumCategoriesViewModel(FakeForumRepository(), languages)
        advanceUntilIdle()
        assertEquals(ForumLanguage.TURKISH, viewModel.language.value)

        languages.setLanguage(AppLanguage.ENGLISH)
        advanceUntilIdle()

        assertEquals(ForumLanguage.ENGLISH, viewModel.language.value)
    }
}
