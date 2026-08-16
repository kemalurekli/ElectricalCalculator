package com.kemalurekli.electricalcalculator.features.theory.domain

import androidx.annotation.StringRes
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId

/**
 * One theory topic — a formula, what it is for, and what it stops being true of.
 *
 * ### Why this shelf is a catalog and the calculators are not
 *
 * Sixteen calculators are sixteen *tools*. Each has its own inputs, its own
 * standard, its own correction tables, its own history record and its own share
 * text; they differ along nearly every axis, so giving each one its own use case,
 * view model and screen is an honest design.
 *
 * Twenty theory topics are **twenty instances of one thing**: a formula, its
 * inputs, its rearrangements, its derivation, and the conditions under which it
 * stops holding. What varies between them is data, not structure. Copying the
 * calculator skeleton twenty times would produce ~120 files that differ only in
 * their identifiers, and would turn every later change to the detail screen into
 * a twenty-file edit.
 *
 * The rule this codebase follows is not "one use case per screen" but **the shape
 * of the code matches the shape of the problem**. The problem here is a *catalog*
 * — the shape the reference topics, the glossary and the general-info notes have
 * already taken.
 *
 * One consequence is worth naming because it is the reason to prefer this: the
 * tests in `TheoryCatalogTest`, `TheorySolverTest` and `TheoryFormattingTest` all
 * iterate [TheoryCatalog.all], so a topic added to the catalog is under test the
 * same second. Adding a calculator, by contrast, requires hand-registering it in
 * `WorkedExampleTest` and `ExplainerTest`, and nothing fails if you forget.
 *
 * ### What separates a topic from the app's other shelves
 *
 * The glossary answers *what is this called*, the reference topics answer *what
 * does the table say*, the general-info notes answer *why is it done that way*,
 * and the calculators answer *what is the number for my job*. A theory topic
 * answers **where does the formula come from, and when does it stop being true**.
 *
 * > A topic derives. If it only puts a number on a job it belongs in a
 * > calculator; if it defines a word it belongs in the glossary.
 *
 * @param key stable identifier, used in a route and in saved state. Never reused
 *   or renamed, for the same reason calculator, topic and term keys are not.
 * @param level how far in the reader has to be. Declaration order of
 *   [TheoryLevel] is the order the list screen reads down.
 * @param titleRes the topic's name — "Ohm's law", not a sentence.
 * @param summaryRes one line, shown in the list. What the formula gives you.
 * @param theoryRes where the formula comes from, in two or three paragraphs.
 *   This is the part a calculator does not carry.
 * @param assumptionsRes the conditions it holds under, one per string. When they
 *   are absent the topic is a formula without a domain, which is how a reader
 *   ends up applying maximum power transfer to a distribution board.
 * @param diagram the circuit this topic talks about, if drawing it helps.
 * @param solutions what the topic can solve for. More than one only where
 *   rearranging genuinely teaches something — Ohm's law, DC power, RMS. Most
 *   topics have exactly one.
 * @param selectorLabelRes what the selector over [solutions] is asking. Defaults
 *   to "Solve for", which is right when the solutions are rearrangements of one
 *   formula. Some topics offer a choice of *circuit* instead — star or delta, a
 *   copper conductor or an aluminium one — and there "Solve for: Star" would be
 *   a sentence that does not parse. The alternative was a second mechanism for
 *   choices; one label makes the existing one honest.
 * @param glossaryTerms keys of terms this topic assumes the reader knows.
 *   Unresolvable keys fail `TheoryCatalogTest`.
 * @param calculator the calculator that applies this to a real job, when one
 *   exists. The link that keeps theory from being a dead end.
 * @param referenceTopic key of the reference topic that tabulates what this
 *   derives, when one exists.
 */
