package com.kemalurekli.electricalcalculator.features.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.ui.model.CalculatorUiModel
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeScreen
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeUiState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Drives the stateless [HomeScreen] with fixed state, so these assertions cover
 * layout and interaction wiring without depending on the Hilt graph, the
 * database or the catalog.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val voltageDrop = CalculatorUiModel(
        id = CalculatorId.VOLTAGE_DROP,
        title = "Voltage Drop",
        description = "Volt drop and percentage over a cable run",
        icon = CalculatorIcon.VOLTAGE_DROP,
        category = CalculatorCategory.CABLE_AND_CONDUIT,
        isFavorite = true,
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
            "Favorites",
            "History",
            "Settings",
        ).forEach { title ->
            composeTestRule
                .onNodeWithContentDescription(title, substring = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun dashboardCardsAnnounceTitleAndSubtitleTogether() {
        setContent(HomeUiState(isLoading = false))

        composeTestRule
            .onNodeWithContentDescription("Settings. Theme, units and app information")
            .assertIsDisplayed()
    }

    @Test
    fun showsPinnedCalculatorsAboveTheDashboard() {
        setContent(
            HomeUiState(favorites = persistentListOf(voltageDrop), isLoading = false),
        )

        composeTestRule.onNodeWithText("Voltage Drop").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browse").assertIsDisplayed()
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
                searchResults = persistentListOf(voltageDrop),
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
            .onNodeWithContentDescription("Settings", substring = true)
            .performClick()

        assertEquals(Route.Settings, route)
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
    ) {
        composeTestRule.setContent {
            ElecToolkitTheme {
                HomeScreen(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onToggleFavorite = {},
                    onNavigate = onNavigate,
                    onCalculatorClick = onCalculatorClick,
                )
            }
        }
    }
}
