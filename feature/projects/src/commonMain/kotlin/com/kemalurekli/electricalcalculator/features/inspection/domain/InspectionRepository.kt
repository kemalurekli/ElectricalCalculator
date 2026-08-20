package com.kemalurekli.electricalcalculator.features.inspection.domain

import kotlinx.coroutines.flow.Flow

/**
 * Readings taken against circuits.
 *
 * Separate from [ProjectRepository] because the two are read by different
 * people at different times: a designer fills in the schedule in an office and
 * an inspector fills in the readings on site, often months later and sometimes
 * without the design.
 */
interface InspectionRepository {

    fun observeForCircuit(circuitId: Long): Flow<List<CircuitTest>>

    fun observeForProject(projectId: Long): Flow<List<CircuitTest>>

    /** Writes a reading, replacing whatever was recorded for that circuit and kind. */
    suspend fun save(test: CircuitTest)

    suspend fun clear(test: CircuitTest)
}
