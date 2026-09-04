package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.features.design.domain.ReportField
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_untitled
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_circuit_incomplete
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_circuit_no_solution
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_external_impedance
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_max_drop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_site
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_untitled
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_binding
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_capacity
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_circuit
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_device
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_drop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_length
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_load
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_loop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_measured_insulation
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_measured_r1r2
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_measured_zs
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_protective
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_result
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_column_size
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_ambient
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_insulation
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_material
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_method

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
 *
 * ### What the measured columns do and do not carry
 *
 * The two readings the design has an opinion about — continuity and loop
 * impedance — plus insulation, which is judged against a fixed minimum, and one
 * overall result. The RCD timings and polarity are on the circuit screen but
 * not here: a schedule already ten columns wide stops being readable, and a
 * full test certificate is a different document with a different shape. That
 * document does not exist yet.
 */
class ScheduleReportBuilder(
    private val stringResolver: StringResolver,
) {

    /**
     * @param plainNumbers true for a file software parses, false for one a
     *   person reads. The CSV needs dot decimals; the PDF should read the way
     *   the screen does, in the reader's own symbols.
     */
    fun build(
        project: Project,
        rows: List<CircuitRow>,
        plainNumbers: Boolean,
    ): ScheduleReport = ScheduleReport(
        title = project.reference.ifBlank { string(Res.string.projects_untitled) },
        supply = supplyFields(project, plainNumbers),
        columns = listOf(
            string(Res.string.report_column_circuit),
            string(Res.string.report_column_load),
            string(Res.string.report_column_length),
            string(Res.string.report_column_device),
            string(Res.string.report_column_size),
            string(Res.string.report_column_protective),
            string(Res.string.report_column_capacity),
            string(Res.string.report_column_drop),
            string(Res.string.report_column_loop),
            string(Res.string.report_column_binding),
            string(Res.string.report_column_measured_r1r2),
            string(Res.string.report_column_measured_zs),
            string(Res.string.report_column_measured_insulation),
            string(Res.string.report_column_result),
        ),
        rows = rows.map { row(it, plainNumbers) },
    )

    /**
     * The parameters the schedule was derived under, straight from the fields
     * the user filled in — so they are text, and they carry whatever separator
     * that reader types with. For the CSV they go through [asTypedNumber],
     * which swaps the separator without touching the digits: rounding a
     * parameter on its way into a file would be a quiet change to the record
     * of what was designed.
     */
    private fun supplyFields(project: Project, plainNumbers: Boolean) = listOf(
        ReportField(string(Res.string.project_site), project.site),
        ReportField(string(Res.string.common_supply_system), string(project.system.label())),
        ReportField(string(Res.string.common_system_voltage), project.systemVoltage.asTypedNumber(plainNumbers)),
        ReportField(
            string(Res.string.settings_engineering_material),
            string(project.material.label()),
        ),
        ReportField(
            string(Res.string.settings_engineering_insulation),
            string(project.insulation.label()),
        ),
        ReportField(string(Res.string.settings_engineering_method), string(project.method.label())),
        ReportField(string(Res.string.settings_engineering_ambient), project.ambientTemperatureC.asTypedNumber(plainNumbers)),
        ReportField(string(Res.string.project_max_drop), project.maxVoltageDropPercent.asTypedNumber(plainNumbers)),
        ReportField(string(Res.string.project_external_impedance), project.externalImpedanceOhms.asTypedNumber(plainNumbers)),
    )

    /**
     * One circuit's line.
     *
     * A circuit that has not been designed still gets a row. Leaving it out
     * would make the file quietly shorter than the schedule on screen, and an
     * export that silently drops the unfinished work is how a circuit gets
     * built without ever having been sized.
     */
    private fun row(row: CircuitRow, plainNumbers: Boolean): List<String> {
        val circuit = row.circuit
        val design = row.design
        val loadUnit = when (circuit.loadKind) {
            CircuitLoadKind.CURRENT -> "A"
            CircuitLoadKind.POWER -> "W"
        }
        return listOf(
            circuit.name.ifBlank { string(Res.string.circuit_untitled) },
            circuit.load.forFile(plainNumbers).withUnit(loadUnit),
            circuit.lengthMetres.forFile(plainNumbers),
            design?.deviceRatingAmps.forFile(plainNumbers),
            design?.crossSectionMm2.forFile(plainNumbers),
            design?.protectiveCrossSectionMm2.forFile(plainNumbers),
            design?.deratedCapacityAmps.forFile(plainNumbers),
            design?.voltageDropPercent.forFile(plainNumbers),
            design?.loopImpedanceOhms.forFile(plainNumbers),
            when {
                design == null -> string(Res.string.project_circuit_incomplete)
                !design.hasSolution -> string(Res.string.project_circuit_no_solution)
                else -> string(design.bindingConstraint.label())
            },
            // The measured columns sit to the right of the designed ones, so a
            // reader scanning across meets the prediction before the reading.
            row.reading(TestKind.CONTINUITY)?.test?.value?.forFile(plainNumbers).orEmpty(),
            row.reading(TestKind.LOOP_IMPEDANCE)?.test?.value?.forFile(plainNumbers).orEmpty(),
            row.reading(TestKind.INSULATION)?.test?.value?.forFile(plainNumbers).orEmpty(),
            string(row.overallVerdict.label()),
        )
    }

    private fun string(resource: StringResource): String = stringResolver.get(resource)
}

private fun Double?.forFile(plain: Boolean): String =
    if (plain) plain() else format()

/** Dot decimal, no grouping, trailing zeros trimmed — what a spreadsheet parses. */
private fun Double?.plain(): String {
    val value = this ?: return ""
    if (!value.isFinite()) return ""
    val rounded = kotlin.math.round(value * PLAIN_SCALE) / PLAIN_SCALE
    return if (rounded == kotlin.math.floor(rounded)) {
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
private fun String.forFile(plain: Boolean): String =
    if (plain) trim().replace(',', '.') else trim()

private fun String.withUnit(unit: String): String = if (isBlank()) "" else "$this $unit"

private const val PLAIN_SCALE = 10_000.0

/**
 * A figure somebody typed, put into the convention the file needs.
 *
 * Only the separator changes, and only when the text is a number at all — a
 * site called "Blok 2, kat 3" is left exactly as written. Reformatting through
 * [NumberFormatter] would round it, and this is the record of what the design
 * assumed, not a display value.
 */
private fun String.asTypedNumber(plain: Boolean): String =
    if (plain && NumberFormatter.parseOrNull(this) != null) replace(',', '.') else this
