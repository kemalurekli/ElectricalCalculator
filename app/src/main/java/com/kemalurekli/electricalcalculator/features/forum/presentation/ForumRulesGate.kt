package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The forum rules, shown once before anyone posts for the first time.
 *
 * Not folded into the app's existing disclaimer: that one is about the app's
 * own calculations, and this is about content other readers wrote and about
 * what the reader is about to write themselves. Someone who accepted the first
 * has not been told anything about the second.
 */
@HiltViewModel
class ForumRulesViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val accepted: StateFlow<Boolean?> = preferences.preferences
        .map { it.forumRulesAccepted }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    fun onAccept() {
        viewModelScope.launch { preferences.setForumRulesAccepted(true) }
    }
}

/**
 * Blocks [onProceed] until the rules have been read.
 *
 * A gate rather than a banner: acknowledging that forum answers are not
 * engineering advice has to happen before the first post, not alongside it.
 */
@Composable
fun ForumRulesDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forum_rules_title)) },
        text = { Text(stringResource(R.string.forum_rules_body)) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.forum_rules_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
