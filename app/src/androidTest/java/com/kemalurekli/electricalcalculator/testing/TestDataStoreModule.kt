package com.kemalurekli.electricalcalculator.testing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kemalurekli.electricalcalculator.core.common.di.ApplicationScope
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.datastore.di.DataStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

/**
 * Gives every instrumented test its own preferences file.
 *
 * Two problems, one fix. The obvious one is isolation: without this, a test
 * that changes a setting would leave it changed for whatever ran next, and for
 * the user's own install on the device.
 *
 * The other is that DataStore refuses to open the same file twice in a process,
 * and Hilt builds a fresh `SingletonComponent` per test — so the second test to
 * ask for preferences would get a *second* DataStore over the first one's file
 * and throw. Naming the file after a counter keeps each component on a file of
 * its own, which is what the singleton rule actually requires.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class],
)
object TestDataStoreModule {

    private val instances = AtomicInteger()

    @Provides
    @Singleton
    fun provideTestPreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> {
        val file = context.cacheDir
            .resolve("test-preferences-${instances.incrementAndGet()}.preferences_pb")
        file.delete()
        return PreferenceDataStoreFactory.create(
            scope = scope + ioDispatcher,
            produceFile = { file },
        )
    }

    /**
     * Provided here as well, because replacing [DataStoreModule] replaces every
     * binding in it.
     *
     * `UserPreferencesDataSource` used to carry an `@Inject constructor` and
     * needed no binding at all. It lost the annotation when it moved to a
     * multiplatform module — `javax.inject` is a JVM API — so the module that
     * builds it has to say how, and so does the module that stands in for it.
     */
    @Provides
    @Singleton
    fun provideTestUserPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): UserPreferencesDataSource = UserPreferencesDataSource(dataStore)
}
