package com.kemalurekli.electricalcalculator.core.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Writes an export to a file and hands it to the share sheet.
 *
 * ### Why a file rather than more text in the share intent
 *
 * The app already shares results as plain text, which is right for one answer.
 * A schedule is a table: put it in `EXTRA_TEXT` and it arrives in an email body
 * as ragged lines nobody can import. A `.csv` attachment opens in the
 * spreadsheet the recipient already uses.
 *
 * ### Why the cache directory
 *
 * The export is a copy handed to another app, not the user's data — the
 * schedule itself lives in the database and can be exported again at any time.
 * Writing to the cache lets the system reclaim the space once the share is done
 * and keeps the app from accumulating stale copies of work that has since
 * changed.
 *
 * Each export overwrites the previous one for the same name, so repeatedly
 * exporting a schedule while editing it does not leave a trail of
 * near-identical files behind for the user to tell apart.
 */
object ReportExporter {

    private const val TAG = "ReportExporter"
    private const val DIRECTORY = "exports"

    /**
     * Writes [content] and opens the chooser.
     *
     * @param baseName the file name without an extension, taken from what the
     *   user called the job so the attachment arrives recognisable.
     * @return false when the file could not be written, which the caller
     *   reports rather than failing silently.
     */
    fun shareCsv(
        context: Context,
        baseName: String,
        content: String,
        chooserTitle: String,
    ): Boolean = share(context, "$baseName.csv", "text/csv", content, chooserTitle)

    private fun share(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String,
        chooserTitle: String,
    ): Boolean {
        val uri = try {
            val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
            val file = File(directory, fileName)
            file.writeText(content)
            FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
        } catch (e: IOException) {
            // A full disk or a revoked cache is not something the user can act
            // on from here, so the screen says the export failed and nothing
            // pretends to have produced a file.
            Log.e(TAG, "Failed to write $fileName", e)
            return false
        } catch (e: IllegalArgumentException) {
            // FileProvider throws this when the path is outside the configured
            // roots, which would mean res/xml/file_paths.xml and this object
            // have drifted apart.
            Log.e(TAG, "Export path is not shareable", e)
            return false
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
        return true
    }

    /**
     * A file name the platform will accept, derived from a project reference.
     *
     * Users name jobs things like `Blok A / Kat 3`, and a slash in a file name
     * is a directory that does not exist. Everything outside a conservative set
     * becomes an underscore rather than being dropped, so two jobs whose names
     * differ only in punctuation do not collide.
     */
    fun safeFileName(reference: String, fallback: String): String {
        val cleaned = reference.trim()
            .map { if (it.isLetterOrDigit() || it == '-' || it == '_') it else '_' }
            .joinToString("")
            .trim('_')
        return cleaned.ifBlank { fallback }.take(MAX_NAME_LENGTH)
    }

    private const val MAX_NAME_LENGTH = 64
}
