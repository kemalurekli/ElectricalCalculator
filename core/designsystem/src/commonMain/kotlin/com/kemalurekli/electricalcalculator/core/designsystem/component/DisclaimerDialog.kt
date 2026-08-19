package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.window.DialogProperties
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_back
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.disclaimer_accept
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.disclaimer_body
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.disclaimer_title

/**
 * The terms the app is used under.
 *
 * Not dismissible by tapping away or by the back button: the whole point is
 * that it was acknowledged, and a dialog that can be swiped off records an
 * acceptance nobody made. The body scrolls, because on a short screen an
 * unscrollable dialog hides the half of the text that matters most.
 *
 * @param onAccept null when the dialog is being read again from Settings
 *   rather than accepted for the first time; the button then just closes it.
 */
@Composable
fun DisclaimerDialog(
    onAccept: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = onDismiss != null,
        ),
        title = { Text(stringResource(Res.string.disclaimer_title)) },
        text = {
            Text(
                text = stringResource(Res.string.disclaimer_body),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = { onDismiss?.invoke() ?: onAccept() }) {
                Text(
                    stringResource(
                        if (onDismiss == null) Res.string.disclaimer_accept else Res.string.action_back,
                    ),
                )
            }
        },
    )
}
