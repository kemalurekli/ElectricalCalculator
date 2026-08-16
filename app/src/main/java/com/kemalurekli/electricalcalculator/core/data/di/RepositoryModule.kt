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
import com.kemalurekli.electricalcalculator.core.data.repository.InspectionRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ProjectRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.UserPreferencesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.InspectionRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository contract to its data-layer implementation.
 *
 * `@Binds` rather than `@Provides` because the implementations are already
 * constructor-injected; this generates no factory of its own.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl,
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: FavoritesRepositoryImpl,
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        impl: SystemTimeProvider,
    ): TimeProvider

    @Binds
    @Singleton
    abstract fun bindInspectionRepository(
        impl: InspectionRepositoryImpl,
    ): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        impl: ProjectRepositoryImpl,
    ): ProjectRepository

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
