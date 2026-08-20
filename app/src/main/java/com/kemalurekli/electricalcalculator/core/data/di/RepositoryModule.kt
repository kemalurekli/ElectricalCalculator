package com.kemalurekli.electricalcalculator.core.data.di

import com.kemalurekli.electricalcalculator.core.common.util.AndroidRegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.AndroidStringResolver
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.SystemTimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.data.repository.AppLanguageRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ForumAuthRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ForumRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.InspectionRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ProjectRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.UserPreferencesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.InspectionRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository contract to its data-layer implementation.
 *
 * `@Binds` where the implementation still carries an `@Inject constructor`, and
 * `@Provides` in [SharedRepositoryModule] where it no longer can — the
 * repositories that moved to multiplatform modules lost those annotations,
 * because `javax.inject` is a JVM API. Hilt caught every one of them at compile
 * time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindForumRepository(
        impl: ForumRepositoryImpl,
    ): ForumRepository

    @Binds
    @Singleton
    abstract fun bindForumAuthRepository(
        impl: ForumAuthRepositoryImpl,
    ): ForumAuthRepository

    @Binds
    @Singleton
    abstract fun bindInspectionRepository(
        impl: InspectionRepositoryImpl,
    ): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindStringResolver(
        impl: AndroidStringResolver,
    ): StringResolver

    @Binds
    @Singleton
    abstract fun bindRegionProvider(
        impl: AndroidRegionProvider,
    ): RegionProvider

    @Binds
    @Singleton
    abstract fun bindAppLanguageRepository(
        impl: AppLanguageRepositoryImpl,
    ): AppLanguageRepository
}
