package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryListViewModel
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryTopicViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The theory list and one open topic. */
val theoryModule: Module = module {
    viewModelOf(::TheoryListViewModel)
    viewModelOf(::TheoryTopicViewModel)
}