data class TheoryTopic(
    val key: String,
    val level: TheoryLevel,
    @StringRes val titleRes: Int,
    @StringRes val summaryRes: Int,
    @StringRes val theoryRes: Int,
    val assumptionsRes: List<Int> = emptyList(),
    val diagram: TheoryDiagram? = null,
    val solutions: List<TheorySolution>,
    @StringRes val selectorLabelRes: Int? = null,
    val glossaryTerms: List<String> = emptyList(),
    val calculator: CalculatorId? = null,
    val referenceTopic: String? = null,
)

/**
 * How far in a topic sits.
 *
 * Declaration order is the order the list screen reads down, so scrolling is the
 * curriculum: a reader who starts at the top and keeps going meets nothing that
 * depends on something they have not passed. That is the whole contract, and it
 * is why reordering this enum is a content decision rather than a cosmetic one.
 *
 * A level is added when its topics are — `TheoryCatalogTest` fails on an empty
 * one, because a level with nothing in it is a filter chip that leads to a blank
 * screen. Alternating current and the network theorems are planned and are not
 * in this enum until they have content; the chip row grows with the shelf.
 */
enum class TheoryLevel {
    /** Direct current, resistance, and the arithmetic that follows from them. */
    FOUNDATION,

    /** Alternating current: reactance, phase, and the three-phase relationships. */
    INTERMEDIATE,

    /** Network theorems and resonance — the ones that need the first two. */
    ADVANCED,
}

/**
 * The circuit a topic is talking about.
 *
 * A concept here rather than a drawing, for the same reason
 * [com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon] is a
 * concept: the domain layer does not depend on Compose. The mapping to an actual
 * drawing lives in `TheoryDiagrams.kt`, in an exhaustive `when`, so a value added
 * here without a drawing is a build error rather than a blank card.
 *
 * A value is added when the topic that needs it is, never ahead of it — an unused
 * value is a drawing and an accessibility description that nothing has ever
 * rendered, which is to say untested work pretending to be finished.
 */
enum class TheoryDiagram {
    /** A source, a switch and one resistance — the circuit Ohm's law describes. */
    SIMPLE_LOOP,

    /** Three resistances end to end, one current through all of them. */
    SERIES_RESISTORS,

    /** Three resistances across one pair of nodes, one voltage across all. */
    PARALLEL_RESISTORS,

    /** Two resistances in series with the output taken from their junction. */
    VOLTAGE_DIVIDER,

    /** Two resistances in parallel with the supply current splitting between them. */
    CURRENT_DIVIDER,

    /** A conductor run drawn long, with its length and cross-section called out. */
    CONDUCTOR_RUN,

    /** A junction with currents arriving and leaving, beside a loop of drops. */
    NODE_AND_LOOP,

    /** One cycle of a sine, with its peak, RMS and average levels marked. */
    SINE_WAVE,

    /** An alternating supply across a capacitor. */
    AC_CAPACITOR,

    /** An alternating supply across an inductor. */
    AC_INDUCTOR,

    /** The right triangle that relates active, reactive and apparent power. */
    POWER_TRIANGLE,

    /** Three windings drawn once as a star and once as a delta. */
    STAR_DELTA_SUPPLY,

    /** A resistor charging a capacitor through a switch. */
    RC_CHARGING,

    /** A resistance, an inductance and a capacitance in one alternating loop. */
    SERIES_RLC,

    /**
     * A source behind a series resistance, feeding a load.
     *
     * Shared by the Thévenin topic and by maximum power transfer, because it is
     * the same circuit read two ways: one asks what the network looks like from
     * the load, the other asks what load takes the most from it.
     */
    THEVENIN,

    /** A current source with a resistance across it, feeding a load. */
    NORTON,

    /** Three resistances as a star beside the same three as a delta. */
    Y_DELTA_TRANSFORM,
}

