package com.kemalurekli.electricalcalculator.testing

import android.content.Context
import androidx.room.Room
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteCalculatorDao
import com.kemalurekli.electricalcalculator.core.database.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Swaps the on-disk database for an in-memory one during instrumented tests.
 *
 * Without this, every test would read and write the user's real database file,
 * so tests would leak state into each other and their results would depend on
 * the order they happened to run in.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
    ): ElecToolkitDatabase = Room
        .inMemoryDatabaseBuilder(context, ElecToolkitDatabase::class.java)
        // Room's own executors would otherwise require the test to coordinate
        // with background threads it does not own.
        .allowMainThreadQueries()
        .build()

    @Provides
    fun provideCalculationHistoryDao(
        database: ElecToolkitDatabase,
    ): CalculationHistoryDao = database.calculationHistoryDao()

    @Provides
    fun provideFavoriteCalculatorDao(
        database: ElecToolkitDatabase,
    ): FavoriteCalculatorDao = database.favoriteCalculatorDao()
}
