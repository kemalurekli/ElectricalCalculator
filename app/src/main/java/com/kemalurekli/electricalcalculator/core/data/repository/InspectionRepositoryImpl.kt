package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitTestDao
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitTestEntity
import com.kemalurekli.electricalcalculator.core.domain.repository.InspectionRepository
import com.kemalurekli.electricalcalculator.features.inspection.domain.CircuitTest
import com.kemalurekli.electricalcalculator.features.inspection.domain.InsulationTestVoltage
import com.kemalurekli.electricalcalculator.features.inspection.domain.RcdType
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InspectionRepositoryImpl @Inject constructor(
    private val dao: CircuitTestDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : InspectionRepository {

    override fun observeForCircuit(circuitId: Long): Flow<List<CircuitTest>> =
        dao.observeForCircuit(circuitId).map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override fun observeForProject(projectId: Long): Flow<List<CircuitTest>> =
        dao.observeForProject(projectId).map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override suspend fun save(test: CircuitTest) = withContext(ioDispatcher) {
        dao.upsert(test.toEntity())
    }

    override suspend fun clear(test: CircuitTest) = withContext(ioDispatcher) {
        dao.delete(test.circuitId, test.kind.name)
    }

    private fun CircuitTest.toEntity() = CircuitTestEntity(
        circuitId = circuitId,
        kind = kind.name,
        value = value,
        passed = passed,
        insulationVoltage = insulationVoltage.name,
        rcdType = rcdType.name,
    )

    /**
     * Null when the stored kind no longer resolves.
     *
     * A test type withdrawn in a later release leaves rows naming it. Dropping
     * the row when read is right here — unlike a preference, a reading the app
     * can no longer interpret has nothing sensible to fall back to, and showing
     * it under the wrong heading would be worse than not showing it.
     */
    private fun CircuitTestEntity.toDomainOrNull(): CircuitTest? {
        val resolved = TestKind.entries.firstOrNull { it.name == kind } ?: return null
        return CircuitTest(
            circuitId = circuitId,
            kind = resolved,
            value = value,
            passed = passed,
            insulationVoltage = InsulationTestVoltage.entries
                .firstOrNull { it.name == insulationVoltage } ?: InsulationTestVoltage.V500,
            rcdType = RcdType.entries.firstOrNull { it.name == rcdType } ?: RcdType.GENERAL,
        )
    }
}
