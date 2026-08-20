package com.kemalurekli.electricalcalculator.core.designsystem.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberResultSharing(): ResultSharing {
    val context = LocalContext.current
    return remember(context) { AndroidResultSharing(context) }
}

private class AndroidResultSharing(private val context: Context) : ResultSharing {

    override fun copy(label: String, text: String): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        // Android 13 draws its own clipboard preview; an in-app snackbar on top
        // of it would be the same news twice.
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    override fun share(subject: String, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, subject))
    }
}
