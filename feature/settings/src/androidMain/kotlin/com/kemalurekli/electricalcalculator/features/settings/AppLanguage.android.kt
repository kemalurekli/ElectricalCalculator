package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository

actual fun createAppLanguageRepository(): AppLanguageRepository = AppLanguageRepositoryImpl()

/** Never called: the picker is in the app. */
actual fun openSystemLanguageSettings() = Unit
