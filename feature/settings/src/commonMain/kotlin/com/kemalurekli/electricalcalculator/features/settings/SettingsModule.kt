package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Settings, and the language repository behind it.
 *
 * The repository is registered here rather than in `coreDataModule` because
 * only this screen writes to it, and on iOS it cannot even do that — see
 * [createAppLanguageRepository].
 */
val settingsModule: Module = module {
    single<AppLanguageRepository> { createAppLanguageRepository() }
    viewModelOf(::SettingsViewModel)
}
