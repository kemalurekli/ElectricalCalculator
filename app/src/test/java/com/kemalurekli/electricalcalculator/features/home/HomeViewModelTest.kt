package com.kemalurekli.electricalcalculator.features.home

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.home.domain.SearchKind
import com.kemalurekli.electricalcalculator.features.home.domain.SearchIndexBuilder
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeViewModel
import com.kemalurekli.electricalcalculator.testing.FakeCalculationHistoryDao
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteItemDao
import com.kemalurekli.electricalcalculator.testing.FakeStringResolver
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalog = CalculatorCatalog()
    private val timeProvider = FakeTimeProvider()
    private val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl(
        dao = FakeFavoriteItemDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )
    private val stringResolver = FakeStringResolver(
        catalog.all.associate { it.title to "Title ${it.id.key}" } +
            catalog.all.associate { it.description to "Description ${it.id.key}" },
    )

    private val historyRepository: HistoryRepository = HistoryRepositoryImpl(
        dao = FakeCalculationHistoryDao(),
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun viewModel(savedState: SavedStateHandle = SavedStateHandle()) =
        HomeViewModel(
            catalog = catalog,
            searchIndexBuilder = SearchIndexBuilder(catalog, stringResolver),
            favoritesRepository = favoritesRepository,
            historyRepository = historyRepository,
            stringResolver = stringResolver,
            savedStateHandle = savedState,
        )

    @Test
    fun `dashboard starts with no favorites and no search`() = runTest {
        viewModel().uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertFalse(state.isSearching)
            assertFalse(state.hasFavorites)
            assertTrue(state.favorites.isEmpty())
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `pinned calculators appear on the dashboard`() = runTest {
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            awaitItem()

            model.onToggleFavorite(CalculatorId.VOLTAGE_DROP)
            val state = awaitItem()

            assertTrue(state.hasFavorites)
            assertEquals(listOf(CalculatorId.VOLTAGE_DROP), state.favorites.map { it.id })
            assertTrue(state.favorites.all { it.isFavorite })
        }
    }

    @Test
    fun `favorites keep pin order rather than catalog order`() = runTest {
        // MOTOR_CURRENT sits after VOLTAGE_DROP in the catalog, so pinning it
        // first proves the dashboard follows the repository's ordering.
        favoritesRepository.setFavorite(CalculatorId.MOTOR_CURRENT, isFavorite = true)
        timeProvider.advanceBy(1_000L)
        favoritesRepository.setFavorite(CalculatorId.VOLTAGE_DROP, isFavorite = true)

        viewModel().uiState.test {
            skipItems(1)
            assertEquals(
                listOf(CalculatorId.MOTOR_CURRENT, CalculatorId.VOLTAGE_DROP),
                awaitItem().favorites.map { it.id },
            )
        }
    }

    @Test
    fun `a query replaces the dashboard with ranked results`() = runTest {
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            awaitItem()

            model.onQueryChange("voltage_drop")
            val state = awaitItem()

            assertTrue(state.isSearching)
            assertTrue(state.favorites.isEmpty())
            assertEquals(
                listOf(CalculatorId.VOLTAGE_DROP.key),
                state.searchResults
                    .single { it.kind == SearchKind.CALCULATOR }
                    .hits
                    .map { it.key },
            )
        }
    }

    @Test
    fun `a query matching nothing reports no results`() = runTest {
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            awaitItem()

            model.onQueryChange("no such calculator")
            val state = awaitItem()

            assertTrue(state.hasNoResults)
            assertTrue(state.searchResults.isEmpty())
        }
    }

    @Test
    fun `search reaches beyond the calculators`() = runTest {
        // Replaces an older test that asserted a search result carried its
        // pinned state. Results are no longer all calculators — a hit may be a
        // reference topic, a glossary term or a symbol — so the row is a plain
        // list item and pinning happens on the calculator list instead.
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            awaitItem()

            // "Ampacity" is a glossary term's English name, which is a plain
            // Kotlin string rather than a resource — so this reaches the
            // glossary shelf without depending on what the fake resolver knows.
            model.onQueryChange("ampacity")
            val kinds = awaitItem().searchResults.map { it.kind }

            assertTrue("the glossary is unreachable from search", SearchKind.GLOSSARY in kinds)
        }
    }

    @Test
    fun `clearing the query restores the dashboard`() = runTest {
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            awaitItem()

            model.onQueryChange("voltage_drop")
            awaitItem()

            model.onClearQuery()

            assertFalse(awaitItem().isSearching)
        }
    }

    // -- Recent calculations ---------------------------------------------------

    private suspend fun saveRun(title: String, summary: String, at: Long) {
        historyRepository.save(
            CalculationRecord(
                calculatorId = CalculatorId.VOLTAGE_DROP,
                title = title,
                summary = summary,
                inputs = emptyMap(),
                results = emptyMap(),
                createdAt = Instant.fromEpochMilliseconds(at),
            ),
        )
    }

    @Test
    fun `the dashboard shows no recent section before anything is calculated`() = runTest {
        viewModel().uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertFalse(state.hasRecent)
            assertEquals(0, state.savedCount)
        }
    }

    @Test
    fun `recent calculations appear newest first`() = runTest {
        saveRun("Oldest", "1 V", 1_000L)
        saveRun("Newest", "3 V", 3_000L)
        saveRun("Middle", "2 V", 2_000L)

        viewModel().uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertTrue(state.hasRecent)
            assertEquals(listOf("Newest", "Middle", "Oldest"), state.recent.map { it.record.title })
        }
    }

    @Test
    fun `the recent section is capped so the dashboard stays a launchpad`() = runTest {
        repeat(10) { index -> saveRun("Run $index", "$index V", index * 1_000L) }

        viewModel().uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertEquals(3, state.recent.size)
            // The count badge still reports the true total, not the capped list.
            assertEquals(10, state.savedCount)
        }
    }

    @Test
    fun `searching hides the recent section`() = runTest {
        saveRun("A run", "1 V", 1_000L)
        val model = viewModel()

        model.uiState.test {
            skipItems(1)
            assertTrue(awaitItem().hasRecent)

            model.onQueryChange("voltage_drop")

            assertFalse(awaitItem().hasRecent)
        }
    }

    @Test
    fun `an in-progress search is restored after process death`() = runTest {
        // SavedStateHandle is what the system hands back on restore; seeding it
        // reproduces the recreated-ViewModel case.
        val restored = viewModel(SavedStateHandle(mapOf("home_query" to "voltage_drop")))

        restored.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertEquals("voltage_drop", state.query)
            assertEquals(
                listOf(CalculatorId.VOLTAGE_DROP.key),
                state.searchResults
                    .single { it.kind == SearchKind.CALCULATOR }
                    .hits
                    .map { it.key },
            )
        }
    }
}
