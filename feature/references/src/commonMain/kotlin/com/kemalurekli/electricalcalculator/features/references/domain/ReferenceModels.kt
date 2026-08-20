package com.kemalurekli.electricalcalculator.features.references.domain

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

/**
 * A piece of text in a reference table.
 *
 * Reference content is a mix of two kinds of text, and conflating them would
 * either leave half the tables untranslated or push symbols through a
 * translator who would be right to change them:
 *
 * - [Symbol] is a language-neutral designation — `IP54`, `IK07`, `L1`, `kV`.
 * - [Localized] is prose — "Dust-tight", "Brown" — and must be translated.
 * - [Quantity] and [Range] are numbers. They are held as numbers rather than
 *   pre-rendered strings so the decimal separator follows the reader: a Turkish
 *   engineer writes 0,017241, and a table that said 0.017241 beside prose full
 *   of commas would be the app contradicting itself.
 */
sealed interface ReferenceText {

    /** A designation that reads the same in every language. */
    data class Symbol(val text: String) : ReferenceText

    /** Text that must be translated. */
    data class Localized(val res: StringResource) : ReferenceText

    /**
     * A number with an optional unit, formatted in the reader's locale.
     *
     * @param significantDigits enough to carry the value unrounded; resistivity
     *   needs five, an impact energy needs two.
     */
    data class Quantity(
        val value: Double,
        val unit: String = "",
        val significantDigits: Int = DEFAULT_DIGITS,
    ) : ReferenceText

    /** An indicative range, "0,8 – 0,85". */
    data class Range(
        val low: Double,
        val high: Double,
        val unit: String = "",
        val significantDigits: Int = DEFAULT_DIGITS,
    ) : ReferenceText

    companion object {
        const val DEFAULT_DIGITS = 6
    }
}

/**
 * One row of a reference table.
 *
 * Three columns cover every table in this section — a designation, what it
 * means, and an optional qualifier — which is why there is no general
 * n-column table type to configure.
 *
 * @param label the designation being looked up: a digit, a code, a conductor.
 * @param value what it means or is worth.
 * @param note an optional qualifier: the test condition, the standard, the
 *   region a figure applies in.
 */
data class ReferenceRow(
    val label: ReferenceText,
    val value: ReferenceText,
    val note: ReferenceText? = null,
)

/**
 * What a section can be made of.
 *
 * The library started as tables, because codes and ratings are tables. The
 * content that makes the app worth keeping is not: a commissioning procedure is
 * an ordered list where the order is the safety-critical part, a diagnosis is
 * prose, and a warning has to look unlike everything around it or it will be
 * skimmed past. Forcing those into a three-column table would either mangle them
 * or leave them unwritten.
 */
sealed interface ReferenceBlock {

    /** Rows of a lookup table — the original and still most common block. */
    data class Table(val rows: List<ReferenceRow>) : ReferenceBlock

    /**
     * Explanatory paragraphs.
     *
     * Each entry is one paragraph. Held separately rather than as one string
     * with newlines so a translator sees the structure and cannot lose it.
     */
    data class Prose(val paragraphs: List<StringResource>) : ReferenceBlock

    /**
     * A numbered procedure, rendered in the order given.
     *
     * Used where sequence carries meaning. In commissioning it carries safety:
     * the dead tests come before the live ones, and a reader who takes them in
     * a different order is working on something they have not proved is dead.
     */
    data class Ordered(val steps: List<StringResource>) : ReferenceBlock

    /**
     * Options side by side, one column each.
     *
     * The three-column [Table] answers "what does this mean". This answers
     * "which one should I use", which is a different question and needs the
     * options adjacent: the value of knowing a soft starter draws 2–4 × In is
     * entirely in the 6–8 × In sitting next to it.
     *
     * Every row must have one cell per column. `ReferenceCatalogTest` fails on a
     * ragged one, because the renderer would silently drop the excess or leave a
     * gap under the wrong heading.
     *
     * @param columns the option names, left to right.
     * @param rows one attribute each, compared across the options.
     */
    data class Comparison(
        val columns: List<StringResource>,
        val rows: List<ComparisonRow>,
    ) : ReferenceBlock

    /** A grid of drawing symbols. */
    data class SymbolGrid(val symbols: List<DrawingSymbol>) : ReferenceBlock

    /**
     * The same apparatus drawn two ways, side by side.
     *
     * The point of the section it belongs to. An engineer reading an American
     * catalogue does not need a chart of ANSI symbols; they need the ANSI symbol
     * next to the IEC one they already know, which is a different page.
     *
     * @param leftLabel the convention on the left — IEC, in practice.
     * @param rightLabel the convention on the right.
     */
    data class SymbolComparison(
        val leftLabel: StringResource,
        val rightLabel: StringResource,
        val pairs: List<SymbolPair>,
    ) : ReferenceBlock

