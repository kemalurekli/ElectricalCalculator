package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryTopicViewModel
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.testing.FakeFavoriteItemDao
import com.kemalurekli.electricalcalculator.testing.FakeStringResolver
import com.kemalurekli.electricalcalculator.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one view model behind every topic.
 *
 * These are written against real catalog entries rather than a fixture, because
 * the class has no per-topic behaviour to isolate: everything it does is driven
 * off the declared fields and the declared solver, so a fixture would only be
 * testing a second, simpler catalog.
 *
 * No coroutine rule and no `runTest`: this shelf saves no history and keeps no
 * favourites, so nothing here is asynchronous.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TheoryTopicViewModelTest {

    private val favoritesRepository = FavoritesRepositoryImpl(
        dao = FakeFavoriteItemDao(),
        timeProvider = FakeTimeProvider(),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun viewModel() = TheoryTopicViewModel(
        stringResolver = FakeStringResolver(),
        favoritesRepository = favoritesRepository,
    )

    // -- Opening --------------------------------------------------------------

    @Test
    fun `opening a topic selects its first target and seeds the defaults`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")

        val state = model.uiState.value
        assertEquals("ohm_law", state.topic?.key)
        assertEquals("current", state.solutionKey)
        assertEquals("230", state.values["u"])
        assertEquals("", state.values["r"])
        assertNull("Nothing has been calculated yet", state.result)
    }

    @Test
    fun `an unknown key leaves no topic loaded`() {
        // The route outlived the topic it named. The screen reads this as its
        // "not found" state rather than crashing on a missing catalog entry.
        val model = viewModel()
        model.onOpenTopic("no_such_topic")

        assertNull(model.uiState.value.topic)
        assertNull(model.uiState.value.solution)
    }

    @Test
    fun `reopening the same topic does not discard the reader's work`() {
        // The screen keys its effect on the route argument, which fires again on
        // a configuration change. Losing a half-typed form to a rotation is the
        // bug this guards.
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "529")
        model.onOpenTopic("ohm_law")

        assertEquals("529", model.uiState.value.values["r"])
    }

    // -- Validation -----------------------------------------------------------

    @Test
    fun `a blank required field reports against that field and nothing else`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onCalculate()

        val errors = model.uiState.value.errors
        assertEquals(setOf("r"), errors.keys)
        assertEquals(ValidationError.Required, errors["r"])
        assertNull("A failed validation must not leave a result", model.uiState.value.result)
    }

    @Test
    fun `a blank optional field is absent rather than invalid`() {
        // Series resistance takes a third resistor the reader may not have. Blank
        // means "not in the circuit"; it must not read as a missing answer.
        val model = viewModel()
        model.onOpenTopic("series_resistance")
        model.onFieldChange("u", "24")
        model.onFieldChange("r1", "100")
        model.onFieldChange("r2", "220")
        model.onCalculate()

        assertTrue("A blank optional field was reported as an error", model.uiState.value.errors.isEmpty())
        assertEquals("320 Ω", model.uiState.value.result?.primary?.value)
    }

    @Test
    fun `a value outside the field's range is refused`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "-5")
        model.onCalculate()

        assertTrue("A negative resistance was accepted", model.uiState.value.errors.containsKey("r"))
    }

    @Test
    fun `editing a field clears the error it carried`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onCalculate()
        assertTrue(model.uiState.value.errors.containsKey("r"))

        model.onFieldChange("r", "529")
        assertTrue("The error outlived the input it was about", model.uiState.value.errors.isEmpty())
    }

    // -- Results --------------------------------------------------------------

    @Test
    fun `a valid form produces a result, a derivation and diagram labels`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "529")
        model.onCalculate()

        val state = model.uiState.value
        assertNotNull(state.result)
        assertEquals("A", state.result?.primary?.unit)
        assertTrue("No worked solution", state.steps.isNotEmpty())
        assertTrue("The diagram was given no live values", state.diagramLabels.isNotEmpty())
    }

    @Test
    fun `editing a field takes the stale answer away with it`() {
        // A result left on screen while its inputs change reads as the answer to
        // what is on screen now. It is not.
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "529")
        model.onCalculate()
        assertNotNull(model.uiState.value.result)

        model.onFieldChange("r", "52")
        assertNull("The old result survived an edit", model.uiState.value.result)
        assertTrue(model.uiState.value.steps.isEmpty())
        assertTrue(model.uiState.value.diagramLabels.isEmpty())
    }

    // -- Switching target -----------------------------------------------------

    @Test
    fun `switching target keeps a value the new target also asks for`() {
        // Rearranging Ohm's law from current to resistance keeps the voltage,
        // because it is the same voltage. Clearing it would make the selector
        // feel like it had thrown the form away.
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("u", "400")
        model.onFieldChange("r", "100")

        model.onSolutionChange("resistance")

        val state = model.uiState.value
        assertEquals("resistance", state.solutionKey)
        assertEquals("400", state.values["u"])
        assertEquals("The new field arrived pre-filled", "", state.values["i"])
    }

    @Test
    fun `switching target drops the answer to the old question`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "529")
        model.onCalculate()

        model.onSolutionChange("voltage")

        assertNull(model.uiState.value.result)
        assertTrue(model.uiState.value.steps.isEmpty())
    }

    @Test
    fun `switching to the target already selected changes nothing`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onFieldChange("r", "529")
        model.onCalculate()
        val before = model.uiState.value

        model.onSolutionChange("current")

        assertEquals(before, model.uiState.value)
    }

    // -- Examples and reset ---------------------------------------------------

    @Test
    fun `applying an example fills the form and works it straight through`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        val example = TheoryCatalog.topicOrNull("ohm_law")!!
            .solutions.first().examples.first { it.key == "heater" }

        model.onApplyExample(example)

        val state = model.uiState.value
        assertEquals("26.5", state.values["r"])
        assertTrue("An example left validation errors", state.errors.isEmpty())
        assertNotNull("An example produced no result", state.result)
        assertTrue("An example produced no derivation", state.steps.isNotEmpty())
    }

    @Test
    fun `an example starts from the defaults rather than from what was typed`() {
        // The unloaded divider example leaves the load blank. Applying it after
        // the loaded one has to give the unloaded circuit, not the previous load
        // left behind in a field this example never mentions.
        val model = viewModel()
        model.onOpenTopic("voltage_divider")
        val examples = TheoryCatalog.topicOrNull("voltage_divider")!!.solutions.first().examples

        model.onApplyExample(examples.first { it.key == "loaded" })
        assertEquals("4", model.uiState.value.result?.primary?.value)

        model.onApplyExample(examples.first { it.key == "unloaded" })
        assertEquals("", model.uiState.value.values["r_load"])
        assertEquals("6", model.uiState.value.result?.primary?.value)
    }

    @Test
    fun `reset clears the form but keeps the chosen target`() {
        val model = viewModel()
        model.onOpenTopic("ohm_law")
        model.onSolutionChange("resistance")
        model.onFieldChange("u", "400")
        model.onFieldChange("i", "8")
        model.onCalculate()

        model.onReset()

        val state = model.uiState.value
        assertEquals("The target was reset along with the values", "resistance", state.solutionKey)
        assertEquals("230", state.values["u"])
        assertEquals("", state.values["i"])
        assertNull(state.result)
    }
}
