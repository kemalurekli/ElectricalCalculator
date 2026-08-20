package com.kemalurekli.electricalcalculator.features.fieldnotes

import com.kemalurekli.electricalcalculator.features.fieldnotes.presentation.FieldNotesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** What the field notes need — the repository and the resolver come from below. */
val fieldNotesModule: Module = module {
    viewModelOf(::FieldNotesViewModel)
}
