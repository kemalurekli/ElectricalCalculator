package com.kemalurekli.electricalcalculator.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitEntity
import com.kemalurekli.electricalcalculator.core.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    /** Most recently touched first: a schedule being worked on stays at the top. */
    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun findById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProjectEntity): Long

    @Update
    suspend fun update(entity: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: Long)

    /** How many circuits each project holds, for the list rows. */
    @Query(
        """
        SELECT project_id AS projectId, COUNT(*) AS count
        FROM circuits GROUP BY project_id
        """,
    )
    fun observeCircuitCounts(): Flow<List<CircuitCount>>
}

/** One row of [ProjectDao.observeCircuitCounts]. */
data class CircuitCount(
    val projectId: Long,
    val count: Int,
)

@Dao
interface CircuitDao {

    @Query("SELECT * FROM circuits WHERE project_id = :projectId ORDER BY position ASC")
    fun observeForProject(projectId: Long): Flow<List<CircuitEntity>>

    @Query("SELECT * FROM circuits WHERE id = :id")
    suspend fun findById(id: Long): CircuitEntity?

    /**
     * The next free slot in a project's schedule.
     *
     * Null for an empty project, which the caller reads as position zero. Room
     * cannot express `COALESCE(MAX(...), -1) + 1` as a non-null Int without
     * lying about the empty case, so the null is handled where it means
     * something.
     */
    @Query("SELECT MAX(position) FROM circuits WHERE project_id = :projectId")
    suspend fun lastPosition(projectId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CircuitEntity): Long

    @Update
    suspend fun update(entity: CircuitEntity)

    @Query("DELETE FROM circuits WHERE id = :id")
    suspend fun delete(id: Long)
}
