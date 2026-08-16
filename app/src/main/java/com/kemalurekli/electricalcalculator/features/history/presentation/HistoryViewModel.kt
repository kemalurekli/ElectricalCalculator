package com.kemalurekli.electricalcalculator.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val query: String = "",
    val records: ImmutableList<CalculationRecord> = persistentListOf(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<HistoryUiState> = _query
        // Typing a term should not issue a database query per keystroke.
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .flatMapLatest { query ->
            historyRepository.observeSearch(query).map { records ->
                HistoryUiState(
                    query = query,
                    records = records.toImmutableList(),
                    isLoading = false,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState(),
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onRename(id: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { historyRepository.rename(id, title) }
    }

    fun onDuplicate(id: Long) {
        viewModelScope.launch { historyRepository.duplicate(id) }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { historyRepository.delete(id) }
    }

    /**
     * Puts a deleted record back, for the undo action on the delete snackbar.
     *
     * Saved as a new row rather than reinstating the old id: the id is an
     * autoincrement key with no meaning outside the table, and a repository that
     * could resurrect one would need a bin to hold it in. The record the user
     * cares about — its title, figures and timestamp — comes back unchanged.
     */
    fun onRestore(record: CalculationRecord) {
        viewModelScope.launch {
            historyRepository.save(record.copy(id = CalculationRecord.NO_ID))
        }
    }

    fun onClearAll() {
        viewModelScope.launch { historyRepository.clearAll() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SEARCH_DEBOUNCE_MILLIS = 250L
    }
}
