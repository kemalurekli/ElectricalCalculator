package com.kemalurekli.electricalcalculator.features.history

import com.kemalurekli.electricalcalculator.features.history.presentation.HistoryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What the history screen needs.
 *
 * Shorter than the converter's module, and for a reason worth noting: the
 * repository it depends on is registered by `coreDataModule`, so this file
 * declares only the screen's own class. That is the layered structure paying
 * off — every feature added from here on should be one line long, and one that
 * is not is a feature reaching past its layer.
 */
val historyModule: Module = module {
    viewModelOf(::HistoryViewModel)
}
