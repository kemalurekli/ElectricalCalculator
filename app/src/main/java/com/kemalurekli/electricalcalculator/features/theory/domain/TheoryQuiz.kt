package com.kemalurekli.electricalcalculator.features.theory.domain

import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter

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
    val targetLabelRes: Int,
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
    fun questionFor(topic: TheoryTopic, solution: TheorySolution, example: TheoryExample): QuizQuestion? {
        val values = mutableMapOf<String, Double>()
        solution.fields.forEach { field ->
            val raw = example.values[field.key] ?: field.default
            if (raw.isBlank()) {
                // An optional field left out is a component that is not in the
                // circuit; a required one missing means the example is broken.
                if (field.optional) return@forEach
                return null
            }
            values[field.key] = NumberFormatter.parseOrNull(raw) ?: return null
        }

        val solved = solution.solve(TheoryInputs(values))
        return QuizQuestion(
            topicKey = topic.key,
            solutionKey = solution.key,
            exampleKey = example.key,
            givens = solution.fields
                .filter { values.containsKey(it.key) }
                .associate { it.key to (example.values[it.key] ?: it.default) },
            expected = solved.primary.number,
            targetLabelRes = solution.targetLabelRes,
        )
    }

    /** Every question a topic can ask, one per example on each of its solutions. */
    fun questionsFor(topic: TheoryTopic): List<QuizQuestion> = topic.solutions.flatMap { solution ->
        solution.examples.mapNotNull { questionFor(topic, solution, it) }
    }

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
