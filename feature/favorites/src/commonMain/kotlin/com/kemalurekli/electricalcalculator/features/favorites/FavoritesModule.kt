package com.kemalurekli.electricalcalculator.features.favorites

import com.kemalurekli.electricalcalculator.features.favorites.presentation.FavoritesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** One screen, reading five catalogues. */
val favoritesModule: Module = module {
    viewModelOf(::FavoritesViewModel)
}
