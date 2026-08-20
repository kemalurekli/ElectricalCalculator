package com.kemalurekli.electricalcalculator.testing

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

/**
 * Points the running Koin graph at an in-memory database for one test.
 *
 * The Hilt equivalent was `@UninstallModules` plus a replacement module and a
 * `HiltAndroidRule` to inject with. Koin has no compile-time graph to uninstall
 * from, so an override module is loaded over the real one and unloaded after —
 * later definitions win, which is the whole mechanism.
 *
 * The application has already called `startKoin` by the time a test runs, so
 * this deliberately does not start one. Two `startKoin` calls in a process is
 * an error, and the failure names neither the test nor the application.
 */
class TestGraph : ExternalResource() {

    private val overrides = module {
        single {
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ElecToolkitDatabase::class.java,
            )
                // Room's own executors would otherwise require the test to
                // coordinate with background threads it does not own.
                .allowMainThreadQueries()
                .build()
        }
    }

    override fun before() = loadKoinModules(overrides)

    override fun after() = unloadKoinModules(overrides)
}
