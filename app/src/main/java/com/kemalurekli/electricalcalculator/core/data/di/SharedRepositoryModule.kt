package com.kemalurekli.electricalcalculator.core.data.di

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.common.util.SystemTimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ProjectRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.UserPreferencesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

/**
 * Constructs the repositories that live in multiplatform modules.
 *
 * They used to carry `@Inject constructor` and need no module at all. That
 * annotation is `javax.inject`, which is a JVM API and does not exist on
 * Kotlin/Native, so the graph is spelled out here instead.
 *
 * This is the shape the whole app takes when Hilt is eventually replaced: a
 * hand-written graph rather than a generated one. Writing it a few classes at a
 * time, as they move, is what keeps that from being one large and unreviewable
 * change later.
 */
@Module
@InstallIn(SingletonComponent::class)
object SharedRepositoryModule {

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider()

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataSource: UserPreferencesDataSource,
    ): UserPreferencesRepository = UserPreferencesRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        dao: CalculationHistoryDao,
        timeProvider: TimeProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): HistoryRepository = HistoryRepositoryImpl(dao, timeProvider, ioDispatcher)

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        dao: FavoriteItemDao,
        timeProvider: TimeProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): FavoritesRepository = FavoritesRepositoryImpl(dao, timeProvider, ioDispatcher)

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao,
        circuitDao: CircuitDao,
        timeProvider: TimeProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): ProjectRepository = ProjectRepositoryImpl(projectDao, circuitDao, timeProvider, ioDispatcher)
}
