package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizQuestion
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizVerdict
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryQuiz
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryExample
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryInputs
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryQuantity
import com.kemalurekli.electricalcalculator.features.theory.domain.TheorySolution
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/** A reported quantity, resolved and formatted for display. */
@Immutable
data class TheoryQuantityView(
    val label: String,
    val value: String,
    val unit: String,
)

/** A finished solution, ready to render. */
@Immutable
data class TheoryResultView(
    val primary: TheoryQuantityView,
    val secondary: ImmutableList<TheoryQuantityView>,
)

@Immutable
data class TheoryTopicUiState(
    val topic: TheoryTopic? = null,
    val isFavorite: Boolean = false,
    val solutionKey: String = "",
    val values: ImmutableMap<String, String> = persistentMapOf(),
    val errors: ImmutableMap<String, ValidationError> = persistentMapOf(),
    val result: TheoryResultView? = null,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
    val diagramLabels: ImmutableMap<String, String> = persistentMapOf(),
    /** The question on offer, or null for a topic with no worked example. */
    val question: QuizQuestion? = null,
    val quizAnswer: String = "",
    val quizVerdict: QuizVerdict? = null,
    /** Revealed only after an attempt, so the card is not a lookup table. */
    val quizExpected: String = "",
) {
    /** Null before a topic is loaded, and when a deep link named one that is gone. */
    val solution: TheorySolution?
        get() = topic?.solutions?.firstOrNull { it.key == solutionKey }

    /** Whether the "solve for" selector is worth showing at all. */
    val hasChoiceOfTarget: Boolean get() = (topic?.solutions?.size ?: 0) > 1
}

/**
 * One view model for every theory topic.
 *
 * ### Why one, rather than twenty
 *
 * See [TheoryTopic] for the full argument. The short of it is that a topic's
 * *structure* is identical across the shelf — declared fields, a chosen target,
 * validation, a solver, a derivation — and only its data differs. Everything this
 * class does is driven off [TheorySolution.fields] and [TheorySolution.solve],
 * so it has no knowledge of any particular topic and gains none when one is added.
 *
 * ### Where formatting happens
 *
 * Here, and only here. The solvers hand back numbers; this class turns them into
 * text with the reader's locale and the reader's language, exactly as each
 * calculator's explainer does. `Locale.getDefault()` is correct rather than
 * lazy: the language picker recreates the Activity, which drops this along with
 * it, so the default is always current by the time anything is formatted.
 */
