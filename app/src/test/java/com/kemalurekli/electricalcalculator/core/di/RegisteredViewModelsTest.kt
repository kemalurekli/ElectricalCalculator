package com.kemalurekli.electricalcalculator.core.di

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Every ViewModel in every feature module is registered with Koin.
 *
 * `KoinGraphTest` verifies that what *is* registered can be built. This asks
 * the other half of the question — whether anything was left out — and the two
 * together are what Hilt gave for free.
 *
 * Nothing catches an unregistered ViewModel at compile time: Koin resolves by
 * type at runtime, so the class compiles, ships, and throws
 * `NoDefinitionFoundException` the first time its screen opens. It has happened
 * five times during this port, most recently with three of the forum's, which
 * are on sections rather than on screens of their own and so are reached by a
 * scroll rather than a tap.
 *
 * Reading the sources rather than the classpath is deliberate: the question is
 * whether the *declaration* exists, and a reflective scan would need the
 * `viewModelOf` lambdas evaluated — which happens on resolution, the very thing
 * that fails.
 */
class RegisteredViewModelsTest {

    @Test
    fun `every ViewModel is registered by its module`() {
        val modules = File("../feature").listFiles().orEmpty()
            .filter { it.isDirectory }
            .sortedBy { it.name }

        modules.forEach { module ->
            val sources = File(module, "src/commonMain/kotlin")
            if (!sources.exists()) return@forEach

            val declared = sources.walkTopDown()
                .filter { it.extension == "kt" }
                .flatMap { file -> VIEW_MODEL.findAll(file.readText()).map { it.groupValues[1] } }
                .toSortedSet()

            val registered = sources.walkTopDown()
                .filter { it.name.endsWith("Module.kt") }
                .flatMap { file -> REGISTERED.findAll(file.readText()).map { it.groupValues[1] } }
                .toSortedSet()

            assertEquals("${module.name} leaves a ViewModel unregistered", declared, registered)
        }
    }

    private companion object {
        val VIEW_MODEL = Regex("""^class (\w+ViewModel)\(""", RegexOption.MULTILINE)
        val REGISTERED = Regex("""viewModelOf\(::(\w+)\)""")
    }
}