/**
 * One thing a topic can solve for.
 *
 * A topic with three solutions is a topic whose formula genuinely rearranges into
 * three useful questions — Ohm's law is the archetype. Most topics have one, and
 * the selector is hidden when they do.
 *
 * @param key stable within its topic; used for saved state and as a list key.
 * @param targetLabelRes the selector's label — "Solve for current".
 * @param formula the symbolic form, already rearranged for this target. Plain
 *   text with Unicode maths, matching the convention the calculators use.
 * @param variables what each symbol in [formula] means.
 * @param fields what the reader has to supply. Validation bounds live here so the
 *   view model can drive [com.kemalurekli.electricalcalculator.core.common.util
 *   .NumericInput] from the declaration rather than from a hand-written branch.
 * @param examples ready-made scenarios that fill [fields] in one tap.
 * @param solve the arithmetic. Pure, total over validated inputs, and free of
 *   any locale or Android type — see [TheoryStep] for how that is kept true.
 */
data class TheorySolution(
    val key: String,
    @StringRes val targetLabelRes: Int,
    val formula: String,
    val variables: List<TheoryVariable> = emptyList(),
    val fields: List<TheoryField>,
    val examples: List<TheoryExample> = emptyList(),
    val solve: (TheoryInputs) -> TheorySolutionResult,
)

/**
 * What one symbol in a formula stands for.
 *
 * Mirrors [com.kemalurekli.electricalcalculator.core.designsystem.component
 * .FormulaVariable], which is the Compose-side type it becomes. Kept separate so
 * the catalog stays free of Compose and holds string resources rather than
 * resolved text.
 */
data class TheoryVariable(
    val symbol: String,
    @StringRes val meaningRes: Int,
    val unit: String,
)

/**
 * One value the reader supplies.
 *
 * The bounds are the whole reason this type exists: stating them here lets one
 * generic view model validate twenty topics' inputs without knowing anything
 * about any of them.
 *
 * @param key referenced by [TheoryInputs] and by [TheoryExample.values]. Lower
 *   snake case, unique within its solution — `TheoryCatalogTest` enforces both.
 * @param optional whether the reader may leave it blank. A third resistance in a
 *   series or parallel network is the case this exists for. The alternative —
 *   treating zero as "not connected" — reads fine in series and is a trap in
 *   parallel, where zero ohms is a genuine short circuit and a reader who means
 *   it would silently get the wrong answer. Blank means absent; zero means zero.
 *   A blank optional field is absent from [TheoryInputs], so a solver reaches it
 *   through [TheoryInputs.getOrNull].
 * @param default what the field starts at. A sensible default is the difference
 *   between a form a reader fills in and a form they abandon; supply voltages
 *   and mains frequency are worth pre-filling, resistances are not.
 * @param hintRes supporting text under the field, for a unit convention that is
 *   not obvious from the label.
 */
data class TheoryField(
    val key: String,
    @StringRes val labelRes: Int,
    val unit: String,
    val min: Double? = null,
    val max: Double? = null,
    val allowZero: Boolean = false,
    val allowNegative: Boolean = false,
    val optional: Boolean = false,
    val default: String = "",
    @StringRes val hintRes: Int? = null,
)

/**
 * A scenario that fills a solution's fields in one tap.
 *
 * Unlike [com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample], which
 * carries a `(S) -> S` function because it has to preserve parts of a calculator's
 * state the example does not own, this carries plain text. A theory form holds
 * nothing worth preserving — there is no favourite flag and no unit preference —
 * so the simpler shape is the honest one.
 *
 * @param values keyed by [TheoryField.key], written exactly as a reader would
 *   type them. `TheoryCatalogTest` requires every required field to be present
 *   and rejects keys the solution does not declare, so an example cannot
 *   half-fill a form or fill one that is not there.
 */
data class TheoryExample(
    val key: String,
    @StringRes val titleRes: Int,
    val values: Map<String, String>,
)

/**
 * A solution's validated inputs, keyed by [TheoryField.key].
 *
 * [get] throws on a missing key rather than returning a default. A solver asking
 * for a field its solution does not declare is a catalog bug, and failing loudly
 * in a unit test is better than silently solving with a zero.
 */
