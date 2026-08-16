package com.kemalurekli.electricalcalculator.features.theory.domain

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial

/**
 * The arithmetic behind every theory topic.
 *
 * ### Why there is no generator for this shelf
 *
 * `gen_field_notes.py` and `gen_glossary.py` exist because those two catalogs are
 * flat tables: a key, a category and two strings, repeated ninety times with no
 * code in them. Writing that by hand is transcription, and transcription is what
 * a generator is for.
 *
 * A theory topic is not that. Every one carries a solver — real Kotlin, with
 * branches for optional components and a derivation whose steps were chosen —
 * plus per-field validation bounds and a diagram. Strip those out and the part a
 * table could hold is a handful of identifiers.
 *
 * The risk the generator was meant to remove is also not present here. A
 * mistyped `R.string.th_…` does not compile; a missing Turkish translation fails
 * `StringResourceIntegrityTest`; a topic with no assumptions, an example that
 * half-fills a form, or a level with nothing in it fails `TheoryCatalogTest`. So
 * the price of a generated catalog — a file nobody may edit, kept in step with a
 * script, for a shelf still being added to — would buy nothing.
 *
 * ### Rules every solver in this file follows
 *
 * - **Pure and total over validated inputs.** Bounds are declared on
 *   [TheoryField], so a solver never re-checks for zero or negative values; it
 *   would be checking something the view model has already refused.
 * - **No locale, no Android types, no formatting.** A step carries numbers and a
 *   slotted sentence; `TheoryFormatting.kt` is the only place a `Locale` enters.
 *   See [TheoryStep] for why that split is worth the indirection.
 * - **A step per idea, not per operation.** Squaring a number is not a step.
 *   Rearranging a formula is.
 * - **Optional fields are read with [TheoryInputs.getOrNull]** and change the
 *   number of steps rather than contributing a zero. A derivation that says
 *   "+ 0 Ω" for a resistor that is not in the circuit implies a component that
 *   is not there.
 */

// ---- Foundation: Ohm's law ------------------------------------------------

