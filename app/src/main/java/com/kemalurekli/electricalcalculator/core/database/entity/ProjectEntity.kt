package com.kemalurekli.electricalcalculator.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * A job and its shared supply parameters.
 *
 * Enums are stored by `name`, as everywhere else in this database, so that
 * reordering a constant cannot silently repoint existing rows. Numeric fields
 * are stored as the text the user typed: they are re-parsed on every
 * calculation anyway, and keeping the text means a value comes back exactly as
 * it was entered.
 */
@Entity(tableName = ProjectEntity.TABLE_NAME)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "reference")
    val reference: String,

    @ColumnInfo(name = "site")
    val site: String,

    @ColumnInfo(name = "system")
    val system: String,

    @ColumnInfo(name = "system_voltage")
    val systemVoltage: String,

    @ColumnInfo(name = "material")
    val material: String,

    @ColumnInfo(name = "insulation")
    val insulation: String,

    @ColumnInfo(name = "method")
    val method: String,

    @ColumnInfo(name = "ambient_temperature_c")
    val ambientTemperatureC: String,

    @ColumnInfo(name = "max_voltage_drop_percent")
    val maxVoltageDropPercent: String,

    @ColumnInfo(name = "external_impedance_ohms")
    val externalImpedanceOhms: String,

    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,

    /** Indexed because the project list orders by it. */
    @ColumnInfo(name = "updated_at", index = true)
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val TABLE_NAME = "projects"
    }
}

/**
 * One circuit in a project's schedule.
 *
 * The foreign key cascades on delete: a circuit has no meaning without the
 * supply parameters it was designed against, so deleting a project must not
 * leave rows behind that would silently be redesigned against another project's
 * ambient if the id were ever reused.
 */
@Entity(
    tableName = CircuitEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CircuitEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "project_id", index = true)
    val projectId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    /** `CURRENT` or `POWER`. */
    @ColumnInfo(name = "load_kind")
    val loadKind: String,

    @ColumnInfo(name = "load")
    val load: String,

    @ColumnInfo(name = "power_factor")
    val powerFactor: String,

    @ColumnInfo(name = "length_metres")
    val lengthMetres: String,

    @ColumnInfo(name = "grouped_circuits")
    val groupedCircuits: String,

    @ColumnInfo(name = "parallel_conductors")
    val parallelConductors: String,

    @ColumnInfo(name = "device_type")
    val deviceType: String,

    @ColumnInfo(name = "disconnection_time_seconds")
    val disconnectionTimeSeconds: String,

    @ColumnInfo(name = "position")
    val position: Int,
) {
    companion object {
        const val TABLE_NAME = "circuits"
    }
}
