package com.kemalurekli.electricalcalculator.core.database.di

import android.content.Context
import androidx.room.Room
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.MIGRATION_1_2
import com.kemalurekli.electricalcalculator.core.database.MIGRATION_2_3
import com.kemalurekli.electricalcalculator.core.database.MIGRATION_3_4
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ElecToolkitDatabase = Room.databaseBuilder(
        context,
        ElecToolkitDatabase::class.java,
        ElecToolkitDatabase.NAME,
    )
        // Explicit rather than destructive: a user's pinned shelf and their
        // saved calculations are the only data this app holds, and losing
        // them to an update would be losing all of it.
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

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
