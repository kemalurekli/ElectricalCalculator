package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.MainViewModel
import com.kemalurekli.electricalcalculator.core.common.util.PlatformRegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.data.repository.AppLanguageRepositoryImpl
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What is left of `:app`'s object graph.
 *
 * Two things that genuinely need Android and cannot move: the region, which
 * comes from `LocaleManager`, and the per-app language, which is driven through
 * `AppCompatDelegate`. Plus the two screens still here.
 *
 * This file replaces nine Hilt modules and the entry point that bridged them.
 * Hilt generated the graph from `@Inject constructor`; Koin cannot, so the
 * graph is written out — but by the time the last feature had moved, almost
 * none of it was left to write. `coreCommonModule` and `coreDataModule` build
 * everything else, and they are the same modules iOS uses.
 */
val androidAppModule: Module = module {
    single<RegionProvider> { PlatformRegionProvider(androidContext()) }
    single<AppLanguageRepository> { AppLanguageRepositoryImpl() }

    viewModelOf(::MainViewModel)
    viewModelOf(::SettingsViewModel)
}
