package com.kemalurekli.electricalcalculator.features.glossary.domain

import androidx.annotation.StringRes
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId

/**
 * One entry in the glossary.
 *
 * ### Why the English name is a plain string
 *
 * Every other piece of text in the app is a resource id, resolved into the
 * reader's language and never seen in any other. This one is deliberately not.
 * A Turkish electrician reads Turkish regulations and English catalogues in the
 * same afternoon, and the gap between "akım taşıma kapasitesi" and "ampacity" is
 * exactly where a datasheet stops being readable. So the English term is carried
 * alongside the localised one and shown next to it.
 *
 * In an English build the two resolve to the same words and the screen shows the
 * name once — the second line earns its place only when it is actually telling
 * the reader something.
 *
 * ### Definitions are ours
 *
 * IEC 60050 (the IEV) is the authority on this vocabulary and its wording is
 * copyrighted. The definitions here are written from scratch to say the same
 * thing in the plainest words that remain correct. Where a term is defined by a
 * standard, the standard is named in the definition so a reader who needs the
 * exact legal wording knows where to go.
 *
 * @param key stable identifier, used for cross-references and saved state. Never
 *   reused or renamed, for the same reason calculator and topic keys are not.
 * @param englishTerm the term as an English-language catalogue prints it.
 * @param termRes the term in the reader's language.
 * @param definitionRes the explanation, in the reader's language.
 * @param symbol the letter it is written as in a formula — `Iz`, `Zs`, `cos φ` —
 *   or null when the term is not a quantity.
 * @param unit the SI unit it is measured in, or null for a term that is not
 *   measured.
 * @param seeAlso keys of terms worth reading next. Unresolvable keys are a build
 *   -time mistake and `GlossaryCatalogTest` fails on them.
 * @param calculator the calculator that computes this quantity, when the app has
 *   one. This is what turns a definition into something you can act on: read
 *   what voltage drop is, then go and work it out.
 * @param referenceTopic key of a reference topic that tabulates this, when one
 *   exists.
 */
data class GlossaryTerm(
    val key: String,
    val englishTerm: String,
    @StringRes val termRes: Int,
    @StringRes val definitionRes: Int,
    val symbol: String? = null,
    val unit: String? = null,
    val seeAlso: List<String> = emptyList(),
    val calculator: CalculatorId? = null,
    val referenceTopic: String? = null,
)
