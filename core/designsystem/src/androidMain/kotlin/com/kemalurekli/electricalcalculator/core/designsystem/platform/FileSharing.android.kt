package com.kemalurekli.electricalcalculator.core.designsystem.platform

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

@Composable
actual fun rememberFileSharing(): FileSharing {
    val context = LocalContext.current
    return remember(context) { AndroidFileSharing(context) }
}

private class AndroidFileSharing(private val context: Context) : FileSharing {

    override fun share(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        chooserTitle: String,
    ): Boolean {
        val uri = try {
            val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
            val file = File(directory, fileName)
            file.writeBytes(bytes)
            FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write $fileName", e)
            return false
        } catch (e: IllegalArgumentException) {
            // FileProvider throws this when the path is outside the configured
            // roots, which would mean res/xml/file_paths.xml and this file have
            // drifted apart.
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

    private companion object {
        const val TAG = "FileSharing"
        const val DIRECTORY = "exports"
    }
}
