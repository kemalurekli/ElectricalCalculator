package com.kemalurekli.electricalcalculator.features.theory.domain

import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import kotlin.math.pow

/**
 * A question, and the answer the topic's own solver gives.
 *
 * ### Why questions are generated rather than written
 *
 * A quiz written by hand is a second copy of the catalog: twenty topics would
 * need a hundred questions, each with its own answer typed in beside it, and
 * every one of those answers would be a transcribed figure no test could prove.
 * The first time a solver was corrected, the quiz would quietly start marking
 * the right answer wrong.
 *
 * Generating from an existing worked example avoids all of it. The question is
 * the scenario the topic already ships, the answer is whatever the solver
 * returns today, and a topic added next year gets a quiz the same afternoon —
 * which is the same property that makes every other test in this shelf iterate
 * the catalog rather than list it.
 *
 * @param givens the values the reader is handed, keyed by field.
 * @param expected the solver's own answer, which is the mark scheme.
 */
data class QuizQuestion(
    val topicKey: String,
    val solutionKey: String,
    val exampleKey: String,
    val givens: Map<String, String>,
    val expected: TheoryNumber,
    val targetLabel: StringResource,
)

/** How close a reader got. */
enum class QuizVerdict {
    /** Within tolerance — right, allowing for rounding along the way. */
    CORRECT,

    /**
     * The right size but not the right number.
     *
     * Worth separating from a plain miss: an answer within a factor of two is
     * usually one rounding or one dropped √3, and telling a reader that is more
     * use than telling them they were wrong.
     */
    CLOSE,

    WRONG,

    /** Nothing readable was entered. */
    UNANSWERED,
}

/**
 * Marks an answer against the solver.
 *
 * The tolerance is generous on purpose. A reader working this on paper rounds
 * at every step, and a quiz that fails them for the third decimal is testing
 * their arithmetic patience rather than whether they understood the topic.
 */
object TheoryQuiz {

    /** Within this fraction of the true answer counts as correct. */
    const val TOLERANCE = 0.02

    /** Within this factor either way counts as close rather than wrong. */
    const val CLOSE_FACTOR = 2.0

    /**
     * Builds a question from a topic's own worked example.
     *
     * Null when the example does not carry every field the solution declares —
     * a mismatch the catalog tests already fail on, and not something to guess
     * around here.
     */
    fun questionFor(topic: TheoryTopic, solution: TheorySolution, example: TheoryExample): QuizQuestion? =
        ask(topic, solution, example, example.key, overrides = emptyMap())

    /**
     * Every question a topic can ask.
     *
     * One per worked example, and then several more built by moving a single
     * given and asking the solver again.
     *
     * ### Why the extras are generated and not written
     *
     * The same reason the first one is. More questions could have been had by
     * writing more worked examples, but an example carries a translated title,
     * so five more on each of twenty-four topics is over a thousand new strings
     * in twelve languages — a week of translation to ask "what if the cable
     * were longer".
     *
     * Moving one number costs nothing and teaches more. A variant is the
     * reader's own scenario with exactly one thing changed, which is how
     * anybody actually builds intuition about a formula: not by meeting five
     * unrelated circuits, but by watching one circuit answer differently. The
     * answer is still whatever the solver returns, so it cannot go stale, and
     * a topic added next year gets the variants too.
     */
    fun questionsFor(topic: TheoryTopic): List<QuizQuestion> = topic.solutions
        .flatMap { solution ->
            solution.examples.flatMap { example ->
                val base = questionFor(topic, solution, example) ?: return@flatMap emptyList()
                listOf(base) + variantsOf(topic, solution, example)
            }
        }
        // Across the whole topic, not within one example. Clamping and rounding
        // can land a variant on the example it came from, and two examples that
        // differ in one field can be moved onto each other. Two identical
        // questions in a row read as the quiz having run out.
        .distinctBy { it.solutionKey to it.givens }

