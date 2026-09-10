package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizQuestion
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizVerdict
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryExample
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryTopic
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.destination_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_not_found_message
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_not_found_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_answer
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_check
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_close
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_correct
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_next
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_note
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_prompt
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_unanswered
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_wrong
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_related_calculator
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_related_reference
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_section_assumptions
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_section_links
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_section_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_solve_for
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_steps
import org.jetbrains.compose.resources.StringResource

@Composable
fun TheoryTopicRoute(
    topicKey: String,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: TheoryTopicViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = uiState.topic?.let { stringResource(it.title) }
        ?: stringResource(Res.string.destination_theory)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val stepsHeading = stringResource(DesignSystemRes.string.calculator_steps)
    val summary = uiState.result?.let { rememberSolutionText(title, it, uiState.steps, stepsHeading) }

    // Keyed on the argument rather than run once, so arriving at a second topic
    // without leaving the screen loads it. The view model ignores a repeat of the
    // key it already holds, which is what keeps a rotation from clearing the form.
    LaunchedEffect(topicKey) { viewModel.onOpenTopic(topicKey) }

    TheoryTopicScreen(
        uiState = uiState,
        onToggleFavorite = viewModel::onToggleFavorite,
        snackbarHostState = snackbarHostState,
        onCopy = {
            summary?.let {
                if (sharing.copy(title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { sharing.share(shareSubject, it) } },
        onSolutionChange = viewModel::onSolutionChange,
        onFieldChange = viewModel::onFieldChange,
        onCalculate = viewModel::onCalculate,
        onApplyExample = viewModel::onApplyExample,
        onQuizAnswerChange = viewModel::onQuizAnswerChange,
        onCheckAnswer = viewModel::onCheckAnswer,
        onNextQuestion = viewModel::onNextQuestion,
        onReset = viewModel::onReset,
        onCalculatorClick = onCalculatorClick,
        onReferenceClick = onReferenceClick,
        onGlossaryClick = onGlossaryClick,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * One topic: the circuit, where the formula comes from, and a form to work it in.
 *
 * ### The order, and how it differs from a calculator
 *
 * A calculator screen opens on its inputs — the reader came to get a number. This
 * one opens on the circuit and the derivation, because the reader came to
 * understand something and the number is how they check that they have. So the
 * diagram sits at the top, the prose and the formula start expanded rather than
 * folded away, and the assumptions get a card of their own instead of being a
 * footnote.
 *
 * What is shared is everything below that: the same examples card, the same
 * numeric fields, the same steps card. A reader moving between the two shelves
 * should find the mechanics identical and only the emphasis different.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryTopicScreen(
    uiState: TheoryTopicUiState,
    onSolutionChange: (String) -> Unit,
    onToggleFavorite: () -> Unit = {},
    onCopy: () -> Unit = {},
    onShare: () -> Unit = {},
    onFieldChange: (String, String) -> Unit,
    onCalculate: () -> Unit,
    onApplyExample: (TheoryExample) -> Unit,
    onQuizAnswerChange: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onReset: () -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()
    val topic = uiState.topic
    val solution = uiState.solution

    // The answer is prepended to the list while the reader is looking at the
    // bottom of the form, so without this it arrives above the viewport and the
    // Calculate button appears to have done nothing. The calculators carry the
    // same effect for the same reason.
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) listState.animateScrollToItem(0)
    }

    ElecScreenScaffold(
        title = topic?.let { stringResource(it.title) }
                        ?: stringResource(Res.string.destination_theory),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            if (topic != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (uiState.isFavorite) ElecIcons.FavoriteOn
                        else ElecIcons.FavoriteOff,
                        contentDescription = stringResource(
                            if (uiState.isFavorite) DesignSystemRes.string.action_favorite_remove
                            else DesignSystemRes.string.action_favorite_add,
                        ),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        if (topic == null || solution == null) {
            ElecEmptyState(
                title = stringResource(Res.string.th_not_found_title),
                message = stringResource(Res.string.th_not_found_message),
                icon = ElecIcons.Theory,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(spacing.lg),
            )
            return@ElecScreenScaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(ElecTestTags.THEORY_FORM),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            uiState.result?.let { result ->
                item(key = "result") {
                    ElecResultCard(
                        label = result.primary.label,
                        value = result.primary.value,
                        unit = result.primary.unit,
                        tone = ResultTone.NEUTRAL,
                        secondaryRows = result.secondary
                            .map { ResultRow(it.label, it.value, it.unit) }
                            .toImmutableList(),
                        onCopy = onCopy,
                        onShare = onShare,
                    )
                }
            }

            topic.diagram?.let { diagram ->
                item(key = "diagram") {
                    ElecCard(modifier = Modifier.fillMaxWidth()) {
                        TheoryDiagramFigure(
                            diagram = diagram,
                            labels = uiState.diagramLabels,
                        )
                    }
                }
            }

            item(key = "theory") {
                TheoryProseCard(
                    title = stringResource(Res.string.th_section_theory),
                    text = stringResource(topic.theory),
                )
            }

            if (solution.examples.isNotEmpty()) {
                item(key = "examples") {
                    // ElecExamplesCard is generic over the state an example
                    // fills, because a calculator's example has to preserve
                    // the parts of that state it does not own. A theory form
                    // holds nothing worth preserving, so the transform is the
                    // identity and the values travel in the domain example.
                    ElecExamplesCard(
                        examples = solution.examples
                            .map { WorkedExample<Unit>(it.key, it.title) { unit -> unit } }
                            .toImmutableList(),
                        onSelect = { chosen ->
                            solution.examples
                                .firstOrNull { it.key == chosen.key }
                                ?.let(onApplyExample)
                        },
                    )
                }
            }

            uiState.question?.let { question ->
                item(key = "quiz") {
                    QuizCard(
                        question = question,
                        answer = uiState.quizAnswer,
                        verdict = uiState.quizVerdict,
                        expected = uiState.quizExpected,
                        fieldLabel = { key ->
                            solution.fields.firstOrNull { it.key == key }?.label
                        },
                        onAnswerChange = onQuizAnswerChange,
                        onCheck = onCheckAnswer,
                        onNext = onNextQuestion,
                    )
                }
            }

            item(key = "inputs-header") {
                ElecSectionHeader(title = stringResource(Res.string.calculator_inputs))
            }

            if (uiState.hasChoiceOfTarget) {
                item(key = "solve-for") {
                    ElecOptionSelector(
                        label = stringResource(topic.selectorLabel ?: Res.string.th_solve_for),
                        options = topic.solutions.map { it.key }.toImmutableList(),
                        selected = uiState.solutionKey,
                        onSelect = onSolutionChange,
                        optionLabel = { key ->
                            val target = topic.solutions.first { it.key == key }
                            stringResource(target.targetLabel)
                        },
                    )
                }
            }

            itemsIndexed(
                items = solution.fields,
                key = { _, field -> "field-${solution.key}-${field.key}" },
            ) { index, field ->
                ElecNumericField(
                    value = uiState.values[field.key].orEmpty(),
                    onValueChange = { onFieldChange(field.key, it) },
                    label = stringResource(field.label),
                    unit = field.unit,
                    error = uiState.errors[field.key],
                    supportingText = field.hint?.let { stringResource(it) },
                    allowNegative = field.allowNegative,
                    imeAction = if (index == solution.fields.lastIndex) ImeAction.Done
                    else ImeAction.Next,
                )
            }

            item(key = "actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    OutlinedButton(onClick = onReset) {
                        Text(text = stringResource(Res.string.action_reset))
                    }
                    Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(Res.string.action_calculate))
                    }
                }
            }

            if (uiState.steps.isNotEmpty()) {
                item(key = "steps") {
                    ElecStepsCard(steps = uiState.steps, initiallyExpanded = true)
                }
            }

            item(key = "formula") {
                ElecFormulaCard(
                    title = stringResource(Res.string.calculator_formula),
                    formula = solution.formula,
                    variables = solution.variables
                        .map { FormulaVariable(it.symbol, stringResource(it.meaning), it.unit) }
                        .toImmutableList(),
                    initiallyExpanded = true,
                )
            }

            if (topic.assumptions.isNotEmpty()) {
                item(key = "assumptions") {
                    ElecNotesCard(
                        title = stringResource(Res.string.th_section_assumptions),
                        notes = topic.assumptions
                            .map { stringResource(it) }
                            .toImmutableList(),
                    )
                }
            }

            if (topic.hasLinks) {
                item(key = "links") {
                    TheoryLinksCard(
                        topic = topic,
                        onCalculatorClick = onCalculatorClick,
                        onReferenceClick = onReferenceClick,
                        onGlossaryClick = onGlossaryClick,
                    )
                }
            }
        }
    }
}