/** U = I · R, with the power the circuit dissipates as a by-product. */
internal fun ohmForVoltage(input: TheoryInputs): TheorySolutionResult {
    val i = input["i"]
    val r = input["r"]
    val u = i * r
    return TheorySolutionResult(
        primary = q(R.string.th_ohm_law_out_voltage, u, "V"),
        secondary = listOf(q(R.string.th_ohm_law_out_power, u * i, "W")),
        steps = listOf(
            step(
                R.string.th_ohm_law_step_voltage, "U = I · R", "{0} × {1}",
                n(i, "A"), n(r, "Ω"), result = n(u, "V"),
            ),
            step(
                R.string.th_ohm_law_step_power, "P = U · I", "{0} × {1}",
                n(u, "V"), n(i, "A"), result = n(u * i, "W"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to n(r, "Ω"), "i" to n(i, "A")),
    )
}

/** I = U / R — the rearrangement a fault-finding meter reading calls for. */
internal fun ohmForCurrent(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val r = input["r"]
    val i = u / r
    return TheorySolutionResult(
        primary = q(R.string.th_ohm_law_out_current, i, "A"),
        secondary = listOf(q(R.string.th_ohm_law_out_power, u * i, "W")),
        steps = listOf(
            step(
                R.string.th_ohm_law_step_current, "I = U / R", "{0} / {1}",
                n(u, "V"), n(r, "Ω"), result = n(i, "A"),
            ),
            step(
                R.string.th_ohm_law_step_power, "P = U · I", "{0} × {1}",
                n(u, "V"), n(i, "A"), result = n(u * i, "W"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to n(r, "Ω"), "i" to n(i, "A")),
    )
}

/** R = U / I — how a resistance is measured rather than read off a band code. */
internal fun ohmForResistance(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val i = input["i"]
    val r = u / i
    return TheorySolutionResult(
        primary = q(R.string.th_ohm_law_out_resistance, r, "Ω", style = NumberStyle.SI_PREFIX),
        secondary = listOf(q(R.string.th_ohm_law_out_power, u * i, "W")),
        steps = listOf(
            step(
                R.string.th_ohm_law_step_resistance, "R = U / I", "{0} / {1}",
                n(u, "V"), n(i, "A"), result = si(r, "Ω"),
            ),
            step(
                R.string.th_ohm_law_step_power, "P = U · I", "{0} × {1}",
                n(u, "V"), n(i, "A"), result = n(u * i, "W"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to si(r, "Ω"), "i" to n(i, "A")),
    )
}

// ---- Foundation: DC power -------------------------------------------------

/**
 * P = U · I, then the same answer through I²R and U²/R.
 *
 * The three forms are one step each because seeing them agree is the point of
 * the topic: a reader who knows only `P = U · I` reaches for a voltage they may
 * not have measured, when the current and the resistance were already in hand.
 */
internal fun powerFromVoltageAndCurrent(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val i = input["i"]
    val p = u * i
    val r = u / i
    return TheorySolutionResult(
        primary = q(R.string.th_dc_power_out_power, p, "W"),
        secondary = listOf(q(R.string.th_dc_power_out_resistance, si(r, "Ω"))),
        steps = listOf(
            step(
                R.string.th_dc_power_step_direct, "P = U · I", "{0} × {1}",
                n(u, "V"), n(i, "A"), result = n(p, "W"),
            ),
            step(
                R.string.th_dc_power_step_resistance, "R = U / I", "{0} / {1}",
                n(u, "V"), n(i, "A"), result = si(r, "Ω"),
            ),
            step(
                R.string.th_dc_power_step_current_form, "P = I² · R", "{0}² × {1}",
                n(i, "A"), si(r, "Ω"), result = n(i * i * r, "W"),
            ),
            step(
                R.string.th_dc_power_step_voltage_form, "P = U² / R", "{0}² / {1}",
                n(u, "V"), si(r, "Ω"), result = n(u * u / r, "W"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to si(r, "Ω"), "i" to n(i, "A")),
    )
}

/** I = P / U — sizing a supply from a nameplate that states watts. */
internal fun currentFromPower(input: TheoryInputs): TheorySolutionResult {
    val p = input["p"]
    val u = input["u"]
    val i = p / u
    val r = u / i
    return TheorySolutionResult(
        primary = q(R.string.th_dc_power_out_current, i, "A"),
        secondary = listOf(q(R.string.th_dc_power_out_resistance, si(r, "Ω"))),
        steps = listOf(
            step(
                R.string.th_dc_power_step_current, "I = P / U", "{0} / {1}",
                n(p, "W"), n(u, "V"), result = n(i, "A"),
            ),
            step(
                R.string.th_dc_power_step_resistance, "R = U / I", "{0} / {1}",
                n(u, "V"), n(i, "A"), result = si(r, "Ω"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to si(r, "Ω"), "i" to n(i, "A")),
    )
}

/** U = P / I — the supply a stated load needs at a stated current. */
internal fun voltageFromPower(input: TheoryInputs): TheorySolutionResult {
    val p = input["p"]
    val i = input["i"]
    val u = p / i
    val r = u / i
    return TheorySolutionResult(
        primary = q(R.string.th_dc_power_out_voltage, u, "V"),
        secondary = listOf(q(R.string.th_dc_power_out_resistance, si(r, "Ω"))),
        steps = listOf(
            step(
                R.string.th_dc_power_step_voltage, "U = P / I", "{0} / {1}",
                n(p, "W"), n(i, "A"), result = n(u, "V"),
            ),
            step(
                R.string.th_dc_power_step_resistance, "R = U / I", "{0} / {1}",
                n(u, "V"), n(i, "A"), result = si(r, "Ω"),
            ),
        ),
        diagramLabels = mapOf("u" to n(u, "V"), "r" to si(r, "Ω"), "i" to n(i, "A")),
    )
}

// ---- Foundation: series resistance ----------------------------------------

/**
 * Resistances end to end: they add, the current is shared, the voltage is not.
 *
 * The per-resistor drops are the reason this topic exists rather than being one
 * line of the divider topic — they are what a reader checks a measurement
 * against, and they are what shows the drops summing back to the supply.
 */
internal fun seriesResistance(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val r1 = input["r1"]
    val r2 = input["r2"]
    val r3 = input.getOrNull("r3")

    val total = r1 + r2 + (r3 ?: 0.0)
    val i = u / total

    val steps = mutableListOf<TheoryStep>()
    steps += if (r3 == null) {
        step(
            R.string.th_series_step_total, "R_t = R₁ + R₂", "{0} + {1}",
            si(r1, "Ω"), si(r2, "Ω"), result = si(total, "Ω"),
        )
    } else {
        step(
            R.string.th_series_step_total, "R_t = R₁ + R₂ + R₃", "{0} + {1} + {2}",
            si(r1, "Ω"), si(r2, "Ω"), si(r3, "Ω"), result = si(total, "Ω"),
        )
    }
    steps += step(
        R.string.th_series_step_current, "I = U / R_t", "{0} / {1}",
        n(u, "V"), si(total, "Ω"), result = n(i, "A"),
    )
    steps += step(
        R.string.th_series_step_drop_one, "U₁ = I · R₁", "{0} × {1}",
        n(i, "A"), si(r1, "Ω"), result = n(i * r1, "V"),
    )
    steps += step(
        R.string.th_series_step_drop_two, "U₂ = I · R₂", "{0} × {1}",
        n(i, "A"), si(r2, "Ω"), result = n(i * r2, "V"),
    )
    if (r3 != null) {
        steps += step(
            R.string.th_series_step_drop_three, "U₃ = I · R₃", "{0} × {1}",
            n(i, "A"), si(r3, "Ω"), result = n(i * r3, "V"),
        )
    }

    val secondary = buildList {
        add(q(R.string.th_series_out_current, i, "A"))
        add(q(R.string.th_series_out_drop_one, i * r1, "V"))
        add(q(R.string.th_series_out_drop_two, i * r2, "V"))
        if (r3 != null) add(q(R.string.th_series_out_drop_three, i * r3, "V"))
    }

    return TheorySolutionResult(
        primary = q(R.string.th_series_out_total, si(total, "Ω")),
        secondary = secondary,
        steps = steps,
        diagramLabels = buildMap {
            put("u", n(u, "V"))
            put("i", n(i, "A"))
            put("r1", si(r1, "Ω"))
            put("r2", si(r2, "Ω"))
            if (r3 != null) put("r3", si(r3, "Ω"))
        },
    )
}

// ---- Foundation: parallel resistance --------------------------------------

/**
 * Resistances across one pair of nodes: the voltage is shared, the current is not.
 *
 * Reported through conductances rather than the reciprocal-of-sum-of-reciprocals
 * written as one expression, because that is the form that stays readable with
 * three branches and the form that makes the result obviously smaller than the
 * smallest branch.
 */
internal fun parallelResistance(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val r1 = input["r1"]
    val r2 = input["r2"]
    val r3 = input.getOrNull("r3")

    val conductance = 1.0 / r1 + 1.0 / r2 + (r3?.let { 1.0 / it } ?: 0.0)
    val total = 1.0 / conductance
    val i1 = u / r1
    val i2 = u / r2
    val i3 = r3?.let { u / it }
    val iTotal = u / total

    val steps = mutableListOf<TheoryStep>()
    steps += if (r3 == null) {
        step(
            R.string.th_parallel_step_conductance, "1/R_t = 1/R₁ + 1/R₂", "1/{0} + 1/{1}",
            si(r1, "Ω"), si(r2, "Ω"), result = sig(conductance, "S"),
        )
    } else {
        step(
            R.string.th_parallel_step_conductance,
            "1/R_t = 1/R₁ + 1/R₂ + 1/R₃", "1/{0} + 1/{1} + 1/{2}",
            si(r1, "Ω"), si(r2, "Ω"), si(r3, "Ω"), result = sig(conductance, "S"),
        )
    }
    steps += step(
        R.string.th_parallel_step_total, "R_t = 1 / (1/R_t)", "1 / {0}",
        sig(conductance, "S"), result = si(total, "Ω"),
    )
    steps += step(
        R.string.th_parallel_step_branch_one, "I₁ = U / R₁", "{0} / {1}",
        n(u, "V"), si(r1, "Ω"), result = n(i1, "A"),
    )
    steps += step(
        R.string.th_parallel_step_branch_two, "I₂ = U / R₂", "{0} / {1}",
        n(u, "V"), si(r2, "Ω"), result = n(i2, "A"),
    )
    if (r3 != null && i3 != null) {
        steps += step(
            R.string.th_parallel_step_branch_three, "I₃ = U / R₃", "{0} / {1}",
            n(u, "V"), si(r3, "Ω"), result = n(i3, "A"),
        )
    }
    steps += step(
        R.string.th_parallel_step_supply, "I = U / R_t", "{0} / {1}",
        n(u, "V"), si(total, "Ω"), result = n(iTotal, "A"),
    )

    val secondary = buildList {
        add(q(R.string.th_parallel_out_supply_current, iTotal, "A"))
        add(q(R.string.th_parallel_out_branch_one, i1, "A"))
        add(q(R.string.th_parallel_out_branch_two, i2, "A"))
        if (i3 != null) add(q(R.string.th_parallel_out_branch_three, i3, "A"))
    }

    return TheorySolutionResult(
        primary = q(R.string.th_parallel_out_total, si(total, "Ω")),
        secondary = secondary,
        steps = steps,
        diagramLabels = buildMap {
            put("u", n(u, "V"))
            put("i", n(iTotal, "A"))
            put("r1", si(r1, "Ω"))
            put("r2", si(r2, "Ω"))
            if (r3 != null) put("r3", si(r3, "Ω"))
        },
    )
}

// ---- Foundation: voltage divider ------------------------------------------

/**
 * Two resistances in series with the output taken from their junction.
 *
 * The load is optional and it is the whole lesson: an unloaded divider is a ratio
 * anyone can read off, and a loaded one is why a divider that works on paper
 * feeds a circuit the wrong voltage. When a load is given, the derivation shows
 * both figures and the error between them.
 */
internal fun voltageDivider(input: TheoryInputs): TheorySolutionResult {
    val u = input["u"]
    val r1 = input["r1"]
    val r2 = input["r2"]
    val load = input.getOrNull("r_load")

    val unloaded = u * r2 / (r1 + r2)

    val steps = mutableListOf<TheoryStep>()
    steps += step(
        R.string.th_divider_step_ratio, "U_out = U · R₂ / (R₁ + R₂)", "{0} × {1} / ({2} + {3})",
        n(u, "V"), si(r2, "Ω"), si(r1, "Ω"), si(r2, "Ω"), result = n(unloaded, "V"),
    )

    if (load == null) {
        return TheorySolutionResult(
            primary = q(R.string.th_divider_out_unloaded, unloaded, "V"),
            secondary = listOf(
                q(R.string.th_divider_out_current, u / (r1 + r2), "A", decimals = 4),
            ),
            steps = steps,
            diagramLabels = mapOf(
                "u" to n(u, "V"), "r1" to si(r1, "Ω"), "r2" to si(r2, "Ω"),
                "uout" to n(unloaded, "V"),
            ),
        )
    }

    val lower = r2 * load / (r2 + load)
    val loaded = u * lower / (r1 + lower)
    val errorPercent = (loaded - unloaded) / unloaded * PERCENT

    steps += step(
        R.string.th_divider_step_load, "R₂∥ = R₂ · R_L / (R₂ + R_L)", "{0} × {1} / ({2} + {3})",
        si(r2, "Ω"), si(load, "Ω"), si(r2, "Ω"), si(load, "Ω"), result = si(lower, "Ω"),
    )
    steps += step(
        R.string.th_divider_step_loaded, "U_out = U · R₂∥ / (R₁ + R₂∥)", "{0} × {1} / ({2} + {3})",
        n(u, "V"), si(lower, "Ω"), si(r1, "Ω"), si(lower, "Ω"), result = n(loaded, "V"),
    )
    steps += step(
        R.string.th_divider_step_error, "Δ = (U_L − U_0) / U_0", "({0} − {1}) / {2}",
        n(loaded, "V"), n(unloaded, "V"), n(unloaded, "V"), result = n(errorPercent, "%"),
    )

    return TheorySolutionResult(
        primary = q(R.string.th_divider_out_loaded, loaded, "V"),
        secondary = listOf(
            q(R.string.th_divider_out_unloaded, unloaded, "V"),
            q(R.string.th_divider_out_error, errorPercent, "%"),
            q(R.string.th_divider_out_current, u / (r1 + lower), "A", decimals = 4),
        ),
        steps = steps,
        diagramLabels = mapOf(
            "u" to n(u, "V"), "r1" to si(r1, "Ω"), "r2" to si(r2, "Ω"),
            "uout" to n(loaded, "V"),
        ),
    )
}

// ---- Foundation: current divider ------------------------------------------

/**
 * Two branches sharing a supply current.
 *
 * The cross-over is the thing readers get backwards, so the derivation states it
 * as its own step: the branch current is proportional to the *other* branch's
 * resistance, because the smaller resistance takes the larger share.
 */
internal fun currentDivider(input: TheoryInputs): TheorySolutionResult {
    val i = input["i"]
    val r1 = input["r1"]
    val r2 = input["r2"]

    val parallel = r1 * r2 / (r1 + r2)
    val u = i * parallel
    val i1 = i * r2 / (r1 + r2)
    val i2 = i * r1 / (r1 + r2)

    return TheorySolutionResult(
        primary = q(R.string.th_current_divider_out_branch_one, i1, "A", decimals = 3),
        secondary = listOf(
            q(R.string.th_current_divider_out_branch_two, i2, "A", decimals = 3),
            q(R.string.th_current_divider_out_voltage, u, "V"),
            q(R.string.th_current_divider_out_parallel, si(parallel, "Ω")),
        ),
        steps = listOf(
            step(
                R.string.th_current_divider_step_parallel,
                "R∥ = R₁ · R₂ / (R₁ + R₂)", "{0} × {1} / ({2} + {3})",
                si(r1, "Ω"), si(r2, "Ω"), si(r1, "Ω"), si(r2, "Ω"), result = si(parallel, "Ω"),
            ),
            step(
                R.string.th_current_divider_step_voltage, "U = I · R∥", "{0} × {1}",
                n(i, "A"), si(parallel, "Ω"), result = n(u, "V"),
            ),
            step(
                R.string.th_current_divider_step_branch_one,
                "I₁ = I · R₂ / (R₁ + R₂)", "{0} × {1} / ({2} + {3})",
                n(i, "A"), si(r2, "Ω"), si(r1, "Ω"), si(r2, "Ω"),
                result = n(i1, "A", decimals = 3),
            ),
            step(
                R.string.th_current_divider_step_branch_two,
                "I₂ = I · R₁ / (R₁ + R₂)", "{0} × {1} / ({2} + {3})",
                n(i, "A"), si(r1, "Ω"), si(r1, "Ω"), si(r2, "Ω"),
                result = n(i2, "A", decimals = 3),
            ),
        ),
        diagramLabels = mapOf(
            "i" to n(i, "A"), "r1" to si(r1, "Ω"), "r2" to si(r2, "Ω"),
            "i1" to n(i1, "A", decimals = 3), "i2" to n(i2, "A", decimals = 3),
        ),
    )
}

// ---- Foundation: conductor resistance -------------------------------------

/**
 * R = ρ(θ) · L / A, with the resistivity corrected for temperature.
 *
 * The constants come from [ConductorMaterial] rather than being restated here.
 * They are transcribed data, registered in `docs/verification-backlog.md`, and a
 * second copy is exactly what that register exists to prevent — voltage drop and
 * this page must never disagree about what copper is.
 */
private fun conductorResistance(
    input: TheoryInputs,
    material: ConductorMaterial,
): TheorySolutionResult {
    val length = input["l"]
    val area = input["a"]
    val temperature = input["theta"]

    val rho = material.resistivityAt(temperature)
    val resistance = rho * length / area

    return TheorySolutionResult(
        primary = q(R.string.th_conductor_out_resistance, si(resistance, "Ω")),
        secondary = listOf(
            q(R.string.th_conductor_out_loop, si(2 * resistance, "Ω")),
            q(R.string.th_conductor_out_resistivity, sig(rho, "Ω·mm²/m", digits = 5)),
        ),
        steps = listOf(
            step(
                R.string.th_conductor_step_resistivity,
                "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]", "{0} × [1 + {1} × ({2} − 20)]",
                sig(material.resistivityAt20C, digits = 5),
                sig(material.temperatureCoefficient, digits = 3),
                n(temperature, "°C"),
                result = sig(rho, "Ω·mm²/m", digits = 5),
            ),
            step(
                R.string.th_conductor_step_resistance, "R = ρ(θ) · L / A", "{0} × {1} / {2}",
                sig(rho, digits = 5), n(length, "m"), n(area, "mm²"),
                result = si(resistance, "Ω"),
            ),
            step(
                R.string.th_conductor_step_loop, "R_loop = 2 · R", "2 × {0}",
                si(resistance, "Ω"), result = si(2 * resistance, "Ω"),
            ),
        ),
        diagramLabels = mapOf(
            "l" to n(length, "m"),
            "a" to n(area, "mm²"),
            "r" to si(resistance, "Ω"),
        ),
    )
}

internal fun copperResistance(input: TheoryInputs) =
    conductorResistance(input, ConductorMaterial.COPPER)

internal fun aluminiumResistance(input: TheoryInputs) =
    conductorResistance(input, ConductorMaterial.ALUMINIUM)

// ---- Intermediate: Kirchhoff's laws ---------------------------------------

/** The currents at a junction sum to zero: what arrives has to leave. */
internal fun kirchhoffNode(input: TheoryInputs): TheorySolutionResult {
    val inOne = input["i_in1"]
    val inTwo = input["i_in2"]
    val outOne = input["i_out1"]
    val unknown = inOne + inTwo - outOne

    return TheorySolutionResult(
        primary = q(R.string.th_kirchhoff_out_branch, unknown, "A", decimals = 3),
        secondary = listOf(
            q(R.string.th_kirchhoff_out_arriving, inOne + inTwo, "A", decimals = 3),
        ),
        steps = listOf(
            step(
                R.string.th_kirchhoff_step_arriving, "ΣI_in = I₁ + I₂", "{0} + {1}",
                n(inOne, "A", 3), n(inTwo, "A", 3), result = n(inOne + inTwo, "A", 3),
            ),
            step(
                R.string.th_kirchhoff_step_branch, "I₄ = ΣI_in − I₃", "{0} − {1}",
                n(inOne + inTwo, "A", 3), n(outOne, "A", 3), result = n(unknown, "A", 3),
            ),
        ),
        diagramLabels = mapOf(
            "i1" to n(inOne, "A", 3),
            "i2" to n(inTwo, "A", 3),
            "i3" to n(outOne, "A", 3),
        ),
    )
}

/** The drops around a closed loop sum to the source driving it. */
internal fun kirchhoffLoop(input: TheoryInputs): TheorySolutionResult {
    val supply = input["u"]
    val dropOne = input["u1"]
    val dropTwo = input["u2"]
    val remaining = supply - dropOne - dropTwo

    return TheorySolutionResult(
        primary = q(R.string.th_kirchhoff_out_remaining, remaining, "V"),
        secondary = listOf(
            q(R.string.th_kirchhoff_out_accounted, dropOne + dropTwo, "V"),
        ),
        steps = listOf(
            step(
                R.string.th_kirchhoff_step_accounted, "ΣU_known = U₁ + U₂", "{0} + {1}",
                n(dropOne, "V"), n(dropTwo, "V"), result = n(dropOne + dropTwo, "V"),
            ),
            step(
                R.string.th_kirchhoff_step_remaining, "U₃ = U − ΣU_known", "{0} − {1}",
                n(supply, "V"), n(dropOne + dropTwo, "V"), result = n(remaining, "V"),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(supply, "V"),
            "u1" to n(dropOne, "V"),
            "u2" to n(dropTwo, "V"),
        ),
    )
}

// ---- Intermediate: RMS, peak and average ----------------------------------

/**
 * The three ways of measuring a sine, from whichever one you have.
 *
 * All three solutions funnel through the peak because every relationship here is
 * defined against it — deriving average from RMS directly would hide that the
 * 0.9 a reader half-remembers is √2 · 2/π and not a constant of nature.
 */
private fun sineLevels(peak: Double, from: TheoryStep?): TheorySolutionResult {
    val rms = peak / SQRT_TWO
    val average = 2.0 * peak / Math.PI

    return TheorySolutionResult(
        primary = q(R.string.th_rms_out_rms, rms, "V", decimals = 3),
        secondary = listOf(
            q(R.string.th_rms_out_peak, peak, "V", decimals = 3),
            q(R.string.th_rms_out_average, average, "V", decimals = 3),
            q(R.string.th_rms_out_form, rms / average, "", decimals = 4),
            q(R.string.th_rms_out_crest, peak / rms, "", decimals = 4),
        ),
        steps = listOfNotNull(
            from,
            step(
                R.string.th_rms_step_rms, "U = Û / √2", "{0} / {1}",
                n(peak, "V", 3), n(SQRT_TWO, "", 3), result = n(rms, "V", 3),
            ),
            step(
                R.string.th_rms_step_average, "U̅ = 2 · Û / π", "2 × {0} / {1}",
                n(peak, "V", 3), n(Math.PI, "", 3), result = n(average, "V", 3),
            ),
            step(
                R.string.th_rms_step_form, "k_f = U / U̅", "{0} / {1}",
                n(rms, "V", 3), n(average, "V", 3), result = n(rms / average, "", 4),
            ),
        ),
        diagramLabels = mapOf(
            "peak" to n(peak, "V", 3),
            "rms" to n(rms, "V", 3),
            "avg" to n(average, "V", 3),
        ),
    )
}

internal fun sineFromRms(input: TheoryInputs): TheorySolutionResult {
    val rms = input["u_rms"]
    val peak = rms * SQRT_TWO
    return sineLevels(
        peak,
        step(
            R.string.th_rms_step_peak, "Û = U · √2", "{0} × {1}",
            n(rms, "V", 3), n(SQRT_TWO, "", 3), result = n(peak, "V", 3),
        ),
    )
}

internal fun sineFromPeak(input: TheoryInputs): TheorySolutionResult =
    sineLevels(input["u_peak"], from = null)

internal fun sineFromAverage(input: TheoryInputs): TheorySolutionResult {
    val average = input["u_avg"]
    val peak = average * Math.PI / 2.0
    return sineLevels(
        peak,
        step(
            R.string.th_rms_step_peak_from_average, "Û = U̅ · π / 2", "{0} × {1} / 2",
            n(average, "V", 3), n(Math.PI, "", 3), result = n(peak, "V", 3),
        ),
    )
}

// ---- Intermediate: reactance ----------------------------------------------

/** X_C = 1 / (2πfC): a capacitor passes more current the faster the supply alternates. */
internal fun capacitiveReactance(input: TheoryInputs): TheorySolutionResult {
    val frequency = input["f"]
    val capacitance = input["c"] / MICRO
    val voltage = input["u"]

    val omega = TWO_PI * frequency
    val reactance = 1.0 / (omega * capacitance)
    val current = voltage / reactance

    return TheorySolutionResult(
        primary = q(R.string.th_xc_out_reactance, si(reactance, "Ω")),
        secondary = listOf(
            q(R.string.th_xc_out_current, current, "A", decimals = 3),
            q(R.string.th_xc_out_reactive, voltage * current, "var"),
        ),
        steps = listOf(
            step(
                R.string.th_reactance_step_omega, "ω = 2 · π · f", "2 × {0} × {1}",
                n(Math.PI, "", 3), n(frequency, "Hz"), result = sig(omega, "rad/s"),
            ),
            step(
                R.string.th_xc_step_reactance, "X_C = 1 / (ω · C)", "1 / ({0} × {1})",
                sig(omega, "rad/s"), si(capacitance, "F"), result = si(reactance, "Ω"),
            ),
            step(
                R.string.th_reactance_step_current, "I = U / X", "{0} / {1}",
                n(voltage, "V"), si(reactance, "Ω"), result = n(current, "A", 3),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(voltage, "V"),
            "c" to si(capacitance, "F"),
            "f" to n(frequency, "Hz"),
            "i" to n(current, "A", 3),
        ),
    )
}

/** X_L = 2πfL: an inductor passes less current the faster the supply alternates. */
internal fun inductiveReactance(input: TheoryInputs): TheorySolutionResult {
    val frequency = input["f"]
    val inductance = input["l"] / MILLI
    val voltage = input["u"]

    val omega = TWO_PI * frequency
    val reactance = omega * inductance
    val current = voltage / reactance

    return TheorySolutionResult(
        primary = q(R.string.th_xl_out_reactance, si(reactance, "Ω")),
        secondary = listOf(
            q(R.string.th_xl_out_current, current, "A", decimals = 3),
            q(R.string.th_xl_out_reactive, voltage * current, "var"),
        ),
        steps = listOf(
            step(
                R.string.th_reactance_step_omega, "ω = 2 · π · f", "2 × 3,142 × {0}",
                n(frequency, "Hz"), result = sig(omega, "rad/s"),
            ),
            step(
                R.string.th_xl_step_reactance, "X_L = ω · L", "{0} × {1}",
                sig(omega, "rad/s"), si(inductance, "H"), result = si(reactance, "Ω"),
            ),
            step(
                R.string.th_reactance_step_current, "I = U / X", "{0} / {1}",
                n(voltage, "V"), si(reactance, "Ω"), result = n(current, "A", 3),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(voltage, "V"),
            "l" to si(inductance, "H"),
            "f" to n(frequency, "Hz"),
            "i" to n(current, "A", 3),
        ),
    )
}

// ---- Intermediate: the power triangle -------------------------------------

/** S, P and Q as the three sides of a right triangle set by the phase angle. */
internal fun powerTriangle(input: TheoryInputs): TheorySolutionResult {
    val active = input["p"] * KILO
    val powerFactor = input["pf"]

    val apparent = active / powerFactor
    val reactive = kotlin.math.sqrt(apparent * apparent - active * active)
    val angle = Math.toDegrees(kotlin.math.acos(powerFactor))

    return TheorySolutionResult(
        primary = q(R.string.th_triangle_out_apparent, apparent / KILO, "kVA"),
        secondary = listOf(
            q(R.string.th_triangle_out_reactive, reactive / KILO, "kvar"),
            q(R.string.th_triangle_out_angle, angle, "°"),
            q(R.string.th_triangle_out_tangent, reactive / active, "", decimals = 4),
        ),
        steps = listOf(
            step(
                R.string.th_triangle_step_apparent, "S = P / cos φ", "{0} / {1}",
                n(active, "W"), n(powerFactor, "", 3), result = n(apparent, "VA"),
            ),
            step(
                R.string.th_triangle_step_reactive, "Q = √(S² − P²)", "√({0}² − {1}²)",
                n(apparent, "VA"), n(active, "W"), result = n(reactive, "var"),
            ),
            step(
                R.string.th_triangle_step_angle, "φ = arccos(cos φ)", "arccos({0})",
                n(powerFactor, "", 3), result = n(angle, "°"),
            ),
        ),
        diagramLabels = mapOf(
            "p" to n(active / KILO, "kW"),
            "q" to n(reactive / KILO, "kvar"),
            "s" to n(apparent / KILO, "kVA"),
            "phi" to n(angle, "°"),
        ),
    )
}

// ---- Intermediate: star and delta -----------------------------------------

private fun threePhase(
    input: TheoryInputs,
    star: Boolean,
): TheorySolutionResult {
    val line = input["u_line"]
    val lineCurrent = input["i_line"]
    val powerFactor = input["pf"]

    val phaseVoltage = if (star) line / SQRT_THREE else line
    val phaseCurrent = if (star) lineCurrent else lineCurrent / SQRT_THREE
    val apparent = SQRT_THREE * line * lineCurrent
    val active = apparent * powerFactor

    val voltageStep = if (star) {
        step(
            R.string.th_three_phase_step_voltage, "U_ph = U_L / √3", "{0} / {1}",
            n(line, "V"), n(SQRT_THREE, "", 3), result = n(phaseVoltage, "V"),
        )
    } else {
        step(
            R.string.th_three_phase_step_voltage, "U_ph = U_L", "{0}",
            n(line, "V"), result = n(phaseVoltage, "V"),
        )
    }
    val currentStep = if (star) {
        step(
            R.string.th_three_phase_step_current, "I_ph = I_L", "{0}",
            n(lineCurrent, "A"), result = n(phaseCurrent, "A"),
        )
    } else {
        step(
            R.string.th_three_phase_step_current, "I_ph = I_L / √3", "{0} / {1}",
            n(lineCurrent, "A"), n(SQRT_THREE, "", 3), result = n(phaseCurrent, "A"),
        )
    }

    return TheorySolutionResult(
        primary = q(R.string.th_three_phase_out_phase_voltage, phaseVoltage, "V"),
        secondary = listOf(
            q(R.string.th_three_phase_out_phase_current, phaseCurrent, "A"),
            q(R.string.th_three_phase_out_active, active / KILO, "kW"),
            q(R.string.th_three_phase_out_apparent, apparent / KILO, "kVA"),
        ),
        steps = listOf(
            voltageStep,
            currentStep,
            step(
                R.string.th_three_phase_step_apparent, "S = √3 · U_L · I_L", "{0} × {1} × {2}",
                n(SQRT_THREE, "", 3), n(line, "V"), n(lineCurrent, "A"),
                result = n(apparent, "VA"),
            ),
            step(
                R.string.th_three_phase_step_active, "P = S · cos φ", "{0} × {1}",
                n(apparent, "VA"), n(powerFactor, "", 3), result = n(active, "W"),
            ),
        ),
        diagramLabels = emptyMap(),
    )
}

internal fun starConnection(input: TheoryInputs) = threePhase(input, star = true)

internal fun deltaConnection(input: TheoryInputs) = threePhase(input, star = false)

// ---- Intermediate: the RC time constant -----------------------------------

/** τ = R · C, and the exponential approach that follows from it. */
internal fun rcTimeConstant(input: TheoryInputs): TheorySolutionResult {
    val resistance = input["r"]
    val capacitance = input["c"] / MICRO
    val supply = input["u"]
    val time = input["t"]

    val tau = resistance * capacitance
    val voltage = supply * (1.0 - kotlin.math.exp(-time / tau))
    val settled = FULL_CHARGE_TAUS * tau

    return TheorySolutionResult(
        primary = q(R.string.th_rc_out_tau, si(tau, "s")),
        secondary = listOf(
            q(R.string.th_rc_out_voltage, voltage, "V", decimals = 3),
            q(R.string.th_rc_out_settled, si(settled, "s")),
        ),
        steps = listOf(
            step(
                R.string.th_rc_step_tau, "τ = R · C", "{0} × {1}",
                si(resistance, "Ω"), si(capacitance, "F"), result = si(tau, "s"),
            ),
            step(
                R.string.th_rc_step_voltage, "u(t) = U · (1 − e^(−t/τ))",
                "{0} × (1 − e^(−{1} / {2}))",
                n(supply, "V"), si(time, "s"), si(tau, "s"), result = n(voltage, "V", 3),
            ),
            step(
                R.string.th_rc_step_settled, "t₉₉ = 5 · τ", "5 × {0}",
                si(tau, "s"), result = si(settled, "s"),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(supply, "V"),
            "r" to si(resistance, "Ω"),
            "c" to si(capacitance, "F"),
            "tau" to si(tau, "s"),
        ),
    )
}

// ---- Advanced: series RLC impedance ---------------------------------------

/**
 * Z = √(R² + (X_L − X_C)²), and the phase angle that comes with it.
 *
 * The two reactances are subtracted before they are squared, which is the step a
 * reader most often gets wrong: they oppose each other, so a circuit with equal
 * reactances is purely resistive no matter how large either one is.
 */
internal fun rlcImpedance(input: TheoryInputs): TheorySolutionResult {
    val resistance = input["r"]
    val inductance = input["l"] / MILLI
    val capacitance = input["c"] / MICRO
    val frequency = input["f"]
    val voltage = input["u"]

    val omega = TWO_PI * frequency
    val inductive = omega * inductance
    val capacitive = 1.0 / (omega * capacitance)
    val net = inductive - capacitive
    val impedance = kotlin.math.sqrt(resistance * resistance + net * net)
    val angle = Math.toDegrees(kotlin.math.atan2(net, resistance))
    val current = voltage / impedance

    return TheorySolutionResult(
        primary = q(R.string.th_rlc_out_impedance, si(impedance, "Ω")),
        secondary = listOf(
            q(R.string.th_rlc_out_angle, angle, "°"),
            q(R.string.th_rlc_out_current, current, "A", decimals = 3),
            q(R.string.th_rlc_out_inductive, si(inductive, "Ω")),
            q(R.string.th_rlc_out_capacitive, si(capacitive, "Ω")),
        ),
        steps = listOf(
            step(
                R.string.th_rlc_step_inductive, "X_L = 2 · π · f · L", "2 × {0} × {1} × {2}",
                n(Math.PI, "", 3), n(frequency, "Hz"), si(inductance, "H"),
                result = si(inductive, "Ω"),
            ),
            step(
                R.string.th_rlc_step_capacitive, "X_C = 1 / (2 · π · f · C)",
                "1 / (2 × {0} × {1} × {2})",
                n(Math.PI, "", 3), n(frequency, "Hz"), si(capacitance, "F"),
                result = si(capacitive, "Ω"),
            ),
            step(
                R.string.th_rlc_step_net, "X = X_L − X_C", "{0} − {1}",
                si(inductive, "Ω"), si(capacitive, "Ω"), result = si(net, "Ω"),
            ),
            step(
                R.string.th_rlc_step_impedance, "Z = √(R² + X²)", "√({0}² + {1}²)",
                si(resistance, "Ω"), si(net, "Ω"), result = si(impedance, "Ω"),
            ),
            step(
                R.string.th_rlc_step_angle, "φ = arctan(X / R)", "arctan({0} / {1})",
                si(net, "Ω"), si(resistance, "Ω"), result = n(angle, "°"),
            ),
            step(
                R.string.th_rlc_step_current, "I = U / Z", "{0} / {1}",
                n(voltage, "V"), si(impedance, "Ω"), result = n(current, "A", 3),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(voltage, "V"),
            "r" to si(resistance, "Ω"),
            "l" to si(inductance, "H"),
            "c" to si(capacitance, "F"),
            "i" to n(current, "A", 3),
        ),
    )
}

// ---- Advanced: series resonance -------------------------------------------

/** The frequency at which the two reactances cancel, and how sharply they do. */
internal fun seriesResonance(input: TheoryInputs): TheorySolutionResult {
    val inductance = input["l"] / MILLI
    val capacitance = input["c"] / MICRO
    val resistance = input["r"]

    val resonant = 1.0 / (TWO_PI * kotlin.math.sqrt(inductance * capacitance))
    val characteristic = kotlin.math.sqrt(inductance / capacitance)
    val quality = characteristic / resistance
    val bandwidth = resonant / quality

    return TheorySolutionResult(
        primary = q(R.string.th_resonance_out_frequency, si(resonant, "Hz")),
        secondary = listOf(
            q(R.string.th_resonance_out_quality, quality, "", decimals = 3),
            q(R.string.th_resonance_out_bandwidth, si(bandwidth, "Hz")),
            q(R.string.th_resonance_out_reactance, si(characteristic, "Ω")),
            q(R.string.th_resonance_out_impedance, si(resistance, "Ω")),
        ),
        steps = listOf(
            step(
                R.string.th_resonance_step_frequency, "f₀ = 1 / (2 · π · √(L · C))",
                "1 / (2 × {0} × √({1} × {2}))",
                n(Math.PI, "", 3), si(inductance, "H"), si(capacitance, "F"),
                result = si(resonant, "Hz"),
            ),
            step(
                R.string.th_resonance_step_reactance, "X₀ = √(L / C)", "√({0} / {1})",
                si(inductance, "H"), si(capacitance, "F"), result = si(characteristic, "Ω"),
            ),
            step(
                R.string.th_resonance_step_quality, "Q = X₀ / R", "{0} / {1}",
                si(characteristic, "Ω"), si(resistance, "Ω"), result = n(quality, "", 3),
            ),
            step(
                R.string.th_resonance_step_bandwidth, "B = f₀ / Q", "{0} / {1}",
                si(resonant, "Hz"), n(quality, "", 3), result = si(bandwidth, "Hz"),
            ),
        ),
        diagramLabels = mapOf(
            "r" to si(resistance, "Ω"),
            "l" to si(inductance, "H"),
            "c" to si(capacitance, "F"),
        ),
    )
}

// ---- Advanced: Thévenin ----------------------------------------------------

/**
 * Any linear network, seen from two terminals, is one source behind one resistance.
 *
 * Worked from a divider because that is the network a reader can already solve
 * both ways — the point is not the answer but that the two agree.
 */
internal fun theveninEquivalent(input: TheoryInputs): TheorySolutionResult {
    val supply = input["u"]
    val upper = input["r1"]
    val lower = input["r2"]
    val load = input["r_load"]

    val openCircuit = supply * lower / (upper + lower)
    val internal = upper * lower / (upper + lower)
    val current = openCircuit / (internal + load)
    val acrossLoad = current * load

    return TheorySolutionResult(
        primary = q(R.string.th_thevenin_out_voltage, openCircuit, "V"),
        secondary = listOf(
            q(R.string.th_thevenin_out_resistance, si(internal, "Ω")),
            q(R.string.th_thevenin_out_current, current, "A", decimals = 4),
            q(R.string.th_thevenin_out_load_voltage, acrossLoad, "V"),
        ),
        steps = listOf(
            step(
                R.string.th_thevenin_step_open, "U_th = U · R₂ / (R₁ + R₂)",
                "{0} × {1} / ({2} + {3})",
                n(supply, "V"), si(lower, "Ω"), si(upper, "Ω"), si(lower, "Ω"),
                result = n(openCircuit, "V"),
            ),
            step(
                R.string.th_thevenin_step_resistance, "R_th = R₁ · R₂ / (R₁ + R₂)",
                "{0} × {1} / ({2} + {3})",
                si(upper, "Ω"), si(lower, "Ω"), si(upper, "Ω"), si(lower, "Ω"),
                result = si(internal, "Ω"),
            ),
            step(
                R.string.th_thevenin_step_current, "I = U_th / (R_th + R_L)", "{0} / ({1} + {2})",
                n(openCircuit, "V"), si(internal, "Ω"), si(load, "Ω"),
                result = n(current, "A", 4),
            ),
            step(
                R.string.th_thevenin_step_load, "U_L = I · R_L", "{0} × {1}",
                n(current, "A", 4), si(load, "Ω"), result = n(acrossLoad, "V"),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(openCircuit, "V"),
            "rth" to si(internal, "Ω"),
            "rload" to si(load, "Ω"),
            "i" to n(current, "A", 4),
        ),
    )
}

// ---- Advanced: Norton ------------------------------------------------------

/** The same network as a current source in parallel with the same resistance. */
internal fun nortonEquivalent(input: TheoryInputs): TheorySolutionResult {
    val thevenin = input["u_th"]
    val internal = input["r_th"]
    val load = input["r_load"]

    val shortCircuit = thevenin / internal
    val throughLoad = shortCircuit * internal / (internal + load)
    val acrossLoad = throughLoad * load

    return TheorySolutionResult(
        primary = q(R.string.th_norton_out_current, shortCircuit, "A", decimals = 4),
        secondary = listOf(
            q(R.string.th_norton_out_resistance, si(internal, "Ω")),
            q(R.string.th_norton_out_load_current, throughLoad, "A", decimals = 4),
            q(R.string.th_norton_out_load_voltage, acrossLoad, "V"),
        ),
        steps = listOf(
            step(
                R.string.th_norton_step_current, "I_N = U_th / R_th", "{0} / {1}",
                n(thevenin, "V"), si(internal, "Ω"), result = n(shortCircuit, "A", 4),
            ),
            step(
                R.string.th_norton_step_resistance, "R_N = R_th", "{0}",
                si(internal, "Ω"), result = si(internal, "Ω"),
            ),
            step(
                R.string.th_norton_step_load, "I_L = I_N · R_N / (R_N + R_L)",
                "{0} × {1} / ({2} + {3})",
                n(shortCircuit, "A", 4), si(internal, "Ω"), si(internal, "Ω"), si(load, "Ω"),
                result = n(throughLoad, "A", 4),
            ),
        ),
        diagramLabels = mapOf(
            "in" to n(shortCircuit, "A", 4),
            "rn" to si(internal, "Ω"),
            "rload" to si(load, "Ω"),
            "i" to n(throughLoad, "A", 4),
        ),
    )
}

// ---- Advanced: maximum power transfer --------------------------------------

/**
 * The load that takes the most power is the one matching the source resistance.
 *
 * Reports the efficiency alongside, because that is the figure that makes the
 * theorem useless in a power system: at the matched load exactly half the energy
 * is burnt inside the source.
 */
internal fun maximumPowerTransfer(input: TheoryInputs): TheorySolutionResult {
    val thevenin = input["u_th"]
    val internal = input["r_th"]
    val load = input["r_load"]

    val current = thevenin / (internal + load)
    val delivered = current * current * load
    val matched = thevenin * thevenin / (MATCHED_DIVISOR * internal)
    val efficiency = load / (internal + load) * PERCENT

    return TheorySolutionResult(
        primary = q(R.string.th_mpt_out_delivered, delivered, "W", decimals = 4),
        secondary = listOf(
            q(R.string.th_mpt_out_maximum, matched, "W", decimals = 4),
            q(R.string.th_mpt_out_optimum, si(internal, "Ω")),
            q(R.string.th_mpt_out_efficiency, efficiency, "%"),
        ),
        steps = listOf(
            step(
                R.string.th_mpt_step_current, "I = U_th / (R_th + R_L)", "{0} / ({1} + {2})",
                n(thevenin, "V"), si(internal, "Ω"), si(load, "Ω"),
                result = n(current, "A", 4),
            ),
            step(
                R.string.th_mpt_step_delivered, "P_L = I² · R_L", "{0}² × {1}",
                n(current, "A", 4), si(load, "Ω"), result = n(delivered, "W", 4),
            ),
            step(
                R.string.th_mpt_step_maximum, "P_max = U_th² / (4 · R_th)", "{0}² / (4 × {1})",
                n(thevenin, "V"), si(internal, "Ω"), result = n(matched, "W", 4),
            ),
            step(
                R.string.th_mpt_step_efficiency, "η = R_L / (R_th + R_L)", "{0} / ({1} + {2})",
                si(load, "Ω"), si(internal, "Ω"), si(load, "Ω"),
                result = n(efficiency, "%"),
            ),
        ),
        diagramLabels = mapOf(
            "u" to n(thevenin, "V"),
            "rth" to si(internal, "Ω"),
            "rload" to si(load, "Ω"),
            "i" to n(current, "A", 4),
        ),
    )
}

// ---- Advanced: star-delta transformation -----------------------------------

/** Y → Δ: each delta arm is the sum of pairwise products over the opposite star arm. */
internal fun starToDelta(input: TheoryInputs): TheorySolutionResult {
    val a = input["ra"]
    val b = input["rb"]
    val c = input["rc"]

    val products = a * b + b * c + c * a
    val ab = products / c
    val bc = products / a
    val ca = products / b

    return TheorySolutionResult(
        primary = q(R.string.th_ydelta_out_ab, si(ab, "Ω")),
        secondary = listOf(
            q(R.string.th_ydelta_out_bc, si(bc, "Ω")),
            q(R.string.th_ydelta_out_ca, si(ca, "Ω")),
        ),
        steps = listOf(
            step(
                R.string.th_ydelta_step_products, "Σ = R_a·R_b + R_b·R_c + R_c·R_a",
                "{0}×{1} + {2}×{3} + {4}×{5}",
                si(a, "Ω"), si(b, "Ω"), si(b, "Ω"), si(c, "Ω"), si(c, "Ω"), si(a, "Ω"),
                result = sig(products, "Ω²"),
            ),
            step(
                R.string.th_ydelta_step_ab, "R_ab = Σ / R_c", "{0} / {1}",
                sig(products, "Ω²"), si(c, "Ω"), result = si(ab, "Ω"),
            ),
            step(
                R.string.th_ydelta_step_bc, "R_bc = Σ / R_a", "{0} / {1}",
                sig(products, "Ω²"), si(a, "Ω"), result = si(bc, "Ω"),
            ),
            step(
                R.string.th_ydelta_step_ca, "R_ca = Σ / R_b", "{0} / {1}",
                sig(products, "Ω²"), si(b, "Ω"), result = si(ca, "Ω"),
            ),
        ),
        diagramLabels = mapOf(
            "ra" to si(a, "Ω"), "rb" to si(b, "Ω"), "rc" to si(c, "Ω"),
            "rab" to si(ab, "Ω"), "rbc" to si(bc, "Ω"), "rca" to si(ca, "Ω"),
        ),
    )
}

/** Δ → Y: each star arm is the product of its two neighbours over the total. */
internal fun deltaToStar(input: TheoryInputs): TheorySolutionResult {
    val ab = input["rab"]
    val bc = input["rbc"]
    val ca = input["rca"]

    val total = ab + bc + ca
    val a = ab * ca / total
    val b = ab * bc / total
    val c = bc * ca / total

    return TheorySolutionResult(
        primary = q(R.string.th_ydelta_out_ra, si(a, "Ω")),
        secondary = listOf(
            q(R.string.th_ydelta_out_rb, si(b, "Ω")),
            q(R.string.th_ydelta_out_rc, si(c, "Ω")),
        ),
        steps = listOf(
            step(
                R.string.th_ydelta_step_total, "ΣR = R_ab + R_bc + R_ca", "{0} + {1} + {2}",
                si(ab, "Ω"), si(bc, "Ω"), si(ca, "Ω"), result = si(total, "Ω"),
            ),
            step(
                R.string.th_ydelta_step_ra, "R_a = R_ab · R_ca / ΣR", "{0} × {1} / {2}",
                si(ab, "Ω"), si(ca, "Ω"), si(total, "Ω"), result = si(a, "Ω"),
            ),
            step(
                R.string.th_ydelta_step_rb, "R_b = R_ab · R_bc / ΣR", "{0} × {1} / {2}",
                si(ab, "Ω"), si(bc, "Ω"), si(total, "Ω"), result = si(b, "Ω"),
            ),
            step(
                R.string.th_ydelta_step_rc, "R_c = R_bc · R_ca / ΣR", "{0} × {1} / {2}",
                si(bc, "Ω"), si(ca, "Ω"), si(total, "Ω"), result = si(c, "Ω"),
            ),
        ),
        diagramLabels = mapOf(
            "ra" to si(a, "Ω"), "rb" to si(b, "Ω"), "rc" to si(c, "Ω"),
            "rab" to si(ab, "Ω"), "rbc" to si(bc, "Ω"), "rca" to si(ca, "Ω"),
        ),
    )
}

private const val PERCENT = 100.0

/** A matched load takes U²/4R, which is where the four comes from. */
private const val MATCHED_DIVISOR = 4.0
private const val KILO = 1_000.0
private const val MILLI = 1_000.0
private const val MICRO = 1_000_000.0
private val SQRT_TWO = kotlin.math.sqrt(2.0)
private val SQRT_THREE = kotlin.math.sqrt(3.0)
private val TWO_PI = 2.0 * Math.PI

/** Five time constants is the conventional "settled" point — 99.3 % of the way. */
private const val FULL_CHARGE_TAUS = 5.0

// ---- Electromagnetism ---------------------------------------------------------

/**
 * The transformer EMF equation, E = 4.44 · f · N · Φmax.
 *
 * The 4.44 is not a fudge. Faraday gives e = N·dΦ/dt; for a sinusoidal flux the
 * peak rate of change is 2πf·Φmax, and the RMS of a sine is that over √2 — so
 * the constant is 2π/√2 = 4.4429, rounded to the figure every textbook prints.
 * Writing it out is the difference between a formula a reader can rebuild and
 * one they have to trust.
 */
internal fun inducedEmf(input: TheoryInputs): TheorySolutionResult {
    val f = input["f"]
    val turns = input["n"]
    val flux = input["phi"]
    val e = EMF_CONSTANT * f * turns * flux
    return TheorySolutionResult(
        primary = q(R.string.th_emf_out_voltage, e, "V"),
        secondary = listOf(q(R.string.th_emf_out_per_turn, e / turns, "V")),
        steps = listOf(
            step(
                // The substitution has to carry a formatted operand: a literal
                // "2π / 1.414" reads identically in every language, which is
                // what the formatting tests are there to catch.
                R.string.th_emf_step_constant, "k = 2π / √2", "2π / {0}",
                n(SQRT_TWO, "", 3),
                result = n(EMF_CONSTANT, "", decimals = 4),
            ),
            step(
                R.string.th_emf_step_voltage, "E = k · f · N · Φ", "4.44 × {0} × {1} × {2}",
                n(f, "Hz"), n(turns, ""), n(flux, "Wb", style = NumberStyle.SIGNIFICANT),
                result = n(e, "V"),
            ),
        ),
    )
}

/**
 * Reluctance and flux in a magnetic circuit.
 *
 * The same arithmetic as Ohm's law with different names: magnetomotive force
 * drives flux through reluctance exactly as voltage drives current through
 * resistance. Saying so is most of the topic — the analogy is what makes a
 * magnetic circuit tractable at all.
 */
internal fun magneticCircuit(input: TheoryInputs): TheorySolutionResult {
    val turns = input["n"]
    val current = input["i"]
    val length = input["l"] / 1000.0
    val area = input["a"] / 1e6
    val mur = input["mur"]

    val mmf = turns * current
    val reluctance = length / (VACUUM_PERMEABILITY * mur * area)
    val flux = mmf / reluctance
    val density = flux / area

    return TheorySolutionResult(
        primary = q(R.string.th_magnetic_out_flux_density, density, "T"),
        secondary = listOf(
            q(R.string.th_magnetic_out_flux, flux, "Wb", style = NumberStyle.SIGNIFICANT),
            q(R.string.th_magnetic_out_mmf, mmf, "A"),
            q(R.string.th_magnetic_out_reluctance, reluctance, "1/H", style = NumberStyle.SIGNIFICANT),
        ),
        steps = listOf(
            step(
                R.string.th_magnetic_step_mmf, "F = N · I", "{0} × {1}",
                n(turns, ""), n(current, "A"), result = n(mmf, "A"),
            ),
            step(
                R.string.th_magnetic_step_reluctance, "ℛ = l / (μ₀ · μr · A)", "{0} / (μ₀ × {1} × {2})",
                n(length, "m", style = NumberStyle.SIGNIFICANT),
                n(mur, ""),
                n(area, "m²", style = NumberStyle.SIGNIFICANT),
                result = n(reluctance, "1/H", style = NumberStyle.SIGNIFICANT),
            ),
            step(
                R.string.th_magnetic_step_flux, "Φ = F / ℛ", "{0} / {1}",
                n(mmf, "A"), n(reluctance, "1/H", style = NumberStyle.SIGNIFICANT),
                result = n(flux, "Wb", style = NumberStyle.SIGNIFICANT),
            ),
            step(
                R.string.th_magnetic_step_density, "B = Φ / A", "{0} / {1}",
                n(flux, "Wb", style = NumberStyle.SIGNIFICANT),
                n(area, "m²", style = NumberStyle.SIGNIFICANT),
                result = n(density, "T"),
            ),
        ),
    )
}

/**
 * What a turns ratio does to voltage, current and impedance.
 *
 * Impedance is the one that surprises people: it transforms by the *square* of
 * the ratio, which is why a small mismatch at the secondary looks enormous from
 * the primary and why matching transformers exist at all.
 */
internal fun transformerRatio(input: TheoryInputs): TheorySolutionResult {
    val np = input["np"]
    val ns = input["ns"]
    val up = input["up"]
    val ip = input["ip"]

    val ratio = np / ns
    val us = up / ratio
    val isec = ip * ratio
    val impedanceRatio = ratio * ratio

    return TheorySolutionResult(
        primary = q(R.string.th_transformer_out_secondary_voltage, us, "V"),
        secondary = listOf(
            q(R.string.th_transformer_out_secondary_current, isec, "A"),
            q(R.string.th_transformer_out_ratio, ratio, ""),
            q(R.string.th_transformer_out_impedance_ratio, impedanceRatio, ""),
        ),
        steps = listOf(
            step(
                R.string.th_transformer_step_ratio, "a = Np / Ns", "{0} / {1}",
                n(np, ""), n(ns, ""), result = n(ratio, ""),
            ),
            step(
                R.string.th_transformer_step_voltage, "Us = Up / a", "{0} / {1}",
                n(up, "V"), n(ratio, ""), result = n(us, "V"),
            ),
            step(
                R.string.th_transformer_step_current, "Is = Ip · a", "{0} × {1}",
                n(ip, "A"), n(ratio, ""), result = n(isec, "A"),
            ),
            step(
                R.string.th_transformer_step_impedance, "Zp / Zs = a²", "{0}²",
                n(ratio, ""), result = n(impedanceRatio, ""),
            ),
        ),
    )
}

/**
 * Per-unit: an impedance expressed against the system it sits in.
 *
 * The reason power engineers use it at all is that per-unit values survive a
 * transformer. An impedance in ohms means nothing until you say which side of
 * which winding it was measured on; the same impedance in per unit is the same
 * number everywhere, which is what makes a network with four voltage levels
 * solvable by hand.
 */
internal fun perUnit(input: TheoryInputs): TheorySolutionResult {
    val baseMva = input["s"]
    val baseKv = input["u"]
    val ohms = input["z"]

    val baseImpedance = baseKv * baseKv / baseMva
    val pu = ohms / baseImpedance
    val baseCurrent = baseMva * 1000.0 / (SQRT_THREE * baseKv)

    return TheorySolutionResult(
        primary = q(R.string.th_per_unit_out_pu, pu, "pu", style = NumberStyle.SIGNIFICANT),
        secondary = listOf(
            q(R.string.th_per_unit_out_base_impedance, baseImpedance, "Ω"),
            q(R.string.th_per_unit_out_base_current, baseCurrent, "A"),
            q(R.string.th_per_unit_out_percent, pu * 100.0, "%"),
        ),
        steps = listOf(
            step(
                R.string.th_per_unit_step_base, "Zbase = U² / S", "{0}² / {1}",
                n(baseKv, "kV"), n(baseMva, "MVA"), result = n(baseImpedance, "Ω"),
            ),
            step(
                R.string.th_per_unit_step_pu, "Zpu = Z / Zbase", "{0} / {1}",
                n(ohms, "Ω"), n(baseImpedance, "Ω"),
                result = n(pu, "pu", style = NumberStyle.SIGNIFICANT),
            ),
            step(
                R.string.th_per_unit_step_current, "Ibase = S / (√3 · U)", "{0} / (√3 × {1})",
                n(baseMva, "MVA"), n(baseKv, "kV"), result = n(baseCurrent, "A"),
            ),
        ),
    )
}

/** 2π/√2, the constant in the transformer EMF equation. */
private const val EMF_CONSTANT = 4.442882938158366

/** μ₀, the permeability of free space, in H/m. */
private const val VACUUM_PERMEABILITY = 1.25663706212e-6
