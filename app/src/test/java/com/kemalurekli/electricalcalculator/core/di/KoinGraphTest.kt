package com.kemalurekli.electricalcalculator.core.di

import androidx.lifecycle.SavedStateHandle
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitTestDao
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.domain.table.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.calculatorsModule
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.favorites.favoritesModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.fieldNotesModule
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import com.kemalurekli.electricalcalculator.features.forum.forumModule
import com.kemalurekli.electricalcalculator.features.pro.proModule
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.features.glossary.glossaryModule
import com.kemalurekli.electricalcalculator.features.history.historyModule
import com.kemalurekli.electricalcalculator.features.home.homeModule
import com.kemalurekli.electricalcalculator.features.projects.projectsModule
import com.kemalurekli.electricalcalculator.features.references.referencesModule
import com.kemalurekli.electricalcalculator.features.settings.settingsModule
import com.kemalurekli.electricalcalculator.features.theory.theoryModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.test.verify.verify

/**
 * Every Koin definition can actually be built.
 *
 * This is the check Hilt used to make at compile time. Koin resolves by type at
 * runtime, so a ViewModel whose dependency nobody registered compiles, ships,
 * and throws `NoDefinitionFoundException` the first time its screen is opened —
 * which, for a screen behind two taps, is not during any test run.
 *
 * It happened five times while the features were being moved, once per missing
 * binding, each found by opening the app and reading logcat. `verify()` walks
 * every definition's constructor by reflection and reports them together.
 *
 * ### Why the extra types are listed, and the trap in them
 *
 * `verify()` reads one module at a time, so anything registered by another —
 * `coreCommonModule`, `coreDataModule`, or a sibling feature — has to be named.
 *
 * Each entry is a promise that *something else* registers that type, and the
 * test cannot check the promise. `CorrectionFactors` sat here after the module
 * that provided it was deleted, and the app crashed on the cable calculator
 * while this stayed green. So every entry names its module below, and an entry
 * whose module cannot be named does not belong here.
 */
class KoinGraphTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every definition can be constructed`() {
        featureModules.forEach { module ->
            module.verify(
                extraTypes = listOf(
                    // coreDataModule
                    HistoryRepository::class,
                    FavoritesRepository::class,
                    ProjectRepository::class,
                    UserPreferencesRepository::class,
                    CorrectionFactors::class,
                    CircuitTestDao::class,
                    // coreCommonModule
                    StringResolver::class,
                    TimeProvider::class,
                    // androidAppModule on Android; nothing on iOS yet, which is
                    // why no shared screen asks for it.
                    RegionProvider::class,
                    // Whether there is a backend at all depends on configuration
                    // the platform supplies; the entry point declares it.
                    // forumModule, from ForumConfig
                    ForumBackend::class,
                    // settingsModule
                    AppLanguageRepository::class,
                    // billingModule, from BillingConfig. Same shape as the
                    // forum: whether there is a store depends on configuration
                    // the build supplies, so the entry point declares it.
                    EntitlementRepository::class,
                    // Koin supplies this from the creation extras a ViewModel is
                    // built with; there is no definition to find.
                    SavedStateHandle::class,
                    // Registered by another feature's module. `verify()` reads
                    // one module at a time, so a dependency that crosses a
                    // module boundary has to be named — the check that remains
                    // is the one that matters: whether a feature registers
                    // everything it owns.
                    // calculatorsModule, read by projects and home
                    CalculatorCatalog::class,
                    AmpacityTable::class,
                    CalculateVoltageDropUseCase::class,
                    CalculateEarthFaultUseCase::class,
                ),
            )
        }
    }

    private val featureModules: List<Module> = listOf(
        converterModule,
        historyModule,
        glossaryModule,
        fieldNotesModule,
        referencesModule,
        calculatorsModule,
        theoryModule,
        favoritesModule,
        homeModule,
        projectsModule,
        forumModule,
        proModule,
        settingsModule,
    )
}
