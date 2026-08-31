package com.kemalurekli.electricalcalculator.features.pro

import com.kemalurekli.electricalcalculator.features.pro.presentation.PaywallViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The paywall. The entitlement it reads is registered by `billingModule`. */
val proModule: Module = module {
    viewModelOf(::PaywallViewModel)
}
