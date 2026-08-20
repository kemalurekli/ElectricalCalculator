package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.CalculationHistoryDao
import com.kemalurekli.electricalcalculator.core.database.entity.CalculationHistoryEntity
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class HistoryRepositoryImpl(
    private val dao: CalculationHistoryDao,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : HistoryRepository {

    override fun observeAll(): Flow<List<CalculationRecord>> =
        dao.observeAll().mapToDomain()

    override fun observeByCalculator(calculatorId: CalculatorId): Flow<List<CalculationRecord>> =
        dao.observeByCalculator(calculatorId.key).mapToDomain()

    override fun observeRecent(limit: Int): Flow<List<CalculationRecord>> {
        // A non-positive limit would make SQLite return every row; treat it as
        // "nothing requested" so a miscomputed limit cannot load the whole table.
        if (limit <= 0) return flowOf(emptyList())
        return dao.observeRecent(limit).mapToDomain()
    }

    override fun observeSearch(query: String): Flow<List<CalculationRecord>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return observeAll()
        return dao.observeSearch(pattern = "%${trimmed.escapeLikeWildcards()}%").mapToDomain()
    }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun findById(id: Long): CalculationRecord? = withContext(ioDispatcher) {
        dao.findById(id)?.toDomainOrNull()
    }

    override suspend fun save(record: CalculationRecord): Long = withContext(ioDispatcher) {
        dao.insert(record.toEntity())
    }

    override suspend fun rename(id: Long, title: String) = withContext(ioDispatcher) {
        dao.rename(id, title.trim())
    }

    override suspend fun duplicate(id: Long): Long? = withContext(ioDispatcher) {
        val existing = dao.findById(id) ?: return@withContext null
        // A duplicate is a new record made now, so it sorts to the top of the
        // list where the user expects to find it.
        dao.insert(
            existing.copy(
                id = 0L,
                createdAtEpochMillis = timeProvider.now().toEpochMilliseconds(),
            ),
        )
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() = withContext(ioDispatcher) {
        dao.deleteAll()
    }

    /**
     * Drops rows whose calculator this build does not recognise.
     *
     * Such rows can exist after a downgrade, or once a calculator is retired.
     * Skipping them keeps the history list readable instead of failing the
     * whole query on one unknown key.
     */
    private fun Flow<List<CalculationHistoryEntity>>.mapToDomain(): Flow<List<CalculationRecord>> =
        map { entities -> entities.mapNotNull { it.toDomainOrNull() } }
}

private fun CalculationHistoryEntity.toDomainOrNull(): CalculationRecord? {
    val id = CalculatorId.fromKeyOrNull(calculatorId) ?: return null
    return CalculationRecord(
        id = this.id,
        calculatorId = id,
        title = title,
        summary = summary,
        inputs = inputs,
        results = results,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
    )
}

private fun CalculationRecord.toEntity() = CalculationHistoryEntity(
    id = id,
    calculatorId = calculatorId.key,
    title = title,
    summary = summary,
    inputs = inputs,
    results = results,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)

/**
 * Escapes the characters SQLite's `LIKE` treats as wildcards.
 *
 * Without this, a query containing `%` matches every row — search for "50%"
 * and the filter silently stops filtering.
 */
private fun String.escapeLikeWildcards(): String =
    replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
