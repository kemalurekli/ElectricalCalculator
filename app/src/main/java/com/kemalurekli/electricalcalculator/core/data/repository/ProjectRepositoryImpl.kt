package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.CircuitDao
import com.kemalurekli.electricalcalculator.core.database.dao.ProjectDao
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitEntity
import com.kemalurekli.electricalcalculator.core.database.entity.ProjectEntity
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val circuitDao: CircuitDao,
    private val timeProvider: TimeProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProjectRepository {

    override fun observeProjects(): Flow<List<ProjectSummary>> = combine(
        projectDao.observeAll(),
        projectDao.observeCircuitCounts(),
    ) { projects, counts ->
        val byProject = counts.associate { it.projectId to it.count }
        projects.map { ProjectSummary(it.toDomain(), byProject[it.id] ?: 0) }
    }

    override fun observeProject(id: Long): Flow<Project?> =
        projectDao.observeById(id).map { it?.toDomain() }

    override fun observeCircuits(projectId: Long): Flow<List<Circuit>> =
        circuitDao.observeForProject(projectId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun findCircuit(id: Long): Circuit? = withContext(ioDispatcher) {
        circuitDao.findById(id)?.toDomain()
    }

    override suspend fun saveProject(project: Project): Long = withContext(ioDispatcher) {
        val now = timeProvider.now()
        if (project.id == Project.NO_ID) {
            projectDao.insert(project.toEntity(createdAt = now, updatedAt = now))
        } else {
            // The creation time is the row's, not the caller's: a screen that
            // round-trips a project through a form has no business rewriting
            // when the job was opened.
            val createdAt = projectDao.findById(project.id)
                ?.let { Instant.fromEpochMilliseconds(it.createdAtEpochMillis) }
                ?: now
            projectDao.update(project.toEntity(createdAt = createdAt, updatedAt = now))
            project.id
        }
    }

    override suspend fun deleteProject(id: Long) = withContext(ioDispatcher) {
        projectDao.delete(id)
    }

    override suspend fun saveCircuit(circuit: Circuit): Long = withContext(ioDispatcher) {
        val id = if (circuit.id == Circuit.NO_ID) {
            val position = (circuitDao.lastPosition(circuit.projectId) ?: -1) + 1
            circuitDao.insert(circuit.toEntity(position))
        } else {
            circuitDao.update(circuit.toEntity(circuit.position))
            circuit.id
        }
        touch(circuit.projectId)
        id
    }

    override suspend fun deleteCircuit(circuit: Circuit) = withContext(ioDispatcher) {
        circuitDao.delete(circuit.id)
        touch(circuit.projectId)
    }

    /** Moves a project's modified time without disturbing anything else on it. */
    private suspend fun touch(projectId: Long) {
        val row = projectDao.findById(projectId) ?: return
        projectDao.update(row.copy(updatedAtEpochMillis = timeProvider.now().toEpochMilliseconds()))
    }

    private fun Project.toEntity(createdAt: Instant, updatedAt: Instant) = ProjectEntity(
        id = id,
        reference = reference,
        site = site,
        system = system.name,
        systemVoltage = systemVoltage,
        material = material.name,
        insulation = insulation.name,
        method = method.name,
        ambientTemperatureC = ambientTemperatureC,
        maxVoltageDropPercent = maxVoltageDropPercent,
        externalImpedanceOhms = externalImpedanceOhms,
        createdAtEpochMillis = createdAt.toEpochMilliseconds(),
        updatedAtEpochMillis = updatedAt.toEpochMilliseconds(),
    )

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        reference = reference,
        site = site,
        system = enumOrDefault(system, SupplySystem.THREE_PHASE_AC),
        systemVoltage = systemVoltage,
        material = enumOrDefault(material, ConductorMaterial.COPPER),
        insulation = enumOrDefault(insulation, CableInsulation.PVC),
        method = enumOrDefault(method, InstallationMethod.C_CLIPPED_DIRECT),
        ambientTemperatureC = ambientTemperatureC,
        maxVoltageDropPercent = maxVoltageDropPercent,
        externalImpedanceOhms = externalImpedanceOhms,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
        updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMillis),
    )

    private fun Circuit.toEntity(position: Int) = CircuitEntity(
        id = id,
        projectId = projectId,
        name = name,
        loadKind = loadKind.name,
        load = load,
        powerFactor = powerFactor,
        lengthMetres = lengthMetres,
        groupedCircuits = groupedCircuits,
        parallelConductors = parallelConductors,
        deviceType = deviceType,
        disconnectionTimeSeconds = disconnectionTimeSeconds,
        position = position,
    )

    private fun CircuitEntity.toDomain() = Circuit(
        id = id,
        projectId = projectId,
        name = name,
        loadKind = enumOrDefault(loadKind, CircuitLoadKind.CURRENT),
        load = load,
        powerFactor = powerFactor,
        lengthMetres = lengthMetres,
        groupedCircuits = groupedCircuits,
        parallelConductors = parallelConductors,
        deviceType = deviceType,
        disconnectionTimeSeconds = disconnectionTimeSeconds,
        position = position,
    )

    /**
     * A stored name that no longer resolves falls back rather than throwing.
     *
     * It happens on a downgrade, or when a constant is withdrawn in a later
     * release while a row still names it. Losing one field of a project is
     * recoverable; a crash that makes the whole shelf unopenable is not.
     */
    private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}
