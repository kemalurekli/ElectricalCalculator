package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The pieces of `:feature:calculators` that `:app` still constructs.
 *
 * Four, and each is a screen that has not moved reaching into one that has:
 * the home and favourites screens list the catalogue, the search index reads
 * it, and the circuit designer runs the cable, voltage-drop and earth-fault
 * calculations rather than reimplementing them.
 *
 * They are `@Provides` rather than `@Inject constructor` for the reason
 * everything in this package is — `javax.inject` is a JVM API and the classes
 * now live in a module that also builds for iOS. On the iOS side the same
 * objects come from `calculatorsModule`, written in Koin.
 */
@Module
@InstallIn(SingletonComponent::class)
object CalculatorGraphModule {

    @Provides
    @Singleton
    fun provideCalculatorCatalog(): CalculatorCatalog = CalculatorCatalog()

    @Provides
    @Singleton
    fun provideAmpacityTable(): AmpacityTable = AmpacityTable()

    @Provides
    @Singleton
    fun provideCalculateVoltageDrop(): CalculateVoltageDropUseCase = CalculateVoltageDropUseCase()

    @Provides
    @Singleton
    fun provideCalculateEarthFault(): CalculateEarthFaultUseCase = CalculateEarthFaultUseCase()
}
