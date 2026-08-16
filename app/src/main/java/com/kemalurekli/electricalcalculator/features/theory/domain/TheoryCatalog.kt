package com.kemalurekli.electricalcalculator.features.theory.domain

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId

/**
 * Every theory topic the app ships.
 *
 * Ordered by [TheoryLevel] and, within a level, from the topic that assumes least
 * to the topic that assumes most. Scrolling the list screen is therefore the
 * curriculum — see [TheoryLevel] for why that ordering is a content decision.
 *
 * Adding a topic means adding one [TheoryTopic] here, its solver in
 * `TheorySolvers.kt` and its strings in both languages. There is no registration
 * step anywhere else: the list screen, the search index and every test iterate
 * [all].
 */
object TheoryCatalog {

    val all: List<TheoryTopic> = listOf(

        // ---- Foundation ---------------------------------------------------

        TheoryTopic(
            key = "ohm_law",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_ohm_law_title,
            summaryRes = R.string.th_ohm_law_summary,
            theoryRes = R.string.th_ohm_law_theory,
            assumptionsRes = listOf(
                R.string.th_ohm_law_assumption_linear,
                R.string.th_ohm_law_assumption_temperature,
                R.string.th_ohm_law_assumption_dc,
            ),
            diagram = TheoryDiagram.SIMPLE_LOOP,
            solutions = listOf(
                TheorySolution(
                    key = "current",
                    targetLabelRes = R.string.th_ohm_law_solve_current,
                    formula = "I = U / R",
                    variables = listOf(
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                        TheoryVariable("R", R.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("r", R.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "lamp", R.string.th_ohm_law_example_lamp,
                            mapOf("u" to "230", "r" to "529"),
                        ),
                        TheoryExample(
                            "heater", R.string.th_ohm_law_example_heater,
                            mapOf("u" to "230", "r" to "26.5"),
                        ),
                    ),
                    solve = ::ohmForCurrent,
                ),
                TheorySolution(
                    key = "voltage",
                    targetLabelRes = R.string.th_ohm_law_solve_voltage,
                    formula = "U = I · R",
                    variables = listOf(
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                        TheoryVariable("R", R.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("i", R.string.th_field_current, "A", min = 0.0),
                        TheoryField("r", R.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "shunt", R.string.th_ohm_law_example_shunt,
                            mapOf("i" to "10", "r" to "0.01"),
                        ),
                    ),
                    solve = ::ohmForVoltage,
                ),
                TheorySolution(
                    key = "resistance",
                    targetLabelRes = R.string.th_ohm_law_solve_resistance,
                    formula = "R = U / I",
                    variables = listOf(
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                        TheoryVariable("R", R.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("i", R.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "element", R.string.th_ohm_law_example_element,
                            mapOf("u" to "230", "i" to "8.7"),
                        ),
                    ),
                    solve = ::ohmForResistance,
                ),
            ),
            glossaryTerms = listOf("voltage_drop", "impedance"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),

        TheoryTopic(
            key = "dc_power",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_dc_power_title,
            summaryRes = R.string.th_dc_power_summary,
            theoryRes = R.string.th_dc_power_theory,
            assumptionsRes = listOf(
                R.string.th_dc_power_assumption_dc,
                R.string.th_dc_power_assumption_resistive,
            ),
            diagram = TheoryDiagram.SIMPLE_LOOP,
            solutions = listOf(
                TheorySolution(
                    key = "power",
                    targetLabelRes = R.string.th_dc_power_solve_power,
                    formula = "P = U · I = I² · R = U² / R",
                    variables = listOf(
                        TheoryVariable("P", R.string.th_var_power, "W"),
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                        TheoryVariable("R", R.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("i", R.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "led_strip", R.string.th_dc_power_example_led,
                            mapOf("u" to "12", "i" to "1.5"),
                        ),
                        TheoryExample(
                            "kettle", R.string.th_dc_power_example_kettle,
                            mapOf("u" to "230", "i" to "9.5"),
                        ),
                    ),
                    solve = ::powerFromVoltageAndCurrent,
                ),
                TheorySolution(
                    key = "current",
                    targetLabelRes = R.string.th_dc_power_solve_current,
                    formula = "I = P / U",
                    variables = listOf(
                        TheoryVariable("P", R.string.th_var_power, "W"),
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("p", R.string.th_field_power, "W", min = 0.0),
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "nameplate", R.string.th_dc_power_example_nameplate,
                            mapOf("p" to "2200", "u" to "230"),
                        ),
                    ),
                    solve = ::currentFromPower,
                ),
                TheorySolution(
                    key = "voltage",
                    targetLabelRes = R.string.th_dc_power_solve_voltage,
                    formula = "U = P / I",
                    variables = listOf(
                        TheoryVariable("P", R.string.th_var_power, "W"),
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("p", R.string.th_field_power, "W", min = 0.0),
                        TheoryField("i", R.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "motor", R.string.th_dc_power_example_motor,
                            mapOf("p" to "750", "i" to "3.2"),
                        ),
                    ),
                    solve = ::voltageFromPower,
                ),
            ),
            glossaryTerms = listOf("active_power", "apparent_power"),
            calculator = CalculatorId.POWER,
        ),

        TheoryTopic(
            key = "series_resistance",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_series_title,
            summaryRes = R.string.th_series_summary,
            theoryRes = R.string.th_series_theory,
            assumptionsRes = listOf(
                R.string.th_series_assumption_one_path,
                R.string.th_series_assumption_ideal_wire,
            ),
            diagram = TheoryDiagram.SERIES_RESISTORS,
            solutions = listOf(
                TheorySolution(
                    key = "total",
                    targetLabelRes = R.string.th_series_solve_total,
                    formula = "R_t = R₁ + R₂ + R₃\nI = U / R_t\nU_n = I · R_n",
                    variables = listOf(
                        TheoryVariable("R_t", R.string.th_var_total_resistance, "Ω"),
                        TheoryVariable("I", R.string.th_var_current, "A"),
                        TheoryVariable("U_n", R.string.th_var_branch_drop, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "24"),
                        TheoryField("r1", R.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", R.string.th_field_resistance_two, "Ω", min = 0.0),
                        TheoryField(
                            "r3", R.string.th_field_resistance_three, "Ω", min = 0.0,
                            optional = true, hintRes = R.string.th_field_optional_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "two_resistors", R.string.th_series_example_two,
                            mapOf("u" to "24", "r1" to "100", "r2" to "220"),
                        ),
                        TheoryExample(
                            "three_resistors", R.string.th_series_example_three,
                            mapOf("u" to "24", "r1" to "100", "r2" to "220", "r3" to "470"),
                        ),
                    ),
                    solve = ::seriesResistance,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "parallel_resistance",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_parallel_title,
            summaryRes = R.string.th_parallel_summary,
            theoryRes = R.string.th_parallel_theory,
            assumptionsRes = listOf(
                R.string.th_parallel_assumption_same_nodes,
                R.string.th_parallel_assumption_ideal_wire,
            ),
            diagram = TheoryDiagram.PARALLEL_RESISTORS,
            solutions = listOf(
                TheorySolution(
                    key = "total",
                    targetLabelRes = R.string.th_parallel_solve_total,
                    formula = "1/R_t = 1/R₁ + 1/R₂ + 1/R₃\nI_n = U / R_n",
                    variables = listOf(
                        TheoryVariable("R_t", R.string.th_var_total_resistance, "Ω"),
                        TheoryVariable("I_n", R.string.th_var_branch_current, "A"),
                        TheoryVariable("U", R.string.th_var_voltage, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("r1", R.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", R.string.th_field_resistance_two, "Ω", min = 0.0),
                        TheoryField(
                            "r3", R.string.th_field_resistance_three, "Ω", min = 0.0,
                            optional = true, hintRes = R.string.th_field_optional_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "two_lamps", R.string.th_parallel_example_two,
                            mapOf("u" to "230", "r1" to "529", "r2" to "529"),
                        ),
                        TheoryExample(
                            "mixed_load", R.string.th_parallel_example_mixed,
                            mapOf("u" to "230", "r1" to "529", "r2" to "26.5", "r3" to "96"),
                        ),
                    ),
                    solve = ::parallelResistance,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "voltage_divider",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_divider_title,
            summaryRes = R.string.th_divider_summary,
            theoryRes = R.string.th_divider_theory,
            assumptionsRes = listOf(
                R.string.th_divider_assumption_no_load,
                R.string.th_divider_assumption_stiff_source,
                R.string.th_divider_assumption_not_a_supply,
            ),
            diagram = TheoryDiagram.VOLTAGE_DIVIDER,
            solutions = listOf(
                TheorySolution(
                    key = "output",
                    targetLabelRes = R.string.th_divider_solve_output,
                    formula = "U_out = U · R₂ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("U", R.string.th_var_supply_voltage, "V"),
                        TheoryVariable("U_out", R.string.th_var_output_voltage, "V"),
                        TheoryVariable("R_L", R.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("r1", R.string.th_field_divider_upper, "Ω", min = 0.0),
                        TheoryField("r2", R.string.th_field_divider_lower, "Ω", min = 0.0),
                        TheoryField(
                            "r_load", R.string.th_field_load_resistance, "Ω", min = 0.0,
                            optional = true, hintRes = R.string.th_field_load_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "unloaded", R.string.th_divider_example_unloaded,
                            mapOf("u" to "12", "r1" to "10000", "r2" to "10000"),
                        ),
                        TheoryExample(
                            "loaded", R.string.th_divider_example_loaded,
                            mapOf("u" to "12", "r1" to "10000", "r2" to "10000", "r_load" to "10000"),
                        ),
                        TheoryExample(
                            "stiff", R.string.th_divider_example_stiff,
                            mapOf("u" to "12", "r1" to "100", "r2" to "100", "r_load" to "10000"),
                        ),
                    ),
                    solve = ::voltageDivider,
                ),
            ),
            glossaryTerms = listOf("voltage_drop"),
        ),

        TheoryTopic(
            key = "current_divider",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_current_divider_title,
            summaryRes = R.string.th_current_divider_summary,
            theoryRes = R.string.th_current_divider_theory,
            assumptionsRes = listOf(
                R.string.th_current_divider_assumption_two_branches,
                R.string.th_current_divider_assumption_resistive,
            ),
            diagram = TheoryDiagram.CURRENT_DIVIDER,
            solutions = listOf(
                TheorySolution(
                    key = "branches",
                    targetLabelRes = R.string.th_current_divider_solve_branches,
                    formula = "I₁ = I · R₂ / (R₁ + R₂)\nI₂ = I · R₁ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("I", R.string.th_var_supply_current, "A"),
                        TheoryVariable("I₁", R.string.th_var_branch_one_current, "A"),
                        TheoryVariable("I₂", R.string.th_var_branch_two_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("i", R.string.th_field_supply_current, "A", min = 0.0),
                        TheoryField("r1", R.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", R.string.th_field_resistance_two, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", R.string.th_current_divider_example_equal,
                            mapOf("i" to "10", "r1" to "100", "r2" to "100"),
                        ),
                        TheoryExample(
                            "uneven", R.string.th_current_divider_example_uneven,
                            mapOf("i" to "10", "r1" to "10", "r2" to "90"),
                        ),
                    ),
                    solve = ::currentDivider,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "conductor_resistance",
            level = TheoryLevel.FOUNDATION,
            titleRes = R.string.th_conductor_title,
            summaryRes = R.string.th_conductor_summary,
            theoryRes = R.string.th_conductor_theory,
            assumptionsRes = listOf(
                R.string.th_conductor_assumption_dc,
                R.string.th_conductor_assumption_uniform,
                R.string.th_conductor_assumption_one_way,
            ),
            diagram = TheoryDiagram.CONDUCTOR_RUN,
            selectorLabelRes = R.string.th_selector_material,
            solutions = listOf(
                conductorSolution("copper", R.string.th_conductor_material_copper, ::copperResistance),
                conductorSolution(
                    "aluminium", R.string.th_conductor_material_aluminium, ::aluminiumResistance,
                ),
            ),
            glossaryTerms = listOf("resistivity", "conductor"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),

        // ---- Intermediate -------------------------------------------------

        TheoryTopic(
            key = "kirchhoff_laws",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_kirchhoff_title,
            summaryRes = R.string.th_kirchhoff_summary,
            theoryRes = R.string.th_kirchhoff_theory,
            assumptionsRes = listOf(
                R.string.th_kirchhoff_assumption_lumped,
                R.string.th_kirchhoff_assumption_signs,
            ),
            diagram = TheoryDiagram.NODE_AND_LOOP,
            selectorLabelRes = R.string.th_selector_law,
            solutions = listOf(
                TheorySolution(
                    key = "node",
                    targetLabelRes = R.string.th_kirchhoff_solve_node,
                    formula = "ΣI_in = ΣI_out",
                    variables = listOf(
                        TheoryVariable("I₁, I₂", R.string.th_var_currents_in, "A"),
                        TheoryVariable("I₃", R.string.th_var_current_out, "A"),
                        TheoryVariable("I₄", R.string.th_var_current_unknown, "A"),
                    ),
                    fields = listOf(
                        TheoryField("i_in1", R.string.th_field_current_in_one, "A", allowNegative = true),
                        TheoryField("i_in2", R.string.th_field_current_in_two, "A", allowNegative = true),
                        TheoryField("i_out1", R.string.th_field_current_out, "A", allowNegative = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "junction", R.string.th_kirchhoff_example_junction,
                            mapOf("i_in1" to "10", "i_in2" to "4", "i_out1" to "6"),
                        ),
                    ),
                    solve = ::kirchhoffNode,
                ),
                TheorySolution(
                    key = "loop",
                    targetLabelRes = R.string.th_kirchhoff_solve_loop,
                    formula = "ΣU_source = ΣU_drop",
                    variables = listOf(
                        TheoryVariable("U", R.string.th_var_supply_voltage, "V"),
                        TheoryVariable("U₁, U₂", R.string.th_var_known_drops, "V"),
                        TheoryVariable("U₃", R.string.th_var_drop_unknown, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("u1", R.string.th_field_drop_one, "V", allowZero = true),
                        TheoryField("u2", R.string.th_field_drop_two, "V", allowZero = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "three_drops", R.string.th_kirchhoff_example_drops,
                            mapOf("u" to "230", "u1" to "6.5", "u2" to "3.2"),
                        ),
                    ),
                    solve = ::kirchhoffLoop,
                ),
            ),
            glossaryTerms = listOf("voltage_drop"),
        ),

        TheoryTopic(
            key = "rms_and_peak",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_rms_title,
            summaryRes = R.string.th_rms_summary,
            theoryRes = R.string.th_rms_theory,
            assumptionsRes = listOf(
                R.string.th_rms_assumption_sinusoidal,
                R.string.th_rms_assumption_meter,
            ),
            diagram = TheoryDiagram.SINE_WAVE,
            selectorLabelRes = R.string.th_selector_known,
            solutions = listOf(
                sineSolution(
                    "from_rms", R.string.th_rms_solve_from_rms,
                    "Û = U · √2", "u_rms", R.string.th_field_rms,
                    "mains", R.string.th_rms_example_mains, "230", ::sineFromRms,
                ),
                sineSolution(
                    "from_peak", R.string.th_rms_solve_from_peak,
                    "U = Û / √2", "u_peak", R.string.th_field_peak,
                    "scope", R.string.th_rms_example_scope, "325", ::sineFromPeak,
                ),
                sineSolution(
                    "from_average", R.string.th_rms_solve_from_average,
                    "Û = U̅ · π / 2", "u_avg", R.string.th_field_average,
                    "meter", R.string.th_rms_example_meter, "207", ::sineFromAverage,
                ),
            ),
            calculator = CalculatorId.POWER,
        ),

        TheoryTopic(
            key = "capacitive_reactance",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_xc_title,
            summaryRes = R.string.th_xc_summary,
            theoryRes = R.string.th_xc_theory,
            assumptionsRes = listOf(
                R.string.th_xc_assumption_sinusoidal,
                R.string.th_xc_assumption_ideal,
                R.string.th_xc_assumption_phase,
            ),
            diagram = TheoryDiagram.AC_CAPACITOR,
            solutions = listOf(
                TheorySolution(
                    key = "reactance",
                    targetLabelRes = R.string.th_xc_solve_reactance,
                    formula = "X_C = 1 / (2 · π · f · C)",
                    variables = listOf(
                        TheoryVariable("X_C", R.string.th_var_capacitive_reactance, "Ω"),
                        TheoryVariable("f", R.string.th_var_frequency, "Hz"),
                        TheoryVariable("C", R.string.th_var_capacitance, "F"),
                    ),
                    fields = listOf(
                        TheoryField("f", R.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("c", R.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "correction", R.string.th_xc_example_correction,
                            mapOf("f" to "50", "c" to "100", "u" to "230"),
                        ),
                        TheoryExample(
                            "small", R.string.th_xc_example_small,
                            mapOf("f" to "50", "c" to "2.2", "u" to "230"),
                        ),
                    ),
                    solve = ::capacitiveReactance,
                ),
            ),
            glossaryTerms = listOf("reactive_power"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
        ),

        TheoryTopic(
            key = "inductive_reactance",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_xl_title,
            summaryRes = R.string.th_xl_summary,
            theoryRes = R.string.th_xl_theory,
            assumptionsRes = listOf(
                R.string.th_xl_assumption_sinusoidal,
                R.string.th_xl_assumption_ideal,
                R.string.th_xl_assumption_saturation,
            ),
            diagram = TheoryDiagram.AC_INDUCTOR,
            solutions = listOf(
                TheorySolution(
                    key = "reactance",
                    targetLabelRes = R.string.th_xl_solve_reactance,
                    formula = "X_L = 2 · π · f · L",
                    variables = listOf(
                        TheoryVariable("X_L", R.string.th_var_inductive_reactance, "Ω"),
                        TheoryVariable("f", R.string.th_var_frequency, "Hz"),
                        TheoryVariable("L", R.string.th_var_inductance, "H"),
                    ),
                    fields = listOf(
                        TheoryField("f", R.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("l", R.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "choke", R.string.th_xl_example_choke,
                            mapOf("f" to "50", "l" to "100", "u" to "230"),
                        ),
                        TheoryExample(
                            "coil", R.string.th_xl_example_coil,
                            mapOf("f" to "50", "l" to "500", "u" to "230"),
                        ),
                    ),
                    solve = ::inductiveReactance,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "ac_power_triangle",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_triangle_title,
            summaryRes = R.string.th_triangle_summary,
            theoryRes = R.string.th_triangle_theory,
            assumptionsRes = listOf(
                R.string.th_triangle_assumption_sinusoidal,
                R.string.th_triangle_assumption_displacement,
            ),
            diagram = TheoryDiagram.POWER_TRIANGLE,
            solutions = listOf(
                TheorySolution(
                    key = "triangle",
                    targetLabelRes = R.string.th_triangle_solve,
                    formula = "S = P / cos φ\nQ = √(S² − P²)",
                    variables = listOf(
                        TheoryVariable("S", R.string.th_var_apparent_power, "VA"),
                        TheoryVariable("P", R.string.th_var_active_power, "W"),
                        TheoryVariable("Q", R.string.th_var_reactive_power, "var"),
                        TheoryVariable("cos φ", R.string.th_var_power_factor, "—"),
                    ),
                    fields = listOf(
                        TheoryField("p", R.string.th_field_active_power, "kW", min = 0.0),
                        TheoryField(
                            "pf", R.string.th_field_power_factor, "—",
                            min = 0.0, max = 1.0, default = "0.85",
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "motor", R.string.th_triangle_example_motor,
                            mapOf("p" to "7.5", "pf" to "0.85"),
                        ),
                        TheoryExample(
                            "corrected", R.string.th_triangle_example_corrected,
                            mapOf("p" to "7.5", "pf" to "0.95"),
                        ),
                    ),
                    solve = ::powerTriangle,
                ),
            ),
            glossaryTerms = listOf("active_power", "reactive_power", "apparent_power"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
        ),

        TheoryTopic(
            key = "three_phase_star_delta",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_three_phase_title,
            summaryRes = R.string.th_three_phase_summary,
            theoryRes = R.string.th_three_phase_theory,
            assumptionsRes = listOf(
                R.string.th_three_phase_assumption_balanced,
                R.string.th_three_phase_assumption_sinusoidal,
            ),
            diagram = TheoryDiagram.STAR_DELTA_SUPPLY,
            selectorLabelRes = R.string.th_selector_connection,
            solutions = listOf(
                threePhaseSolution(
                    "star", R.string.th_three_phase_solve_star,
                    "U_ph = U_L / √3\nI_ph = I_L",
                    "board", R.string.th_three_phase_example_board, "32", ::starConnection,
                ),
                threePhaseSolution(
                    "delta", R.string.th_three_phase_solve_delta,
                    "U_ph = U_L\nI_ph = I_L / √3",
                    "motor", R.string.th_three_phase_example_motor, "14", ::deltaConnection,
                ),
            ),
            glossaryTerms = listOf("apparent_power"),
            calculator = CalculatorId.POWER,
        ),

        TheoryTopic(
            key = "rc_time_constant",
            level = TheoryLevel.INTERMEDIATE,
            titleRes = R.string.th_rc_title,
            summaryRes = R.string.th_rc_summary,
            theoryRes = R.string.th_rc_theory,
            assumptionsRes = listOf(
                R.string.th_rc_assumption_step,
                R.string.th_rc_assumption_ideal,
                R.string.th_rc_assumption_never,
            ),
            diagram = TheoryDiagram.RC_CHARGING,
            solutions = listOf(
                TheorySolution(
                    key = "charging",
                    targetLabelRes = R.string.th_rc_solve_charging,
                    formula = "τ = R · C\nu(t) = U · (1 − e^(−t/τ))",
                    variables = listOf(
                        TheoryVariable("τ", R.string.th_var_time_constant, "s"),
                        TheoryVariable("u(t)", R.string.th_var_capacitor_voltage, "V"),
                        TheoryVariable("t", R.string.th_var_elapsed_time, "s"),
                    ),
                    fields = listOf(
                        TheoryField("r", R.string.th_field_resistance, "Ω", min = 0.0),
                        TheoryField("c", R.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("t", R.string.th_field_time, "s", min = 0.0, allowZero = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "timer", R.string.th_rc_example_timer,
                            mapOf("r" to "100000", "c" to "10", "u" to "12", "t" to "1"),
                        ),
                        TheoryExample(
                            "debounce", R.string.th_rc_example_debounce,
                            mapOf("r" to "10000", "c" to "0.1", "u" to "5", "t" to "0.001"),
                        ),
                    ),
                    solve = ::rcTimeConstant,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        // ---- Advanced -----------------------------------------------------

        TheoryTopic(
            key = "rlc_impedance",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_rlc_title,
            summaryRes = R.string.th_rlc_summary,
            theoryRes = R.string.th_rlc_theory,
            assumptionsRes = listOf(
                R.string.th_rlc_assumption_series,
                R.string.th_rlc_assumption_sinusoidal,
                R.string.th_rlc_assumption_ideal,
            ),
            diagram = TheoryDiagram.SERIES_RLC,
            solutions = listOf(
                TheorySolution(
                    key = "impedance",
                    targetLabelRes = R.string.th_rlc_solve,
                    formula = "Z = √(R² + (X_L − X_C)²)\nφ = arctan((X_L − X_C) / R)",
                    variables = listOf(
                        TheoryVariable("Z", R.string.th_var_impedance, "Ω"),
                        TheoryVariable("X_L", R.string.th_var_inductive_reactance, "Ω"),
                        TheoryVariable("X_C", R.string.th_var_capacitive_reactance, "Ω"),
                        TheoryVariable("φ", R.string.th_var_phase_angle, "°"),
                    ),
                    fields = listOf(
                        TheoryField("r", R.string.th_field_resistance, "Ω", min = 0.0),
                        TheoryField("l", R.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("c", R.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("f", R.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("u", R.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "inductive", R.string.th_rlc_example_inductive,
                            mapOf("r" to "10", "l" to "200", "c" to "100", "f" to "50", "u" to "230"),
                        ),
                        TheoryExample(
                            "capacitive", R.string.th_rlc_example_capacitive,
                            mapOf("r" to "10", "l" to "10", "c" to "100", "f" to "50", "u" to "230"),
                        ),
                    ),
                    solve = ::rlcImpedance,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "series_resonance",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_resonance_title,
            summaryRes = R.string.th_resonance_summary,
            theoryRes = R.string.th_resonance_theory,
            assumptionsRes = listOf(
                R.string.th_resonance_assumption_series,
                R.string.th_resonance_assumption_ideal,
                R.string.th_resonance_assumption_voltage,
            ),
            diagram = TheoryDiagram.SERIES_RLC,
            solutions = listOf(
                TheorySolution(
                    key = "resonance",
                    targetLabelRes = R.string.th_resonance_solve,
                    formula = "f₀ = 1 / (2 · π · √(L · C))\nQ = (1/R) · √(L / C)",
                    variables = listOf(
                        TheoryVariable("f₀", R.string.th_var_resonant_frequency, "Hz"),
                        TheoryVariable("Q", R.string.th_var_quality_factor, "—"),
                        TheoryVariable("B", R.string.th_var_bandwidth, "Hz"),
                    ),
                    fields = listOf(
                        TheoryField("l", R.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("c", R.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("r", R.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "filter", R.string.th_resonance_example_filter,
                            mapOf("l" to "10", "c" to "1", "r" to "5"),
                        ),
                        TheoryExample(
                            "sharp", R.string.th_resonance_example_sharp,
                            mapOf("l" to "10", "c" to "1", "r" to "0.5"),
                        ),
                    ),
                    solve = ::seriesResonance,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "thevenin_equivalent",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_thevenin_title,
            summaryRes = R.string.th_thevenin_summary,
            theoryRes = R.string.th_thevenin_theory,
            assumptionsRes = listOf(
                R.string.th_thevenin_assumption_linear,
                R.string.th_thevenin_assumption_terminals,
                R.string.th_thevenin_assumption_internal,
            ),
            diagram = TheoryDiagram.THEVENIN,
            solutions = listOf(
                TheorySolution(
                    key = "equivalent",
                    targetLabelRes = R.string.th_thevenin_solve,
                    formula = "U_th = U · R₂ / (R₁ + R₂)\nR_th = R₁ · R₂ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("U_th", R.string.th_var_open_circuit_voltage, "V"),
                        TheoryVariable("R_th", R.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("R_L", R.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", R.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("r1", R.string.th_field_divider_upper, "Ω", min = 0.0),
                        TheoryField("r2", R.string.th_field_divider_lower, "Ω", min = 0.0),
                        TheoryField("r_load", R.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "divider", R.string.th_thevenin_example_divider,
                            mapOf("u" to "12", "r1" to "1000", "r2" to "1000", "r_load" to "1000"),
                        ),
                    ),
                    solve = ::theveninEquivalent,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        TheoryTopic(
            key = "norton_equivalent",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_norton_title,
            summaryRes = R.string.th_norton_summary,
            theoryRes = R.string.th_norton_theory,
            assumptionsRes = listOf(
                R.string.th_norton_assumption_same,
                R.string.th_norton_assumption_internal,
            ),
            diagram = TheoryDiagram.NORTON,
            solutions = listOf(
                TheorySolution(
                    key = "equivalent",
                    targetLabelRes = R.string.th_norton_solve,
                    formula = "I_N = U_th / R_th\nR_N = R_th",
                    variables = listOf(
                        TheoryVariable("I_N", R.string.th_var_short_circuit_current, "A"),
                        TheoryVariable("R_N", R.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("R_L", R.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u_th", R.string.th_field_thevenin_voltage, "V", min = 0.0),
                        TheoryField("r_th", R.string.th_field_thevenin_resistance, "Ω", min = 0.0),
                        TheoryField("r_load", R.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "converted", R.string.th_norton_example_converted,
                            mapOf("u_th" to "6", "r_th" to "500", "r_load" to "1000"),
                        ),
                    ),
                    solve = ::nortonEquivalent,
                ),
            ),
            glossaryTerms = listOf("prospective_short_circuit_current"),
        ),

        TheoryTopic(
            key = "max_power_transfer",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_mpt_title,
            summaryRes = R.string.th_mpt_summary,
            theoryRes = R.string.th_mpt_theory,
            assumptionsRes = listOf(
                R.string.th_mpt_assumption_signal,
                R.string.th_mpt_assumption_fixed_source,
                R.string.th_mpt_assumption_efficiency,
            ),
            diagram = TheoryDiagram.THEVENIN,
            solutions = listOf(
                TheorySolution(
                    key = "transfer",
                    targetLabelRes = R.string.th_mpt_solve,
                    formula = "P_L = I² · R_L\nP_max = U_th² / (4 · R_th)  at  R_L = R_th",
                    variables = listOf(
                        TheoryVariable("P_L", R.string.th_var_delivered_power, "W"),
                        TheoryVariable("R_th", R.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("η", R.string.th_var_efficiency, "%"),
                    ),
                    fields = listOf(
                        TheoryField("u_th", R.string.th_field_thevenin_voltage, "V", min = 0.0),
                        TheoryField("r_th", R.string.th_field_thevenin_resistance, "Ω", min = 0.0),
                        TheoryField("r_load", R.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "matched", R.string.th_mpt_example_matched,
                            mapOf("u_th" to "12", "r_th" to "50", "r_load" to "50"),
                        ),
                        TheoryExample(
                            "efficient", R.string.th_mpt_example_efficient,
                            mapOf("u_th" to "12", "r_th" to "50", "r_load" to "500"),
                        ),
                    ),
                    solve = ::maximumPowerTransfer,
                ),
            ),
            glossaryTerms = listOf("active_power"),
        ),

        TheoryTopic(
            key = "star_delta_transform",
            level = TheoryLevel.ADVANCED,
            titleRes = R.string.th_ydelta_title,
            summaryRes = R.string.th_ydelta_summary,
            theoryRes = R.string.th_ydelta_theory,
            assumptionsRes = listOf(
                R.string.th_ydelta_assumption_terminals,
                R.string.th_ydelta_assumption_resistive,
                R.string.th_ydelta_assumption_not_supply,
            ),
            diagram = TheoryDiagram.Y_DELTA_TRANSFORM,
            selectorLabelRes = R.string.th_selector_direction,
            solutions = listOf(
                TheorySolution(
                    key = "star_to_delta",
                    targetLabelRes = R.string.th_ydelta_solve_to_delta,
                    formula = "R_ab = (R_a·R_b + R_b·R_c + R_c·R_a) / R_c",
                    variables = listOf(
                        TheoryVariable("R_a, R_b, R_c", R.string.th_var_star_arms, "Ω"),
                        TheoryVariable("R_ab …", R.string.th_var_delta_arms, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("ra", R.string.th_field_star_a, "Ω", min = 0.0),
                        TheoryField("rb", R.string.th_field_star_b, "Ω", min = 0.0),
                        TheoryField("rc", R.string.th_field_star_c, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", R.string.th_ydelta_example_equal,
                            mapOf("ra" to "10", "rb" to "10", "rc" to "10"),
                        ),
                        TheoryExample(
                            "uneven", R.string.th_ydelta_example_uneven,
                            mapOf("ra" to "10", "rb" to "20", "rc" to "30"),
                        ),
                    ),
                    solve = ::starToDelta,
                ),
                TheorySolution(
                    key = "delta_to_star",
                    targetLabelRes = R.string.th_ydelta_solve_to_star,
                    formula = "R_a = R_ab · R_ca / (R_ab + R_bc + R_ca)",
                    variables = listOf(
                        TheoryVariable("R_ab …", R.string.th_var_delta_arms, "Ω"),
                        TheoryVariable("R_a, R_b, R_c", R.string.th_var_star_arms, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("rab", R.string.th_field_delta_ab, "Ω", min = 0.0),
                        TheoryField("rbc", R.string.th_field_delta_bc, "Ω", min = 0.0),
                        TheoryField("rca", R.string.th_field_delta_ca, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", R.string.th_ydelta_example_delta_equal,
                            mapOf("rab" to "30", "rbc" to "30", "rca" to "30"),
                        ),
                    ),
                    solve = ::deltaToStar,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),
    )

    /** The two conductor materials differ only in which solver they call. */
    private fun conductorSolution(
        key: String,
        titleRes: Int,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabelRes = titleRes,
        formula = "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]\nR = ρ(θ) · L / A",
        variables = listOf(
            TheoryVariable("R", R.string.th_var_conductor_resistance, "Ω"),
            TheoryVariable("ρ(θ)", R.string.th_var_resistivity, "Ω·mm²/m"),
            TheoryVariable("L", R.string.th_var_length, "m"),
            TheoryVariable("A", R.string.th_var_area, "mm²"),
        ),
        fields = listOf(
            TheoryField("l", R.string.th_field_length, "m", min = 0.0),
            TheoryField("a", R.string.th_field_area, "mm²", min = 0.0),
            TheoryField(
                "theta", R.string.th_field_temperature, "°C",
                min = -50.0, max = 250.0, allowZero = true, allowNegative = true,
                default = "20", hintRes = R.string.th_field_temperature_hint,
            ),
        ),
        examples = listOf(
            TheoryExample(
                "radial", R.string.th_conductor_example_radial,
                mapOf("l" to "25", "a" to "2.5", "theta" to "70"),
            ),
            TheoryExample(
                "submain", R.string.th_conductor_example_submain,
                mapOf("l" to "60", "a" to "16", "theta" to "30"),
            ),
        ),
        solve = solve,
    )

    /** The three sine solutions differ only in which level the reader has. */
    private fun sineSolution(
        key: String,
        targetLabelRes: Int,
        formula: String,
        fieldKey: String,
        fieldLabelRes: Int,
        exampleKey: String,
        exampleTitleRes: Int,
        exampleValue: String,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabelRes = targetLabelRes,
        formula = formula,
        variables = listOf(
            TheoryVariable("U", R.string.th_var_rms, "V"),
            TheoryVariable("Û", R.string.th_var_peak, "V"),
            TheoryVariable("U̅", R.string.th_var_average, "V"),
        ),
        fields = listOf(TheoryField(fieldKey, fieldLabelRes, "V", min = 0.0)),
        examples = listOf(
            TheoryExample(exampleKey, exampleTitleRes, mapOf(fieldKey to exampleValue)),
        ),
        solve = solve,
    )

    /** Star and delta take the same readings and differ in what they mean. */
    private fun threePhaseSolution(
        key: String,
        targetLabelRes: Int,
        formula: String,
        exampleKey: String,
        exampleTitleRes: Int,
        exampleCurrent: String,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabelRes = targetLabelRes,
        formula = formula,
        variables = listOf(
            TheoryVariable("U_L", R.string.th_var_line_voltage, "V"),
            TheoryVariable("U_ph", R.string.th_var_phase_voltage, "V"),
            TheoryVariable("I_L", R.string.th_var_line_current, "A"),
            TheoryVariable("I_ph", R.string.th_var_phase_current, "A"),
        ),
        fields = listOf(
            TheoryField("u_line", R.string.th_field_line_voltage, "V", min = 0.0, default = "400"),
            TheoryField("i_line", R.string.th_field_line_current, "A", min = 0.0),
            TheoryField(
                "pf", R.string.th_field_power_factor, "—",
                min = 0.0, max = 1.0, default = "0.85",
            ),
        ),
        examples = listOf(
            TheoryExample(
                exampleKey, exampleTitleRes,
                mapOf("u_line" to "400", "i_line" to exampleCurrent, "pf" to "0.85"),
            ),
        ),
        solve = solve,
    )

    private val byKey: Map<String, TheoryTopic> = all.associateBy { it.key }

    /** The topic with this key, or null when nothing matches — an unknown deep link. */
    fun topicOrNull(key: String): TheoryTopic? = byKey[key]

    /** Every topic at one level, in catalog order. */
    fun inLevel(level: TheoryLevel): List<TheoryTopic> = all.filter { it.level == level }
}
