package com.kemalurekli.electricalcalculator.features.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.search.SearchKind
import com.kemalurekli.electricalcalculator.core.domain.search.SearchableItem
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.ui.model.CalculatorUiModel
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeScreen
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeUiState
import com.kemalurekli.electricalcalculator.features.home.presentation.SearchSection
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Drives the stateless [HomeScreen] with fixed state, so these assertions cover
 * layout and interaction wiring without depending on the Hilt graph, the
 * database or the catalog.
 *
 * ### On the locale
 *
 * The assertions below name English strings, and the app carries a per-app
 * language the user can change — which persists in app storage and survives a
 * reinstall. Left alone, the suite renders in whatever language the device last
 * ran the app in and fails against a screen that is perfectly correct. So the
 * locale is pinned in [pinLocale] rather than assumed.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun pinLocale() {
        // Must run on the main thread: AppCompatDelegate touches the active
        // configuration.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
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
    @Test
    fun showsEveryDashboardCard() {
        setContent(HomeUiState(isLoading = false))

        listOf(
            "Electrical Calculators",
            "Unit Converter",
            "Electrical References",
            "Glossary",
            "Favorites",
            "History",
        ).forEach { title ->
            composeTestRule
                .onNodeWithContentDescription(title, substring = true)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun dashboardCardsAnnounceTitleAndSubtitleTogether() {
        setContent(HomeUiState(isLoading = false))

        composeTestRule
            .onNodeWithContentDescription("Unit Converter. Electrical, physical and AWG conversions")
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
            .onNodeWithContentDescription("Settings. Theme, units and app information")
            .assertDoesNotExist()
    }

    @Test
    fun tappingTheTopBarSettingsActionReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        assertEquals(Route.Settings, route)
    }

    @Test
    fun showsPinnedCalculatorsAboveTheDashboard() {
        setContent(
            HomeUiState(favorites = persistentListOf(voltageDrop), isLoading = false),
        )

        composeTestRule.onNodeWithText("Voltage Drop").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browse").performScrollTo().assertIsDisplayed()
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
            .onNodeWithContentDescription("Unit Converter", substring = true)
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
            .onNodeWithContentDescription("No matches", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun typingInSearchReportsTheQuery() {
        var query = ""
        setContent(HomeUiState(isLoading = false), onQueryChange = { query = it })

        composeTestRule
            .onNodeWithText("Search calculators, converters, references")
            .performTextInput("volt")

        assertEquals("volt", query)
    }

    @Test
    fun tappingADashboardCardReportsItsRoute() {
        var route: Route? = null
        setContent(HomeUiState(isLoading = false), onNavigate = { route = it })

        composeTestRule
            .onNodeWithContentDescription("Unit Converter", substring = true)
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

    private fun setContent(
        uiState: HomeUiState,
        onQueryChange: (String) -> Unit = {},
        onNavigate: (Route) -> Unit = {},
        onCalculatorClick: (CalculatorId) -> Unit = {},
        onOpenSearchHit: (SearchableItem) -> Unit = {},
    ) {
        composeTestRule.setContent {
            ElecToolkitTheme {
                HomeScreen(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onToggleFavorite = {},
                    onNavigate = onNavigate,
                    onCalculatorClick = onCalculatorClick,
                    onOpenSearchHit = onOpenSearchHit,
                )
            }
        }
    }
}
