package com.kemalurekli.electricalcalculator.features.calculators.domain

import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_voltage_drop_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_voltage_drop_description
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorSearchTest {

    private val voltageDrop = searchable(
        id = CalculatorId.VOLTAGE_DROP,
        title = "Voltage Drop",
        description = "Volt drop and percentage over a cable run",
        keywords = listOf("vd", "cable loss"),
    )
    private val cableSize = searchable(
        id = CalculatorId.CABLE_SIZE,
        title = "Cable Size",
        description = "Minimum conductor cross-section for a load",
        keywords = listOf("awg", "mm2"),
    )
    private val motorCurrent = searchable(
        id = CalculatorId.MOTOR_CURRENT,
        title = "Motor Current",
        description = "Full load current from motor power rating",
        keywords = listOf("flc"),
    )

    private val items = listOf(voltageDrop, cableSize, motorCurrent)

    @Test
    fun `blank query returns everything unchanged`() {
        assertEquals(items, CalculatorSearch.filter(items, ""))
        assertEquals(items, CalculatorSearch.filter(items, "   "))
    }

    @Test
    fun `matching is case insensitive`() {
        val results = CalculatorSearch.filter(items, "VOLTAGE")
        assertEquals(listOf(voltageDrop), results)
    }

    @Test
    fun `title prefix outranks a description mention`() {
        // "Cable Size" starts with the query; "Voltage Drop" only mentions
        // cable in its description.
        val results = CalculatorSearch.filter(items, "cable")
        assertEquals(cableSize, results.first())
        assertTrue(voltageDrop in results)
    }

    @Test
    fun `a word inside the title matches`() {
        assertEquals(listOf(voltageDrop), CalculatorSearch.filter(items, "drop"))
    }

    @Test
    fun `keywords find a calculator by its industry synonym`() {
        assertEquals(listOf(motorCurrent), CalculatorSearch.filter(items, "flc"))
        assertEquals(listOf(cableSize), CalculatorSearch.filter(items, "awg"))
    }

    @Test
    fun `a query matching nothing returns empty`() {
        assertTrue(CalculatorSearch.filter(items, "transformer").isEmpty())
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals(listOf(voltageDrop), CalculatorSearch.filter(items, "  voltage  "))
    }

    @Test
    fun `equally ranked results keep catalog order`() {
        // Both match only via description, so neither is promoted.
        val results = CalculatorSearch.filter(items, "load")
        assertEquals(listOf(cableSize, motorCurrent), results)
    }

    private fun searchable(
        id: CalculatorId,
        title: String,
        description: String,
        keywords: List<String>,
    ) = SearchableCalculator(
        descriptor = CalculatorDescriptor(
            id = id,
            // Never resolved: the ranking reads the already-resolved title
            // and description passed in beside the descriptor. Two real
            // handles, because the field is typed.
            title = Res.string.calculator_voltage_drop_title,
            description = Res.string.calculator_voltage_drop_description,
            category = CalculatorCategory.CABLE_AND_CONDUIT,
            icon = CalculatorIcon.CABLE,
            searchKeywords = keywords,
        ),
        title = title,
        description = description,
    )
}
