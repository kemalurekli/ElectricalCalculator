package com.kemalurekli.electricalcalculator.core.ui.search

import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.search.SearchKind
import com.kemalurekli.electricalcalculator.core.domain.search.SearchableItem
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.presentation.categoryLabelRes
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCatalog
import com.kemalurekli.electricalcalculator.features.fieldnotes.presentation.title
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceBlock
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import javax.inject.Inject

/**
 * Builds the searchable view of everything the app contains.
 *
 * ### Why it is rebuilt on every query rather than cached
 *
 * Resolving the index means about 300 `getString` calls, which read a cached
 * resource table and cost microseconds. Caching it would mean holding text in
 * the reader's *old* language after an in-app language change, since a
 * ViewModel outlives the Activity that the change recreates. Paying a
 * negligible cost to be certainly right beats holding a cache that is wrong in
 * exactly the situation this app ships a language picker for.
 *
 * ### What goes in `keywords` rather than `title`
 *
 * The English name of a glossary term, and a symbol's IEC 81346 designation.
 * Both are things a reader searches for and neither should outrank a real title
 * match — someone typing "F" wants the fuse before every term whose definition
 * happens to contain an F.
 */
class SearchIndexBuilder @Inject constructor(
    private val catalog: CalculatorCatalog,
    private val stringResolver: ResourceIdResolver,
) {

    fun build(): List<SearchableItem> = buildList {
        addAll(calculators())
        addAll(converterCategories())
        addAll(referenceTopics())
        addAll(glossaryTerms())
        addAll(drawingSymbols())
        addAll(fieldNotes())
        addAll(theoryTopics())
    }

    /**
     * Theory topics, indexed on the derivation as well as the title.
     *
     * The shelf's own list deliberately does not search the body — see
     * [com.kemalurekli.electricalcalculator.features.theory.presentation
     * .TheoryListViewModel]. Here it is included, because a reader who types
     * "resonance" into the app-wide box wants every place the app discusses it,
     * and `body` scores below every title and keyword match anyway.
     */
    private fun theoryTopics(): List<SearchableItem> = TheoryCatalog.all.map { topic ->
        SearchableItem(
            kind = SearchKind.THEORY,
            key = topic.key,
            title = stringResolver.get(topic.title),
            subtitle = stringResolver.get(topic.summary),
            body = stringResolver.get(topic.theory),
        )
    }

    private fun calculators(): List<SearchableItem> = catalog.all.map { descriptor ->
        SearchableItem(
            kind = SearchKind.CALCULATOR,
            key = descriptor.id.key,
            title = stringResolver.get(descriptor.title),
            subtitle = stringResolver.get(descriptor.description),
            keywords = descriptor.searchKeywords,
            body = stringResolver.get(descriptor.description),
        )
    }

    private fun converterCategories(): List<SearchableItem> = UnitCatalog.all.map { category ->
        SearchableItem(
            kind = SearchKind.CONVERTER,
            key = category.key,
            title = stringResolver.get(category.categoryLabelRes()),
            // Unit symbols are language-neutral, so they are searchable as
            // themselves: someone hunting "kcmil" types kcmil in any language.
            subtitle = category.units.joinToString(" · ") { it.symbol },
            keywords = category.units.map { it.symbol },
        )
    }

    /**
     * Field notes are matched on their body as well as their title.
     *
     * A note is looked for by what it is about at least as often as by how it is
     * titled — "capacitor" should find the note about stored charge, whose title
     * mentions neither the word nor the hazard.
     */
    private fun fieldNotes(): List<SearchableItem> = FieldNoteCatalog.all.map { note ->
        SearchableItem(
            kind = SearchKind.FIELD_NOTE,
            key = note.key,
            title = stringResolver.get(note.title),
            subtitle = stringResolver.get(note.category.title()),
            body = stringResolver.get(note.body),
        )
    }

    private fun referenceTopics(): List<SearchableItem> = ReferenceCatalog.all.map { topic ->
        SearchableItem(
            kind = SearchKind.REFERENCE,
            key = topic.key,
            title = stringResolver.get(topic.title),
            subtitle = stringResolver.get(topic.description),
            // Section headings are what a reader half-remembers about a topic —
            // "the one with the test sequence in it".
            body = (
                listOf(stringResolver.get(topic.description)) +
                    topic.sections.map { stringResolver.get(it.title) }
                ).joinToString(" "),
        )
    }

    private fun glossaryTerms(): List<SearchableItem> = GlossaryCatalog.all.map { term ->
        SearchableItem(
            kind = SearchKind.GLOSSARY,
            key = term.key,
            title = stringResolver.get(term.term),
            subtitle = term.englishTerm,
            keywords = listOfNotNull(term.englishTerm, term.symbol),
            body = stringResolver.get(term.definition),
        )
    }

    private fun drawingSymbols(): List<SearchableItem> = ReferenceCatalog.all
        .flatMap { topic -> topic.sections.map { topic to it } }
        .flatMap { (topic, section) ->
            section.blocks
                .filterIsInstance<ReferenceBlock.SymbolGrid>()
                .flatMap { grid -> grid.symbols.map { topic to it } }
        }
        .map { (topic, symbol) ->
            SearchableItem(
                kind = SearchKind.SYMBOL,
                // A symbol is not its own destination; tapping opens the topic
                // it lives on, which is where its neighbours are.
                key = topic.key,
                title = stringResolver.get(symbol.name),
                subtitle = stringResolver.get(topic.title),
                keywords = listOfNotNull(symbol.designation),
            )
        }
}
