package com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain

import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Works out how many modules may go in series on one string.
 *
 * ### Method
 *
 * A module's datasheet voltages are quoted at 25 °C. Correcting them to the
 * temperatures the array actually reaches:
 *
 * ```
 * Voc(T) = Voc_STC · (1 + β · (T − 25) / 100)
 * Vmp(T) = Vmp_STC · (1 + β · (T − 25) / 100)
 * ```
 *
 * with `β` negative, so a cold module produces **more** voltage than its
 * nameplate and a hot one less. Then:
 *
 * ```
 * n_max = ⌊ V_inverter_max / Voc(T_min) ⌋
 * n_min = ⌈ V_mppt_min    / Vmp(T_max) ⌉
 * ```
 *
 * ### Why the cold case is the dangerous one
 *
 * The maximum is a destruction limit, not a performance one. A string sized on
 * the nameplate 25 °C figure will exceed the inverter's DC maximum on the first
 * cold clear morning — open circuit, before the inverter has started, when the
 * array is at ambient and the sun is already on it. The equipment is damaged
 * before it has generated anything.
 *
 * The minimum is only a production limit: fall under the MPPT window on a hot
 * afternoon and the array stops earning, which is expensive but not destructive.
 * Both are reported, and the cold one is the one to get right.
 *
 * ### Assumptions
 *
 * Cell temperature is taken as given rather than derived from air temperature —
 * the relationship depends on mounting, ventilation and irradiance, and guessing
 * it here would put a large unstated approximation inside a limit that has to be
 * respected exactly.
 */
class CalculateSolarStringUseCase @Inject constructor() {

    operator fun invoke(input: SolarStringInput): SolarStringResult {
        val vocCold = corrected(
            input.moduleVocVolts,
            input.voltageCoefficientPercentPerK,
            input.minimumCellTemperatureC,
        )
        val vmpHot = corrected(
            input.moduleVmpVolts,
            input.voltageCoefficientPercentPerK,
            input.maximumCellTemperatureC,
        )

        // Floor: one module too many is the damaging direction.
        val maxModules = if (vocCold > 0.0) {
            floor(input.inverterMaxDcVolts / vocCold).toInt().coerceAtLeast(0)
        } else {
            0
        }

        // Ceiling: one too few leaves the string below the tracking window.
        val minModules = if (vmpHot > 0.0) {
            ceil(input.inverterMpptMinVolts / vmpHot).toInt().coerceAtLeast(1)
        } else {
            0
        }

        return SolarStringResult(
            vocAtMinimumTemperature = vocCold,
            vmpAtMaximumTemperature = vmpHot,
            maximumModules = maxModules,
            minimumModules = minModules,
            stringVocAtMaximum = maxModules * vocCold,
            stringVmpAtMinimum = minModules * vmpHot,
        )
    }

    /** A datasheet voltage moved from 25 °C to [temperatureC]. */
    private fun corrected(
        voltageAtStc: Double,
        coefficientPercentPerK: Double,
        temperatureC: Double,
    ): Double = voltageAtStc *
        (1.0 + coefficientPercentPerK * (temperatureC - SolarStringInput.STC_TEMPERATURE_C) / PERCENT)

    private companion object {
        const val PERCENT = 100.0
    }
}
