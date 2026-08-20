package com.kemalurekli.electricalcalculator.core.datastore.di

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.datastore.createPreferencesDataStore
import com.kemalurekli.electricalcalculator.core.datastore.preferencesContext
import com.kemalurekli.electricalcalculator.core.common.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Hilt owns the store's lifetime; the shared module owns how it is opened.
     *
     * The corruption handling and the file name moved into
     * `createPreferencesDataStore` so both platforms get them. What is left
     * here is handing the shared code the two things only Android supplies: a
     * `Context` to resolve a path from, and the application's coroutine scope.
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> {
        preferencesContext = context
        return createPreferencesDataStore(scope + ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): UserPreferencesDataSource = UserPreferencesDataSource(dataStore)
}
