package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumRepository
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The people this reader has blocked, and the way back.
 *
 * Blocking without unblocking is a one-way door, and the confirmation dialog
 * tells people they can undo it here — so this is what makes that sentence
 * true rather than a promise the app does not keep.
 */
@HiltViewModel
class ForumBlockedViewModel @Inject constructor(
    private val repository: ForumRepository,
    private val authRepository: ForumAuthRepository,
) : ViewModel() {

    private val _blocked = MutableStateFlow<ImmutableList<ForumProfile>>(persistentListOf())
    val blocked: StateFlow<ImmutableList<ForumProfile>> = _blocked.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val ids = (repository.blockedUserIds() as? ForumResult.Success)?.value.orEmpty()
            // Names rather than ids. "You blocked 6cf84dee-2199…" is not a list
            // anybody can act on.
            _blocked.value = ids
                .mapNotNull { (authRepository.profile(it) as? ForumResult.Success)?.value }
                .toImmutableList()
        }
    }

    fun onUnblock(userId: String) {
        viewModelScope.launch {
            if (repository.unblock(userId) is ForumResult.Success) load()
        }
    }
}

@Composable
fun ForumBlockedSection(
    modifier: Modifier = Modifier,
    viewModel: ForumBlockedViewModel = hiltViewModel(),
) {
    val blocked by viewModel.blocked.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        if (blocked.isEmpty()) {
            Text(
                text = stringResource(R.string.forum_blocked_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            blocked.forEach { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = { viewModel.onUnblock(profile.id) }) {
                        Text(stringResource(R.string.forum_unblock))
                    }
                }
            }
        }
    }
}
