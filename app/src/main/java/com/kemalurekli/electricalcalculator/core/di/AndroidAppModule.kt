package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.MainViewModel
import com.kemalurekli.electricalcalculator.core.common.util.PlatformRegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What is left of `:app`'s object graph.
 *
 * One thing that genuinely needs Android and cannot move: the device region,
 * which comes from `LocaleManager`. Plus `MainViewModel`, which seeds the
 * engineering defaults from it on first launch.
 *
 * This file replaces nine Hilt modules and the entry point that bridged them.
 * Hilt generated the graph from `@Inject constructor`; Koin cannot, so the
 * graph is written out — but by the time the last feature had moved, almost
 * none of it was left to write. `coreCommonModule` and `coreDataModule` build
 * everything else, and they are the same modules iOS uses.
 */
val androidAppModule: Module = module {
    single<RegionProvider> { PlatformRegionProvider(androidContext()) }

    viewModelOf(::MainViewModel)
}
