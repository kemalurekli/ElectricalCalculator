package com.kemalurekli.electricalcalculator.core.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService

/**
 * Copying and sharing of calculation results.
 *
 * Both go through the platform APIs rather than a Compose abstraction because
 * sharing needs an `Intent` chooser regardless, and keeping the pair together
 * makes it obvious that they format the same text.
 */
object ResultSharing {

    /**
     * Puts [text] on the clipboard.
     *
     * @return true when the caller should show its own confirmation. Android 13
     *   and later display a system clipboard preview, so an in-app snackbar on
     *   top of it would be a duplicate notification.
     */
    fun copy(context: Context, label: String, text: String): Boolean {
        val clipboard = context.getSystemService<ClipboardManager>() ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    /** Opens the system share sheet with [text]. */
    fun share(context: Context, subject: String, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, subject))
    }
}
