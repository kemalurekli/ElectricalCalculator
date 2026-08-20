package com.kemalurekli.electricalcalculator.features.home

import com.kemalurekli.electricalcalculator.features.home.domain.SearchIndexBuilder
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The dashboard and the index its search reads. */
val homeModule: Module = module {
    singleOf(::SearchIndexBuilder)
    viewModelOf(::HomeViewModel)
}
