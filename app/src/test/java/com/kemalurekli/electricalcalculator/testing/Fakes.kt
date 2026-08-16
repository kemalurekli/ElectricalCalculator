package com.kemalurekli.electricalcalculator.testing

import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.Locale

/** A clock the test controls, so timestamps and ordering are deterministic. */
class FakeTimeProvider(private var current: Instant = Instant.ofEpochMilli(1_000L)) : TimeProvider {
    override fun now(): Instant = current

    fun advanceBy(millis: Long) {
        current = current.plusMillis(millis)
    }
}

/**
 * Resolves ids from [values], falling back to [default].
 *
 * The default carries `%s` placeholders so that production code calling
 * `String.format` on a resolved template still works under test without loading
 * real Android resources.
 */
class FakeStringResolver(
    private val values: Map<Int, String> = emptyMap(),
    private val default: String? = null,
) : StringResolver {
    override fun get(id: Int): String = values[id] ?: default ?: "res:$id"
}

/**
 * In-memory [CalculationHistoryDao].
 *
 * Reproduces the ordering, auto-increment and `LIKE` semantics the real queries
 * rely on, so repository behaviour can be verified without a device.
 */
class FakeCalculationHistoryDao : CalculationHistoryDao {

    private val rows = MutableStateFlow<List<CalculationHistoryEntity>>(emptyList())
    private var nextId = 1L

    private fun sorted(list: List<CalculationHistoryEntity>) =
        list.sortedByDescending { it.createdAtEpochMillis }

    override fun observeAll(): Flow<List<CalculationHistoryEntity>> = rows.map(::sorted)

    override fun observeByCalculator(calculatorId: String): Flow<List<CalculationHistoryEntity>> =
        rows.map { all -> sorted(all.filter { it.calculatorId == calculatorId }) }

    override fun observeRecent(limit: Int): Flow<List<CalculationHistoryEntity>> =
        rows.map { sorted(it).take(limit) }

    override fun observeSearch(pattern: String): Flow<List<CalculationHistoryEntity>> {
        val regex = pattern.toLikeRegex()
        return rows.map { all ->
            sorted(all.filter { regex.matches(it.title) || regex.matches(it.summary) })
        }
    }

    override fun observeCount(): Flow<Int> = rows.map { it.size }

    override suspend fun findById(id: Long): CalculationHistoryEntity? =
        rows.value.firstOrNull { it.id == id }

    override suspend fun insert(entity: CalculationHistoryEntity): Long {
        val id = if (entity.id == 0L) nextId++ else entity.id
        val stored = entity.copy(id = id)
        rows.value = rows.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun rename(id: Long, title: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(title = title) else it }
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    /**
     * Translates a SQL `LIKE` pattern into a regex, honouring the backslash
     * escapes the real query declares via its `ESCAPE` clause.
     */
    private fun String.toLikeRegex(): Regex {
        val builder = StringBuilder()
        var index = 0
        while (index < length) {
            when (val char = this[index]) {
                '\\' -> {
                    index++
                    if (index < length) builder.append(Regex.escape(this[index].toString()))
                }
                '%' -> builder.append(".*")
                '_' -> builder.append('.')
                else -> builder.append(Regex.escape(char.toString()))
            }
            index++
        }
        return Regex(builder.toString(), RegexOption.IGNORE_CASE)
    }
}

/** In-memory [FavoriteCalculatorDao] preserving pin order. */
class FakeFavoriteItemDao : FavoriteItemDao {

    private val rows = MutableStateFlow<List<FavoriteItemEntity>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteItemEntity>> =
        rows.map { list -> list.sortedBy { it.pinnedAtEpochMillis } }

    override fun observeIsFavorite(kind: String, key: String): Flow<Boolean> =
        rows.map { list -> list.any { it.kind == kind && it.key == key } }

    override suspend fun isFavorite(kind: String, key: String): Boolean =
        rows.value.any { it.kind == kind && it.key == key }

    override suspend fun insert(entity: FavoriteItemEntity) {
        rows.value = rows.value.filterNot { it.kind == entity.kind && it.key == entity.key } + entity
    }

    override suspend fun delete(kind: String, key: String) {
        rows.value = rows.value.filterNot { it.kind == kind && it.key == key }
    }
}

/**
 * In-memory [AppLanguageRepository].
 *
 * The real implementation talks to `AppCompatDelegate`, which needs a running
 * Android runtime; this stands in for it on the JVM.
 */
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
class FakeRegionProvider(var locale: Locale = Locale.forLanguageTag("tr-TR")) : RegionProvider {
    override fun current(): Locale = locale
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

    override suspend fun setDynamicColor(enabled: Boolean) {
        state.value = state.value.copy(useDynamicColor = enabled)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        state.value = state.value.copy(unitSystem = unitSystem)
    }

    override suspend fun setEngineeringDefaults(defaults: EngineeringDefaults) {
        state.value = state.value.copy(engineering = defaults, engineeringSeeded = true)
    }

    override suspend fun seedEngineeringDefaults(locale: Locale) {
        if (state.value.engineeringSeeded) return
        setEngineeringDefaults(EngineeringDefaults.seedFor(locale))
    }
}
