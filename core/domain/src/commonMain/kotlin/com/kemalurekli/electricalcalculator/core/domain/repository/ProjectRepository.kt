package com.kemalurekli.electricalcalculator.core.domain.repository

import com.kemalurekli.electricalcalculator.core.domain.model.Circuit
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import kotlinx.coroutines.flow.Flow

/** A project as the list needs it: the job, and how much is in it. */
data class ProjectSummary(
    val project: Project,
    val circuitCount: Int,
)

/**
 * Jobs and the circuits in them.
 *
 * Reads are flows because a project is the one thing in this app two screens
 * look at simultaneously — a schedule open in one place and a circuit being
 * edited in another must not disagree.
 */
interface ProjectRepository {

    /** Most recently worked on first. */
    fun observeProjects(): Flow<List<ProjectSummary>>

    /** Null once the project has been deleted, which the screen reads as "go back". */
    fun observeProject(id: Long): Flow<Project?>

    fun observeCircuits(projectId: Long): Flow<List<Circuit>>

    suspend fun findCircuit(id: Long): Circuit?

    /** Inserts or updates, returning the row id. Stamps the modified time. */
    suspend fun saveProject(project: Project): Long

    suspend fun deleteProject(id: Long)

    /**
     * Inserts or updates a circuit, and marks its project as worked on.
     *
     * The project's timestamp moves because editing a circuit *is* working on
     * the project; leaving it untouched would sink an actively edited job down
     * the list behind ones nobody has opened in weeks.
     */
    suspend fun saveCircuit(circuit: Circuit): Long

    suspend fun deleteCircuit(circuit: Circuit)
}
