package com.kemalurekli.electricalcalculator.features.references

import com.kemalurekli.electricalcalculator.features.references.presentation.FavoriteToggleViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What the reference section needs.
 *
 * One entry, and it is not a screen's view model: the topics are compile-time
 * data, so the screens hold no state at all. Pinning is the exception, and
 * `FavoriteToggleViewModel` is the whole of it.
 */
val referencesModule: Module = module {
    viewModelOf(::FavoriteToggleViewModel)
}
