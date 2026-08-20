package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.theory.domain.NumberStyle
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryInputs
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryNumber
import com.kemalurekli.electricalcalculator.features.theory.presentation.format
import com.kemalurekli.electricalcalculator.features.theory.presentation.formatValue
import com.kemalurekli.electricalcalculator.features.theory.presentation.toCalculationStep
import com.kemalurekli.electricalcalculator.features.theory.presentation.unitLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols

/**
 * The properties every derivation on the shelf has, checked over all of them.
 *
 * `ExplainerTest` checks the same two things for the calculators — that a formula
 * line is language-neutral and a substitution uses the reader's decimal separator
 * — but it checks them per calculator, against a hand-kept list of explainers,
 * because each calculator formats its own steps.
 *
 * Here formatting happens in one function, so these run over the whole catalog
 * and cover a topic the moment it is added. Nothing has to be registered.
 *
 * Neither symbols is ever `currentNumberSymbols()`. A test that reads the machine's
 * symbols is a test of the machine.
 */
class TheoryFormattingTest {

    private val us = NumberSymbols(decimalSeparator = '.', groupingSeparator = ',')
    private val turkey = NumberSymbols(decimalSeparator = ',', groupingSeparator = '.')

    // -- Properties over every solution ---------------------------------------

    @Test
    fun `no substitution is left holding an unfilled slot`() {
        // A slot the operands never reach renders a literal "{1}" on the screen.
        forEachStep { id, step ->
            assertTrue(
                "$id: substitution still holds a slot: \"${step.substitution}\"",
                !step.substitution.contains(SLOT),
            )
        }
    }

    @Test
    fun `no substitution still contains its symbols`() {
        // The line is meant to be the formula with the reader's numbers in it. A
        // symbol surviving means an operand was never substituted for it.
        val symbols = listOf("R_t", "R∥", "R₂∥", "U_out", "U_L", "U_0", "I₁", "I₂", "R₁", "R₂", "R₃")
        forEachStep { id, step ->
            symbols.forEach { symbol ->
                assertTrue(
                    "$id: substitution still says \"$symbol\": \"${step.substitution}\"",
                    !step.substitution.contains(symbol),
                )
            }
        }
    }

    @Test
    fun `the formula line is the same in both languages`() {
        forEachSolution { id, steps ->
            val english = steps(us).map { it.formula }
            val turkish = steps(turkey).map { it.formula }
            assertEquals("$id: the symbolic line changed with the language", english, turkish)
        }
    }

    @Test
    fun `the step labels are the same resources in both languages`() {
        // The label is resolved from a resource at render time, so the id must
        // not depend on the symbols the solution was worked in.
        forEachSolution { id, steps ->
            assertEquals(
                "$id: a step's label resource changed with the language",
                steps(us).map { it.label },
                steps(turkey).map { it.label },
            )
        }
    }

    @Test
    fun `substitutions follow the reader's decimal separator`() {
        // Somewhere in the catalog a substitution has to carry a fractional
        // number, or this property is passing vacuously.
        var compared = 0
        forEachSolution { id, steps ->
            steps(us).zip(steps(turkey)).forEach { (english, turkish) ->
                if (english.substitution.contains('.')) {
                    compared++
                    assertTrue(
                        "$id: \"${english.substitution}\" reads the same in Turkish",
                        english.substitution != turkish.substitution,
                    )
                    assertTrue(
                        "$id: the Turkish substitution keeps a full stop: \"${turkish.substitution}\"",
                        turkish.substitution.contains(','),
                    )
                }
            }
        }
        assertTrue("No substitution anywhere carries a decimal", compared > 0)
    }

    @Test
    fun `no formatted step comes out blank`() {
        forEachStep { id, step ->
            assertTrue("$id: blank formula", step.formula.isNotBlank())
            assertTrue("$id: blank substitution", step.substitution.isNotBlank())
            assertTrue("$id: blank result", step.result.isNotBlank())
        }
    }

    // -- The number styles ----------------------------------------------------

    @Test
    fun `a fixed number carries its unit beside it`() {
        val number = TheoryNumber(value = 8.7, unit = "A", decimals = 2)
        assertEquals("8.7 A", number.format(us))
        assertEquals("8,7 A", number.format(turkey))
        assertEquals("8.7", number.formatValue(us))
        assertEquals("A", number.unitLabel)
    }

    @Test
    fun `an SI-prefixed number keeps the prefix with the unit`() {
        // Splitting a prefixed value into a number and a unit would print "3.183"
        // beside "Ω" and lose the mega entirely, so these come back whole and the
        // result card is told to print no unit of its own.
        val number = TheoryNumber(
            value = 3_183_098.86,
            unit = "Ω",
            decimals = 4,
            style = NumberStyle.SI_PREFIX,
        )
        assertEquals("3.183 MΩ", number.format(us))
        assertEquals("3.183 MΩ", number.formatValue(us))
        assertEquals("", number.unitLabel)
    }

    @Test
    fun `a number with no unit is not given a trailing space`() {
        val number = TheoryNumber(value = 0.85, decimals = 2)
        assertEquals("0.85", number.format(us))
    }

    // -- Plumbing -------------------------------------------------------------

    /** Runs the body once per solution, with a way to work it in a given symbols. */
    private fun forEachSolution(check: (String, (NumberSymbols) -> List<CalculationStep>) -> Unit) {
        var seen = 0
        TheoryCatalog.all.forEach { topic ->
            topic.solutions.forEach { solution ->
                val example = solution.examples.firstOrNull() ?: return@forEach
                val parsed = solution.fields.mapNotNull { field ->
                    val raw = example.values[field.key].orEmpty()
                    if (field.optional && raw.isBlank()) null
                    else field.key to requireNotNull(raw.toDoubleOrNull())
                }.toMap()

                seen++
                val solved = solution.solve(TheoryInputs(parsed))
                check("${topic.key}/${solution.key}") { symbols ->
                    solved.steps.map { it.toCalculationStep(symbols) }
                }
            }
        }
        assertTrue("No solution in the catalog carries an example to work", seen > 0)
    }

    private fun forEachStep(check: (String, CalculationStep) -> Unit) {
        forEachSolution { id, steps ->
            listOf(us, turkey).forEach { symbols ->
                steps(symbols).forEachIndexed { index, step ->
                    check("$id step $index (${symbols.decimalSeparator})", step)
                }
            }
        }
    }

    private companion object {
        val SLOT = Regex("""\{\d+}""")
    }
}
