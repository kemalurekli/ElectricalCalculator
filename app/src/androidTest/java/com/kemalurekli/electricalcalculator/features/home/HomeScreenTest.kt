package com.kemalurekli.electricalcalculator.features.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import android.content.Context
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.home.domain.SearchKind
import com.kemalurekli.electricalcalculator.features.home.domain.SearchableItem
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorUiModel
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeScreen
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeUiState
import com.kemalurekli.electricalcalculator.features.home.presentation.SearchSection
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant
import com.kemalurekli.electricalcalculator.testing.moduleString
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.search_hint
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.Res as NavigationRes
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_calculators_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_converter_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_converter_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_favorites_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_field_notes_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_glossary_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_history_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_references_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_settings_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_settings_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_theory_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res as CalculatorsRes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.state_empty_calculators_title
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.Res as HomeRes
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.home_browse

/**
 * Drives the stateless [HomeScreen] with fixed state, so these assertions cover
 * layout and interaction wiring without depending on the Hilt graph, the
 * database or the catalog.
 *
 * ### On the locale
 *
 * Expected text is read from resources in [Strings] rather than written out as
 * English literals. The app carries a per-app language the user can change, and
 * it persists in app storage, so a suite that hard-codes one language fails
 * against a screen that is perfectly correct — which is exactly what these
 * tests did. Resolving through the same [LocalContext] the screen composes with
 * makes them assert the wiring, in whatever language the device is in.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Captured from inside the composition, so it is the very same resource
     * table the screen under test rendered from.
     */
    private lateinit var strings: Strings

    private class Strings(context: Context) {
        val searchHint: String = moduleString(DesignSystemRes.string.search_hint)
        val browse: String = moduleString(HomeRes.string.home_browse)
        val noMatches: String = moduleString(CalculatorsRes.string.state_empty_calculators_title)

        val calculators: String = moduleString(NavigationRes.string.dashboard_calculators_title)
        val converter: String = moduleString(NavigationRes.string.dashboard_converter_title)
        val references: String = moduleString(NavigationRes.string.dashboard_references_title)
        val glossary: String = moduleString(NavigationRes.string.dashboard_glossary_title)
        val favorites: String = moduleString(NavigationRes.string.dashboard_favorites_title)
        val history: String = moduleString(NavigationRes.string.dashboard_history_title)
        val settings: String = moduleString(NavigationRes.string.dashboard_settings_title)
        val fieldNotes: String = moduleString(NavigationRes.string.dashboard_field_notes_title)
        val theory: String = moduleString(NavigationRes.string.dashboard_theory_title)

        /** A card announces itself as "title. subtitle" in one merged node. */
        val converterCard: String =
            converter + ". " + moduleString(NavigationRes.string.dashboard_converter_subtitle)
        val settingsCard: String =
            settings + ". " + moduleString(NavigationRes.string.dashboard_settings_subtitle)
    }

    private val voltageDrop = CalculatorUiModel(
        id = CalculatorId.VOLTAGE_DROP,
        title = "Voltage Drop",
        description = "Volt drop and percentage over a cable run",
        icon = CalculatorIcon.VOLTAGE_DROP,
        category = CalculatorCategory.CABLE_AND_CONDUIT,
        isFavorite = true,
    )

    private val voltageDropHit = SearchableItem(
        kind = SearchKind.CALCULATOR,
        key = CalculatorId.VOLTAGE_DROP.key,
        title = "Voltage Drop",
        subtitle = "Volt drop and percentage over a cable run",
    )

    /**
     * Dashboard cards expose one merged accessibility node reading
     * "title. subtitle" rather than separate text nodes, so they are asserted
     * by content description. That also makes these tests verify the
     * screen-reader contract, not just that pixels were drawn.
     */
    /**
     * The dashboard shows what is not a tab, and nothing that is.
     *
     * Calculators, Projects and Forum used to have cards here, back when the
     * dashboard was the only way to reach anything. They are tabs now, and a
     * card directly above the tab that opens the same room is a second door
     * into it — so their absence is the assertion, not an omission.
     */
    @Test
    fun showsEveryDashboardCard() {
        setContent(HomeUiState(isLoading = false))

        listOf(
            strings.converter,
            strings.references,
            strings.glossary,
            strings.favorites,
            strings.history,
            strings.theory,
            strings.fieldNotes,
        ).forEach { title ->
            composeTestRule
                .onNodeWithContentDescription(title, substring = true)
                .performScrollTo()
                .assertIsDisplayed()
        }

        composeTestRule
            .onAllNodesWithContentDescription(strings.calculators, substring = true)
            .assertCountEquals(0)
    }

    @Test
    fun dashboardCardsAnnounceTitleAndSubtitleTogether() {
        setContent(HomeUiState(isLoading = false))

        composeTestRule
            .onNodeWithContentDescription(strings.converterCard)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Settings is reached from the top bar rather than from a card. Asserting
     * its absence from the grid is what stops it drifting back in and stranding
     * a seventh card on a row of its own.
     */
    @Test
    fun settingsIsNotADashboardCard() {
        setContent(HomeUiState(isLoading = false))

        composeTestRule
            .onNodeWithContentDescription(strings.settingsCard)
            .assertDoesNotExist()
    }

    /**
     * The two reading shelves sit at the end of the grid rather than as bands
     * beneath it, which is what an even number of cards buys. Asserted by route
     * rather than only by sight, because a card that renders and navigates
     * nowhere looks identical in a screenshot.
     */
    @Test
    fun tappingTheTheoryCardReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule
            .onNodeWithContentDescription(strings.theory, substring = true)
            .performScrollTo()
            .performClick()

        assertEquals(Route.Theory, route)
    }

    @Test
    fun tappingTheFieldNotesCardReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule
            .onNodeWithContentDescription(strings.fieldNotes, substring = true)
            .performScrollTo()
            .performClick()

        assertEquals(Route.FieldNotes(), route)
    }

    @Test
    fun tappingTheTopBarSettingsActionReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule.onNodeWithContentDescription(strings.settings).performClick()

        assertEquals(Route.Settings, route)
    }

    @Test
    fun showsPinnedCalculatorsAboveTheDashboard() {
        setContent(
            HomeUiState(favorites = persistentListOf(voltageDrop), isLoading = false),
        )

        composeTestRule.onNodeWithText("Voltage Drop").assertIsDisplayed()
        composeTestRule.onNodeWithText(strings.browse).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun hidesThePinnedSectionWhenNothingIsPinned() {
        setContent(HomeUiState(isLoading = false))

        composeTestRule.onNodeWithText("Voltage Drop").assertDoesNotExist()
    }

    @Test
    fun searchResultsReplaceTheDashboard() {
        setContent(
            HomeUiState(
                query = "voltage",
                searchResults = persistentListOf(
                    SearchSection(
                        kind = SearchKind.CALCULATOR,
                        hits = persistentListOf(voltageDropHit),
                    ),
                ),
                isLoading = false,
            ),
        )

        composeTestRule.onNodeWithText("Voltage Drop").assertIsDisplayed()
        // Dashboard cards are not rendered while a search is active.
        composeTestRule
            .onNodeWithContentDescription(strings.converter, substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun searchHitsReportTheItemTheyCameFrom() {
        var opened: SearchableItem? = null
        setContent(
            HomeUiState(
                query = "voltage",
                searchResults = persistentListOf(
                    SearchSection(
                        kind = SearchKind.CALCULATOR,
                        hits = persistentListOf(voltageDropHit),
                    ),
                ),
                isLoading = false,
            ),
            onOpenSearchHit = { opened = it },
        )

        composeTestRule.onNodeWithText("Voltage Drop").performClick()

        assertEquals(voltageDropHit, opened)
    }

    @Test
    fun showsAnEmptyStateWhenNothingMatches() {
        setContent(HomeUiState(query = "zzz", isLoading = false))

        // The empty state is also one merged node ("title. message").
        composeTestRule
            .onNodeWithContentDescription(strings.noMatches, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun typingInSearchReportsTheQuery() {
        var query = ""
        setContent(HomeUiState(isLoading = false), onQueryChange = { query = it })

        composeTestRule
            .onNodeWithText(strings.searchHint)
            .performTextInput("volt")

        assertEquals("volt", query)
    }

    @Test
    fun tappingADashboardCardReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule
            .onNodeWithContentDescription(strings.converter, substring = true)
            .performScrollTo()
            .performClick()

        assertEquals(Route.Converter, route)
    }

    @Test
    fun tappingAPinnedCalculatorReportsItsId() {
        var clicked: CalculatorId? = null
        setContent(
            HomeUiState(favorites = persistentListOf(voltageDrop), isLoading = false),
            onCalculatorClick = { clicked = it },
        )

        composeTestRule.onNodeWithText("Voltage Drop").performClick()

        assertEquals(CalculatorId.VOLTAGE_DROP, clicked)
    }

    /**
     * The row reports the whole record, not just which calculator it was.
     *
     * It used to call `onCalculatorClick(record.calculatorId)`, which has
     * nowhere to put the row's id — so tapping a past calculation opened an
     * empty form of the right calculator. Nothing failed and nothing was
     * logged; the screen just quietly forgot which calculation you asked for.
     * The identical row on the History screen always passed the id, which is
     * what made the difference so easy to miss.
     */
    @Test
    fun tappingARecentCalculationReportsTheWholeRecord() {
        var opened: CalculationRecord? = null
        setContent(
            HomeUiState(recent = persistentListOf(recentRecord), isLoading = false),
            onOpenRecord = { opened = it },
        )

        // By description, not by text: the row collapses to a single
        // accessibility node reading "title. summary. timestamp", so a
        // screen-reader user hears one target instead of three fragments —
        // which also means its title is not a node of its own to click.
        composeTestRule
            .onNodeWithContentDescription(recentRecord.title, substring = true)
            .performScrollTo()
            .performClick()

        assertEquals(recentRecord.id, opened?.id)
        assertEquals(CalculatorId.VOLTAGE_DROP, opened?.calculatorId)
    }

    private val recentRecord = CalculationRecord(
        id = 42L,
        calculatorId = CalculatorId.VOLTAGE_DROP,
        title = "Voltage drop — 4 mm², 30 m",
        summary = "5.36 V",
        inputs = mapOf("length" to "30"),
        results = mapOf("drop" to "5.36"),
        createdAt = Instant.parse("2026-08-01T12:00:00Z"),
    )

    private fun setContent(
        uiState: HomeUiState,
        onQueryChange: (String) -> Unit = {},
        onNavigate: (Route) -> Unit = {},
        onCalculatorClick: (CalculatorId) -> Unit = {},
        onOpenRecord: (CalculationRecord) -> Unit = {},
        onOpenSearchHit: (SearchableItem) -> Unit = {},
    ) {
        composeTestRule.setContent {
            strings = Strings(LocalContext.current)
            ElecToolkitTheme {
                HomeScreen(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onToggleFavorite = {},
                    onNavigate = onNavigate,
                    onCalculatorClick = onCalculatorClick,
                    onOpenRecord = onOpenRecord,
                    onOpenSearchHit = onOpenSearchHit,
            hasSettings = true,
                )
            }
        }
    }
}
