package com.kemalurekli.electricalcalculator.core.data.di

import com.kemalurekli.electricalcalculator.core.common.util.SystemTimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.HistoryRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.ProjectRepositoryImpl
import com.kemalurekli.electricalcalculator.core.data.repository.UserPreferencesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.createElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.datastore.createPreferencesDataStore
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Storage, and everything that reads from it.
 *
 * The mirror of `SharedRepositoryModule` on the Android side, and deliberately
 * so: two graphs describing the same objects, one written for Hilt and one for
 * Koin, kept side by side until Android stops needing the first. Keeping them
 * as separate files rather than one clever shared thing is what makes the
 * eventual deletion a deletion.
 *
 * The database and the preference store are singletons because they hold open
 * file handles. Asking for a second `ElecToolkitDatabase` would not fail — it
 * would quietly open the same file twice, and two connections with their own
 * write-ahead logs are how a row disappears.
 */
val coreDataModule: Module = module {
    single<TimeProvider> { SystemTimeProvider() }

    single { createElecToolkitDatabase() }
    single { get<ElecToolkitDatabase>().calculationHistoryDao() }
    single { get<ElecToolkitDatabase>().favoriteItemDao() }
    single { get<ElecToolkitDatabase>().projectDao() }
    single { get<ElecToolkitDatabase>().circuitDao() }
    single { get<ElecToolkitDatabase>().circuitTestDao() }

    // The scope the preference store collects on. It outlives every screen, so
    // it is a supervisor job that is never cancelled rather than anything tied
    // to a lifecycle; on Android the equivalent scope comes from the Hilt
    // graph and is likewise application-wide.
    single { UserPreferencesDataSource(createPreferencesDataStore(CoroutineScope(SupervisorJob() + Dispatchers.Default))) }

    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }

    // `Dispatchers.IO` is a JVM notion and is absent from common code, so the
    // repositories are handed the default dispatcher instead. Their work is a
    // suspending call into Room, which does its own thread hand-off — the
    // dispatcher here decides where the mapping runs, not the query.
    single<HistoryRepository> { HistoryRepositoryImpl(get(), get(), Dispatchers.Default) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get(), Dispatchers.Default) }
    single<ProjectRepository> { ProjectRepositoryImpl(get(), get(), get(), Dispatchers.Default) }
}
