package com.kemalurekli.electricalcalculator.core.database.di

import android.content.Context
import androidx.room.Room
import com.kemalurekli.electricalcalculator.core.database.ElecToolkitDatabase
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteCalculatorDao
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
    ).build()

    @Provides
    fun provideCalculationHistoryDao(
        database: ElecToolkitDatabase,
    ): CalculationHistoryDao = database.calculationHistoryDao()

    @Provides
    fun provideFavoriteCalculatorDao(
        database: ElecToolkitDatabase,
    ): FavoriteCalculatorDao = database.favoriteCalculatorDao()
}
