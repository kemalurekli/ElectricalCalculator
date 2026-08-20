package com.kemalurekli.electricalcalculator.core.data.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.kemalurekli.electricalcalculator.core.common.util.AndroidStringResolver
import com.kemalurekli.electricalcalculator.core.common.util.PlatformRegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.SystemTimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.data.repository.AppLanguageRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.features.inspection.data.InspectionRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ProjectRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.UserPreferencesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.inspection.domain.InspectionRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
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
    abstract fun bindInspectionRepository(
        impl: InspectionRepositoryImpl,
    ): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindResourceIdResolver(
        impl: AndroidStringResolver,
    ): ResourceIdResolver

    /**
     * The same object under its multiplatform supertype, for the screens that
     * have left `:app` and carry `StringResource` handles rather than ids.
     * Both bindings must name one instance — it caches nothing, but two would
     * still be two.
     */
    @Binds
    @Singleton
    abstract fun bindStringResolver(
        impl: AndroidStringResolver,
    ): StringResolver

    companion object {
        /**
         * Constructed rather than bound: `PlatformRegionProvider` lives in
         * `:core:common` and takes a plain `Context`, because a multiplatform
         * module cannot carry Hilt's annotations.
         */
        @Provides
        @Singleton
        fun provideRegionProvider(
            @ApplicationContext context: Context,
        ): RegionProvider = PlatformRegionProvider(context)
    }

    @Binds
    @Singleton
    abstract fun bindAppLanguageRepository(
        impl: AppLanguageRepositoryImpl,
    ): AppLanguageRepository
}
