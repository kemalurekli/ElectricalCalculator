package com.kemalurekli.electricalcalculator.core.data.repository

import app.cash.turbine.test
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteCalculatorDao
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    private val dao = FakeFavoriteCalculatorDao()
    private val timeProvider = FakeTimeProvider()
    private val repository = FavoritesRepositoryImpl(
        dao = dao,
        timeProvider = timeProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `favorites start empty`() = runTest {
        repository.observeFavorites().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `toggle pins then unpins`() = runTest {
        assertTrue(repository.toggle(CalculatorId.VOLTAGE_DROP))
        repository.observeFavorites().test {
            assertEquals(listOf(CalculatorId.VOLTAGE_DROP), awaitItem())
        }

        assertFalse(repository.toggle(CalculatorId.VOLTAGE_DROP))
        repository.observeFavorites().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `favorites keep the order they were pinned in`() = runTest {
        repository.setFavorite(CalculatorId.MOTOR_CURRENT, isFavorite = true)
        timeProvider.advanceBy(1_000L)
        repository.setFavorite(CalculatorId.VOLTAGE_DROP, isFavorite = true)
        timeProvider.advanceBy(1_000L)
        repository.setFavorite(CalculatorId.CABLE_SIZE, isFavorite = true)

        repository.observeFavorites().test {
            assertEquals(
                listOf(
                    CalculatorId.MOTOR_CURRENT,
                    CalculatorId.VOLTAGE_DROP,
                    CalculatorId.CABLE_SIZE,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `pinning twice does not duplicate the entry`() = runTest {
        repository.setFavorite(CalculatorId.POWER, isFavorite = true)
        repository.setFavorite(CalculatorId.POWER, isFavorite = true)

        repository.observeFavorites().test {
            assertEquals(listOf(CalculatorId.POWER), awaitItem())
        }
    }

    @Test
    fun `unpinning something never pinned is a no-op`() = runTest {
        repository.setFavorite(CalculatorId.POWER, isFavorite = false)

        repository.observeFavorites().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `observeIsFavorite tracks a single calculator`() = runTest {
        repository.observeIsFavorite(CalculatorId.BATTERY_RUNTIME).test {
            assertFalse(awaitItem())

            repository.setFavorite(CalculatorId.BATTERY_RUNTIME, isFavorite = true)
            assertTrue(awaitItem())

            // A different calculator must not emit on this flow.
            repository.setFavorite(CalculatorId.POWER, isFavorite = true)
            expectNoEvents()
        }
    }
}
