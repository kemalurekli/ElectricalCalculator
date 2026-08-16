package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryDiagram
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural checks over the theory shelf.
 *
 * ### Why these iterate the catalog
 *
 * Every assertion here runs over `TheoryCatalog.all`, so a topic added tomorrow
 * is checked tomorrow with nothing registered anywhere. That is the concrete
 * pay-off of the catalog design argued for in `TheoryModels.kt`: adding a
 * calculator requires hand-registering it in `WorkedExampleTest` and
 * `ExplainerTest`, and forgetting is silent.
 *
 * No test can tell whether a derivation is *right* — `TheorySolverTest` checks
 * the arithmetic against worked figures, and the prose is a review matter.
 */
class TheoryCatalogTest {

    private val topics = TheoryCatalog.all

    @Test
    fun `the catalog is not empty`() {
        // A generator that emitted nothing would make every other assertion here
        // vacuously true.
        assertTrue("No theory topics in the catalog", topics.isNotEmpty())
    }

    @Test
    fun `topic keys are unique`() {
        val keys = topics.map { it.key }
        assertEquals("Duplicate topic key", keys.size, keys.distinct().size)
    }

    @Test
    fun `keys are lower snake case`() {
        val pattern = Regex("""^[a-z0-9]+(_[a-z0-9]+)*$""")
        topics.forEach { topic ->
            assertTrue("Topic key is not lower_snake_case: ${topic.key}", pattern.matches(topic.key))
            topic.solutions.forEach { solution ->
                assertTrue(
                    "Solution key is not lower_snake_case: ${topic.key}/${solution.key}",
                    pattern.matches(solution.key),
                )
                solution.fields.forEach { field ->
                    assertTrue(
                        "Field key is not lower_snake_case: ${topic.key}/${field.key}",
                        pattern.matches(field.key),
                    )
                }
            }
        }
    }

    @Test
    fun `every topic is reachable by its key`() {
        topics.forEach { topic ->
            assertEquals(topic, TheoryCatalog.topicOrNull(topic.key))
        }
    }

    @Test
    fun `an unknown key resolves to nothing rather than throwing`() {
        // A stale deep link or a route restored from an older release lands here,
        // and it has to land softly — the screen shows a "not found" state.
        assertNull(TheoryCatalog.topicOrNull("no_such_topic"))
    }

    @Test
    fun `every topic carries the text the screen needs`() {
        topics.forEach { topic ->
            assertTrue("${topic.key} has no title resource", topic.titleRes != 0)
            assertTrue("${topic.key} has no summary resource", topic.summaryRes != 0)
            assertTrue("${topic.key} has no theory resource", topic.theoryRes != 0)
        }
    }

    @Test
    fun `no two topics share a title, summary or theory resource`() {
        // Catches the copy-paste that leaves a new topic showing an old one's
        // text, which nothing else here would notice.
        listOf(
            "title" to topics.map { it.titleRes },
            "summary" to topics.map { it.summaryRes },
            "theory" to topics.map { it.theoryRes },
        ).forEach { (what, resources) ->
            assertEquals(
                "Two topics share a $what resource",
                resources.size,
                resources.distinct().size,
            )
        }
    }

    @Test
    fun `every topic states the conditions it holds under`() {
        // A formula without a domain is how a reader ends up applying maximum
        // power transfer to a distribution board. This is the one piece of
        // content that separates this shelf from the calculators, so its absence
        // is a failure rather than an omission.
        topics.forEach { topic ->
            assertTrue("${topic.key} lists no assumptions", topic.assumptionsRes.isNotEmpty())
            topic.assumptionsRes.forEach {
                assertTrue("${topic.key} has an empty assumption resource", it != 0)
            }
        }
    }

    // -- Solutions -----------------------------------------------------------

    @Test
    fun `every topic can solve for something`() {
        topics.forEach { topic ->
            assertTrue("${topic.key} has no solutions", topic.solutions.isNotEmpty())
        }
    }

    @Test
    fun `solution keys are unique within a topic`() {
        topics.forEach { topic ->
            val keys = topic.solutions.map { it.key }
            assertEquals("${topic.key} repeats a solution key", keys.size, keys.distinct().size)
        }
    }

