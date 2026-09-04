package com.kemalurekli.electricalcalculator.features.design

import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.Project
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.projects.presentation.ScheduleReportBuilder
import kotlin.time.Instant
import org.jetbrains.compose.resources.StringResource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The parameters that travel above the schedule, in two files with opposite
 * needs.
 *
 * They are the one part of the report that never passes through a formatter:
 * they are text the user typed, so they carry that reader's separator. In the
 * PDF that is right — it should read the way the screen does. In the CSV it is
 * a comma inside a comma-separated file, which survives only by being quoted,
 * and a quoted number lands in a spreadsheet as text.
 */
class ScheduleReportBuilderTest {

    private val builder = ScheduleReportBuilder(
        object : StringResolver {
            override fun get(resource: StringResource): String = resource.key
            override fun get(resource: StringResource, vararg args: Any): String = resource.key
        },
    )

    private fun project(
        voltage: String = "230",
        ambient: String = "30",
        drop: String = "5",
        ze: String = "0,35",
    ) = Project(
        reference = "Block A",
        site = "Depot",
        system = SupplySystem.SINGLE_PHASE_AC,
        systemVoltage = voltage,
        material = ConductorMaterial.COPPER,
        insulation = CableInsulation.PVC,
        method = InstallationMethod.B1_CONDUIT_ON_WALL,
        ambientTemperatureC = ambient,
        maxVoltageDropPercent = drop,
        externalImpedanceOhms = ze,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private fun supplyValues(project: Project, plainNumbers: Boolean) =
        builder.build(project, emptyList(), plainNumbers).supply.map { it.value }

    @Test
    fun `a parameter typed with a comma is written with a dot in the file`() {
        assertEquals(
            listOf("0.35"),
            supplyValues(project(ze = "0,35"), plainNumbers = true).filter { "." in it },
        )
    }

    @Test
    fun `the same parameter keeps the reader's comma in the document`() {
        assertEquals(
            listOf("0,35"),
            supplyValues(project(ze = "0,35"), plainNumbers = false).filter { "," in it },
        )
    }

    @Test
    fun `the digits are the ones that were typed, not a rounding of them`() {
        // A parameter is the record of what the design assumed. Sending it
        // through a formatter on the way into a file would round 0,353 to two
        // places and change that record.
        assertEquals(
            listOf("0.353"),
            supplyValues(project(ze = "0,353"), plainNumbers = true).filter { "." in it },
        )
    }

    @Test
    fun `a parameter that is not a number is left exactly as written`() {
        val values = supplyValues(project(ambient = ""), plainNumbers = true)
        assertEquals(1, values.count { it.isEmpty() })
    }
}
