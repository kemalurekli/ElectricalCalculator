package com.kemalurekli.electricalcalculator.features.fieldnotes.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import org.jetbrains.compose.resources.StringResource

/**
 * One field note — a piece of working knowledge, in card form.
 *
 * ### What separates a note from the app's other three shelves
 *
 * The app already answers three questions. The glossary answers *what is this
 * called*, the reference topics answer *what does the table say*, and the
 * calculators answer *what is the number*. None of them answers the one an
 * apprentice actually asks on site: **why is it done that way, and what goes
 * wrong when it isn't**.
 *
 * That is the whole of this shelf, and the boundary is worth stating as a rule
 * because it is what stops it becoming a fourth place to look for the same
 * thing:
 *
 * > A note makes a claim you can act on. If it defines a word it belongs in the
 * > glossary; if it quotes a tabulated figure it belongs in the reference topics.
 *
 * `FieldNoteCatalogTest` cannot enforce that — no test reads prose — so it is
 * enforced by review, and the cross-links below are what make it cheap to obey.
 * A note that wants to define a term links to the term instead.
 *
 * ### These are claims, not transcriptions
 *
 * Nothing here is copied from a standard, and that makes it *more* dangerous
 * than the tables, not less. A wrong table entry misleads a reader; a wrong rule
 * of thumb makes them do the wrong thing and feel confident about it. Registered
 * in `docs/verification-backlog.md` as `field-note-claims` for that reason.
 *
 * Where a note leans on a figure the app already holds, it links to the topic
 * that tabulates it rather than restating the number, so a note cannot drift
 * away from the table it came from.
 *
 * @param key stable identifier, used in a route and in saved state. Never reused
 *   or renamed, for the same reason calculator, topic and term keys are not.
 * @param category the section it is listed under.
 * @param titleRes the claim itself, stated in a line. A title that reads as a
 *   sentence is what lets the list be skimmed without opening anything.
 * @param bodyRes why it is true and what it costs to ignore, in the reader's
 *   language.
 * @param glossaryTerms keys of terms this note assumes the reader knows.
 *   Unresolvable keys fail `FieldNoteCatalogTest`.
 * @param calculator the calculator that puts a number on this note, when the app
 *   has one.
 * @param referenceTopic key of the reference topic that tabulates what this note
 *   talks around, when one exists.
 */
data class FieldNote(
    val key: String,
    val category: FieldNoteCategory,
    val title: StringResource,
    val body: StringResource,
    val glossaryTerms: List<String> = emptyList(),
    val calculator: CalculatorId? = null,
    val referenceTopic: String? = null,
)

/**
 * How the notes are grouped.
 *
 * Deliberately not the same axis as [com.kemalurekli.electricalcalculator
 * .features.references.domain.ReferenceCategory]. That one sorts material by
 * *what kind of document it is* — a table, a selection guide, a primer. This one
 * sorts by **when you need it**: standing at the board, holding a meter, sizing
 * something in your head. Two shelves cut the same subject along different
 * grains, which is what keeps a reader from wondering which one to open.
 *
 * A category is added when its notes are, never ahead of them —
 * `FieldNoteCatalogTest` fails on an empty one, because a category with nothing
 * in it is a filter chip that leads to a blank screen. Installation craft, fault
 * finding, materials, motors and drives, and energy and cost are the next five;
 * they are in the plan, not in this enum, until they have content.
 */
enum class FieldNoteCategory {
    /** Staying alive, and the habits that make that automatic. */
    SAFETY_AND_PRACTICE,

    /** Numbers worth carrying in your head, and when they stop being true. */
    RULES_OF_THUMB,

    /** Holding a meter: what to measure, in what order, and what the reading means. */
    MEASUREMENT_AND_TESTING,
}
