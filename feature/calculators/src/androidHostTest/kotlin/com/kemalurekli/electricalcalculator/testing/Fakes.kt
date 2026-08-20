package com.kemalurekli.electricalcalculator.testing

import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.formatPositional
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * The in-memory stand-ins this module's tests need.
 *
 * A copy of the ones in `:app`, not a share: a test source set cannot depend on
 * another module's test source set, and publishing a test-fixtures artifact for
 * five small classes would cost more than the duplication. They converge again
 * when `:app` has nothing left to test.
 */
class FakeTimeProvider(private var current: Instant = Instant.fromEpochMilliseconds(1_000L)) : TimeProvider {
    override fun now(): Instant = current

    fun advanceBy(millis: Long) {
        current += millis.milliseconds
    }
}
class FakeStringResolver(
    private val values: Map<StringResource, String> = emptyMap(),
    private val default: String? = null,
) : StringResolver {

    /**
     * The modules that have left `:app` own their strings now, and a test fake
     * has no resource table to read them from. The key is the string's name in
     * the XML, which is enough for a test asserting that the right entry was
     * asked for; a test asserting on the text itself resolves it directly.
     */
    override fun get(resource: StringResource): String =
        values[resource] ?: default ?: resource.key

    /**
     * Substituted, not appended. The real resolver hands the arguments to
     * Compose Resources, which puts them where the template says; a fake that
     * tacked them on the end passed every assertion about *which* string was
     * asked for and failed the ones about what came out.
     */
    override fun get(resource: StringResource, vararg args: Any): String =
        get(resource).formatPositional(*args)
}
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
