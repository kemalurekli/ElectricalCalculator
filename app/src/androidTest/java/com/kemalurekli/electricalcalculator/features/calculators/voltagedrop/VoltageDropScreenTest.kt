package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropField
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropScreen
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Drives the stateless screen with fixed state. Calculation correctness is
 * covered by the use-case tests; these assertions cover the form wiring, the
 * error surface and the result presentation.
 */
class VoltageDropScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val calculate = CalculateVoltageDropUseCase()

    private fun resultState() = VoltageDropUiState(
        voltage = "230",
        current = "20",
        length = "30",
        crossSection = "4",
        temperature = "20",
        result = calculate(
            VoltageDropInput(
                systemVoltage = 230.0,
                loadCurrent = 20.0,
                lengthMetres = 30.0,
                crossSectionMm2 = 4.0,
                material = ConductorMaterial.COPPER,
                system = SupplySystem.SINGLE_PHASE_AC,
                powerFactor = 1.0,
                conductorTemperatureC = 20.0,
                parallelConductors = 1,
            ),
        ),
    )

    @Test
    fun showsTheInputFormByDefault() {
        setContent(VoltageDropUiState())

        composeTestRule.onNodeWithText("System voltage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Design current").assertIsDisplayed()

        scrollTo("Calculate")
        composeTestRule.onNodeWithText("Calculate").assertIsDisplayed()
    }

    @Test
    fun powerFactorIsHiddenOnDc() {
        setContent(VoltageDropUiState(system = SupplySystem.DC))

        composeTestRule.onNodeWithText("Power factor (cos φ)").assertDoesNotExist()
    }

    @Test
    fun powerFactorIsShownOnAc() {
        setContent(VoltageDropUiState(system = SupplySystem.SINGLE_PHASE_AC))

        scrollTo("Power factor (cos φ)")
        composeTestRule.onNodeWithText("Power factor (cos φ)").assertIsDisplayed()
    }

    @Test
    fun validationErrorsAreShownAgainstTheirField() {
        setContent(
            VoltageDropUiState(
                errors = mapOf(VoltageDropField.CROSS_SECTION to ValidationError.MustBePositive),
            ),
        )

        scrollTo("Enter a value greater than zero")
        composeTestRule.onNodeWithText("Enter a value greater than zero").assertIsDisplayed()
    }

    @Test
    fun theResultIsRenderedWithItsUnitAndStatus() {
        setContent(resultState())

        // 230 V / 20 A / 30 m / 4 mm² copper at 20 °C -> 5.17 V, 2.25 %
        composeTestRule
            .onNodeWithContentDescription("Voltage drop: 5.17 V", substring = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("within the 3 % lighting limit", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun secondaryResultsAreAnnouncedWithTheHeadline() {
        setContent(resultState())

        composeTestRule
            .onNodeWithContentDescription("Voltage at load", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun copyAndShareAreHiddenBeforeAResultExists() {
        setContent(VoltageDropUiState())

        // Offering them with nothing to copy would be a dead action.
        composeTestRule.onNodeWithText("Copy").assertDoesNotExist()
        composeTestRule.onNodeWithText("Share").assertDoesNotExist()
    }

    @Test
    fun copyAndShareAppearWithAResult() {
        setContent(resultState())

        composeTestRule.onNodeWithText("Copy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share").assertIsDisplayed()
    }

    @Test
    fun theFormulaExpandsOnTap() {
        setContent(VoltageDropUiState())

        composeTestRule.onNodeWithText("R = ρ(θ) · L / (A · n)", substring = true)
            .assertDoesNotExist()

        scrollTo("Formula")
        composeTestRule.onNodeWithText("Formula").performClick()

        composeTestRule.onNodeWithText("R = ρ(θ) · L / (A · n)", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun engineeringNotesExpandOnTap() {
        setContent(VoltageDropUiState())

        scrollTo("Engineering notes")
        composeTestRule.onNodeWithText("Engineering notes").performClick()

        scrollTo("Conductor reactance is neglected")
        composeTestRule
            .onNodeWithText("Conductor reactance is neglected", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun editingAFieldReportsTheNewValue() {
        var voltage = ""
        setContent(VoltageDropUiState(), onVoltageChange = { voltage = it })

        composeTestRule.onNodeWithText("System voltage").performTextReplacement("400")

        assertEquals("400", voltage)
    }

    @Test
    fun calculateReportsTheAction() {
        var calculated = false
        setContent(VoltageDropUiState(), onCalculate = { calculated = true })

        scrollTo("Calculate")
        composeTestRule.onNodeWithText("Calculate").performClick()

        assertEquals(true, calculated)
    }

    /**
     * Scrolls the form until [text] is composed.
     *
     * A LazyColumn does not compose off-screen items, so `performScrollTo` on
     * the target node cannot work — the node does not exist yet. The list has
     * to be scrolled instead.
     */
    private fun scrollTo(text: String) {
        composeTestRule
            .onNodeWithTag(ElecTestTags.CALCULATOR_FORM)
            .performScrollToNode(hasText(text, substring = true))
    }

    private fun setContent(
        uiState: VoltageDropUiState,
        onVoltageChange: (String) -> Unit = {},
        onCalculate: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            ElecToolkitTheme {
                VoltageDropScreen(
                    uiState = uiState,
                    onSystemChange = {},
                    onMaterialChange = {},
                    onVoltageChange = onVoltageChange,
                    onCurrentChange = {},
                    onLengthChange = {},
                    onCrossSectionChange = {},
                    onPowerFactorChange = {},
                    onTemperatureChange = {},
                    onParallelConductorsChange = {},
                    onCalculate = onCalculate,
                    onReferenceClick = {},
                    onApplyExample = {},
                    onReset = {},
                    onToggleFavorite = {},
                    onCopy = {},
                    onShare = {},
                    onNavigateBack = {},
                )
            }
        }
    }
}
