package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.features.design.domain.ReportField
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import javax.inject.Inject

/**
 * Turns a designed schedule into a document.
 *
 * ### Why the figures are written plainly
 *
 * Everything here goes into a file that a spreadsheet parses. A Turkish reader
 * sees `3,479` on screen, and that same string in a comma-separated file is
 * either a broken row or, once quoted, a number the spreadsheet treats as text.
 * So the export writes `3.479` — dot decimal, no grouping — and accepts that
 * the file does not read exactly like the screen. The alternative is a file
 * that looks right and imports wrong, which is worse in a document someone
 * hands to a contractor.
 *
 * The headings are still translated. They are read by a person; the numbers are
 * read by software first.
 */
class ScheduleReportBuilder @Inject constructor(
    private val stringResolver: StringResolver,
) {

    fun build(project: Project, rows: List<CircuitRow>): ScheduleReport = ScheduleReport(
        title = project.reference.ifBlank { string(R.string.projects_untitled) },
        supply = supplyFields(project),
        columns = listOf(
            string(R.string.report_column_circuit),
            string(R.string.report_column_load),
            string(R.string.report_column_length),
            string(R.string.report_column_device),
            string(R.string.report_column_size),
            string(R.string.report_column_protective),
            string(R.string.report_column_capacity),
            string(R.string.report_column_drop),
            string(R.string.report_column_loop),
            string(R.string.report_column_binding),
        ),
        rows = rows.map(::row),
    )

    private fun supplyFields(project: Project) = listOf(
        ReportField(string(R.string.project_site), project.site),
        ReportField(string(R.string.common_supply_system), string(project.system.labelRes())),
        ReportField(string(R.string.common_system_voltage), project.systemVoltage),
        ReportField(
            string(R.string.settings_engineering_material),
            string(project.material.labelRes()),
        ),
        ReportField(
            string(R.string.settings_engineering_insulation),
            string(project.insulation.labelRes()),
        ),
        ReportField(string(R.string.settings_engineering_method), string(project.method.labelRes())),
        ReportField(string(R.string.settings_engineering_ambient), project.ambientTemperatureC),
        ReportField(string(R.string.project_max_drop), project.maxVoltageDropPercent),
        ReportField(string(R.string.project_external_impedance), project.externalImpedanceOhms),
    )

    /**
     * One circuit's line.
     *
     * A circuit that has not been designed still gets a row. Leaving it out
     * would make the file quietly shorter than the schedule on screen, and an
     * export that silently drops the unfinished work is how a circuit gets
     * built without ever having been sized.
     */
    private fun row(row: CircuitRow): List<String> {
        val circuit = row.circuit
        val design = row.design
        val loadUnit = when (circuit.loadKind) {
            CircuitLoadKind.CURRENT -> "A"
            CircuitLoadKind.POWER -> "W"
        }
        return listOf(
            circuit.name.ifBlank { string(R.string.circuit_untitled) },
            circuit.load.plainNumber().withUnit(loadUnit),
            circuit.lengthMetres.plainNumber(),
            design?.deviceRatingAmps.plain(),
            design?.crossSectionMm2.plain(),
            design?.protectiveCrossSectionMm2.plain(),
            design?.deratedCapacityAmps.plain(),
            design?.voltageDropPercent.plain(),
            design?.loopImpedanceOhms.plain(),
            when {
                design == null -> string(R.string.project_circuit_incomplete)
                !design.hasSolution -> string(R.string.project_circuit_no_solution)
                else -> string(design.bindingConstraint.labelRes())
            },
        )
    }

    private fun string(id: Int): String = stringResolver.get(id)
}

/** Dot decimal, no grouping, trailing zeros trimmed — what a spreadsheet parses. */
private fun Double?.plain(): String {
    val value = this ?: return ""
    if (!value.isFinite()) return ""
    val rounded = Math.round(value * PLAIN_SCALE) / PLAIN_SCALE
    return if (rounded == Math.floor(rounded)) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

/**
 * A stored field re-written with a dot separator.
 *
 * The user may have typed `2,5`, which is a perfectly good 2.5 on screen and a
 * row-breaking comma in a CSV field.
 */
private fun String.plainNumber(): String = trim().replace(',', '.')

private fun String.withUnit(unit: String): String = if (isBlank()) "" else "$this $unit"

private const val PLAIN_SCALE = 10_000.0
