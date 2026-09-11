package com.kemalurekli.electricalcalculator.core.feedback.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackFailure
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackReport
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackRepository
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One dialog's worth of state.
 *
 * The text is held here rather than in the composable so that a rotation, a
 * language change or a trip to the background does not throw away three
 * sentences somebody has just typed — and so that a failed send can keep them,
 * which is the whole reason [ReportIssueState.message] survives a failure.
 */
class ReportIssueViewModel(
    private val repository: FeedbackRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportIssueState())
    val state: StateFlow<ReportIssueState> = _state.asStateFlow()

    fun onMessageChange(message: String) {
        _state.value = _state.value.copy(
            message = message.take(MAX_LENGTH),
            // Typing is the answer to an error message; it should not have to
            // be dismissed as well.
            failure = null,
        )
    }

    fun onSend(area: String, locale: String) {
        val message = _state.value.message.trim()
        if (message.length < MIN_LENGTH || _state.value.isSending) return

        _state.value = _state.value.copy(isSending = true, failure = null)
        viewModelScope.launch {
            val result = repository.send(FeedbackReport(area, message, locale))
            _state.value = when (result) {
                FeedbackResult.Sent ->
                    // Cleared, so that the next report from this screen does
                    // not open on the last one.
                    ReportIssueState(isSent = true)

                is FeedbackResult.Failed ->
                    _state.value.copy(isSending = false, failure = result.reason)
            }
        }
    }

    /** Called when the dialog closes, whichever way it closed. */
    fun onDismiss() {
        _state.value = if (_state.value.isSent) ReportIssueState() else _state.value.copy(failure = null)
    }

    companion object {
        /**
         * Matches the check constraint on the table.
         *
         * Enforced here as well so that a report too short to mean anything is
         * refused by a disabled button rather than by a round trip and a red
         * line — and so the two cannot drift apart silently.
         */
        const val MIN_LENGTH = 5
        const val MAX_LENGTH = 2000
    }
}

data class ReportIssueState(
    val message: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false,
    val failure: FeedbackFailure? = null,
) {
    val canSend: Boolean
        get() = !isSending && message.trim().length >= ReportIssueViewModel.MIN_LENGTH
}
