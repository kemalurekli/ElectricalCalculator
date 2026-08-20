package com.kemalurekli.electricalcalculator.core.di

import com.kemalurekli.electricalcalculator.MainViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What is left of `:app`'s object graph.
 *
 * Nothing but `MainViewModel`, which holds the theme the activity reads before
 * it draws. Everything else the app needs is registered by `coreCommonModule`
 * and `coreDataModule` — the same two modules iOS starts with.
 *
 * This file replaces nine Hilt modules and the entry point that bridged them.
 * Hilt generated the graph from `@Inject constructor`; Koin cannot, so the
 * graph is written out — but by the time the last feature had moved, almost
 * none of it was left to write. `coreCommonModule` and `coreDataModule` build
 * everything else, and they are the same modules iOS uses.
 */
val androidAppModule: Module = module {

    viewModelOf(::MainViewModel)
}
