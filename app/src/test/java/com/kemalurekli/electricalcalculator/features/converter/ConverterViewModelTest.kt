package com.kemalurekli.electricalcalculator.features.converter

import com.kemalurekli.electricalcalculator.features.converter.domain.ConvertUnitUseCase
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterViewModel
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConverterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = ConverterViewModel(ConvertUnitUseCase())

    private fun ConverterViewModel.selectUnits(from: String, to: String) {
        val category = uiState.value.category
        onFromChange(requireNotNull(category.unitOrNull(from)))
        onToChange(requireNotNull(category.unitOrNull(to)))
    }

    // -- Defaults ------------------------------------------------------------------

    @Test
    fun `the converter opens on voltage with two different units`() {
        val state = viewModel().uiState.value

        assertEquals("voltage", state.category.key)
        assertEquals(UnitCatalog.all.size, state.categories.size)
        assertTrue(state.from.key != state.to.key)
    }

    @Test
    fun `nothing is converted until a value is entered`() {
        val state = viewModel().uiState.value

        assertTrue(state.isEmpty)
        assertNull(state.result)
        assertTrue(state.allUnits.isEmpty())
    }

    // -- Converting ------------------------------------------------------------------

    @Test
    fun `a value converts as it is typed, with no button to press`() {
        val model = viewModel()
        model.selectUnits("kV", "V")

        model.onInputChange("11")

        assertEquals(11_000.0, model.uiState.value.result!!, 1e-9)
        assertFalse(model.uiState.value.isEmpty)
    }

    @Test
    fun `a partial number produces no result rather than an error`() {
        // "-" and "." are on the way to a valid number, not mistakes to report.
        val model = viewModel()

        model.onInputChange("-")

        assertNull(model.uiState.value.result)
    }

    @Test
    fun `a comma decimal separator is accepted`() {
        val model = viewModel()
        model.selectUnits("kV", "V")

        model.onInputChange("0,4")

        assertEquals(400.0, model.uiState.value.result!!, 1e-9)
    }

    @Test
    fun `a negative value converts, which temperature and gauge both need`() {
        val model = viewModel()
        model.onCategoryChange(UnitCatalog.temperature)
        model.selectUnits("C", "F")

        model.onInputChange("-40")

        assertEquals(-40.0, model.uiState.value.result!!, 1e-9)
    }

    @Test
    fun `clearing the value clears the result`() {
        val model = viewModel()
        model.onInputChange("11")
        assertNotNull(model.uiState.value.result)

        model.onInputChange("")

        assertNull(model.uiState.value.result)
        assertTrue(model.uiState.value.allUnits.isEmpty())
    }

    // -- Unit and category selection ------------------------------------------------------

    @Test
    fun `swapping the units inverts the conversion`() {
        val model = viewModel()
        model.selectUnits("kV", "V")
        model.onInputChange("11")

        model.onSwap()

        val state = model.uiState.value
        assertEquals("V", state.from.key)
        assertEquals("kV", state.to.key)
        assertEquals(0.011, state.result!!, 1e-12)
    }

    @Test
    fun `changing category keeps the value the user typed`() {
        // Someone converting 400 in one category usually wants the same number
        // in another; clearing it would be busywork.
        val model = viewModel()
        model.onInputChange("400")

        model.onCategoryChange(UnitCatalog.length)

        assertEquals("400", model.uiState.value.input)
        assertNotNull(model.uiState.value.result)
    }

    @Test
    fun `changing category selects units from the new category`() {
        val model = viewModel()

        model.onCategoryChange(UnitCatalog.wireGauge)

        val state = model.uiState.value
        assertNotNull(state.category.unitOrNull(state.from.key))
        assertNotNull(state.category.unitOrNull(state.to.key))
        assertTrue(state.from.key != state.to.key)
    }

    @Test
    fun `the conversion follows a change of unit immediately`() {
        val model = viewModel()
        model.onInputChange("11")
        model.selectUnits("kV", "V")
        assertEquals(11_000.0, model.uiState.value.result!!, 1e-9)

        model.onToChange(requireNotNull(UnitCatalog.voltage.unitOrNull("mV")))

        assertEquals(11_000_000.0, model.uiState.value.result!!, 1e-6)
    }

    // -- The full list --------------------------------------------------------------------

    @Test
    fun `every other unit in the category is listed`() {
        val model = viewModel()
        model.onCategoryChange(UnitCatalog.wireGauge)
        model.selectUnits("mm2", "awg")

        model.onInputChange("16")

        val all = model.uiState.value.allUnits
        assertEquals(UnitCatalog.wireGauge.units.size - 1, all.size)
        assertTrue(all.none { it.unit.key == "mm2" })
    }

    @Test
    fun `the listed values match the headline conversion`() {
        val model = viewModel()
        model.onCategoryChange(UnitCatalog.wireGauge)
        model.selectUnits("mm2", "awg")
        model.onInputChange("16")

        val state = model.uiState.value
        val awgRow = state.allUnits.single { it.unit.key == "awg" }
        assertEquals(state.result!!, awgRow.value, 1e-12)
    }

    @Test
    fun `sixteen square millimetres sits between AWG 6 and 5`() {
        // The metric and imperial ladders do not line up: 16 mm² is larger
        // than AWG 6 (13.30 mm²) and smaller than AWG 5 (16.77 mm²), so the
        // honest answer is a gauge with a fraction in it.
        val model = viewModel()
        model.onCategoryChange(UnitCatalog.wireGauge)
        model.selectUnits("mm2", "awg")

        model.onInputChange("16")

        assertEquals(5.204, model.uiState.value.result!!, 1e-3)
    }
}