    @Test
    fun `every solution asks for something and says what it computes`() {
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                val id = "${topic.key}/${solution.key}"
                assertTrue("$id declares no fields", solution.fields.isNotEmpty())
                assertTrue("$id has no target label", solution.targetLabelRes != 0)
                assertTrue("$id has no formula", solution.formula.isNotBlank())
            }
        }
    }

    @Test
    fun `field keys are unique within a solution`() {
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                val keys = solution.fields.map { it.key }
                assertEquals(
                    "${topic.key}/${solution.key} declares the same field twice",
                    keys.size,
                    keys.distinct().size,
                )
            }
        }
    }

    @Test
    fun `every field is labelled`() {
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                solution.fields.forEach { field ->
                    assertTrue(
                        "${topic.key}/${solution.key}/${field.key} has no label",
                        field.labelRes != 0,
                    )
                }
            }
        }
    }

    @Test
    fun `an optional field never also carries a default`() {
        // A default in a field the reader may leave blank means the form arrives
        // with the optional component already fitted, which is the opposite of
        // what "optional" leads them to expect.
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                solution.fields.filter { it.optional }.forEach { field ->
                    assertTrue(
                        "${topic.key}/${solution.key}/${field.key} is optional but pre-filled",
                        field.default.isBlank(),
                    )
                }
            }
        }
    }

    @Test
    fun `every formula variable is explained`() {
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                solution.variables.forEach { variable ->
                    assertTrue(
                        "${topic.key}/${solution.key} has an unexplained symbol " +
                            "\"${variable.symbol}\"",
                        variable.meaningRes != 0,
                    )
                }
            }
        }
    }

    // -- Examples ------------------------------------------------------------

    @Test
    fun `example keys are unique within a solution`() {
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                val keys = solution.examples.map { it.key }
                assertEquals(
                    "${topic.key}/${solution.key} repeats an example key",
                    keys.size,
                    keys.distinct().size,
                )
            }
        }
    }

    @Test
    fun `every example fills exactly the fields its solution declares`() {
        // Both directions matter. A missing required key leaves the reader with a
        // half-filled form and a validation error they did not cause; an unknown
        // key is a field that was renamed with the example left behind, and it
        // would be silently ignored.
        topics.forEach { topic ->
            topic.solutions.forEach { solution ->
                val required = solution.fields.filterNot { it.optional }.map { it.key }.toSet()
                val known = solution.fields.map { it.key }.toSet()

                solution.examples.forEach { example ->
                    val id = "${topic.key}/${solution.key}/${example.key}"
                    assertTrue("$id has no title", example.titleRes != 0)
                    assertTrue(
                        "$id leaves required fields empty: ${required - example.values.keys}",
                        required.all { it in example.values },
                    )
                    assertTrue(
                        "$id fills fields that do not exist: ${example.values.keys - known}",
                        known.containsAll(example.values.keys),
                    )
                    example.values.forEach { (key, value) ->
                        assertTrue("$id gives $key a blank value", value.isNotBlank())
                    }
                }
            }
        }
    }

    // -- Cross-links ---------------------------------------------------------

    @Test
    fun `every glossary term a topic leans on exists`() {
        topics.forEach { topic ->
            topic.glossaryTerms.forEach { key ->
                assertTrue(
                    "${topic.key} points at glossary term \"$key\", which does not exist",
                    GlossaryCatalog.termOrNull(key) != null,
                )
            }
        }
    }

    @Test
    fun `term lists have no duplicates`() {
        topics.forEach { topic ->
            assertEquals(
                "${topic.key} lists the same term twice",
                topic.glossaryTerms.size,
                topic.glossaryTerms.distinct().size,
            )
        }
    }

    @Test
    fun `every referenced calculator is in the catalog`() {
        val known = CalculatorCatalog().all.map { it.id }.toSet()
        topics.mapNotNull { topic -> topic.calculator?.let { topic.key to it } }
            .forEach { (key, id) ->
                assertTrue("$key points at calculator $id, which is not in the catalog", id in known)
            }
    }

    @Test
    fun `every referenced reference topic exists`() {
        val known = ReferenceCatalog.all.map { it.key }.toSet()
        topics.mapNotNull { topic -> topic.referenceTopic?.let { topic.key to it } }
            .forEach { (key, reference) ->
                assertTrue(
                    "$key points at reference topic \"$reference\", which does not exist",
                    reference in known,
                )
            }
    }

    // -- Levels and diagrams --------------------------------------------------

    @Test
    fun `every declared level carries at least one topic`() {
        // A level with no topics is a filter chip that leads to an empty list.
        // Levels are added with their content, not ahead of it.
        TheoryLevel.entries.forEach { level ->
            assertTrue(
                "Level $level is declared but has no topics",
                TheoryCatalog.inLevel(level).isNotEmpty(),
            )
        }
    }

    @Test
    fun `inLevel returns exactly the topics filed under it`() {
        TheoryLevel.entries.forEach { level ->
            assertEquals(topics.filter { it.level == level }, TheoryCatalog.inLevel(level))
        }
    }

    @Test
    fun `the catalog is ordered by level`() {
        // Scrolling the list screen is the curriculum, and the screen renders
        // sections in TheoryLevel declaration order. A catalog that jumps back to
        // an easier level mid-file is a file whose reading order has quietly
        // stopped matching the screen's.
        val ordinals = topics.map { it.level.ordinal }
        assertEquals("The catalog is not ordered by level", ordinals.sorted(), ordinals)
    }

    @Test
    fun `every declared diagram is used by a topic`() {
        // The reverse — a topic naming a diagram with no drawing — is a compile
        // error, because TheoryDiagramFigure switches exhaustively. This covers
        // the direction the compiler cannot: a value left behind after the topic
        // that needed it was renamed, carrying a drawing nothing renders.
        val used = topics.mapNotNull { it.diagram }.toSet()
        TheoryDiagram.entries.forEach { diagram ->
            assertTrue("Diagram $diagram is declared but no topic uses it", diagram in used)
        }
    }
}
