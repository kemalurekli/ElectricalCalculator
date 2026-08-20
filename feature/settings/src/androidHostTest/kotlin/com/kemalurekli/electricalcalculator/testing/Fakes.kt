package com.kemalurekli.electricalcalculator.testing

import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A copy of `:app`'s, for the same reason the other modules keep one. */
class FakeAppLanguageRepository(
    initial: AppLanguage = AppLanguage.SYSTEM,
) : AppLanguageRepository {

    private val state = MutableStateFlow(initial)

    override val language: StateFlow<AppLanguage> = state.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        state.value = language
    }
}

/** A device region the test states outright, rather than mutating a JVM global. */
class FakeRegionProvider(var region: String = "TR") : RegionProvider {
    override fun currentRegion(): String = region
}

/** In-memory [UserPreferencesRepository]. */
class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences.Default,
) : UserPreferencesRepository {

    private val state = MutableStateFlow(initial)

    override val preferences: Flow<UserPreferences> = state.asStateFlow()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        state.value = state.value.copy(unitSystem = unitSystem)
    }

    override suspend fun setDisclaimerAccepted(accepted: Boolean) {
        state.value = state.value.copy(disclaimerAccepted = accepted)
    }

    override suspend fun setThreadPinned(threadId: String, pinned: Boolean) {
        val current = state.value.pinnedThreadIds
        state.value = state.value.copy(
            pinnedThreadIds = if (pinned) current + threadId else current - threadId,
        )
    }

    override suspend fun setForumRulesAccepted(accepted: Boolean) {
        state.value = state.value.copy(forumRulesAccepted = accepted)
    }

    override suspend fun setEngineeringDefaults(defaults: EngineeringDefaults) {
        state.value = state.value.copy(engineering = defaults, engineeringSeeded = true)
    }

    override suspend fun seedEngineeringDefaults(regionCode: String) {
        if (state.value.engineeringSeeded) return
        setEngineeringDefaults(EngineeringDefaults.seedFor(regionCode))
    }
}
