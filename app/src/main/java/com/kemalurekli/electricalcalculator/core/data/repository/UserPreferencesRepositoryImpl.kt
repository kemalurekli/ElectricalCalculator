package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backs [UserPreferencesRepository] with Preferences DataStore.
 *
 * A thin pass-through today; it exists so the domain layer depends on an
 * interface rather than on DataStore, which is what lets a future release move
 * settings to cloud backup without touching any consumer.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataSource.preferences

    override suspend fun setThemeMode(themeMode: ThemeMode) =
        dataSource.setThemeMode(themeMode)

    override suspend fun setDynamicColor(enabled: Boolean) =
        dataSource.setDynamicColor(enabled)

    override suspend fun setUnitSystem(unitSystem: UnitSystem) =
        dataSource.setUnitSystem(unitSystem)

    override suspend fun setDisclaimerAccepted(accepted: Boolean) =
        dataSource.setDisclaimerAccepted(accepted)

    override suspend fun setForumRulesAccepted(accepted: Boolean) =
        dataSource.setForumRulesAccepted(accepted)

    override suspend fun setEngineeringDefaults(defaults: EngineeringDefaults) =
        dataSource.setEngineeringDefaults(defaults)

    override suspend fun seedEngineeringDefaults(locale: Locale) =
        dataSource.seedEngineeringDefaultsIfUnset(EngineeringDefaults.seedFor(locale))
}
