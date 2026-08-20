package com.kemalurekli.electricalcalculator.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_clear_all
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_delete
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_duplicate
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_rename
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_save
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.action_undo
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.destination_history
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_actions
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_clear_message
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_clear_title
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_deleted
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_rename_label
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.history_rename_title
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.search_history_hint
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.state_empty_history_message
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.state_empty_history_title

@Composable
fun HistoryRoute(
    onOpenRecord: (CalculationRecord) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(Res.string.history_deleted)
    val undoLabel = stringResource(Res.string.action_undo)

    HistoryScreen(
        uiState = uiState,
        // The field renders the raw query so typing stays responsive; the
        // debounced value in `uiState` only drives the results.
        query = query,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onOpenRecord = onOpenRecord,
        onRename = viewModel::onRename,
        onDuplicate = viewModel::onDuplicate,
        onDelete = { record ->
            viewModel.onDelete(record.id)
            // Deleting from a menu has no confirmation step, so the way back is
            // the snackbar. The record is still in hand, which is what makes an
            // undo possible without the repository needing a bin.
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = deletedMessage,
                    actionLabel = undoLabel,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onRestore(record)
            }
        },
        onClearAll = viewModel::onClearAll,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * The saved calculations.
 *
 * Every card carries an overflow menu rather than a swipe: a swipe is invisible
 * until it is discovered, and this list is the only place the three management
 * actions exist. Deleting is immediate with an undo, because a confirmation
 * dialog on every row would make clearing a few stale entries tedious — the
 * dialog is reserved for "clear all", which genuinely cannot be undone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenRecord: (CalculationRecord) -> Unit,
    onRename: (Long, String) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (CalculationRecord) -> Unit,
    onClearAll: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    var renaming by remember { mutableStateOf<CalculationRecord?>(null) }
    var confirmingClear by rememberSaveable { mutableStateOf(false) }

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_history),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            if (uiState.records.isNotEmpty()) {
                IconButton(onClick = { confirmingClear = true }) {
                    Icon(
                        imageVector = ElecIcons.Delete,
                        contentDescription = stringResource(Res.string.action_clear_all),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ElecSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                // This field searches saved calculations, not the catalog, so
                // it must not borrow the home screen's placeholder.
                placeholder = stringResource(Res.string.search_history_hint),
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.sm,
                ),
            )

            when {
                uiState.isLoading -> ElecLoadingState()

                uiState.records.isEmpty() -> ElecEmptyState(
                    title = stringResource(Res.string.state_empty_history_title),
                    message = stringResource(Res.string.state_empty_history_message),
                    icon = ElecIcons.History,
                    modifier = Modifier.fillMaxWidth(),
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = spacing.screenHorizontal,
                        end = spacing.screenHorizontal,
                        bottom = spacing.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        HistoryCard(
                            record = record,
                            onOpen = { onOpenRecord(record) },
                            onRename = { renaming = record },
                            onDuplicate = { onDuplicate(record.id) },
                            onDelete = { onDelete(record) },
                        )
                    }
                }
            }
        }
    }

    renaming?.let { record ->
        RenameDialog(
            initialTitle = record.title,
            onDismiss = { renaming = null },
            onConfirm = { title ->
                onRename(record.id, title)
                renaming = null
            },
        )
    }

    if (confirmingClear) {
        AlertDialog(
            onDismissRequest = { confirmingClear = false },
            title = { Text(stringResource(Res.string.history_clear_title)) },
            text = { Text(stringResource(Res.string.history_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        confirmingClear = false
                    },
                ) { Text(stringResource(Res.string.action_clear_all)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingClear = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun HistoryCard(
    record: CalculationRecord,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    var menuOpen by remember { mutableStateOf(false) }

    // The card opens the calculation; the menu manages it. Tapping the body is
    // the affordance a list of saved work is expected to have, and the record
    // already stores the inputs it needs to reopen with.
    ElecCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.lg, top = spacing.lg, bottom = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = record.summary,
                    style = NumericCompactTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = record.createdAt.formatAsDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = ElecIcons.More,
                        contentDescription = stringResource(Res.string.history_actions),
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_rename)) },
                        leadingIcon = { Icon(ElecIcons.Rename, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_duplicate)) },
                        leadingIcon = { Icon(ElecIcons.Copy, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDuplicate()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_delete)) },
                        leadingIcon = { Icon(ElecIcons.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.history_rename_title)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(Res.string.history_rename_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                // A blank title would leave the row with nothing to identify it
                // by; the repository ignores it, so the button says so first.
                enabled = title.isNotBlank(),
            ) { Text(stringResource(Res.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}
