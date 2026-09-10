package com.kemalurekli.electricalcalculator.core.vision

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

/**
 * The system camera, then ML Kit's bundled Latin recogniser.
 *
 * Bundled rather than the build that downloads its model through Play services:
 * a nameplate is read in a plant room or a basement, and a feature that needs
 * the network the first time it is used is a feature that fails on the day it
 * is needed.
 */
@Composable
actual fun rememberNameplateScanner(onRead: (NameplateReading) -> Unit): NameplateScanner {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onRead)
    // One file, overwritten. The photograph is a means to a reading and is of
    // no interest afterwards; keeping every one of them would fill the cache
    // with pictures of motors nobody will look at again.
    val target = remember(context) { File(context.cacheDir, PHOTOGRAPH).apply { parentFile?.mkdirs() } }
    val uri = remember(target) { target.toContentUri(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        // False is a reader who backed out. Nothing to say about it.
        if (taken) recognise(target, callback.value)
    }

    return remember(context, uri) {
        object : NameplateScanner {
            override val isSupported: Boolean =
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

            override fun scan() = launcher.launch(uri)
        }
    }
}

private fun recognise(file: File, onRead: (NameplateReading) -> Unit) {
    val recogniser = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    runCatching { InputImage.fromBitmap(BitmapFactory.decodeFile(file.absolutePath), 0) }
        .onFailure {
            Log.w(TAG, "Could not read the photograph", it)
            onRead(NameplateReading())
        }
        .onSuccess { image ->
            recogniser.process(image)
                // Line by line rather than as one block: a nameplate is a grid,
                // and the recogniser's own idea of where a line ends is a better
                // guess at which figure goes with which symbol than anything
                // this code could work out from coordinates.
                .addOnSuccessListener { text ->
                    val lines = text.textBlocks.flatMap { it.lines }.map { it.text }
                    // The count, never the content. "Nothing was readable" and
                    // "it read fine and the parser rejected it" are the two
                    // answers anybody debugging this needs, and neither of them
                    // requires a reader's photograph in a log.
                    Log.d(TAG, "Recognised ${lines.size} line(s)")
                    onRead(NameplateReader.read(lines))
                }
                .addOnFailureListener {
                    Log.w(TAG, "Recognition failed", it)
                    onRead(NameplateReading())
                }
                .addOnCompleteListener { recogniser.close() }
        }
}

private fun File.toContentUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.exports", this)

/** Under the directory the file provider already publishes. */
private const val PHOTOGRAPH = "exports/nameplate.jpg"

private const val TAG = "Nameplate"