@HiltViewModel
class TheoryTopicViewModel @Inject constructor(
    private val stringResolver: ResourceIdResolver,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TheoryTopicUiState())
    val uiState: StateFlow<TheoryTopicUiState> = _uiState.asStateFlow()

    /** Cancelled and restarted when a different topic is opened. */
    private var favoriteWatch: Job? = null

    /**
     * Loads a topic.
     *
     * Does nothing when the key is already loaded, so the screen's `LaunchedEffect`
     * can be keyed on the argument without discarding work the reader has done.
     * An unknown key leaves [TheoryTopicUiState.topic] null and the screen shows
     * an empty state — a route that outlived the topic it named.
     */
    fun onOpenTopic(key: String) {
        if (_uiState.value.topic?.key == key) return
        val topic = TheoryCatalog.topicOrNull(key)
        if (topic == null) {
            _uiState.value = TheoryTopicUiState()
            return
        }
        val first = topic.solutions.first()
        _uiState.value = TheoryTopicUiState(
            topic = topic,
            solutionKey = first.key,
            values = defaultsFor(first).toImmutableMap(),
            question = TheoryQuiz.questionsFor(topic).randomOrNull(),
        )
        watchFavorite(topic.key)
    }

    // -- Test yourself -------------------------------------------------------

    fun onQuizAnswerChange(value: String) {
        _uiState.update { it.copy(quizAnswer = value, quizVerdict = null, quizExpected = "") }
    }

    /**
     * Marks the attempt and reveals the answer.
     *
     * The expected value is formatted here, where the locale already lives, and
     * only once an attempt has been made — a card that shows the answer beside
     * the question is a worked example with extra steps.
     */
    fun onCheckAnswer() {
        val state = _uiState.value
        val question = state.question ?: return
        val verdict = TheoryQuiz.mark(question, state.quizAnswer)
        _uiState.update {
            it.copy(
                quizVerdict = verdict,
                quizExpected = if (verdict == QuizVerdict.UNANSWERED) {
                    ""
                } else {
                    question.expected.format(Locale.getDefault())
                },
            )
        }
    }

    /** Another question from the same topic, and a cleared field. */
    fun onNextQuestion() {
        val topic = _uiState.value.topic ?: return
        val questions = TheoryQuiz.questionsFor(topic)
        if (questions.isEmpty()) return
        // Avoids handing back the question just answered when there is a
        // choice; with one question there is nothing to rotate to.
        val current = _uiState.value.question
        val next = questions.filter { it != current }.randomOrNull() ?: questions.first()
        _uiState.update {
            it.copy(question = next, quizAnswer = "", quizVerdict = null, quizExpected = "")
        }
    }

    fun onToggleFavorite() {
        val key = _uiState.value.topic?.key ?: return
        viewModelScope.launch { favoritesRepository.toggle(FavoriteItem(FavoriteKind.THEORY, key)) }
    }

    private fun watchFavorite(key: String) {
        favoriteWatch?.cancel()
        favoriteWatch = viewModelScope.launch {
            favoritesRepository.observeIsFavorite(FavoriteItem(FavoriteKind.THEORY, key))
                .collect { pinned -> _uiState.update { it.copy(isFavorite = pinned) } }
        }
    }

    /**
     * Switches which quantity is being solved for.
     *
     * Values the reader has already typed are kept where the new target declares
     * the same field. Rearranging Ohm's law from current to resistance keeps the
     * voltage, because it is the same voltage — clearing it would make the
     * selector feel like it had thrown the form away.
     */
    fun onSolutionChange(key: String) {
        val state = _uiState.value
        val next = state.topic?.solutions?.firstOrNull { it.key == key } ?: return
        if (key == state.solutionKey) return

        val carried = defaultsFor(next) + state.values.filterKeys { fieldKey ->
            next.fields.any { it.key == fieldKey } && state.values[fieldKey]?.isNotBlank() == true
        }
        _uiState.value = state.copy(
            solutionKey = key,
            values = carried.toImmutableMap(),
            errors = persistentMapOf(),
            result = null,
            steps = persistentListOf(),
            diagramLabels = persistentMapOf(),
        )
    }

    /**
     * Records a keystroke and drops the stale answer.
     *
     * A result left on screen while its inputs change is the classic calculator
     * mistake: it reads as the answer to what is on screen now.
     */
    fun onFieldChange(key: String, value: String) {
        _uiState.update { state ->
            state.copy(
                values = (state.values + (key to value)).toImmutableMap(),
                errors = (state.errors - key).toImmutableMap(),
                result = null,
                steps = persistentListOf(),
                diagramLabels = persistentMapOf(),
            )
        }
    }

    fun onCalculate() {
        val state = _uiState.value
        val solution = state.solution ?: return

        val errors = mutableMapOf<String, ValidationError>()
        val parsed = mutableMapOf<String, Double>()

        solution.fields.forEach { field ->
            val raw = state.values[field.key].orEmpty()
            // A blank optional field is a component that is not in the circuit,
            // not a value of zero. The solver reads it back as absent.
            if (field.optional && raw.isBlank()) return@forEach

            when (
                val outcome = NumericInput.validate(
                    raw = raw,
                    min = field.min,
                    max = field.max,
                    allowZero = field.allowZero,
                    allowNegative = field.allowNegative,
                )
            ) {
                is Outcome.Success -> parsed[field.key] = outcome.value
                is Outcome.Failure -> errors[field.key] = outcome.error
            }
        }

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    errors = errors.toImmutableMap(),
                    result = null,
                    steps = persistentListOf(),
                    diagramLabels = persistentMapOf(),
                )
            }
            return
        }

        val locale = Locale.getDefault()
        val solved = solution.solve(TheoryInputs(parsed))

        _uiState.update {
            it.copy(
                errors = persistentMapOf(),
                result = TheoryResultView(
                    primary = solved.primary.toView(locale),
                    secondary = solved.secondary.map { quantity -> quantity.toView(locale) }
                        .toImmutableList(),
                ),
                steps = solved.steps.map { step -> step.toCalculationStep(locale) }.toImmutableList(),
                diagramLabels = solved.diagramLabels
                    .mapValues { (_, number) -> number.format(locale) }
                    .toImmutableMap(),
            )
        }
    }

    /** Fills the form from a ready-made scenario and works it straight through. */
    fun onApplyExample(example: TheoryExample) {
        val solution = _uiState.value.solution ?: return
        _uiState.update {
            // Starts from the defaults rather than from what is on screen, so an
            // example is the same circuit every time it is tapped — including
            // when the previous one filled an optional field this one leaves out.
            it.copy(values = (defaultsFor(solution) + example.values).toImmutableMap())
        }
        onCalculate()
    }

    /** Clears the form back to the topic's defaults, keeping the chosen target. */
    fun onReset() {
        val state = _uiState.value
        val solution = state.solution ?: return
        _uiState.value = state.copy(
            values = defaultsFor(solution).toImmutableMap(),
            errors = persistentMapOf(),
            result = null,
            steps = persistentListOf(),
            diagramLabels = persistentMapOf(),
        )
    }

    private fun defaultsFor(solution: TheorySolution): Map<String, String> =
        solution.fields.associate { it.key to it.default }

    private fun TheoryQuantity.toView(locale: Locale) = TheoryQuantityView(
        label = stringResolver.get(labelRes),
        value = number.formatValue(locale),
        unit = number.unitLabel,
    )
}
