package com.kemalurekli.electricalcalculator.core.feedback.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackFailure
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_cancel
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_failed_network
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_failed_not_configured
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_failed_too_many
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_hint
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_label
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_note
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_send
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_sent
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_sent_close
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_sent_title
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The small window the report is written in.
 *
 * ### What it says it sends
 *
 * Plainly, above the buttons: the screen, the app version, the platform and the
 * language. Nothing typed into a calculator goes with it, which is why the
 * note can be short — there is nothing to disclose that a reader would be
 * surprised by. An app that promises to work offline should be able to say in
 * one sentence what the one online button does.
 *
 * ### Why it becomes a receipt rather than closing
 *
 * A dialog that vanishes on send leaves the reader guessing, and a snackbar
 * behind a dialog that is already gone is easy to miss. The same window says
 * "sent"; closing it is then their decision, not a side effect.
 */
@Composable
internal fun ReportIssueDialog(
    area: String,
    onDismiss: () -> Unit,
    viewModel: ReportIssueViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing
    // The translation that was actually on screen, which is the thing a report
    // is about. The language *setting* is usually "follow the device".
    val locale = Locale.current.language

    if (state.isSent) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onDismiss()
                onDismiss()
            },
            title = { Text(stringResource(Res.string.feedback_sent_title)) },
            text = { Text(stringResource(Res.string.feedback_sent)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDismiss()
                        onDismiss()
                    },
                ) {
                    Text(stringResource(Res.string.feedback_sent_close))
                }
            },
        )
        return
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.onDismiss()
            onDismiss()
        },
        title = { Text(stringResource(Res.string.feedback_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = FIELD_MIN_HEIGHT),
                    label = { Text(stringResource(Res.string.feedback_label)) },
                    placeholder = { Text(stringResource(Res.string.feedback_hint)) },
                    isError = state.failure != null,
                    enabled = !state.isSending,
                    supportingText = {
                        val failure = state.failure
                        if (failure == null) {
                            Text(stringResource(Res.string.feedback_note))
                        } else {
                            Text(
                                text = stringResource(
                                    when (failure) {
                                        FeedbackFailure.NOT_CONFIGURED ->
                                            Res.string.feedback_failed_not_configured
                                        FeedbackFailure.TOO_MANY ->
                                            Res.string.feedback_failed_too_many
                                        FeedbackFailure.NETWORK ->
                                            Res.string.feedback_failed_network
                                    },
                                ),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.onSend(area, locale) },
                enabled = state.canSend,
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.heightIn(max = spacing.md),
                        strokeWidth = STROKE,
                    )
                } else {
                    Text(stringResource(Res.string.feedback_send))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.onDismiss()
                    onDismiss()
                },
                enabled = !state.isSending,
            ) {
                Text(stringResource(Res.string.feedback_cancel))
            }
        },
    )
}

private val FIELD_MIN_HEIGHT = 120.dp
private val STROKE = 2.dp
