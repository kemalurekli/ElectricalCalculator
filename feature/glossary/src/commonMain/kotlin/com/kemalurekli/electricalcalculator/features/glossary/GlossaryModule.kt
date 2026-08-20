package com.kemalurekli.electricalcalculator.features.glossary

import com.kemalurekli.electricalcalculator.features.glossary.presentation.GlossaryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What the glossary needs.
 *
 * `SavedStateHandle` is not listed: Koin supplies it from the creation extras
 * the ViewModel is built with, the same way Hilt did. It is worth knowing that
 * it works, because the in-progress search surviving process death depends on
 * it and nothing about the DI swap would announce it if it stopped.
 */
val glossaryModule: Module = module {
    viewModelOf(::GlossaryViewModel)
}
