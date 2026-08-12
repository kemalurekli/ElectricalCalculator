package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
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
 *
 * ### On the locale
 *
 * Expected text is resolved from resources in [Strings], captured from the same
 * [LocalContext] the screen composes with, rather than written out in English.
 * The app carries a per-app language the user can change and that persists in
 * app storage, so literals made this suite pass or fail on a preference rather
 * than on the code: it asserted "System voltage" against a screen correctly
 * reading "Sebeke gerilimi".
 *
 * Numbers go through [NumberFormatter], the formatter the screen itself uses,
 * for the same reason — the decimal separator follows the locale, so the drop
 * is "5.17" in English and "5,17" in Turkish.
 */
class VoltageDropScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val calculate = CalculateVoltageDropUseCase()

    private lateinit var strings: Strings

    private class Strings(private val context: Context) {
        val systemVoltage: String = context.getString(R.string.common_system_voltage)
        val designCurrent: String = context.getString(R.string.common_design_current)
        val powerFactor: String = context.getString(R.string.common_power_factor)
        val calculate: String = context.getString(R.string.action_calculate)
        val copy: String = context.getString(R.string.action_copy)
        val share: String = context.getString(R.string.action_share)
        val formulaHeading: String = context.getString(R.string.calculator_formula)
        val notesHeading: String = context.getString(R.string.calculator_notes)
        val reactanceNote: String = context.getString(R.string.vd_note_reactance)
        val mustBePositive: String = context.getString(R.string.validation_must_be_positive)
        val resultLabel: String = context.getString(R.string.vd_result_label)
        val voltageAtLoad: String = context.getString(R.string.vd_result_voltage_at_load)

        /** The symbols are identical in every locale; the first line is enough. */
        val formulaFirstLine: String =
            context.getString(R.string.vd_formula).substringBefore('\n')

        fun withinLightingLimit(percentage: String): String =
            context.getString(R.string.vd_status_within_lighting, percentage)
    }

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

        composeTestRule.onNodeWithText(strings.systemVoltage).assertIsDisplayed()
        composeTestRule.onNodeWithText(strings.designCurrent).assertIsDisplayed()

        scrollTo(strings.calculate)
        composeTestRule.onNodeWithText(strings.calculate).assertIsDisplayed()
    }

    @Test
    fun powerFactorIsHiddenOnDc() {
        setContent(VoltageDropUiState(system = SupplySystem.DC))

        composeTestRule.onNodeWithText(strings.powerFactor).assertDoesNotExist()
    }

    @Test
    fun powerFactorIsShownOnAc() {
        setContent(VoltageDropUiState(system = SupplySystem.SINGLE_PHASE_AC))

        scrollTo(strings.powerFactor)
        composeTestRule.onNodeWithText(strings.powerFactor).assertIsDisplayed()
    }

    @Test
    fun validationErrorsAreShownAgainstTheirField() {
        setContent(
            VoltageDropUiState(
                errors = mapOf(VoltageDropField.CROSS_SECTION to ValidationError.MustBePositive),
            ),
        )

        scrollTo(strings.mustBePositive)
        composeTestRule.onNodeWithText(strings.mustBePositive).assertIsDisplayed()
    }

    @Test
    fun theResultIsRenderedWithItsUnitAndStatus() {
        val state = resultState()
        setContent(state)

        // 230 V / 20 A / 30 m / 4 mm² copper at 20 °C -> 5.17 V, 2.25 %
        // Composed exactly as the screen composes it, formatter included.
        val result = requireNotNull(state.result)
        val drop = NumberFormatter.format(result.voltageDrop, decimals = 2)
        val percentage = NumberFormatter.format(result.dropPercentage, decimals = 2)

        composeTestRule
            .onNodeWithContentDescription("${strings.resultLabel}: $drop V", substring = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(
                strings.withinLightingLimit(percentage),
                substring = true,
            )
            .assertIsDisplayed()
    }

    @Test
    fun secondaryResultsAreAnnouncedWithTheHeadline() {
        setContent(resultState())

        composeTestRule
            .onNodeWithContentDescription(strings.voltageAtLoad, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun copyAndShareAreHiddenBeforeAResultExists() {
        setContent(VoltageDropUiState())

        // Offering them with nothing to copy would be a dead action.
        composeTestRule.onNodeWithText(strings.copy).assertDoesNotExist()
        composeTestRule.onNodeWithText(strings.share).assertDoesNotExist()
    }

    @Test
    fun copyAndShareAppearWithAResult() {
        setContent(resultState())

        composeTestRule.onNodeWithText(strings.copy).assertIsDisplayed()
        composeTestRule.onNodeWithText(strings.share).assertIsDisplayed()
    }

    @Test
    fun theFormulaExpandsOnTap() {
        setContent(VoltageDropUiState())

        composeTestRule.onNodeWithText(strings.formulaFirstLine, substring = true)
            .assertDoesNotExist()

        scrollTo(strings.formulaHeading)
        composeTestRule.onNodeWithText(strings.formulaHeading).performClick()

        composeTestRule.onNodeWithText(strings.formulaFirstLine, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun engineeringNotesExpandOnTap() {
        setContent(VoltageDropUiState())

        scrollTo(strings.notesHeading)
        composeTestRule.onNodeWithText(strings.notesHeading).performClick()

        scrollTo(strings.reactanceNote)
        composeTestRule
            .onNodeWithText(strings.reactanceNote, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun editingAFieldReportsTheNewValue() {
        var voltage = ""
        setContent(VoltageDropUiState(), onVoltageChange = { voltage = it })

        composeTestRule.onNodeWithText(strings.systemVoltage).performTextReplacement("400")

        assertEquals("400", voltage)
    }

    @Test
    fun calculateReportsTheAction() {
        var calculated = false
        setContent(VoltageDropUiState(), onCalculate = { calculated = true })

        scrollTo(strings.calculate)
        composeTestRule.onNodeWithText(strings.calculate).performClick()

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
            strings = Strings(LocalContext.current)
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
