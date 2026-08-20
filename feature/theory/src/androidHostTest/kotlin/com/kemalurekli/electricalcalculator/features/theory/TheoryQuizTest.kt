package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_quiz_prompt
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryNumber
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryQuiz
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizQuestion
import com.kemalurekli.electricalcalculator.features.theory.domain.QuizVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The quiz, checked the way the rest of this shelf is: over the whole catalog.
 *
 * Because questions are generated from the topics' own worked examples, a topic
 * added later is covered here the moment it lands — and a topic whose example
 * stops matching its fields fails this rather than shipping a question with no
 * answer.
 */
class TheoryQuizTest {

    @Test
    fun `every topic that ships an example can ask about it`() {
        val topicsWithExamples = TheoryCatalog.all.filter { topic ->
            topic.solutions.any { it.examples.isNotEmpty() }
        }
        assertTrue("the catalog should carry examples at all", topicsWithExamples.isNotEmpty())

        topicsWithExamples.forEach { topic ->
            val questions = TheoryQuiz.questionsFor(topic)
            assertTrue("${topic.key} produced no question", questions.isNotEmpty())
        }
    }

    @Test
    fun `a generated question carries the values it hands the reader`() {
        val topic = TheoryCatalog.all.first { it.solutions.any { s -> s.examples.isNotEmpty() } }
        val question = TheoryQuiz.questionsFor(topic).first()

        assertNotNull(question)
        assertTrue("a question with no givens is not a question", question.givens.isNotEmpty())
        assertTrue(question.expected.value.isFinite())
    }

    @Test
    fun `the solver's own answer is marked correct`() {
        // The property the whole design rests on. If a solver is corrected, the
        // mark scheme moves with it rather than going stale.
        TheoryCatalog.all.forEach { topic ->
            TheoryQuiz.questionsFor(topic).forEach { question ->
                val exact = question.expected.value.toString()
                assertEquals(
                    "${topic.key}/${question.exampleKey}",
                    QuizVerdict.CORRECT,
                    TheoryQuiz.mark(question, exact),
                )
            }
        }
    }

    private fun question(expected: Double) = QuizQuestion(
        topicKey = "t",
        solutionKey = "s",
        exampleKey = "e",
        givens = mapOf("a" to "1"),
        expected = TheoryNumber(expected, "A"),
        targetLabel = Res.string.th_quiz_prompt,
    )

    @Test
    fun `rounding along the way is forgiven`() {
        // A reader working on paper rounds at every step. Failing them for the
        // third decimal tests arithmetic patience, not understanding.
        val q = question(230.0)
        assertEquals(QuizVerdict.CORRECT, TheoryQuiz.mark(q, "230"))
        assertEquals(QuizVerdict.CORRECT, TheoryQuiz.mark(q, "228"))
        assertEquals(QuizVerdict.CORRECT, TheoryQuiz.mark(q, "234"))
    }

    @Test
    fun `the right size but the wrong number is told apart from a miss`() {
        // Within a factor of two is usually one dropped root three or one
        // rounding, and saying so is more use than "wrong".
        val q = question(100.0)
        assertEquals(QuizVerdict.CLOSE, TheoryQuiz.mark(q, "58"))
        assertEquals(QuizVerdict.CLOSE, TheoryQuiz.mark(q, "173"))
        assertEquals(QuizVerdict.WRONG, TheoryQuiz.mark(q, "10"))
        assertEquals(QuizVerdict.WRONG, TheoryQuiz.mark(q, "1000"))
    }

    @Test
    fun `a sign error is wrong, not close`() {
        val q = question(50.0)
        assertEquals(QuizVerdict.WRONG, TheoryQuiz.mark(q, "-50"))
    }

    @Test
    fun `either decimal separator is accepted`() {
        val q = question(1.5)
        assertEquals(QuizVerdict.CORRECT, TheoryQuiz.mark(q, "1.5"))
        assertEquals(QuizVerdict.CORRECT, TheoryQuiz.mark(q, "1,5"))
    }

    @Test
    fun `nothing readable is unanswered rather than wrong`() {
        val q = question(10.0)
        listOf("", "   ", "abc").forEach {
            assertEquals("'$it'", QuizVerdict.UNANSWERED, TheoryQuiz.mark(q, it))
        }
    }
}
