package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
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
}

/**
 * The window itself. `single { }` runs on first resolution rather than when
 * the module is declared, so the application is already in Koin's graph and
 * Hilt's component already built by the time either is asked for.
 */
val hiltBridgeModule: Module = module {
    single<HistoryRepository> {
        EntryPoints.get(androidApplication(), SharedGraphEntryPoint::class.java).historyRepository()
    }
}
