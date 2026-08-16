package com.kemalurekli.electricalcalculator.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * One measurement against one circuit.
 *
 * Keyed on the circuit and the kind of test rather than on a row id: a circuit
 * has one continuity reading, not a history of them, and re-measuring replaces
 * the value. That also makes the write an upsert with no read first.
 *
 * Cascades with the circuit. A reading whose circuit is gone describes nothing.
 */
@Entity(
    tableName = CircuitTestEntity.TABLE_NAME,
    primaryKeys = ["circuit_id", "kind"],
    foreignKeys = [
        ForeignKey(
            entity = CircuitEntity::class,
            parentColumns = ["id"],
            childColumns = ["circuit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CircuitTestEntity(
    @ColumnInfo(name = "circuit_id", index = true)
    val circuitId: Long,

    @ColumnInfo(name = "kind")
    val kind: String,

    /** The text the tester read, kept verbatim as every numeric field here is. */
    @ColumnInfo(name = "value")
    val value: String,

    /** Set for polarity alone, which has no figure to record. */
    @ColumnInfo(name = "passed")
    val passed: Boolean?,

    @ColumnInfo(name = "insulation_voltage")
    val insulationVoltage: String,

    @ColumnInfo(name = "rcd_type")
    val rcdType: String,
) {
    companion object {
        const val TABLE_NAME = "circuit_tests"
    }
}
