package com.kemalurekli.electricalcalculator.features.calculators

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalog = CalculatorCatalog()
    private val favoritesRepository = FavoritesRepositoryImpl(
        dao = FakeFavoriteItemDao(),
        timeProvider = FakeTimeProvider(),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    // Resolves each resource id to a distinct, predictable string so search
    // behaviour can be asserted without loading Android resources.
    private val stringResolver = FakeStringResolver(
        catalog.all.associate { it.title to "Title ${it.id.key}" } +
            catalog.all.associate { it.description to "Description ${it.id.key}" },
    )

    private val viewModel = CalculatorsViewModel(
        catalog = catalog,
        favoritesRepository = favoritesRepository,
        stringResolver = stringResolver,
        savedStateHandle = SavedStateHandle(),
    )

    @Test
    fun `browsing groups every calculator into sections`() = runTest {
        viewModel.uiState.test {
            // Drop the initial placeholder emitted before the first combine.
            skipItems(1)
            val state = awaitItem()

            assertFalse(state.isSearching)
            assertEquals(catalog.all.size, state.sections.sumOf { it.items.size })
        }
    }

    @Test
    fun `sections follow the category enum order`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val categories = awaitItem().sections.map { it.category }

            assertEquals(categories.sortedBy { it.ordinal }, categories)
        }
    }

    @Test
    fun `a query switches to flat ranked results`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onQueryChange("voltage_drop")
            val state = awaitItem()

            assertTrue(state.isSearching)
            assertTrue(state.sections.isEmpty())
            assertEquals(
                listOf(CalculatorId.VOLTAGE_DROP),
                state.searchResults.map { it.id },
            )
        }
    }

    @Test
    fun `a query matching nothing reports empty`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onQueryChange("no such calculator")

            assertTrue(awaitItem().isEmpty)
        }
    }

    @Test
    fun `clearing the query restores the sections`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.onQueryChange("voltage_drop")
            awaitItem()

            viewModel.onQueryChange("")
            val state = awaitItem()

            assertFalse(state.isSearching)
            assertEquals(catalog.all.size, state.sections.sumOf { it.items.size })
        }
    }

    @Test
    fun `toggling a favorite updates the matching row`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val initial = awaitItem()
            assertTrue(initial.sections.none { section -> section.items.any { it.isFavorite } })

            viewModel.onToggleFavorite(CalculatorId.VOLTAGE_DROP)
            val updated = awaitItem()

            val favorites = updated.sections
                .flatMap { it.items }
                .filter { it.isFavorite }
                .map { it.id }
            assertEquals(listOf(CalculatorId.VOLTAGE_DROP), favorites)
        }
    }

    @Test
    fun `every catalog category is represented`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val categories = awaitItem().sections.map { it.category }.toSet()

            assertEquals(CalculatorCategory.entries.toSet(), categories)
        }
    }
}
