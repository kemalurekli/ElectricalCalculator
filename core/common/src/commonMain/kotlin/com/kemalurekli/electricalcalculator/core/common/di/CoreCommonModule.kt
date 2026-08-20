package com.kemalurekli.electricalcalculator.core.common.di

import com.kemalurekli.electricalcalculator.core.common.util.ComposeStringResolver
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.SystemTimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.createRegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The handful of shared services that are not repositories.
 *
 * Not registered on Android: Hilt builds these already, and `HiltBridgeModule`
 * hands Koin the instances it made. Two graphs each holding their own
 * `StringResolver` would be harmless — it caches nothing — but the rule that
 * Android has exactly one object graph is worth more than the exception,
 * because the next thing registered twice would be the database.
 */
val coreCommonModule: Module = module {
    single<TimeProvider> { SystemTimeProvider() }
    single<StringResolver> { ComposeStringResolver() }
    single<RegionProvider> { createRegionProvider() }
}