/**
 * The whole solution as plain text, for the clipboard and the share sheet.
 *
 * Carries the derivation and not only the answer, which is the difference
 * between this shelf and a calculator: someone pasting a theory result into a
 * message is showing their working, and the working is the part worth reading.
 *
 * Composable so the step labels can be resolved, and remembered on the result so
 * a scroll does not rebuild the string.
 */
@Composable
private fun rememberSolutionText(
    title: String,
    result: TheoryResultView,
    steps: ImmutableList<CalculationStep>,
    stepsHeading: String,
): String {
    val stepLabels = steps.map { stringResource(it.label) }
    return remember(title, result, steps, stepsHeading) {
        buildString {
            appendLine(title)
            appendLine()
            appendLine("${result.primary.label}: ${result.primary.value} ${result.primary.unit}".trim())
            result.secondary.forEach {
                appendLine("${it.label}: ${it.value} ${it.unit}".trim())
            }
            if (steps.isNotEmpty()) {
                appendLine()
                appendLine(stepsHeading)
                steps.forEachIndexed { index, step ->
                    appendLine()
                    appendLine(stepLabels[index])
                    appendLine(step.formula)
                    appendLine("= ${step.substitution}")
                    appendLine("= ${step.result}")
                }
            }
        }.trimEnd()
    }
}

