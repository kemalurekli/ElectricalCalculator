package com.kemalurekli.electricalcalculator.features.glossary

import androidx.lifecycle.SavedStateHandle
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.glossary.presentation.GlossaryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.BreakIterator
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.data.repository.FavoritesRepositoryImpl
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import org.jetbrains.compose.resources.StringResource

/**
 * The glossary screen's state.
 *
 * Names resolve to each term's English name — real words, so grouping and
 * alphabetical order are actually exercised — while definitions resolve to
 * their resource id. That keeps these tests independent of the 117 definitions:
 * rewording an entry must not fail a test about grouping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GlossaryViewModelTest {

    /**
     * Names resolve to each term's English name and definitions to their id.
     *
     * The English names are what make grouping and ordering testable at all: a
     * resolver that returned "res123" for everything would file all 117 terms
     * under R and let a broken alphabet pass unnoticed.
     */
    private val defaultNames: Map<StringResource, String> =
        GlossaryCatalog.all.associate { it.term to it.englishTerm }

    /**
     * Falls back to the resource's own name, which is what the definitions get.
     *
     * A definition resolving to `gl_ampacity_def` rather than its 300 words is
     * the point: these tests are about grouping, ordering and ranking by name,
     * and rewording an entry must not fail one of them.
     */
    private class MapResolver(
        private val strings: Map<StringResource, String>,
    ) : StringResolver {
        override fun get(resource: StringResource): String =
            strings[resource] ?: resource.key

        override fun get(resource: StringResource, vararg args: Any): String = get(resource)
    }

    /** Pin order, in memory — the module has no access to `:app`'s fakes. */
    private class FakeFavoriteItemDao : FavoriteItemDao {
        private val rows = MutableStateFlow<List<FavoriteItemEntity>>(emptyList())

        override fun observeAll(): Flow<List<FavoriteItemEntity>> =
            rows.map { list -> list.sortedBy { it.pinnedAtEpochMillis } }

        override fun observeIsFavorite(kind: String, key: String): Flow<Boolean> =
            rows.map { list -> list.any { it.kind == kind && it.key == key } }

        override suspend fun isFavorite(kind: String, key: String): Boolean =
            rows.value.any { it.kind == kind && it.key == key }

        override suspend fun insert(entity: FavoriteItemEntity) {
            rows.value = rows.value.filterNot {
                it.kind == entity.kind && it.key == entity.key
            } + entity
        }

        override suspend fun delete(kind: String, key: String) {
            rows.value = rows.value.filterNot { it.kind == kind && it.key == key }
        }
    }

    private object FixedTime : TimeProvider {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_000L)
    }

    private fun viewModel(overrides: Map<StringResource, String> = emptyMap()) = GlossaryViewModel(
        stringResolver = MapResolver(defaultNames + overrides),
        savedStateHandle = SavedStateHandle(),
        favoritesRepository = FavoritesRepositoryImpl(
            dao = FakeFavoriteItemDao(),
            timeProvider = FixedTime,
            ioDispatcher = UnconfinedTestDispatcher(),
        ),
    )

    /** Runs [block] with [locale] as the JVM default, then puts it back. */
    private fun withLocale(locale: Locale, block: () -> Unit) {
        val previous = Locale.getDefault()
        Locale.setDefault(locale)
        try {
            block()
        } finally {
            Locale.setDefault(previous)
        }
    }

    // -- Contents -----------------------------------------------------------------

    @Test
    fun `every catalog term reaches the screen`() {
        // Grouping must not quietly drop an entry — a term that exists but is
        // unreachable is worse than one that was never written.
        val state = viewModel().uiState.value

        assertEquals(GlossaryCatalog.all.size, state.termCount)
    }

    @Test
    fun `browsing groups terms under letter headings`() {
        val state = viewModel().uiState.value

        assertTrue(state.sections.isNotEmpty())
        state.sections.forEach { section ->
            assertTrue("a browse section has no letter", section.letter.isNotBlank())
            assertTrue("${section.letter} is empty", section.terms.isNotEmpty())
        }
    }

    @Test
    fun `letters are ordered by the reader's alphabet, not by code point`() {
        val state = viewModel().uiState.value
        val letters = state.sections.map { it.letter }
        val collator = Collator.getInstance(Locale.getDefault())

        assertTrue("only one letter, so ordering proves nothing", letters.size > 5)
        assertEquals(letters.sortedWith(collator), letters)
    }

    @Test
    fun `Turkish letters sort where a Turkish reader expects them`() {
        // The case a code-point sort gets wrong: Ç belongs straight after C,
        // and Ş after S, not at the end of the alphabet past Z.
        withLocale(Locale.forLanguageTag("tr-TR")) {
            val ampacity = requireNotNull(GlossaryCatalog.termOrNull("ampacity"))
            val conductor = requireNotNull(GlossaryCatalog.termOrNull("conductor"))
            val efficiency = requireNotNull(GlossaryCatalog.termOrNull("efficiency"))

            val letters = viewModel(
                mapOf(
                    ampacity.term to "Cihaz",
                    conductor.term to "Çalışma",
                    efficiency.term to "Dağıtım",
                ),
            ).uiState.value.sections.map { it.letter }

            assertEquals(
                listOf("C", "Ç", "D"),
                letters.filter { it in listOf("C", "Ç", "D") },
            )
        }
    }

    @Test
    fun `a term is filed under the first letter of its own name`() {
        val state = viewModel().uiState.value

        state.sections.forEach { section ->
            section.terms.forEach { term ->
                val characters = BreakIterator.getCharacterInstance(Locale.getDefault())
                characters.setText(term.name)
                assertEquals(
                    "${term.name} is filed under ${section.letter}",
                    section.letter,
                    term.name.substring(0, characters.next()).uppercase(Locale.getDefault()),
                )
            }
        }
    }

    // -- The bilingual line ----------------------------------------------------------

    @Test
    fun `the English name is dropped when it repeats the localised one`() {
        // In an English build the two are the same words, and a second line
        // saying it again would be noise.
        val ampacity = requireNotNull(GlossaryCatalog.termOrNull("ampacity"))
        val model = viewModel(mapOf(ampacity.term to "Ampacity"))
            .uiState.value
            .sections
            .flatMap { it.terms }
            .single { it.key == "ampacity" }

        assertNull(model.englishName)
    }

    @Test
    fun `the English name is kept when it differs`() {
        val ampacity = requireNotNull(GlossaryCatalog.termOrNull("ampacity"))
        val model = viewModel(mapOf(ampacity.term to "Akım taşıma kapasitesi"))
            .uiState.value
            .sections
            .flatMap { it.terms }
            .single { it.key == "ampacity" }

        assertEquals("Ampacity", model.englishName)
    }

    // -- Search -------------------------------------------------------------------------

    @Test
    fun `searching drops the letter headings`() {
        // Results are ranked; letter headings would fight that order.
        val model = viewModel()
        model.onQueryChange("ampacity")

        val state = model.uiState.value
        assertTrue(state.isSearching)
        assertEquals(1, state.sections.size)
        assertEquals("", state.sections.single().letter)
    }

    @Test
    fun `an unmatched search reports no results rather than an empty screen`() {
        val model = viewModel()

        model.onQueryChange("qqqqzzzz")

        assertTrue(model.uiState.value.hasNoResults)
        assertTrue(model.uiState.value.sections.isEmpty())
    }

    @Test
    fun `clearing the query restores the full A to Z`() {
        val model = viewModel()
        val total = model.uiState.value.termCount
        model.onQueryChange("ampacity")

        model.onClearQuery()

        assertFalse(model.uiState.value.isSearching)
        assertEquals(total, model.uiState.value.termCount)
    }

    // -- Expansion ------------------------------------------------------------------------

    @Test
    fun `nothing is open to begin with`() {
        assertNull(viewModel().uiState.value.expandedKey)
    }

    @Test
    fun `opening a second term closes the first`() {
        // One at a time, so a 117-entry list stays navigable.
        val model = viewModel()

        model.onToggleExpanded("ampacity")
        model.onToggleExpanded("voltage_drop")

        assertEquals("voltage_drop", model.uiState.value.expandedKey)
    }

    @Test
    fun `tapping the open term closes it`() {
        val model = viewModel()
        model.onToggleExpanded("ampacity")

        model.onToggleExpanded("ampacity")

        assertNull(model.uiState.value.expandedKey)
    }

    @Test
    fun `a term the search has hidden does not stay open behind it`() {
        val model = viewModel()
        model.onToggleExpanded("ampacity")

        model.onQueryChange("qqqqzzzz")

        assertNull(model.uiState.value.expandedKey)
    }

    // -- Cross-references --------------------------------------------------------------------

    @Test
    fun `following a link opens that term and clears the search`() {
        // The link is only visible from inside another expanded term, so the
        // search that was covering the target has to come off with it.
        val model = viewModel()
        model.onQueryChange("ampacity")

        model.onFollowLink("derating")

        assertEquals("derating", model.uiState.value.expandedKey)
        assertFalse(model.uiState.value.isSearching)
    }

    @Test
    fun `related terms arrive named, not as keys`() {
        val derating = requireNotNull(GlossaryCatalog.termOrNull("derating"))
        val model = viewModel(mapOf(derating.term to "Kapasite düşürme"))
            .uiState.value
            .sections
            .flatMap { it.terms }
            .single { it.key == "ampacity" }

        val link = model.seeAlso.single { it.key == "derating" }
        assertEquals("Kapasite düşürme", link.name)
    }

    @Test
    fun `a term that has a calculator carries it through to the screen`() {
        val model = viewModel().uiState.value
            .sections
            .flatMap { it.terms }
            .single { it.key == "voltage_drop" }

        assertNotNull(model.calculator)
    }
}
