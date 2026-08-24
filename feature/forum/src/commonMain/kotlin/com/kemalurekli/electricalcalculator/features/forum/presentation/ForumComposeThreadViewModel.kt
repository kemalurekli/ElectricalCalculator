package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForumComposeThreadViewModel(
    private val repository: ForumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.ForumComposeThread>()

    /** The section this thread is being written into, for the screen to name. */
    val categoryTitle: String = route.categoryTitle
    val categoryKey: String = route.categoryKey

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed.asStateFlow()

    /** The new thread's id once it exists, which is the cue to navigate to it. */
    private val _created = MutableStateFlow<String?>(null)
    val created: StateFlow<String?> = _created.asStateFlow()

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onBodyChange(value: String) {
        _body.value = value
    }

    fun onSend() {
        val title = _title.value.trim()
        val body = _body.value.trim()
        if (title.isEmpty() || body.isEmpty() || _sending.value) return

        viewModelScope.launch {
            _sending.value = true
            // The language comes from the category being posted into, not from
            // the device: it is what keeps a Turkish thread out of the English
            // forum even if somebody switches language mid-compose.
            val language = ForumLanguage.entries.first { it.code == route.language }

            when (val result = repository.createThread(route.categoryId, language, title, body)) {
                is ForumResult.Success -> {
                    _failed.value = false
                    _created.value = result.value
                }

                is ForumResult.Failure -> _failed.value = true
            }
            _sending.value = false
        }
    }

    /** Clears the navigation cue so returning to this screen does not re-fire it. */
    fun onNavigated() {
        _created.value = null
    }
}
