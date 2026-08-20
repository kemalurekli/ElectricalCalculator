package com.kemalurekli.electricalcalculator.features.forum.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTextField
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumReportReason
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_abuse
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_note
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_other
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_privacy
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_send
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_spam
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_report_unsafe

/**
 * Reporting a message.
 *
 * A fixed list of reasons rather than a text box. Free text produces a queue
 * nobody can triage, and it asks somebody to compose a sentence at the moment
 * they are annoyed. The note is optional and comes after the choice.
 */
@Composable
fun ForumReportDialog(
    onConfirm: (ForumReportReason, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf(ForumReportReason.SPAM) }
    var note by remember { mutableStateOf("") }
    val spacing = ElecTheme.spacing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.forum_report_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Column(Modifier.selectableGroup()) {
                    ForumReportReason.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = reason == option,
                                    onClick = { reason = option },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = reason == option, onClick = null)
                            Text(
                                text = stringResource(option.label()),
                                modifier = Modifier.padding(start = spacing.sm),
                            )
                        }
                    }
                }

                ElecTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(Res.string.forum_report_note),
                    maxLength = NOTE_MAX_LENGTH,
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason, note) }) {
                Text(stringResource(Res.string.forum_report_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

private fun ForumReportReason.label(): StringResource = when (this) {
    ForumReportReason.SPAM -> Res.string.forum_report_spam
    ForumReportReason.ABUSE -> Res.string.forum_report_abuse
    ForumReportReason.UNSAFE_ADVICE -> Res.string.forum_report_unsafe
    ForumReportReason.PRIVACY -> Res.string.forum_report_privacy
    ForumReportReason.OTHER -> Res.string.forum_report_other
}

/** The schema allows 500 for the whole reason string, key included. */
private const val NOTE_MAX_LENGTH = 400
