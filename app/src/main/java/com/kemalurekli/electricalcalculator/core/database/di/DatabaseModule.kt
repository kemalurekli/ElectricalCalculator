package com.kemalurekli.electricalcalculator.core.database.di

import android.content.Context
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.createElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.databaseContext
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitTestDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    /**
     * Hilt still owns the *lifetime* of the database on Android; the shared
     * module owns how it is opened.
     *
     * The migrations, the SQLite driver and the file location moved into
     * `createElecToolkitDatabase` so that iOS opens exactly the same database
     * the same way. What is left here is handing the shared code the one thing
     * only Android has.
     */
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ElecToolkitDatabase {
        databaseContext = context
        return createElecToolkitDatabase()
    }

    @Provides
    fun provideCalculationHistoryDao(
        database: ElecToolkitDatabase,
    ): CalculationHistoryDao = database.calculationHistoryDao()

    @Provides
    fun provideFavoriteItemDao(
        database: ElecToolkitDatabase,
    ): FavoriteItemDao = database.favoriteItemDao()

    @Provides
    fun provideProjectDao(
        database: ElecToolkitDatabase,
    ): ProjectDao = database.projectDao()

    @Provides
    fun provideCircuitDao(
        database: ElecToolkitDatabase,
    ): CircuitDao = database.circuitDao()

    @Provides
    fun provideCircuitTestDao(
        database: ElecToolkitDatabase,
    ): CircuitTestDao = database.circuitTestDao()
}