/**
 * The derivation, in prose.
 *
 * Not [ElecNotesCard], which bullets its contents: a bullet in front of a
 * three-paragraph argument reads as a list with one very long item. This splits
 * on blank lines and sets the paragraphs as body text, and it starts open — on
 * this shelf the derivation is the page's subject rather than a caveat attached
 * to a number, and a reader who came to read it should not have to open it first.
 */
@Composable
private fun TheoryProseCard(title: String, text: String) {
    val spacing = ElecTheme.spacing

    ElecCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            text.split(PARAGRAPH_BREAK).forEach { paragraph ->
                Text(
                    text = paragraph.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Where a reader goes next: the calculator that applies this, the table that
 * records it, the terms it assumed they knew.
 *
 * Its own card rather than [ElecNotesCard]'s links row, which only carries
 * reference topics and is shared by sixteen calculator screens — widening it to
 * take glossary and calculator jumps for one caller would push this shelf's
 * shape onto all of them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TheoryLinksCard(
    topic: TheoryTopic,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
) {
    val spacing = ElecTheme.spacing

    ElecCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.th_section_links),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                topic.calculator?.let { id ->
                    AssistChip(
                        onClick = { onCalculatorClick(id) },
                        label = { Text(stringResource(Res.string.th_related_calculator)) },
                    )
                }
                topic.referenceTopic?.let { key ->
                    AssistChip(
                        onClick = { onReferenceClick(key) },
                        label = { Text(stringResource(Res.string.th_related_reference)) },
                    )
                }
                topic.glossaryTerms.forEach { key ->
                    val term = GlossaryCatalog.termOrNull(key) ?: return@forEach
                    AssistChip(
                        onClick = { onGlossaryClick(key) },
                        // The glossary's text lives in :feature:glossary now, so this one
                        // reads through Compose Resources while the rest of the
                        // screen still reads R.string ids.
                        label = { Text(stringResource(term.term)) },
                    )
                }
            }
        }
    }
}

private val TheoryTopic.hasLinks: Boolean
    get() = calculator != null || referenceTopic != null || glossaryTerms.isNotEmpty()

private const val PARAGRAPH_BREAK = "\n\n"

/**
 * A question built from the topic's own worked example.
 *
 * Placed above the form rather than below the result, so that a reader who
 * wants to try first is not shown the answer on the way there. The expected
 * value appears only after an attempt — a card that prints the answer beside
 * the question is a worked example with extra steps.
 */
@Composable
private fun QuizCard(
    question: QuizQuestion,
    answer: String,
    verdict: QuizVerdict?,
    expected: String,
    fieldLabel: (String) -> StringResource?,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.th_quiz_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    Res.string.th_quiz_prompt,
                    stringResource(question.targetLabel),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            question.givens.forEach { (key, value) ->
                val label = fieldLabel(key)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = label?.let { stringResource(it) } ?: key,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = value, style = MaterialTheme.typography.bodySmall)
                }
            }

            ElecNumericField(
                value = answer,
                onValueChange = onAnswerChange,
                label = stringResource(Res.string.th_quiz_answer),
                unit = question.expected.unit.takeIf { it.isNotBlank() },
                allowNegative = true,
                imeAction = ImeAction.Done,
            )

            verdict?.let {
                Text(
                    text = when (it) {
                        QuizVerdict.CORRECT -> stringResource(Res.string.th_quiz_correct)
                        QuizVerdict.CLOSE -> stringResource(Res.string.th_quiz_close, expected)
                        QuizVerdict.WRONG -> stringResource(Res.string.th_quiz_wrong, expected)
                        QuizVerdict.UNANSWERED -> stringResource(Res.string.th_quiz_unanswered)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (it) {
                        QuizVerdict.CORRECT -> MaterialTheme.colorScheme.primary
                        QuizVerdict.WRONG -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Button(onClick = onCheck) {
                    Text(stringResource(Res.string.th_quiz_check))
                }
                OutlinedButton(onClick = onNext) {
                    Text(stringResource(Res.string.th_quiz_next))
                }
            }

            Text(
                text = stringResource(Res.string.th_quiz_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
