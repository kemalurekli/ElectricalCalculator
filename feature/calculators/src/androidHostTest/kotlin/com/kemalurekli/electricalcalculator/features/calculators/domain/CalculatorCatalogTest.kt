package com.kemalurekli.electricalcalculator.features.calculators.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the invariants the rest of the app relies on: the catalog is the only
 * registry of calculators, so a missing or duplicated entry would silently
 * break the list, search, favourites and history at once.
 */
class CalculatorCatalogTest {

    private val catalog = CalculatorCatalog()

    @Test
    fun `every calculator id has a descriptor`() {
        val registered = catalog.all.map { it.id }.toSet()
        val missing = CalculatorId.entries.toSet() - registered

        assertTrue("Calculators missing from the catalog: $missing", missing.isEmpty())
    }

    @Test
    fun `no calculator is registered twice`() {
        assertEquals(catalog.all.size, catalog.all.map { it.id }.distinct().size)
    }

    @Test
    fun `persisted keys are unique`() {
        val keys = CalculatorId.entries.map { it.key }
        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `every descriptor carries title and description resources`() {
        catalog.all.forEach { descriptor ->
            assertTrue("${descriptor.id} has no title resource", descriptor.title.key.isNotEmpty())
            assertTrue("${descriptor.id} has no description resource", descriptor.description.key.isNotEmpty())
        }
    }

    @Test
    fun `get resolves every registered id`() {
        CalculatorId.entries.forEach { id ->
            assertEquals(id, catalog[id].id)
        }
    }

    @Test
    fun `byCategory covers the whole catalog`() {
        assertEquals(catalog.all.size, catalog.byCategory.values.sumOf { it.size })
    }

    @Test
    fun `keys round trip through fromKeyOrNull`() {
        CalculatorId.entries.forEach { id ->
            assertEquals(id, CalculatorId.fromKeyOrNull(id.key))
        }
    }

    @Test
    fun `unknown keys resolve to null rather than throwing`() {
        // Reached when a record written by a newer build is read back, or after
        // a downgrade. Callers filter these out instead of crashing.
        assertNull(CalculatorId.fromKeyOrNull("not_a_calculator"))
        assertNull(CalculatorId.fromKeyOrNull(""))
    }

    @Test
    fun `findById returns the descriptor for a registered calculator`() {
        val descriptor = catalog.findById(CalculatorId.VOLTAGE_DROP)

        assertNotNull(descriptor)
        assertEquals(CalculatorId.VOLTAGE_DROP, descriptor?.id)
    }
}
