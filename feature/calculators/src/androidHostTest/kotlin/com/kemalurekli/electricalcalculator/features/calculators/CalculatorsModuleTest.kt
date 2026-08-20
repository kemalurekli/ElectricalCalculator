package com.kemalurekli.electricalcalculator.features.calculators

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Every ViewModel in this module is registered with Koin.
 *
 * Hilt made this test unnecessary: a `@HiltViewModel` that nothing could build
 * was a compile error. Koin resolves by type at runtime, so a ViewModel missing
 * from `CalculatorsModule` compiles, ships, and throws
 * `NoDefinitionFoundException` the first time its screen is opened — which is a
 * screen nobody visits during a normal test run.
 *
 * It happened twice while this module was being assembled, both times because
 * a file arrived after the module was written. So the check is mechanical and
 * runs every build.
 *
 * Reading the sources rather than the classpath is deliberate: the question is
 * whether the *declaration* is there, and a reflective scan would need the
 * `viewModelOf` lambdas to have been evaluated, which only happens on
 * resolution — the very thing that fails.
 */
class CalculatorsModuleTest {

    private val sourceRoot = File("src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators")

    @Test
    fun `every ViewModel is registered`() {
        val declared = sourceRoot.walkTopDown()
            .filter { it.extension == "kt" && it.name != "CalculatorsModule.kt" }
            .flatMap { file -> VIEW_MODEL.findAll(file.readText()).map { it.groupValues[1] } }
            .toSortedSet()

        val registered = File(sourceRoot, "CalculatorsModule.kt").readText()
            .let { REGISTERED.findAll(it).map { match -> match.groupValues[1] }.toSortedSet() }

        assertEquals(declared, registered)
    }

    private companion object {
        val VIEW_MODEL = Regex("""^class (\w+ViewModel)\(""", RegexOption.MULTILINE)
        val REGISTERED = Regex("""viewModelOf\(::(\w+)\)""")
    }
}
