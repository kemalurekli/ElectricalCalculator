package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.runtime.Composable
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryRoute
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeRoute
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightRoute
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillRoute
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultRoute
import com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation.EnergyCostRoute
import com.kemalurekli.electricalcalculator.features.calculators.evse.presentation.EvseRoute
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation.HarmonicsRoute
import com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation.LightingRoute
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorRoute
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation.MotorStartingRoute
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation.NeutralCurrentRoute
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerRoute
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorRoute
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDetailRoute
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsRoute
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation.SelectivityRoute
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitRoute
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation.SolarStringRoute
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerRoute
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillRoute
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropRoute

/**
 * Opens the screen for [id].
 *
 * Each calculator gets its own screen as its phase lands. Until then the shared
 * detail screen renders the catalog entry with an in-development notice, so the
 * destination is never a dead end.
 *
 * This used to be a `when` inside `ElecNavHost`. It moved here because it is
 * knowledge about the calculators rather than about navigation — which screen
 * exists for which id — and because it is now read twice: once by the Android
 * navigation graph and once by the iOS shell, which has no graph yet.
 *
 * @param recordId a saved calculation to reopen, or null for a blank form.
 */
@Composable
fun CalculatorDestination(
    id: CalculatorId?,
    recordId: Long?,
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
        when (id) {
        CalculatorId.VOLTAGE_DROP ->
            VoltageDropRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.CABLE_SIZE ->
            CableSizeRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.TRANSFORMER_CURRENT ->
            TransformerRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.MOTOR_CURRENT ->
            MotorRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.POWER ->
            PowerRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.POWER_FACTOR_CORRECTION ->
            PowerFactorRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.BATTERY_RUNTIME ->
            BatteryRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.CABLE_WEIGHT ->
            CableWeightRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.CONDUIT_FILL ->
            ConduitFillRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.CABLE_TRAY_FILL ->
            TrayFillRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.SHORT_CIRCUIT ->
            ShortCircuitRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.EARTH_FAULT_LOOP ->
            EarthFaultRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.LIGHTING_LUMEN ->
            LightingRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.SOLAR_STRING ->
            SolarStringRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.NEUTRAL_CURRENT ->
            NeutralCurrentRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.ENERGY_COST ->
            EnergyCostRoute(
                onReferenceClick = onReferenceClick,
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.SELECTIVITY ->
            SelectivityRoute(
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.MOTOR_STARTING ->
            MotorStartingRoute(
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.HARMONICS ->
            HarmonicsRoute(
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        CalculatorId.EVSE ->
            EvseRoute(
                onNavigateBack = onNavigateBack,
                recordId = recordId,
            )

        else -> CalculatorDetailRoute(onNavigateBack = onNavigateBack)
    }
}
