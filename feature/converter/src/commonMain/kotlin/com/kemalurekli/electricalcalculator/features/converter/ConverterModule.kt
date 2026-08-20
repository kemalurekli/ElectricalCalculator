package com.kemalurekli.electricalcalculator.features.converter

import com.kemalurekli.electricalcalculator.features.converter.domain.ConvertUnitUseCase
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * What the converter needs, and how to build it.
 *
 * Hilt read `@Inject constructor` and wrote this for us. Koin cannot — it has
 * no compiler plugin — so the graph is declared by hand. That is the real cost
 * of the swap, and it is why it is being paid one feature at a time rather than
 * across ninety-four files in an afternoon.
 *
 * The constructors themselves are unchanged. `viewModelOf` and `singleOf` take
 * a constructor reference and resolve its parameters from the graph, so a
 * dependency added to `ConverterViewModel` needs no edit here — only a
 * dependency that is not yet registered does.
 */
val converterModule: Module = module {
    single { ConvertUnitUseCase() }
    viewModelOf(::ConverterViewModel)
}
