package com.kemalurekli.electricalcalculator

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every resource the manifest names exists in the release variant.
 *
 * `app_name` went missing when the strings moved into the feature modules: the
 * sweep took the string and left the comment above it. Nothing noticed, because
 * `src/debug/res` carries its own `app_name` — "ElecToolkit (debug)", so that a
 * debug build and a release install can sit on one device. Every build anyone
 * ran was a debug build, and the release variant would not link for months.
 *
 * Read from the manifest rather than checked against a list, so a resource
 * added there later is covered without anyone remembering this file exists.
 *
 * A unit test rather than a CI step because the failure mode is silence: the
 * app runs, ships nothing, and only says so at the moment of release.
 */
class ManifestResourcesTest {

    @Test
    fun `the release variant defines every string the manifest asks for`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val declared = File("src/main/res/values").listFiles().orEmpty()
            .filter { it.extension == "xml" }
            .flatMap { file -> STRING_NAME.findAll(file.readText()).map { it.groupValues[1] } }
            .toSet()

        val referenced = MANIFEST_STRING.findAll(manifest).map { it.groupValues[1] }.toSet()
        val missing = referenced - declared

        assertTrue(
            "manifest names @string/${missing.joinToString()} but src/main/res does not " +
                "define it — the release build will not link",
            missing.isEmpty(),
        )
    }

    private companion object {
        val MANIFEST_STRING = Regex("""@string/(\w+)""")
        val STRING_NAME = Regex("""<string name="(\w+)"""")
    }
}
