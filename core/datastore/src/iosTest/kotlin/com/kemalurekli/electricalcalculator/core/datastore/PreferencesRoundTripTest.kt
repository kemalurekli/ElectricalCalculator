package com.kemalurekli.electricalcalculator.core.datastore

import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Writes settings to a real file on a simulator and reads them back.
 *
 * iOS-only for the same reason the database round-trip is: the Android side
 * needs a `Context` that a JVM host test has no way to produce, and Android's
 * DataStore is exercised by the app itself on every launch.
 *
 * What this actually checks is the `expect`/`actual` path resolution. Everything
 * else — the keys, the enum encoding, the defaults — is common code that would
 * fail identically on both platforms; the file location is the one thing that
 * could be wrong on exactly one of them.
 */
class PreferencesRoundTripTest {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val dataSource = UserPreferencesDataSource(createPreferencesDataStore(scope))

    @AfterTest
    fun tearDown() {
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `a stored theme survives being read back`() = runTest {
        dataSource.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, dataSource.preferences.first().themeMode)
    }

    @Test
    fun `unrelated settings are left alone by a write`() = runTest {
        dataSource.setUnitSystem(UnitSystem.IMPERIAL)
        dataSource.setThemeMode(ThemeMode.LIGHT)

        val stored = dataSource.preferences.first()

        assertEquals(UnitSystem.IMPERIAL, stored.unitSystem)
        assertEquals(ThemeMode.LIGHT, stored.themeMode)
    }
}
