package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_change
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_code_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_code_sent
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_resend
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_send_code
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_verify
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_email_resend_in
import androidx.compose.foundation.text.KeyboardActions

/**
 * Signing in with an address and a six-digit code.
 *
 * The route that needs no account anywhere else. Google is unavailable on a
 * device without Play Services — which in this app's home market means every
 * Huawei — and Apple's is unavailable to anyone not on an iPhone. This one
 * asks for nothing the reader does not already have, and it is the only one
 * that gives the same person the same account on both platforms.
 *
 * Takes the ViewModel rather than injecting it, because both hosts — the
 * settings section and the prompt over the forum — already hold one. Injecting
 * here would give this its own, and the code would arrive in a state machine
 * nobody was watching.
 */
@Composable
fun ForumEmailSignIn(
    viewModel: ForumAccountViewModel,
    modifier: Modifier = Modifier,
) {
    val step by viewModel.emailStep.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val resendIn by viewModel.resendIn.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        when (val current = step) {
            ForumAccountViewModel.EmailStep.Address -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.forum_email_hint)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send,
                    ),
                    // The key has to do what it says. Asking for `Send` and
                    // then only closing the keyboard leaves the reader hunting
                    // for a button that the keyboard is standing on.
                    keyboardActions = KeyboardActions(onSend = { viewModel.onSendCode() }),
                )
                Button(
                    onClick = viewModel::onSendCode,
                    enabled = !busy && email.isNotBlank() && resendIn == 0,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (resendIn > 0) {
                            stringResource(Res.string.forum_email_resend_in, resendIn)
                        } else {
                            stringResource(Res.string.forum_email_send_code)
                        },
                    )
                }
            }

            is ForumAccountViewModel.EmailStep.Code -> {
                Text(
                    text = stringResource(Res.string.forum_email_code_sent, current.email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = viewModel::onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.forum_email_code_hint)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(
                        // NumberPassword rather than Number: it is the one
                        // numeric keyboard with no suggestion strip, which is
                        // what stops a code being autofilled into the wrong
                        // field or offered back later.
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onVerifyCode() }),
                )
                Button(
                    onClick = viewModel::onVerifyCode,
                    enabled = !busy && code.length == CODE_LENGTH,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.forum_email_verify))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    TextButton(onClick = viewModel::onSendCode, enabled = !busy && resendIn == 0) {
                        Text(
                            if (resendIn > 0) {
                                stringResource(Res.string.forum_email_resend_in, resendIn)
                            } else {
                                stringResource(Res.string.forum_email_resend)
                            },
                        )
                    }
                    TextButton(onClick = viewModel::onUseAnotherAddress, enabled = !busy) {
                        Text(stringResource(Res.string.forum_email_change))
                    }
                }
            }
        }
    }
}

/** Six, because that is what Supabase mints and what the email says. */
private const val CODE_LENGTH = 6
