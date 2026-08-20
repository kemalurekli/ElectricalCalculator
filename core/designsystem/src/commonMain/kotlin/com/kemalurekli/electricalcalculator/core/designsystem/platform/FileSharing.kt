package com.kemalurekli.electricalcalculator.core.designsystem.platform

import androidx.compose.runtime.Composable

/**
 * Handing a generated document to the operating system.
 *
 * The sibling of [ResultSharing], and obtained from composition for the same
 * reason: Android needs a `Context` for the cache directory, the content
 * provider and the chooser; iOS needs the view controller to present from.
 *
 * ### Why it takes bytes
 *
 * The Android version used to take a `ScheduleReport` and an `OutputStream`,
 * so the PDF could be drawn straight into the file rather than buffered. That
 * was the right trade when both halves lived in the same module — but a
 * `ScheduleReport` is a projects concept, and a stream is a JVM type. Building
 * the document is now the feature's job and this only carries the result.
 *
 * A schedule is a few pages; the buffer is measured in tens of kilobytes.
 */
interface FileSharing {

    /**
     * Writes [bytes] to a temporary file called [fileName] and offers it.
     *
     * @return false when the file could not be written. A full disk is not
     *   something the user can act on from an export button, so the screen says
     *   the export failed rather than pretending a file exists.
     */
    fun share(fileName: String, mimeType: String, bytes: ByteArray, chooserTitle: String): Boolean
}

@Composable
expect fun rememberFileSharing(): FileSharing

/**
 * A file name the platform will accept, derived from a project reference.
 *
 * Users name jobs things like `Blok A / Kat 3`, and a slash in a file name is a
 * directory that does not exist. Everything outside a conservative set becomes
 * an underscore rather than being dropped, so two jobs whose names differ only
 * in punctuation do not collide.
 */
fun safeFileName(reference: String, fallback: String): String {
    val cleaned = reference.trim()
        .map { if (it.isLetterOrDigit() || it == '-' || it == '_') it else '_' }
        .joinToString("")
        .trim('_')
    return cleaned.ifEmpty { fallback }
}
