package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitTestDao
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.domain.table.CorrectionFactors
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Lets Koin definitions resolve objects that Hilt still owns.
 *
 * A ported feature's Koin module names the repositories it needs; on iOS those
 * come from `coreDataModule`, which builds the database itself. Registering
 * that module on Android as well would be the obvious move and the wrong one —
 * Hilt has already built a database, and a second `ElecToolkitDatabase` opens
 * the same file a second time. Two connections with their own write-ahead logs
 * is how a saved calculation goes missing.
 *
 * So on Android there is exactly one graph holding real objects, and this file
 * is a window into it. Each entry shrinks as its owner moves to a `:core:*`
 * module, and the file disappears with Hilt.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedGraphEntryPoint {
    fun historyRepository(): HistoryRepository
    fun favoritesRepository(): FavoritesRepository
    fun userPreferencesRepository(): UserPreferencesRepository
    fun projectRepository(): ProjectRepository
    fun stringResolver(): StringResolver
    fun timeProvider(): TimeProvider
    fun correctionFactors(): CorrectionFactors
    fun circuitTestDao(): CircuitTestDao
}

/**
 * The window itself. `single { }` runs on first resolution rather than when
 * the module is declared, so the application is already in Koin's graph and
 * Hilt's component already built by the time either is asked for.
 */
val hiltBridgeModule: Module = module {
    fun entryPoint(application: android.app.Application) =
        EntryPoints.get(application, SharedGraphEntryPoint::class.java)

    single<HistoryRepository> { entryPoint(androidApplication()).historyRepository() }
    single<FavoritesRepository> { entryPoint(androidApplication()).favoritesRepository() }
    single<UserPreferencesRepository> { entryPoint(androidApplication()).userPreferencesRepository() }
    single<ProjectRepository> { entryPoint(androidApplication()).projectRepository() }
    single<StringResolver> { entryPoint(androidApplication()).stringResolver() }
    single<TimeProvider> { entryPoint(androidApplication()).timeProvider() }
    single { entryPoint(androidApplication()).correctionFactors() }
    single { entryPoint(androidApplication()).circuitTestDao() }
}
