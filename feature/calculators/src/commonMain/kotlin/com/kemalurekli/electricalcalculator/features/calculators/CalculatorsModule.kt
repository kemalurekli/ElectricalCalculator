package com.kemalurekli.electricalcalculator.features.calculators

import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.CalculateEnergyCostUseCase
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.CalculateEvseUseCase
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.CalculateHarmonicsUseCase
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.CalculateLightingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.CalculateMotorStartingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.CalculateNeutralCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.CheckSelectivityUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.CalculateSolarStringUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryViewModel
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeViewModel
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightViewModel
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillViewModel
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultViewModel
import com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation.EnergyCostViewModel
import com.kemalurekli.electricalcalculator.features.calculators.evse.presentation.EvseViewModel
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation.HarmonicsViewModel
import com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation.LightingViewModel
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorViewModel
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation.MotorStartingViewModel
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation.NeutralCurrentViewModel
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerViewModel
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorViewModel
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDetailViewModel
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsViewModel
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation.SelectivityViewModel
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitViewModel
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation.SolarStringViewModel
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerViewModel
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillViewModel
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Twenty-one calculators, the catalogue that lists them, and what they
 * compute with.
 *
 * The longest module in the app, and the clearest measure of what replacing
 * Hilt costs: every line here was an `@Inject constructor` that Dagger read
 * and wrote this file from. Koin has no compiler plugin, so it is written
 * out — once, mechanically, and touched again only when a calculator gains
 * a dependency that is not yet registered.
 *
 * `viewModelOf` and `singleOf` take a constructor reference and resolve its
 * parameters from the graph, so adding a parameter to an existing class
 * needs no edit here. A class *missing* from this file is not a compile
 * error — it is a `NoDefinitionFoundException` the first time its screen
 * opens, which is how the four list-and-detail screens announced that they
 * had arrived after the file was generated.
 */
val calculatorsModule: Module = module {
    singleOf(::CalculateBatteryRuntimeUseCase)
    singleOf(::AmpacityTable)
    singleOf(::CalculateCableSizeUseCase)
    singleOf(::CalculateCableWeightUseCase)
    singleOf(::CalculateConduitFillUseCase)
    singleOf(::CalculatorCatalog)
    singleOf(::CalculateEarthFaultUseCase)
    singleOf(::CalculateEnergyCostUseCase)
    singleOf(::CalculateEvseUseCase)
    singleOf(::CalculateHarmonicsUseCase)
    singleOf(::CalculateLightingUseCase)
    singleOf(::CalculateMotorCurrentUseCase)
    singleOf(::CalculateMotorStartingUseCase)
    singleOf(::CalculateNeutralCurrentUseCase)
    singleOf(::CalculatePowerUseCase)
    singleOf(::CalculatePowerFactorCorrectionUseCase)
    singleOf(::CheckSelectivityUseCase)
    singleOf(::CalculateShortCircuitUseCase)
    singleOf(::CalculateSolarStringUseCase)
    singleOf(::CalculateTransformerCurrentUseCase)
    singleOf(::CalculateTrayFillUseCase)
    singleOf(::CalculateVoltageDropUseCase)

    viewModelOf(::BatteryViewModel)
    viewModelOf(::CableSizeViewModel)
    viewModelOf(::CableWeightViewModel)
    viewModelOf(::ConduitFillViewModel)
    viewModelOf(::EarthFaultViewModel)
    viewModelOf(::EnergyCostViewModel)
    viewModelOf(::EvseViewModel)
    viewModelOf(::HarmonicsViewModel)
    viewModelOf(::LightingViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::MotorStartingViewModel)
    viewModelOf(::NeutralCurrentViewModel)
    viewModelOf(::PowerViewModel)
    viewModelOf(::PowerFactorViewModel)
    viewModelOf(::CalculatorDetailViewModel)
    viewModelOf(::CalculatorsViewModel)
    viewModelOf(::SelectivityViewModel)
    viewModelOf(::ShortCircuitViewModel)
    viewModelOf(::SolarStringViewModel)
    viewModelOf(::TransformerViewModel)
    viewModelOf(::TrayFillViewModel)
    viewModelOf(::VoltageDropViewModel)
}
