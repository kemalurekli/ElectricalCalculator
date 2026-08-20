package com.kemalurekli.electricalcalculator.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kemalurekli.electricalcalculator.core.database.entity.CircuitTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitTestDao {

    @Query("SELECT * FROM circuit_tests WHERE circuit_id = :circuitId")
    fun observeForCircuit(circuitId: Long): Flow<List<CircuitTestEntity>>

    /** Every reading in a project, for the schedule and the export. */
    @Query(
        """
        SELECT t.* FROM circuit_tests t
        INNER JOIN circuits c ON c.id = t.circuit_id
        WHERE c.project_id = :projectId
        """,
    )
    fun observeForProject(projectId: Long): Flow<List<CircuitTestEntity>>

    /** Replaces on conflict, because re-measuring overwrites the reading. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CircuitTestEntity)

    @Query("DELETE FROM circuit_tests WHERE circuit_id = :circuitId AND kind = :kind")
    suspend fun delete(circuitId: Long, kind: String)
}