    /**
     * A boxed aside that must not read as body text.
     *
     * @param kind what the box is for, which decides how loud it looks.
     */
    data class Callout(
        val kind: CalloutKind,
        val text: StringResource,
    ) : ReferenceBlock
}

/**
 * One symbol on a drawing, with what a reader needs to identify it.
 *
 * ### Why the designation letter is here
 *
 * A schematic labels its devices `-Q1`, `-F2`, `-K3`. Those letters are IEC
 * 81346 reference designations and they are half of what a symbol means: the
 * shape says "switching device", the Q says which kind of role it plays in the
 * installation. A symbol gallery without them is a picture book.
 *
 * ### Why some carry a note and most do not
 *
 * The note is reserved for the pairs that are actually confused — disconnector
 * against switch-disconnector against circuit breaker, contactor against relay,
 * RCD against RCBO. Explaining a busbar would be noise; explaining why the
 * arc-quenching half-circle matters is the reason this section exists.
 *
 * @param key stable identifier, unique across the whole catalog.
 * @param image the drawing, on the shared 48 × 48 grid.
 * @param name what it is called, translated.
 * @param designation the IEC 81346 letter, or null where none applies.
 * @param note what distinguishes it from the symbol it is mistaken for.
 */
/**
 * One element drawn in two conventions.
 *
 * @param note what the difference is, where it is not self-evident. A
 *   resistor speaks for itself; an ANSI circuit breaker does not.
 */
data class SymbolPair(
    val key: String,
    val name: StringResource,
    val left: ImageVector,
    val right: ImageVector,
    val note: StringResource? = null,
)

data class DrawingSymbol(
    val key: String,
    val image: ImageVector,
    val name: StringResource,
    val designation: String? = null,
    val note: StringResource? = null,
)

/**
 * One attribute, compared across every option in a [ReferenceBlock.Comparison].
 *
 * @param label what is being compared — starting current, cost, where it suits.
 * @param cells one per column, in the same order as the column headings.
 */
data class ComparisonRow(
    val label: StringResource,
    val cells: List<ReferenceText>,
)

/** Why a [ReferenceBlock.Callout] is there, and therefore how it is styled. */
enum class CalloutKind {
    /** Something that will hurt someone if ignored. */
    SAFETY,

    /** Something that will produce a wrong answer if ignored. */
    WARNING,

    /** Something worth knowing that costs nothing to skip. */
    TIP,
}

/**
 * A titled group of content.
 *
 * @param footnote text below the blocks. Used where a table needs a caveat to
 *   be read correctly — an indicative figure that is not a standard, or a
 *   colour code that has been superseded but is still in installed plant.
 */
data class ReferenceSection(
    val title: StringResource,
    val blocks: List<ReferenceBlock>,
    val footnote: StringResource? = null,
) {
    /** Every table row in this section, flattened. */
    val rows: List<ReferenceRow>
        get() = blocks.filterIsInstance<ReferenceBlock.Table>().flatMap { it.rows }
}

/**
 * A section that is a single table, which most of them are.
 *
 * A function rather than a second constructor: after erasure both would take
 * `(Int, List, Integer)` and the JVM cannot tell them apart. Naming it also
 * makes the catalog say which sections are tables and which are not, which is
 * information the reader of that file wants anyway.
 */
fun tableSection(
    title: StringResource,
    rows: List<ReferenceRow>,
    footnote: StringResource? = null,
): ReferenceSection = ReferenceSection(
    title = title,
    blocks = listOf(ReferenceBlock.Table(rows)),
    footnote = footnote,
)

/**
 * How the reference library is grouped on its index.
 *
 * The list is long enough that a flat one stops being scannable, so it is
 * sectioned the same way the calculator list already is — same enum-driven
 * shape, same [com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader].
 */
enum class ReferenceCategory {
    /** Devices, earthing arrangements and the times they must act in. */
    PROTECTION_AND_EARTHING,

    /** Proving an installation works, and working out why one does not. */
    COMMISSIONING_AND_DIAGNOSIS,

    /** Choosing between options that all work. */
    SELECTION_GUIDES,

    /** Background an engineer met once and needs refreshing. */
    ENGINEERING_FOUNDATIONS,

    /** How apparatus is drawn on a schematic. */
    DRAWING_SYMBOLS,

    /** Lookup tables and identification codes. */
    TABLES_AND_CODES,
}

/**
 * A reference topic, as listed and then opened.
 *
 * @param key stable identifier, safe to persist and to put in a route.
 * @param category the section it is listed under.
 * @param source the standard the content follows, shown on the detail
 *   screen. Reference data is only as good as its provenance, and an
 *   unattributed table invites the reader to trust it further than they should.
 */
data class ReferenceTopic(
    val key: String,
    val category: ReferenceCategory,
    val title: StringResource,
    val description: StringResource,
    val source: StringResource,
    val sections: List<ReferenceSection>,
)