@JvmInline
value class TheoryInputs(private val values: Map<String, Double>) {

    operator fun get(key: String): Double = values.getValue(key)

    fun getOrNull(key: String): Double? = values[key]
}

/**
 * What a solver produces.
 *
 * @param primary the answer the reader asked for; the headline of the result card.
 * @param secondary everything else worth reporting. A theory topic usually knows
 *   more than it was asked — solve an RLC circuit for impedance and you have the
 *   phase angle and both reactances for free — and withholding them would be an
 *   odd kind of tidiness.
 * @param steps the derivation, in order. The part that makes this a theory topic
 *   rather than a calculator.
 * @param diagramLabels live values for the circuit drawing, keyed by a slot name
 *   the drawing knows. A slot with no value falls back to its symbol, so a
 *   diagram is readable before anything is calculated.
 */
data class TheorySolutionResult(
    val primary: TheoryQuantity,
    val secondary: List<TheoryQuantity> = emptyList(),
    val steps: List<TheoryStep>,
    val diagramLabels: Map<String, TheoryNumber> = emptyMap(),
)

/** A reported quantity: what it is, and the number. */
data class TheoryQuantity(
    @StringRes val labelRes: Int,
    val number: TheoryNumber,
)

/**
 * A number, plus how it should be read.
 *
 * [style] exists because one fixed number of decimals is wrong across the range
 * this shelf covers: a capacitive reactance at 1 nF is 3.18 MΩ and at 100 µF is
 * 31.8 mΩ, and `%.2f` renders one of those as `3183098.86` and the other as
 * `0.03`. Declaring the reading at the point the number is produced is how the
 * formatter stays a single function.
 */
data class TheoryNumber(
    val value: Double,
    val unit: String = "",
    val decimals: Int = 2,
    val style: NumberStyle = NumberStyle.FIXED,
)

/** How a [TheoryNumber] is rendered. Maps onto the three `NumberFormatter` modes. */
enum class NumberStyle {
    /** Fixed decimals. Right for voltages, currents and percentages. */
    FIXED,

    /** Significant figures. Right for a quantity whose magnitude varies widely. */
    SIGNIFICANT,

    /** Significant figures with an SI prefix — 3.18 MΩ rather than 3183098.86 Ω. */
    SI_PREFIX,
}

/**
 * One line of a derivation, before it has been formatted for a reader.
 *
 * ### Why this is not a `CalculationStep`
 *
 * [com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep] holds
 * finished text: its `substitution` is already formatted, which means whoever
 * builds it has to hold a `Locale`. Each of the sixteen calculators does exactly
 * that, and `ExplainerTest` then checks, by hand and per calculator, that the
 * formula line came out language-neutral and the substitution used the reader's
 * decimal separator.
 *
 * Twenty more hand-written explainers would be twenty more chances to get that
 * wrong. So this type keeps the numbers apart from the sentence: [substitution]
 * carries `{0}`, `{1}` slots, [operands] carries the numbers, and the single
 * function `TheoryStep.toCalculationStep(locale)` puts them together. The formula
 * line is copied untouched and therefore *cannot* pick up a locale; the numbers
 * go through the formatter and therefore *cannot* miss one. Both properties hold
 * by construction rather than by test.
 *
 * @param formula the symbolic form — `I = U / R`. Language-neutral by definition.
 * @param substitution the same line with slots where the numbers go — `{0} / {1}`.
 * @param operands the numbers, in slot order. `TheorySolverTest` checks the count
 *   matches the highest slot index.
 * @param result what this step establishes, with its unit.
 */
data class TheoryStep(
    @StringRes val labelRes: Int,
    val formula: String,
    val substitution: String,
    val operands: List<TheoryNumber> = emptyList(),
    val result: TheoryNumber,
)
