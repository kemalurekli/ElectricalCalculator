package com.kemalurekli.electricalcalculator.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kemalurekli.electricalcalculator.core.common.di.ApplicationScope
import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
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

    private const val PREFERENCES_NAME = "user_preferences"

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        // A corrupted preferences file resets to defaults rather than crashing
        // on every launch, which would otherwise require a reinstall.
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope + ioDispatcher,
        produceFile = { context.preferencesDataStoreFile(PREFERENCES_NAME) },
    )
}
