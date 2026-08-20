package com.kemalurekli.electricalcalculator.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A stored calculation.
 *
 * [calculatorId] holds the [
 *   com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId.key
 * ] string rather than an enum ordinal, so that reordering the enum cannot
 * silently repoint existing rows at the wrong calculator.
 *
 * Inputs and results are JSON maps. Each calculator owns a different set of
 * fields, and a column-per-field schema would need a migration for every new
 * calculator; a serialised map keeps the table stable as the app grows.
 */
@Entity(tableName = CalculationHistoryEntity.TABLE_NAME)
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "calculator_id", index = true)
    val calculatorId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "summary")
    val summary: String,

    /** JSON object of field key to raw input value. */
    @ColumnInfo(name = "inputs")
    val inputs: Map<String, String>,

    /** JSON object of result key to formatted output value. */
    @ColumnInfo(name = "results")
    val results: Map<String, String>,

    /** Epoch milliseconds; indexed because every query orders by it. */
    @ColumnInfo(name = "created_at", index = true)
    val createdAtEpochMillis: Long,
) {
    companion object {
        const val TABLE_NAME = "calculation_history"
    }
}
