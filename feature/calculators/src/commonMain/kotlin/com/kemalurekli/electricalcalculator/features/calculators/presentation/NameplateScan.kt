package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.vision.NameplateField
import com.kemalurekli.electricalcalculator.core.vision.NameplateFieldKind
import com.kemalurekli.electricalcalculator.core.vision.NameplateReading
import com.kemalurekli.electricalcalculator.core.vision.fields
import com.kemalurekli.electricalcalculator.core.vision.rememberNameplateScanner
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nameplate_apply
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nameplate_found_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nameplate_nothing
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nameplate_scan
import org.jetbrains.compose.resources.stringResource

/**
 * Photograph a nameplate, then decide what to do with what it said.
 *
 * ### Why it asks first
 *
 * Recognition is a guess, and this one is made in a plant room on a plate that
 * may be painted over. A field quietly filled with a misread figure is worse
 * than an empty one, because the reader has no reason to look at it again — so
 * the sheet says what was found and applying it is a decision.
 *
 * ### Why the figures are shown as the plate writes them
 *
 * "7,5 kW" and "cos φ 0,86" need no translating and are what an electrician
 * has just been looking at. Naming them in words would put a label between the
 * reader and the thing they are checking, in twelve languages, for no gain.
 */
@Composable
fun NameplateScanAction(
    onApply: (NameplateReading) -> Unit,
    modifier: Modifier = Modifier,
) {
    var reading by remember { mutableStateOf<NameplateReading?>(null) }
    val scanner = rememberNameplateScanner { reading = it }

    // Absent rather than disabled on a device with no camera. Nothing unlocks
    // it, so an explanation would be an apology.
    if (!scanner.isSupported) return

    OutlinedButton(onClick = scanner::scan, modifier = modifier) {
        Icon(
            imageVector = ElecIcons.Nameplate,
            contentDescription = null,
            modifier = Modifier.size(ICON),
        )
        Text(
            text = stringResource(Res.string.nameplate_scan),
            modifier = Modifier.padding(start = ElecTheme.spacing.sm),
        )
    }

    reading?.let { found ->
        NameplateDialog(
            reading = found,
            onApply = {
                onApply(found)
                reading = null
            },
            onDismiss = { reading = null },
        )
    }
}

@Composable
private fun NameplateDialog(
    reading: NameplateReading,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val found = reading.fields()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.nameplate_found_title)) },
        text = {
            if (found.isEmpty()) {
                Text(stringResource(Res.string.nameplate_nothing))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ElecTheme.spacing.xs)) {
                    found.forEach { field ->
                        Text(text = field.written(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            // Nothing to apply is not an action. The sheet then has one way
            // out, which is the way the reader was already reaching for.
            if (found.isNotEmpty()) {
                TextButton(onClick = onApply) { Text(stringResource(Res.string.nameplate_apply)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(DesignRes.string.action_cancel)) }
        },
    )
}

/** The figure as the plate prints it, in the reader's own decimal separator. */
private fun NameplateField.written(): String {
    val number = NumberFormatter.formatSignificant(value)
    return when (kind) {
        NameplateFieldKind.POWER_KILOWATTS -> "$number kW"
        NameplateFieldKind.POWER_HORSEPOWER -> "$number HP"
        NameplateFieldKind.VOLTAGE -> "$number V"
        NameplateFieldKind.CURRENT -> "$number A"
        NameplateFieldKind.POWER_FACTOR -> "cos φ $number"
        NameplateFieldKind.EFFICIENCY -> "η $number %"
        NameplateFieldKind.FREQUENCY -> "$number Hz"
        NameplateFieldKind.PHASES -> "${value.toInt()}~"
    }
}

private val ICON = 18.dp