    /**
     * One given moved, the rest left alone, once per field and factor.
     *
     * Rotating through the fields rather than disturbing them all at once is
     * the point: a reader learns what a formula does by seeing one input change
     * and the answer follow it. Five factors either side of the example give a
     * topic with a single worked scenario six questions and one with two
     * scenarios twelve.
     */
    private fun variantsOf(
        topic: TheoryTopic,
        solution: TheorySolution,
        example: TheoryExample,
    ): List<QuizQuestion> {
        val varied = solution.fields.filter { field ->
            val raw = example.values[field.key] ?: field.default
            raw.isNotBlank() && NumberFormatter.parseOrNull(raw) != null
        }
        if (varied.isEmpty()) return emptyList()

        return FACTORS.mapIndexedNotNull { index, factor ->
            val field = varied[index % varied.size]
            val raw = example.values[field.key] ?: field.default
            val moved = move(NumberFormatter.parseOrNull(raw) ?: return@mapIndexedNotNull null, factor, field)
                ?: return@mapIndexedNotNull null
            ask(
                topic = topic,
                solution = solution,
                example = example,
                exampleKey = "${example.key}$VARIANT_MARK${index + 1}",
                overrides = mapOf(field.key to moved),
            )
        }
    }

    /**
     * Scales a given, keeps it inside what its field will accept, and rounds it
     * to something a person would write down.
     *
     * Two significant figures: 230 stays 230 and 264.5 becomes 260, which reads
     * as a scenario rather than as a number that fell out of a multiplication.
     * Null when the field's own limits leave nowhere to move to.
     */
    private fun move(value: Double, factor: Double, field: TheoryField): String? {
        val scaled = round(value * factor)
        val bounded = scaled
            .coerceAtLeast(field.min ?: Double.NEGATIVE_INFINITY)
            .coerceAtMost(field.max ?: Double.POSITIVE_INFINITY)
        if (!bounded.isFinite()) return null
        if (bounded == 0.0 && !field.allowZero) return null
        if (bounded < 0.0 && !field.allowNegative) return null
        if (bounded == value) return null
        return NumberFormatter.format(bounded, DECIMALS).replace(',', '.')
    }

    /** Two significant figures, keeping small values from collapsing to zero. */
    private fun round(value: Double): Double {
        if (value == 0.0) return 0.0
        val magnitude = kotlin.math.floor(kotlin.math.log10(kotlin.math.abs(value)))
        val step = (10.0).pow(magnitude - 1)
        return kotlin.math.round(value / step) * step
    }

    private fun ask(
        topic: TheoryTopic,
        solution: TheorySolution,
        example: TheoryExample,
        exampleKey: String,
        overrides: Map<String, String>,
    ): QuizQuestion? {
        val raws = mutableMapOf<String, String>()
        val values = mutableMapOf<String, Double>()
        solution.fields.forEach { field ->
            val raw = overrides[field.key] ?: example.values[field.key] ?: field.default
            if (raw.isBlank()) {
                // An optional field left out is a component that is not in the
                // circuit; a required one missing means the example is broken.
                if (field.optional) return@forEach
                return null
            }
            values[field.key] = NumberFormatter.parseOrNull(raw) ?: return null
            raws[field.key] = raw
        }

        // A moved given can put a solver somewhere it cannot go — a square root
        // of a negative, a division by a difference that is now zero. The
        // catalog's own examples never do; a variant is allowed to, and is
        // dropped rather than shown.
        val solved = runCatching { solution.solve(TheoryInputs(values)) }.getOrNull() ?: return null
        if (!solved.primary.number.value.isFinite()) return null

        return QuizQuestion(
            topicKey = topic.key,
            solutionKey = solution.key,
            exampleKey = exampleKey,
            givens = raws,
            expected = solved.primary.number,
            targetLabel = solution.targetLabel,
        )
    }

    /** Either side of the worked example, and never so far as to be silly. */
    private val FACTORS = listOf(0.5, 1.5, 0.75, 2.0, 1.25, 0.4)

    private const val VARIANT_MARK = "#"
    private const val DECIMALS = 4

    /**
     * Marks [answer], which is whatever the reader typed.
     *
     * Accepts either decimal separator, as every numeric field in this app does.
     */
    fun mark(question: QuizQuestion, answer: String): QuizVerdict {
        val given = NumberFormatter.parseOrNull(answer.trim()) ?: return QuizVerdict.UNANSWERED
        val expected = question.expected.value

        if (expected == 0.0) {
            return if (given == 0.0) QuizVerdict.CORRECT else QuizVerdict.WRONG
        }

        val ratio = given / expected
        return when {
            kotlin.math.abs(ratio - 1.0) <= TOLERANCE -> QuizVerdict.CORRECT
            // A negative answer to a positive quantity is a sign error, not a
            // near miss, so the ratio has to be positive to count as close.
            ratio > 0.0 && ratio <= CLOSE_FACTOR && ratio >= 1.0 / CLOSE_FACTOR -> QuizVerdict.CLOSE
            else -> QuizVerdict.WRONG
        }
    }
}
