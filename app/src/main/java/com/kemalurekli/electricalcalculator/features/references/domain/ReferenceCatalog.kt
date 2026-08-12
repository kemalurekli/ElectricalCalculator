package com.kemalurekli.electricalcalculator.features.references.domain

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.symbol.AnsiSymbols
import com.kemalurekli.electricalcalculator.core.designsystem.symbol.ControlSymbols
import com.kemalurekli.electricalcalculator.core.designsystem.symbol.InstallationSymbols
import com.kemalurekli.electricalcalculator.core.designsystem.symbol.SingleLineSymbols
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.COMMISSIONING_AND_DIAGNOSIS
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.DRAWING_SYMBOLS
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.ENGINEERING_FOUNDATIONS
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.PROTECTION_AND_EARTHING
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.SELECTION_GUIDES
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory.TABLES_AND_CODES
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText.Localized
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText.Quantity
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText.Range
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText.Symbol
import com.kemalurekli.electricalcalculator.features.references.domain.SymbolPair

/**
 * The reference tables the app ships.
 *
 * ### What this content is, and what it is not
 *
 * These are transcriptions of published tables. Unlike the calculators, nothing
 * here can be derived or proven by a test — a wrong digit stays wrong however
 * many tests run — so each topic names the standard it follows and each table
 * that is *not* from a standard says so in a footnote.
 *
 * Where a figure already exists in the app's domain model it is read from
 * there rather than typed again, so a reference table cannot drift away from
 * the calculator that uses the same constant.
 */
object ReferenceCatalog {

    // -- IEC 60529, the IP code --------------------------------------------------

    private val ingressProtection = ReferenceTopic(
        key = "ip_rating",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_ip_title,
        descriptionRes = R.string.ref_ip_description,
        sourceRes = R.string.ref_ip_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_ip_first_digit,
                rows = listOf(
                    row("0", R.string.ref_ip_solid_0),
                    row("1", R.string.ref_ip_solid_1, R.string.ref_ip_solid_1_note),
                    row("2", R.string.ref_ip_solid_2, R.string.ref_ip_solid_2_note),
                    row("3", R.string.ref_ip_solid_3, R.string.ref_ip_solid_3_note),
                    row("4", R.string.ref_ip_solid_4, R.string.ref_ip_solid_4_note),
                    row("5", R.string.ref_ip_solid_5),
                    row("6", R.string.ref_ip_solid_6),
                ),
                footnoteRes = R.string.ref_ip_first_digit_note,
            ),
            tableSection(
                titleRes = R.string.ref_ip_second_digit,
                rows = listOf(
                    row("0", R.string.ref_ip_water_0),
                    row("1", R.string.ref_ip_water_1),
                    row("2", R.string.ref_ip_water_2),
                    row("3", R.string.ref_ip_water_3),
                    row("4", R.string.ref_ip_water_4),
                    row("5", R.string.ref_ip_water_5),
                    row("6", R.string.ref_ip_water_6),
                    row("7", R.string.ref_ip_water_7),
                    row("8", R.string.ref_ip_water_8),
                    row("9", R.string.ref_ip_water_9),
                ),
                footnoteRes = R.string.ref_ip_second_digit_note,
            ),
            tableSection(
                titleRes = R.string.ref_ip_common,
                rows = listOf(
                    row("IP20", R.string.ref_ip_common_ip20),
                    row("IP44", R.string.ref_ip_common_ip44),
                    row("IP54", R.string.ref_ip_common_ip54),
                    row("IP55", R.string.ref_ip_common_ip55),
                    row("IP65", R.string.ref_ip_common_ip65),
                    row("IP66", R.string.ref_ip_common_ip66),
                    row("IP67", R.string.ref_ip_common_ip67),
                    row("IP68", R.string.ref_ip_common_ip68),
                ),
            ),
        ),
    )

    // -- IEC 62262, the IK code ----------------------------------------------------

    private val impactProtection = ReferenceTopic(
        key = "ik_rating",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_ik_title,
        descriptionRes = R.string.ref_ik_description,
        sourceRes = R.string.ref_ik_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_ik_energy,
                rows = listOf(
                    ReferenceRow(Symbol("IK00"), Localized(R.string.ref_ik_00)),
                    impact("IK01", 0.14, 0.2, 70),
                    impact("IK02", 0.2, 0.2, 100),
                    impact("IK03", 0.35, 0.2, 175),
                    impact("IK04", 0.5, 0.2, 250),
                    impact("IK05", 0.7, 0.2, 350),
                    impact("IK06", 1.0, 0.5, 200),
                    impact("IK07", 2.0, 0.5, 400),
                    impact("IK08", 5.0, 1.7, 300),
                    impact("IK09", 10.0, 5.0, 200),
                    impact("IK10", 20.0, 5.0, 400),
                ),
                footnoteRes = R.string.ref_ik_note,
            ),
        ),
    )

    // -- IEC 60445 / IEC 60446, conductor identification -----------------------------

    private val conductorColours = ReferenceTopic(
        key = "conductor_colours",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_colours_title,
        descriptionRes = R.string.ref_colours_description,
        sourceRes = R.string.ref_colours_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_colours_iec_ac,
                rows = listOf(
                    row("L1", R.string.ref_colour_brown),
                    row("L2", R.string.ref_colour_black),
                    row("L3", R.string.ref_colour_grey),
                    row("N", R.string.ref_colour_blue),
                    row("PE", R.string.ref_colour_green_yellow),
                    row("PEN", R.string.ref_colour_blue_green_yellow, R.string.ref_colours_pen_note),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_colours_iec_dc,
                rows = listOf(
                    row("L+", R.string.ref_colour_brown),
                    row("L−", R.string.ref_colour_grey),
                    row("M", R.string.ref_colour_blue, R.string.ref_colours_dc_mid_note),
                    row("PE", R.string.ref_colour_green_yellow),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_colours_uk_old,
                rows = listOf(
                    row("L", R.string.ref_colour_red),
                    row("N", R.string.ref_colour_black),
                    row("E", R.string.ref_colour_green_or_green_yellow),
                ),
                footnoteRes = R.string.ref_colours_uk_old_note,
            ),
            tableSection(
                titleRes = R.string.ref_colours_us,
                rows = listOf(
                    row("120/208 V", R.string.ref_colours_us_low, R.string.ref_colours_us_low_note),
                    row("277/480 V", R.string.ref_colours_us_high, R.string.ref_colours_us_high_note),
                ),
                footnoteRes = R.string.ref_colours_us_note,
            ),
        ),
    )

    // -- Material constants ------------------------------------------------------------

    private val materials = ReferenceTopic(
        key = "materials",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_materials_title,
        descriptionRes = R.string.ref_materials_description,
        sourceRes = R.string.ref_materials_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_materials_resistivity,
                // Read from the domain model the calculators use, so a figure
                // quoted here cannot disagree with a figure they compute with.
                rows = ConductorMaterial.entries.map { material ->
                    ReferenceRow(
                        label = Localized(material.labelRes()),
                        value = Quantity(material.resistivityAt20C, "Ω·mm²/m"),
                        note = Quantity(material.temperatureCoefficient, "1/K α₂₀"),
                    )
                },
                footnoteRes = R.string.ref_materials_resistivity_note,
            ),
            tableSection(
                titleRes = R.string.ref_materials_density,
                rows = ConductorMaterial.entries.map { material ->
                    ReferenceRow(
                        label = Localized(material.labelRes()),
                        value = Quantity(material.densityKgPerDm3, "kg/dm³"),
                        note = Quantity(
                            material.oneSquareMillimetrePerKilometre(),
                            "kg/km per mm²",
                        ),
                    )
                },
            ),
            tableSection(
                titleRes = R.string.ref_materials_conductivity,
                rows = listOf(
                    ReferenceRow(
                        label = Localized(R.string.common_material_copper),
                        value = Quantity(58.0, "MS/m", 2),
                        note = Localized(R.string.ref_materials_iacs_copper),
                    ),
                    ReferenceRow(
                        label = Localized(R.string.common_material_aluminium),
                        value = Quantity(35.4, "MS/m", 3),
                        note = Localized(R.string.ref_materials_iacs_aluminium),
                    ),
                ),
            ),
        ),
    )

    // -- IEC 60038, standard voltages ------------------------------------------------

    private val standardVoltages = ReferenceTopic(
        key = "standard_voltages",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_voltages_title,
        descriptionRes = R.string.ref_voltages_description,
        sourceRes = R.string.ref_voltages_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_voltages_lv,
                rows = listOf(
                    ReferenceRow(Symbol("230/400 V"), Symbol("50 Hz"), Localized(R.string.ref_voltages_region_iec)),
                    ReferenceRow(Symbol("400/690 V"), Symbol("50 Hz"), Localized(R.string.ref_voltages_industrial)),
                    ReferenceRow(Symbol("120/240 V"), Symbol("60 Hz"), Localized(R.string.ref_voltages_region_na)),
                    ReferenceRow(Symbol("120/208 V"), Symbol("60 Hz"), Localized(R.string.ref_voltages_region_na)),
                    ReferenceRow(Symbol("277/480 V"), Symbol("60 Hz"), Localized(R.string.ref_voltages_region_na)),
                ),
                footnoteRes = R.string.ref_voltages_lv_note,
            ),
            tableSection(
                titleRes = R.string.ref_voltages_mv,
                rows = listOf(
                    ReferenceRow(Quantity(3.3, "kV", 2), Localized(R.string.ref_voltages_mv_series)),
                    ReferenceRow(Quantity(6.6, "kV", 2), Localized(R.string.ref_voltages_mv_series)),
                    ReferenceRow(Symbol("11 kV"), Localized(R.string.ref_voltages_mv_series)),
                    ReferenceRow(Symbol("22 kV"), Localized(R.string.ref_voltages_mv_series)),
                    ReferenceRow(Symbol("33 kV"), Localized(R.string.ref_voltages_mv_series)),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_voltages_tolerance,
                rows = listOf(
                    ReferenceRow(Symbol("±10 %"), Localized(R.string.ref_voltages_tolerance_supply)),
                    ReferenceRow(Symbol("3 %"), Localized(R.string.ref_voltages_drop_lighting)),
                    ReferenceRow(Symbol("5 %"), Localized(R.string.ref_voltages_drop_other)),
                ),
                footnoteRes = R.string.ref_voltages_tolerance_note,
            ),
        ),
    )

    // -- Quantities and their symbols --------------------------------------------------

    private val symbols = ReferenceTopic(
        key = "symbols",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_symbols_title,
        descriptionRes = R.string.ref_symbols_description,
        sourceRes = R.string.ref_symbols_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_symbols_quantities,
                rows = listOf(
                    ReferenceRow(Symbol("U, V"), Localized(R.string.ref_symbol_voltage), Symbol("V")),
                    ReferenceRow(Symbol("I"), Localized(R.string.ref_symbol_current), Symbol("A")),
                    ReferenceRow(Symbol("R"), Localized(R.string.ref_symbol_resistance), Symbol("Ω")),
                    ReferenceRow(Symbol("X"), Localized(R.string.ref_symbol_reactance), Symbol("Ω")),
                    ReferenceRow(Symbol("Z"), Localized(R.string.ref_symbol_impedance), Symbol("Ω")),
                    ReferenceRow(Symbol("P"), Localized(R.string.ref_symbol_active_power), Symbol("W")),
                    ReferenceRow(Symbol("Q"), Localized(R.string.ref_symbol_reactive_power), Symbol("var")),
                    ReferenceRow(Symbol("S"), Localized(R.string.ref_symbol_apparent_power), Symbol("VA")),
                    ReferenceRow(Symbol("cos φ"), Localized(R.string.ref_symbol_power_factor), Symbol("—")),
                    ReferenceRow(Symbol("f"), Localized(R.string.ref_symbol_frequency), Symbol("Hz")),
                    ReferenceRow(Symbol("C"), Localized(R.string.ref_symbol_capacitance), Symbol("F")),
                    ReferenceRow(Symbol("L"), Localized(R.string.ref_symbol_inductance), Symbol("H")),
                    ReferenceRow(Symbol("ρ"), Localized(R.string.ref_symbol_resistivity), Symbol("Ω·mm²/m")),
                    ReferenceRow(Symbol("γ, σ"), Localized(R.string.ref_symbol_conductivity), Symbol("S/m")),
                    ReferenceRow(Symbol("W"), Localized(R.string.ref_symbol_energy), Symbol("J, Wh")),
                    ReferenceRow(Symbol("η"), Localized(R.string.ref_symbol_efficiency), Symbol("—")),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_symbols_prefixes,
                rows = listOf(
                    ReferenceRow(Symbol("p"), Symbol("10⁻¹²"), Localized(R.string.ref_prefix_pico)),
                    ReferenceRow(Symbol("n"), Symbol("10⁻⁹"), Localized(R.string.ref_prefix_nano)),
                    ReferenceRow(Symbol("µ"), Symbol("10⁻⁶"), Localized(R.string.ref_prefix_micro)),
                    ReferenceRow(Symbol("m"), Symbol("10⁻³"), Localized(R.string.ref_prefix_milli)),
                    ReferenceRow(Symbol("k"), Symbol("10³"), Localized(R.string.ref_prefix_kilo)),
                    ReferenceRow(Symbol("M"), Symbol("10⁶"), Localized(R.string.ref_prefix_mega)),
                    ReferenceRow(Symbol("G"), Symbol("10⁹"), Localized(R.string.ref_prefix_giga)),
                ),
            ),
        ),
    )

    // -- Typical power factors ------------------------------------------------------------

    private val powerFactors = ReferenceTopic(
        key = "power_factors",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_pf_title,
        descriptionRes = R.string.ref_pf_description,
        sourceRes = R.string.ref_pf_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_pf_loads,
                rows = listOf(
                    ReferenceRow(Localized(R.string.ref_pf_incandescent), Quantity(1.0)),
                    ReferenceRow(Localized(R.string.ref_pf_heating), Quantity(1.0)),
                    ReferenceRow(Localized(R.string.ref_pf_fluorescent_uncorrected), Quantity(0.5)),
                    ReferenceRow(Localized(R.string.ref_pf_fluorescent_corrected), Quantity(0.9)),
                    ReferenceRow(Localized(R.string.ref_pf_led_driver), Range(0.9, 0.95)),
                    ReferenceRow(Localized(R.string.ref_pf_motor_full), Range(0.8, 0.85)),
                    ReferenceRow(Localized(R.string.ref_pf_motor_half), Range(0.7, 0.75)),
                    ReferenceRow(Localized(R.string.ref_pf_motor_idle), Range(0.15, 0.2)),
                    ReferenceRow(Localized(R.string.ref_pf_welding_arc), Range(0.5, 0.7)),
                    ReferenceRow(Localized(R.string.ref_pf_induction_furnace), Range(0.6, 0.7)),
                    ReferenceRow(Localized(R.string.ref_pf_transformer_idle), Range(0.1, 0.2)),
                ),
                footnoteRes = R.string.ref_pf_note,
            ),
        ),
    )


    // -- IEC 60898-1, breaker characteristics ------------------------------------------

    private val breakerCurves = ReferenceTopic(
        key = "breaker_curves",
        category = PROTECTION_AND_EARTHING,
        titleRes = R.string.ref_curves_title,
        descriptionRes = R.string.ref_curves_description,
        sourceRes = R.string.ref_curves_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_curves_magnetic,
                rows = listOf(
                    ReferenceRow(Symbol("B"), Symbol("3 – 5 × In"), Localized(R.string.ref_curves_b_use)),
                    ReferenceRow(Symbol("C"), Symbol("5 – 10 × In"), Localized(R.string.ref_curves_c_use)),
                    ReferenceRow(Symbol("D"), Symbol("10 – 20 × In"), Localized(R.string.ref_curves_d_use)),
                ),
                footnoteRes = R.string.ref_curves_magnetic_note,
            ),
            tableSection(
                titleRes = R.string.ref_curves_thermal,
                rows = listOf(
                    ReferenceRow(Quantity(1.13, "× In"), Localized(R.string.ref_curves_non_tripping)),
                    ReferenceRow(Quantity(1.45, "× In"), Localized(R.string.ref_curves_tripping)),
                ),
                footnoteRes = R.string.ref_curves_thermal_note,
            ),
            tableSection(
                titleRes = R.string.ref_curves_breaking,
                rows = listOf(
                    ReferenceRow(Symbol("3 000 A"), Localized(R.string.ref_curves_domestic)),
                    ReferenceRow(Symbol("6 000 A"), Localized(R.string.ref_curves_common)),
                    ReferenceRow(Symbol("10 000 A"), Localized(R.string.ref_curves_industrial)),
                    ReferenceRow(Symbol("Icu / Ics"), Localized(R.string.ref_curves_mccb)),
                ),
                footnoteRes = R.string.ref_curves_breaking_note,
            ),
        ),
    )

    // -- IEC 61008 / 61009, residual current devices ------------------------------------

    private val residualDevices = ReferenceTopic(
        key = "rcd_types",
        category = PROTECTION_AND_EARTHING,
        titleRes = R.string.ref_rcd_title,
        descriptionRes = R.string.ref_rcd_description,
        sourceRes = R.string.ref_rcd_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_rcd_types,
                rows = listOf(
                    row("AC", R.string.ref_rcd_ac, R.string.ref_rcd_ac_note),
                    row("A", R.string.ref_rcd_a, R.string.ref_rcd_a_note),
                    row("F", R.string.ref_rcd_f, R.string.ref_rcd_f_note),
                    row("B", R.string.ref_rcd_b, R.string.ref_rcd_b_note),
                ),
                footnoteRes = R.string.ref_rcd_types_note,
            ),
            tableSection(
                titleRes = R.string.ref_rcd_ratings,
                rows = listOf(
                    ReferenceRow(Symbol("10 mA"), Localized(R.string.ref_rcd_10ma)),
                    ReferenceRow(Symbol("30 mA"), Localized(R.string.ref_rcd_30ma)),
                    ReferenceRow(Symbol("100 mA"), Localized(R.string.ref_rcd_100ma)),
                    ReferenceRow(Symbol("300 mA"), Localized(R.string.ref_rcd_300ma)),
                ),
                footnoteRes = R.string.ref_rcd_ratings_note,
            ),
            tableSection(
                titleRes = R.string.ref_rcd_times,
                rows = listOf(
                    ReferenceRow(Symbol("IΔn"), Symbol("≤ 300 ms")),
                    ReferenceRow(Symbol("5 × IΔn"), Symbol("≤ 40 ms")),
                    ReferenceRow(Symbol("S"), Localized(R.string.ref_rcd_selective)),
                ),
                footnoteRes = R.string.ref_rcd_times_note,
            ),
        ),
    )

    // -- IEC 60364-1, earthing arrangements ----------------------------------------------

    private val earthingSystems = ReferenceTopic(
        key = "earthing_systems",
        category = PROTECTION_AND_EARTHING,
        titleRes = R.string.ref_earthing_title,
        descriptionRes = R.string.ref_earthing_description,
        sourceRes = R.string.ref_earthing_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_earthing_letters,
                rows = listOf(
                    row("T", R.string.ref_earthing_first_t, R.string.ref_earthing_first_position),
                    row("I", R.string.ref_earthing_first_i, R.string.ref_earthing_first_position),
                    row("T", R.string.ref_earthing_second_t, R.string.ref_earthing_second_position),
                    row("N", R.string.ref_earthing_second_n, R.string.ref_earthing_second_position),
                ),
                footnoteRes = R.string.ref_earthing_letters_note,
            ),
            tableSection(
                titleRes = R.string.ref_earthing_systems,
                rows = listOf(
                    row("TN-S", R.string.ref_earthing_tns, R.string.ref_earthing_tns_note),
                    row("TN-C", R.string.ref_earthing_tnc, R.string.ref_earthing_tnc_note),
                    row("TN-C-S", R.string.ref_earthing_tncs, R.string.ref_earthing_tncs_note),
                    row("TT", R.string.ref_earthing_tt, R.string.ref_earthing_tt_note),
                    row("IT", R.string.ref_earthing_it, R.string.ref_earthing_it_note),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_earthing_consequences,
                rows = listOf(
                    row("TN", R.string.ref_earthing_tn_protection),
                    row("TT", R.string.ref_earthing_tt_protection),
                    row("IT", R.string.ref_earthing_it_protection),
                ),
                footnoteRes = R.string.ref_earthing_consequences_note,
            ),
        ),
    )

    // -- IEC 60364-4-41, disconnection times ----------------------------------------------

    private val disconnectionTimes = ReferenceTopic(
        key = "disconnection_times",
        category = PROTECTION_AND_EARTHING,
        titleRes = R.string.ref_times_title,
        descriptionRes = R.string.ref_times_description,
        sourceRes = R.string.ref_times_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_times_final,
                rows = listOf(
                    ReferenceRow(Symbol("50 < U₀ ≤ 120 V"), Symbol("TN 0,8 s · TT 0,3 s")),
                    ReferenceRow(Symbol("120 < U₀ ≤ 230 V"), Symbol("TN 0,4 s · TT 0,2 s")),
                    ReferenceRow(Symbol("230 < U₀ ≤ 400 V"), Symbol("TN 0,2 s · TT 0,07 s")),
                    ReferenceRow(Symbol("U₀ > 400 V"), Symbol("TN 0,1 s · TT 0,04 s")),
                ),
                footnoteRes = R.string.ref_times_final_note,
            ),
            tableSection(
                titleRes = R.string.ref_times_distribution,
                rows = listOf(
                    ReferenceRow(Symbol("TN"), Symbol("5 s")),
                    ReferenceRow(Symbol("TT"), Symbol("1 s")),
                ),
                footnoteRes = R.string.ref_times_distribution_note,
            ),
        ),
    )

    // -- Preferred ratings -------------------------------------------------------------------

    private val ratingSeries = ReferenceTopic(
        key = "rating_series",
        category = PROTECTION_AND_EARTHING,
        titleRes = R.string.ref_ratings_title,
        descriptionRes = R.string.ref_ratings_description,
        sourceRes = R.string.ref_ratings_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_ratings_mcb,
                rows = listOf(
                    ReferenceRow(
                        Localized(R.string.ref_ratings_mcb_series),
                        Symbol("6 · 10 · 13 · 16 · 20 · 25 · 32 · 40 · 50 · 63 A"),
                    ),
                    ReferenceRow(
                        Localized(R.string.ref_ratings_mcb_large),
                        Symbol("80 · 100 · 125 A"),
                    ),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_ratings_fuse,
                rows = listOf(
                    ReferenceRow(
                        Localized(R.string.ref_ratings_fuse_series),
                        Symbol("2 · 4 · 6 · 10 · 16 · 20 · 25 · 32 · 40 · 50 · 63 A"),
                    ),
                    ReferenceRow(
                        Localized(R.string.ref_ratings_fuse_large),
                        Symbol("80 · 100 · 125 · 160 · 200 · 250 A"),
                    ),
                ),
                footnoteRes = R.string.ref_ratings_fuse_note,
            ),
            tableSection(
                titleRes = R.string.ref_ratings_coordination,
                rows = listOf(
                    ReferenceRow(Symbol("Ib ≤ In ≤ Iz"), Localized(R.string.ref_ratings_first)),
                    ReferenceRow(Symbol("I₂ ≤ 1,45 × Iz"), Localized(R.string.ref_ratings_second)),
                ),
                footnoteRes = R.string.ref_ratings_coordination_note,
            ),
        ),
    )

    /** Every topic, in the order the reference list presents them. */
    // -- IEC 60364-6, proving an installation ------------------------------------

    private val commissioningTests = ReferenceTopic(
        key = "commissioning_tests",
        category = COMMISSIONING_AND_DIAGNOSIS,
        titleRes = R.string.ref_commissioning_title,
        descriptionRes = R.string.ref_commissioning_description,
        sourceRes = R.string.ref_commissioning_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_commissioning_sequence,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_commissioning_sequence_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_commissioning_step_1,
                            R.string.ref_commissioning_step_2,
                            R.string.ref_commissioning_step_3,
                            R.string.ref_commissioning_step_4,
                            R.string.ref_commissioning_step_5,
                            R.string.ref_commissioning_step_6,
                            R.string.ref_commissioning_step_7,
                            R.string.ref_commissioning_step_8,
                            R.string.ref_commissioning_step_9,
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_commissioning_sequence_warning),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_commissioning_insulation,
                rows = listOf(
                    ReferenceRow(Localized(R.string.ref_commissioning_ins_selv), Symbol("250 V"), Quantity(0.5, "MΩ", 2)),
                    ReferenceRow(Localized(R.string.ref_commissioning_ins_500), Symbol("500 V"), Quantity(1.0, "MΩ", 2)),
                    ReferenceRow(Localized(R.string.ref_commissioning_ins_1000), Symbol("1 000 V"), Quantity(1.0, "MΩ", 2)),
                ),
                footnoteRes = R.string.ref_commissioning_insulation_note,
            ),
            tableSection(
                titleRes = R.string.ref_commissioning_rcd,
                rows = listOf(
                    ReferenceRow(Localized(R.string.ref_commissioning_rcd_general), Symbol("≤ 300 ms")),
                    ReferenceRow(Localized(R.string.ref_commissioning_rcd_general5), Symbol("≤ 40 ms")),
                    ReferenceRow(Localized(R.string.ref_commissioning_rcd_s), Symbol("130 – 500 ms")),
                    ReferenceRow(Localized(R.string.ref_commissioning_rcd_test), Localized(R.string.ref_commissioning_rcd_button)),
                ),
                footnoteRes = R.string.ref_commissioning_rcd_note,
            ),
            tableSection(
                titleRes = R.string.ref_commissioning_continuity,
                rows = listOf(
                    ReferenceRow(Symbol("R₁ + R₂"), Localized(R.string.ref_commissioning_r1r2_value)),
                    ReferenceRow(Symbol("Zs"), Localized(R.string.ref_commissioning_zs_value)),
                    ReferenceRow(Localized(R.string.ref_commissioning_bonding), Localized(R.string.ref_commissioning_bonding_value)),
                ),
            ),
        ),
    )

    // -- Working out why an installation misbehaves --------------------------------

    private val faultDiagnosis = ReferenceTopic(
        key = "fault_diagnosis",
        category = COMMISSIONING_AND_DIAGNOSIS,
        titleRes = R.string.ref_diagnosis_title,
        descriptionRes = R.string.ref_diagnosis_description,
        sourceRes = R.string.ref_diagnosis_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_diagnosis_rcd,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_diagnosis_rcd_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_diagnosis_rcd_1,
                            R.string.ref_diagnosis_rcd_2,
                            R.string.ref_diagnosis_rcd_3,
                            R.string.ref_diagnosis_rcd_4,
                            R.string.ref_diagnosis_rcd_5,
                            R.string.ref_diagnosis_rcd_6,
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_diagnosis_motor,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_diagnosis_motor_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_diagnosis_motor_1,
                            R.string.ref_diagnosis_motor_2,
                            R.string.ref_diagnosis_motor_3,
                            R.string.ref_diagnosis_motor_4,
                            R.string.ref_diagnosis_motor_5,
                            R.string.ref_diagnosis_motor_6,
                            R.string.ref_diagnosis_motor_7,
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_diagnosis_volts,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_diagnosis_volts_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_diagnosis_volts_1,
                            R.string.ref_diagnosis_volts_2,
                            R.string.ref_diagnosis_volts_3,
                            R.string.ref_diagnosis_volts_4,
                            R.string.ref_diagnosis_volts_5,
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_diagnosis_mcb,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_diagnosis_mcb_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_diagnosis_mcb_1,
                            R.string.ref_diagnosis_mcb_2,
                            R.string.ref_diagnosis_mcb_3,
                            R.string.ref_diagnosis_mcb_4,
                            R.string.ref_diagnosis_mcb_5,
                            R.string.ref_diagnosis_mcb_6,
                        ),
                    ),
                ),
            ),
        ),
    )

    // -- Defects that pass inspection ------------------------------------------------

    private val commonMistakes = ReferenceTopic(
        key = "common_mistakes",
        category = COMMISSIONING_AND_DIAGNOSIS,
        titleRes = R.string.ref_mistakes_title,
        descriptionRes = R.string.ref_mistakes_description,
        sourceRes = R.string.ref_mistakes_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_mistakes_intro_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_mistakes_intro)),
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_mistakes_item_1,
                            R.string.ref_mistakes_item_2,
                            R.string.ref_mistakes_item_3,
                            R.string.ref_mistakes_item_4,
                            R.string.ref_mistakes_item_5,
                            R.string.ref_mistakes_item_6,
                            R.string.ref_mistakes_item_7,
                            R.string.ref_mistakes_item_8,
                            R.string.ref_mistakes_item_9,
                            R.string.ref_mistakes_item_10,
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_mistakes_callout),
                ),
            ),
        ),
    )

    // -- Choosing between options ---------------------------------------------------

    private val selectionStarting = ReferenceTopic(
        key = "selection_starting",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_starting_title,
        descriptionRes = R.string.ref_sel_starting_description,
        sourceRes = R.string.ref_sel_starting_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_starting_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_starting_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_starting_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_starting_col_0, R.string.ref_sel_starting_col_1, R.string.ref_sel_starting_col_2, R.string.ref_sel_starting_col_3),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_0,
                            cells = listOf(Symbol("6 – 8 × In"), Symbol("2 – 3 × In"), Symbol("2 – 4 × In"), Symbol("≤ 1,5 × In")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_1,
                            cells = listOf(Symbol("100 %"), Symbol("33 %"), Symbol("25 – 50 %"), Symbol("≥ 100 %")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_2,
                            cells = listOf(Symbol("—"), Symbol("—"), Symbol("—"), Localized(R.string.ref_sel_starting_r2_c3)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_3,
                            cells = listOf(Symbol("1×"), Symbol("2×"), Symbol("4×"), Symbol("8×")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_4,
                            cells = listOf(Localized(R.string.ref_sel_starting_r4_c0), Localized(R.string.ref_sel_starting_r4_c1), Localized(R.string.ref_sel_starting_r4_c2), Localized(R.string.ref_sel_starting_r4_c3)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_starting_row_5,
                            cells = listOf(Localized(R.string.ref_sel_starting_r5_c0), Localized(R.string.ref_sel_starting_r5_c1), Localized(R.string.ref_sel_starting_r5_c2), Localized(R.string.ref_sel_starting_r5_c3)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.TIP, R.string.ref_sel_starting_callout),
                ),
            ),
        ),
    )

    private val selectionRcdtype = ReferenceTopic(
        key = "selection_rcdtype",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_rcdtype_title,
        descriptionRes = R.string.ref_sel_rcdtype_description,
        sourceRes = R.string.ref_sel_rcdtype_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_rcdtype_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_rcdtype_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_rcdtype_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_rcdtype_col_0, R.string.ref_sel_rcdtype_col_1, R.string.ref_sel_rcdtype_col_2, R.string.ref_sel_rcdtype_col_3),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_rcdtype_row_0,
                            cells = listOf(Symbol("✓"), Symbol("✓"), Symbol("✓"), Symbol("✓")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_rcdtype_row_1,
                            cells = listOf(Symbol("—"), Symbol("✓"), Symbol("✓"), Symbol("✓")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_rcdtype_row_2,
                            cells = listOf(Symbol("—"), Symbol("—"), Symbol("✓"), Symbol("✓")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_rcdtype_row_3,
                            cells = listOf(Symbol("—"), Symbol("—"), Symbol("—"), Symbol("✓")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_rcdtype_row_4,
                            cells = listOf(Localized(R.string.ref_sel_rcdtype_r4_c0), Localized(R.string.ref_sel_rcdtype_r4_c1), Localized(R.string.ref_sel_rcdtype_r4_c2), Localized(R.string.ref_sel_rcdtype_r4_c3)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.TIP, R.string.ref_sel_rcdtype_callout),
                ),
            ),
        ),
    )

    private val selectionInsulation = ReferenceTopic(
        key = "selection_insulation",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_insulation_title,
        descriptionRes = R.string.ref_sel_insulation_description,
        sourceRes = R.string.ref_sel_insulation_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_insulation_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_insulation_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_insulation_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_insulation_col_0, R.string.ref_sel_insulation_col_1, R.string.ref_sel_insulation_col_2, R.string.ref_sel_insulation_col_3),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_insulation_row_0,
                            cells = listOf(Symbol("70 °C"), Symbol("90 °C"), Symbol("90 °C"), Symbol("70 – 90 °C")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_insulation_row_1,
                            cells = listOf(Localized(R.string.ref_sel_insulation_r1_c0), Localized(R.string.ref_sel_insulation_r1_c1), Localized(R.string.ref_sel_insulation_r1_c2), Localized(R.string.ref_sel_insulation_r1_c3)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_insulation_row_2,
                            cells = listOf(Localized(R.string.ref_sel_insulation_r2_c0), Localized(R.string.ref_sel_insulation_r2_c1), Localized(R.string.ref_sel_insulation_r2_c2), Localized(R.string.ref_sel_insulation_r2_c3)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_insulation_row_3,
                            cells = listOf(Symbol("1×"), Symbol("1,2×"), Symbol("2×"), Symbol("1,8×")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_insulation_row_4,
                            cells = listOf(Localized(R.string.ref_sel_insulation_r4_c0), Localized(R.string.ref_sel_insulation_r4_c1), Localized(R.string.ref_sel_insulation_r4_c2), Localized(R.string.ref_sel_insulation_r4_c3)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_sel_insulation_callout),
                ),
            ),
        ),
    )

    private val selectionMaterial = ReferenceTopic(
        key = "selection_material",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_material_title,
        descriptionRes = R.string.ref_sel_material_description,
        sourceRes = R.string.ref_sel_material_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_material_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_material_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_material_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_material_col_0, R.string.ref_sel_material_col_1),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_0,
                            cells = listOf(Symbol("0,017241"), Symbol("0,028264")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_1,
                            cells = listOf(Symbol("8,89 kg/dm³"), Symbol("2,70 kg/dm³")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_2,
                            cells = listOf(Symbol("1×"), Symbol("≈ 1,5×")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_3,
                            cells = listOf(Symbol("1×"), Symbol("≈ 0,5×")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_4,
                            cells = listOf(Localized(R.string.ref_sel_material_r4_c0), Localized(R.string.ref_sel_material_r4_c1)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_material_row_5,
                            cells = listOf(Localized(R.string.ref_sel_material_r5_c0), Localized(R.string.ref_sel_material_r5_c1)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_sel_material_callout),
                ),
            ),
        ),
    )

    private val selectionEnclosure = ReferenceTopic(
        key = "selection_enclosure",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_enclosure_title,
        descriptionRes = R.string.ref_sel_enclosure_description,
        sourceRes = R.string.ref_sel_enclosure_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_enclosure_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_enclosure_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_enclosure_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_enclosure_col_0, R.string.ref_sel_enclosure_col_1, R.string.ref_sel_enclosure_col_2),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_0,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r0_c0), Symbol("IP20"), Symbol("IK07")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_1,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r1_c0), Symbol("IP44"), Symbol("IK07")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_2,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r2_c0), Symbol("IP65"), Symbol("IK08")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_3,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r3_c0), Symbol("IP54"), Symbol("IK10")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_4,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r4_c0), Symbol("IP69K"), Symbol("IK08")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_enclosure_row_5,
                            cells = listOf(Localized(R.string.ref_sel_enclosure_r5_c0), Symbol("IP6X"), Symbol("IK08")),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.TIP, R.string.ref_sel_enclosure_callout),
                ),
            ),
        ),
    )

    private val selectionBattery = ReferenceTopic(
        key = "selection_battery",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_battery_title,
        descriptionRes = R.string.ref_sel_battery_description,
        sourceRes = R.string.ref_sel_battery_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_battery_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_battery_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_battery_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_battery_col_0, R.string.ref_sel_battery_col_1, R.string.ref_sel_battery_col_2),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_0,
                            cells = listOf(Symbol("50 %"), Symbol("50 %"), Symbol("80 – 90 %")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_1,
                            cells = listOf(Symbol("1,1 – 1,3"), Symbol("1,2 – 1,4"), Symbol("≈ 1,05")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_2,
                            cells = listOf(Symbol("300 – 600"), Symbol("1 000 – 2 000"), Symbol("3 000 – 6 000")),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_3,
                            cells = listOf(Localized(R.string.ref_sel_battery_r3_c0), Localized(R.string.ref_sel_battery_r3_c1), Localized(R.string.ref_sel_battery_r3_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_4,
                            cells = listOf(Localized(R.string.ref_sel_battery_r4_c0), Localized(R.string.ref_sel_battery_r4_c1), Localized(R.string.ref_sel_battery_r4_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_battery_row_5,
                            cells = listOf(Localized(R.string.ref_sel_battery_r5_c0), Localized(R.string.ref_sel_battery_r5_c1), Localized(R.string.ref_sel_battery_r5_c2)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_sel_battery_callout),
                ),
            ),
        ),
    )

    private val selectionCabletype = ReferenceTopic(
        key = "selection_cabletype",
        category = SELECTION_GUIDES,
        titleRes = R.string.ref_sel_cabletype_title,
        descriptionRes = R.string.ref_sel_cabletype_description,
        sourceRes = R.string.ref_sel_cabletype_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sel_cabletype_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sel_cabletype_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sel_cabletype_table_title,
                blocks = listOf(
                    ReferenceBlock.Comparison(
                        columnsRes = listOf(R.string.ref_sel_cabletype_col_1, R.string.ref_sel_cabletype_col_2),
                        rows = listOf(
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_0,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r0_c1), Localized(R.string.ref_sel_cabletype_r0_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_1,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r1_c1), Localized(R.string.ref_sel_cabletype_r1_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_2,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r2_c1), Localized(R.string.ref_sel_cabletype_r2_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_3,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r3_c1), Localized(R.string.ref_sel_cabletype_r3_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_4,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r4_c1), Localized(R.string.ref_sel_cabletype_r4_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_5,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r5_c1), Localized(R.string.ref_sel_cabletype_r5_c2)),
                        ),
                        ComparisonRow(
                            labelRes = R.string.ref_sel_cabletype_row_6,
                            cells = listOf(Localized(R.string.ref_sel_cabletype_r6_c1), Localized(R.string.ref_sel_cabletype_r6_c2)),
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_sel_cabletype_callout),
                ),
            ),
        ),
    )

    // -- Background worth refreshing ---------------------------------------------------

    private val primerHarmonics = ReferenceTopic(
        key = "primer_harmonics",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_harmonics_title,
        descriptionRes = R.string.ref_pr_harmonics_description,
        sourceRes = R.string.ref_pr_harmonics_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_harmonics_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_harmonics_s0_p0, R.string.ref_pr_harmonics_s0_p1)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_harmonics_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Localized(R.string.ref_pr_harmonics_s1_r0_l), Symbol("3 · 5 · 7"), Localized(R.string.ref_pr_harmonics_s1_r0_n)),
                            ReferenceRow(Localized(R.string.ref_pr_harmonics_s1_r1_l), Symbol("5 · 7 · 11"), Localized(R.string.ref_pr_harmonics_s1_r1_n)),
                            ReferenceRow(Localized(R.string.ref_pr_harmonics_s1_r2_l), Symbol("3 · 5"), Localized(R.string.ref_pr_harmonics_s1_r2_n)),
                            ReferenceRow(Localized(R.string.ref_pr_harmonics_s1_r3_l), Symbol("—"), Localized(R.string.ref_pr_harmonics_s1_r3_n)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_harmonics_s2_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_harmonics_s2_p0)),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_pr_harmonics_callout),
                ),
            ),
        ),
    )

    private val primerSelectivity = ReferenceTopic(
        key = "primer_selectivity",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_selectivity_title,
        descriptionRes = R.string.ref_pr_selectivity_description,
        sourceRes = R.string.ref_pr_selectivity_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_selectivity_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_selectivity_s0_p0, R.string.ref_pr_selectivity_s0_p1)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_selectivity_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Localized(R.string.ref_pr_selectivity_s1_r0_l), Localized(R.string.ref_pr_selectivity_s1_r0_v), Localized(R.string.ref_pr_selectivity_s1_r0_n)),
                            ReferenceRow(Localized(R.string.ref_pr_selectivity_s1_r1_l), Localized(R.string.ref_pr_selectivity_s1_r1_v), Localized(R.string.ref_pr_selectivity_s1_r1_n)),
                            ReferenceRow(Localized(R.string.ref_pr_selectivity_s1_r2_l), Localized(R.string.ref_pr_selectivity_s1_r2_v), Localized(R.string.ref_pr_selectivity_s1_r2_n)),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_pr_selectivity_callout),
                ),
            ),
        ),
    )

    private val primerPowerquality = ReferenceTopic(
        key = "primer_powerquality",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_powerquality_title,
        descriptionRes = R.string.ref_pr_powerquality_description,
        sourceRes = R.string.ref_pr_powerquality_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_powerquality_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_powerquality_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_powerquality_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Localized(R.string.ref_pr_powerquality_s1_r0_l), Symbol("10 – 90 %, 10 ms – 1 min"), Localized(R.string.ref_pr_powerquality_s1_r0_n)),
                            ReferenceRow(Localized(R.string.ref_pr_powerquality_s1_r1_l), Symbol("> 110 %, 10 ms – 1 min"), Localized(R.string.ref_pr_powerquality_s1_r1_n)),
                            ReferenceRow(Localized(R.string.ref_pr_powerquality_s1_r2_l), Symbol("< 10 %"), Localized(R.string.ref_pr_powerquality_s1_r2_n)),
                            ReferenceRow(Localized(R.string.ref_pr_powerquality_s1_r3_l), Symbol("Pst, Plt"), Localized(R.string.ref_pr_powerquality_s1_r3_n)),
                            ReferenceRow(Localized(R.string.ref_pr_powerquality_s1_r4_l), Symbol("≤ 2 %"), Localized(R.string.ref_pr_powerquality_s1_r4_n)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_powerquality_s2_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_powerquality_s2_p0)),
                ),
            ),
        ),
    )

    private val primerBonding = ReferenceTopic(
        key = "primer_bonding",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_bonding_title,
        descriptionRes = R.string.ref_pr_bonding_description,
        sourceRes = R.string.ref_pr_bonding_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_bonding_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_bonding_s0_p0, R.string.ref_pr_bonding_s0_p1)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_bonding_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("PE"), Localized(R.string.ref_pr_bonding_s1_r0_v), Localized(R.string.ref_pr_bonding_s1_r0_n)),
                            ReferenceRow(Localized(R.string.ref_pr_bonding_s1_r1_l), Localized(R.string.ref_pr_bonding_s1_r1_v), Localized(R.string.ref_pr_bonding_s1_r1_n)),
                            ReferenceRow(Localized(R.string.ref_pr_bonding_s1_r2_l), Localized(R.string.ref_pr_bonding_s1_r2_v), Localized(R.string.ref_pr_bonding_s1_r2_n)),
                            ReferenceRow(Localized(R.string.ref_pr_bonding_s1_r3_l), Localized(R.string.ref_pr_bonding_s1_r3_v), Localized(R.string.ref_pr_bonding_s1_r3_n)),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_pr_bonding_callout),
                ),
            ),
        ),
    )

    private val primerCableanatomy = ReferenceTopic(
        key = "primer_cableanatomy",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_cableanatomy_title,
        descriptionRes = R.string.ref_pr_cableanatomy_description,
        sourceRes = R.string.ref_pr_cableanatomy_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_cableanatomy_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_cableanatomy_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_cableanatomy_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r0_l), Localized(R.string.ref_pr_cableanatomy_s1_r0_v), Localized(R.string.ref_pr_cableanatomy_s1_r0_n)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r1_l), Localized(R.string.ref_pr_cableanatomy_s1_r1_v), Localized(R.string.ref_pr_cableanatomy_s1_r1_n)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r2_l), Localized(R.string.ref_pr_cableanatomy_s1_r2_v)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r3_l), Localized(R.string.ref_pr_cableanatomy_s1_r3_v)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r4_l), Localized(R.string.ref_pr_cableanatomy_s1_r4_v), Localized(R.string.ref_pr_cableanatomy_s1_r4_n)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r5_l), Localized(R.string.ref_pr_cableanatomy_s1_r5_v), Localized(R.string.ref_pr_cableanatomy_s1_r5_n)),
                            ReferenceRow(Localized(R.string.ref_pr_cableanatomy_s1_r6_l), Localized(R.string.ref_pr_cableanatomy_s1_r6_v), Localized(R.string.ref_pr_cableanatomy_s1_r6_n)),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_pr_cableanatomy_callout),
                ),
            ),
        ),
    )

    private val primerNameplate = ReferenceTopic(
        key = "primer_nameplate",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_nameplate_title,
        descriptionRes = R.string.ref_pr_nameplate_description,
        sourceRes = R.string.ref_pr_nameplate_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_nameplate_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_nameplate_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_nameplate_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("kW / hp"), Localized(R.string.ref_pr_nameplate_s1_r0_v), Localized(R.string.ref_pr_nameplate_s1_r0_n)),
                            ReferenceRow(Symbol("S1 … S9"), Localized(R.string.ref_pr_nameplate_s1_r1_v), Localized(R.string.ref_pr_nameplate_s1_r1_n)),
                            ReferenceRow(Symbol("IE2 / IE3 / IE4"), Localized(R.string.ref_pr_nameplate_s1_r2_v)),
                            ReferenceRow(Symbol("SF"), Localized(R.string.ref_pr_nameplate_s1_r3_v), Localized(R.string.ref_pr_nameplate_s1_r3_n)),
                            ReferenceRow(Symbol("IP / IK"), Localized(R.string.ref_pr_nameplate_s1_r4_v)),
                            ReferenceRow(Symbol("Δ / Y"), Localized(R.string.ref_pr_nameplate_s1_r5_v), Localized(R.string.ref_pr_nameplate_s1_r5_n)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_nameplate_s2_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("kVA"), Localized(R.string.ref_pr_nameplate_s2_r0_v)),
                            ReferenceRow(Symbol("uk %"), Localized(R.string.ref_pr_nameplate_s2_r1_v), Localized(R.string.ref_pr_nameplate_s2_r1_n)),
                            ReferenceRow(Symbol("Dyn11"), Localized(R.string.ref_pr_nameplate_s2_r2_v), Localized(R.string.ref_pr_nameplate_s2_r2_n)),
                            ReferenceRow(Symbol("ONAN / AN"), Localized(R.string.ref_pr_nameplate_s2_r3_v), Localized(R.string.ref_pr_nameplate_s2_r3_n)),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val primerVectorgroup = ReferenceTopic(
        key = "primer_vectorgroup",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_vectorgroup_title,
        descriptionRes = R.string.ref_pr_vectorgroup_description,
        sourceRes = R.string.ref_pr_vectorgroup_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_vectorgroup_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_vectorgroup_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_vectorgroup_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("Dyn11"), Localized(R.string.ref_pr_vectorgroup_s1_r0_v), Localized(R.string.ref_pr_vectorgroup_s1_r0_n)),
                            ReferenceRow(Symbol("Dyn5"), Localized(R.string.ref_pr_vectorgroup_s1_r1_v), Localized(R.string.ref_pr_vectorgroup_s1_r1_n)),
                            ReferenceRow(Symbol("Yyn0"), Localized(R.string.ref_pr_vectorgroup_s1_r2_v), Localized(R.string.ref_pr_vectorgroup_s1_r2_n)),
                            ReferenceRow(Symbol("Dd0"), Localized(R.string.ref_pr_vectorgroup_s1_r3_v), Localized(R.string.ref_pr_vectorgroup_s1_r3_n)),
                            ReferenceRow(Symbol("Yzn11"), Localized(R.string.ref_pr_vectorgroup_s1_r4_v), Localized(R.string.ref_pr_vectorgroup_s1_r4_n)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_vectorgroup_s2_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_vectorgroup_s2_p0)),
                ),
            ),
        ),
    )

    private val primerHazardous = ReferenceTopic(
        key = "primer_hazardous",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_hazardous_title,
        descriptionRes = R.string.ref_pr_hazardous_description,
        sourceRes = R.string.ref_pr_hazardous_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_hazardous_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_hazardous_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_hazardous_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("Zone 0"), Localized(R.string.ref_pr_hazardous_s1_r0_v), Localized(R.string.ref_pr_hazardous_gas)),
                            ReferenceRow(Symbol("Zone 1"), Localized(R.string.ref_pr_hazardous_s1_r1_v), Localized(R.string.ref_pr_hazardous_gas)),
                            ReferenceRow(Symbol("Zone 2"), Localized(R.string.ref_pr_hazardous_s1_r2_v), Localized(R.string.ref_pr_hazardous_gas)),
                            ReferenceRow(Symbol("Zone 20"), Localized(R.string.ref_pr_hazardous_s1_r3_v), Localized(R.string.ref_pr_hazardous_dust)),
                            ReferenceRow(Symbol("Zone 21"), Localized(R.string.ref_pr_hazardous_s1_r4_v), Localized(R.string.ref_pr_hazardous_dust)),
                            ReferenceRow(Symbol("Zone 22"), Localized(R.string.ref_pr_hazardous_s1_r5_v), Localized(R.string.ref_pr_hazardous_dust)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_hazardous_s2_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("Ex d"), Localized(R.string.ref_pr_hazardous_s2_r0_v)),
                            ReferenceRow(Symbol("Ex e"), Localized(R.string.ref_pr_hazardous_s2_r1_v)),
                            ReferenceRow(Symbol("Ex i"), Localized(R.string.ref_pr_hazardous_s2_r2_v), Localized(R.string.ref_pr_hazardous_s2_r2_n)),
                            ReferenceRow(Symbol("Ex p"), Localized(R.string.ref_pr_hazardous_s2_r3_v)),
                            ReferenceRow(Symbol("Ex n"), Localized(R.string.ref_pr_hazardous_s2_r4_v)),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_pr_hazardous_callout),
                ),
            ),
        ),
    )

    private val primerSwitchboard = ReferenceTopic(
        key = "primer_switchboard",
        category = ENGINEERING_FOUNDATIONS,
        titleRes = R.string.ref_pr_switchboard_title,
        descriptionRes = R.string.ref_pr_switchboard_description,
        sourceRes = R.string.ref_pr_switchboard_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_pr_switchboard_s0_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_switchboard_s0_p0)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_switchboard_s1_title,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("Form 1"), Localized(R.string.ref_pr_switchboard_s1_r0_v)),
                            ReferenceRow(Symbol("Form 2"), Localized(R.string.ref_pr_switchboard_s1_r1_v)),
                            ReferenceRow(Symbol("Form 3"), Localized(R.string.ref_pr_switchboard_s1_r2_v)),
                            ReferenceRow(Symbol("Form 4"), Localized(R.string.ref_pr_switchboard_s1_r3_v), Localized(R.string.ref_pr_switchboard_s1_r3_n)),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_pr_switchboard_s2_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_pr_switchboard_s2_p0)),
                ),
            ),
        ),
    )

    // -- Drawn to IEC 60617 -------------------------------------------------------------

    private val singleLineSymbols = ReferenceTopic(
        key = "symbols_single_line",
        category = DRAWING_SYMBOLS,
        titleRes = R.string.ref_sym_single_title,
        descriptionRes = R.string.ref_sym_single_description,
        sourceRes = R.string.ref_sym_single_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sym_single_section,
                blocks = listOf(
                    ReferenceBlock.SymbolGrid(
                        listOf(
                        DrawingSymbol(
                            key = "conductor",
                            image = SingleLineSymbols.Conductor,
                            nameRes = R.string.ref_sym_conductor_name,
                        ),
                        DrawingSymbol(
                            key = "junction",
                            image = SingleLineSymbols.Junction,
                            nameRes = R.string.ref_sym_junction_name,
                            noteRes = R.string.ref_sym_junction_note,
                        ),
                        DrawingSymbol(
                            key = "crossing",
                            image = SingleLineSymbols.Crossing,
                            nameRes = R.string.ref_sym_crossing_name,
                        ),
                        DrawingSymbol(
                            key = "earth",
                            image = SingleLineSymbols.Earth,
                            nameRes = R.string.ref_sym_earth_name,
                        ),
                        DrawingSymbol(
                            key = "busbar",
                            image = SingleLineSymbols.Busbar,
                            nameRes = R.string.ref_sym_busbar_name,
                            designation = "W",
                        ),
                        DrawingSymbol(
                            key = "terminal",
                            image = SingleLineSymbols.Terminal,
                            nameRes = R.string.ref_sym_terminal_name,
                            designation = "X",
                        ),
                        DrawingSymbol(
                            key = "disconnector",
                            image = SingleLineSymbols.Disconnector,
                            nameRes = R.string.ref_sym_disconnector_name,
                            designation = "Q",
                            noteRes = R.string.ref_sym_disconnector_note,
                        ),
                        DrawingSymbol(
                            key = "switch_disconnector",
                            image = SingleLineSymbols.SwitchDisconnector,
                            nameRes = R.string.ref_sym_switch_disconnector_name,
                            designation = "Q",
                            noteRes = R.string.ref_sym_switch_disconnector_note,
                        ),
                        DrawingSymbol(
                            key = "circuit_breaker",
                            image = SingleLineSymbols.CircuitBreaker,
                            nameRes = R.string.ref_sym_circuit_breaker_name,
                            designation = "Q",
                            noteRes = R.string.ref_sym_circuit_breaker_note,
                        ),
                        DrawingSymbol(
                            key = "contactor",
                            image = SingleLineSymbols.Contactor,
                            nameRes = R.string.ref_sym_contactor_name,
                            designation = "K",
                            noteRes = R.string.ref_sym_contactor_note,
                        ),
                        DrawingSymbol(
                            key = "fuse",
                            image = SingleLineSymbols.Fuse,
                            nameRes = R.string.ref_sym_fuse_name,
                            designation = "F",
                        ),
                        DrawingSymbol(
                            key = "fuse_switch",
                            image = SingleLineSymbols.FuseSwitch,
                            nameRes = R.string.ref_sym_fuse_switch_name,
                            designation = "Q",
                        ),
                        DrawingSymbol(
                            key = "mcb",
                            image = SingleLineSymbols.MiniatureBreaker,
                            nameRes = R.string.ref_sym_mcb_name,
                            designation = "F",
                        ),
                        DrawingSymbol(
                            key = "rcd",
                            image = SingleLineSymbols.ResidualCurrentDevice,
                            nameRes = R.string.ref_sym_rcd_name,
                            designation = "F",
                            noteRes = R.string.ref_sym_rcd_note,
                        ),
                        DrawingSymbol(
                            key = "rcbo",
                            image = SingleLineSymbols.ResidualCurrentBreakerOverload,
                            nameRes = R.string.ref_sym_rcbo_name,
                            designation = "F",
                            noteRes = R.string.ref_sym_rcbo_note,
                        ),
                        DrawingSymbol(
                            key = "motor",
                            image = SingleLineSymbols.Motor,
                            nameRes = R.string.ref_sym_motor_name,
                            designation = "M",
                        ),
                        DrawingSymbol(
                            key = "generator",
                            image = SingleLineSymbols.Generator,
                            nameRes = R.string.ref_sym_generator_name,
                            designation = "G",
                        ),
                        DrawingSymbol(
                            key = "battery",
                            image = SingleLineSymbols.Battery,
                            nameRes = R.string.ref_sym_battery_name,
                            designation = "G",
                        ),
                        DrawingSymbol(
                            key = "transformer",
                            image = SingleLineSymbols.Transformer,
                            nameRes = R.string.ref_sym_transformer_name,
                            designation = "T",
                        ),
                        DrawingSymbol(
                            key = "autotransformer",
                            image = SingleLineSymbols.Autotransformer,
                            nameRes = R.string.ref_sym_autotransformer_name,
                            designation = "T",
                            noteRes = R.string.ref_sym_autotransformer_note,
                        ),
                        DrawingSymbol(
                            key = "current_transformer",
                            image = SingleLineSymbols.CurrentTransformer,
                            nameRes = R.string.ref_sym_current_transformer_name,
                            designation = "T",
                            noteRes = R.string.ref_sym_current_transformer_note,
                        ),
                        DrawingSymbol(
                            key = "voltage_transformer",
                            image = SingleLineSymbols.VoltageTransformer,
                            nameRes = R.string.ref_sym_voltage_transformer_name,
                            designation = "T",
                            noteRes = R.string.ref_sym_voltage_transformer_note,
                        ),
                        DrawingSymbol(
                            key = "spd",
                            image = SingleLineSymbols.SurgeProtectiveDevice,
                            nameRes = R.string.ref_sym_spd_name,
                            designation = "F",
                            noteRes = R.string.ref_sym_spd_note,
                        ),
                        DrawingSymbol(
                            key = "capacitor",
                            image = SingleLineSymbols.Capacitor,
                            nameRes = R.string.ref_sym_capacitor_name,
                            designation = "C",
                        ),
                        DrawingSymbol(
                            key = "resistor",
                            image = SingleLineSymbols.Resistor,
                            nameRes = R.string.ref_sym_resistor_name,
                            designation = "R",
                        ),
                        DrawingSymbol(
                            key = "inductor",
                            image = SingleLineSymbols.Inductor,
                            nameRes = R.string.ref_sym_inductor_name,
                            designation = "L",
                        ),
                        DrawingSymbol(
                            key = "rectifier",
                            image = SingleLineSymbols.Rectifier,
                            nameRes = R.string.ref_sym_rectifier_name,
                            designation = "T",
                            noteRes = R.string.ref_sym_rectifier_note,
                        ),
                        DrawingSymbol(
                            key = "inverter",
                            image = SingleLineSymbols.Inverter,
                            nameRes = R.string.ref_sym_inverter_name,
                            designation = "T",
                        ),
                        DrawingSymbol(
                            key = "drive",
                            image = SingleLineSymbols.Drive,
                            nameRes = R.string.ref_sym_drive_name,
                            designation = "T",
                        ),
                        DrawingSymbol(
                            key = "lamp",
                            image = SingleLineSymbols.Lamp,
                            nameRes = R.string.ref_sym_lamp_name,
                            designation = "E",
                        ),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.TIP, R.string.ref_sym_copyright),
                ),
            ),
        ),
    )

    private val controlSymbols = ReferenceTopic(
        key = "symbols_control",
        category = DRAWING_SYMBOLS,
        titleRes = R.string.ref_sym_control_title,
        descriptionRes = R.string.ref_sym_control_description,
        sourceRes = R.string.ref_sym_control_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sym_control_section,
                blocks = listOf(
                    ReferenceBlock.SymbolGrid(
                        listOf(
                        DrawingSymbol(
                            key = "contact_no",
                            image = ControlSymbols.ContactNormallyOpen,
                            nameRes = R.string.ref_sym_contact_no_name,
                            noteRes = R.string.ref_sym_contact_no_note,
                        ),
                        DrawingSymbol(
                            key = "contact_nc",
                            image = ControlSymbols.ContactNormallyClosed,
                            nameRes = R.string.ref_sym_contact_nc_name,
                            noteRes = R.string.ref_sym_contact_nc_note,
                        ),
                        DrawingSymbol(
                            key = "contact_changeover",
                            image = ControlSymbols.ContactChangeover,
                            nameRes = R.string.ref_sym_contact_changeover_name,
                        ),
                        DrawingSymbol(
                            key = "contact_delay_close",
                            image = ControlSymbols.ContactDelayedClosing,
                            nameRes = R.string.ref_sym_contact_delay_close_name,
                            noteRes = R.string.ref_sym_contact_delay_close_note,
                        ),
                        DrawingSymbol(
                            key = "contact_delay_open",
                            image = ControlSymbols.ContactDelayedOpening,
                            nameRes = R.string.ref_sym_contact_delay_open_name,
                        ),
                        DrawingSymbol(
                            key = "button_make",
                            image = ControlSymbols.PushButtonMake,
                            nameRes = R.string.ref_sym_button_make_name,
                        ),
                        DrawingSymbol(
                            key = "button_break",
                            image = ControlSymbols.PushButtonBreak,
                            nameRes = R.string.ref_sym_button_break_name,
                        ),
                        DrawingSymbol(
                            key = "emergency_stop",
                            image = ControlSymbols.EmergencyStop,
                            nameRes = R.string.ref_sym_emergency_stop_name,
                            noteRes = R.string.ref_sym_emergency_stop_note,
                        ),
                        DrawingSymbol(
                            key = "limit_switch",
                            image = ControlSymbols.LimitSwitch,
                            nameRes = R.string.ref_sym_limit_switch_name,
                        ),
                        DrawingSymbol(
                            key = "selector",
                            image = ControlSymbols.SelectorSwitch,
                            nameRes = R.string.ref_sym_selector_name,
                        ),
                        DrawingSymbol(
                            key = "coil",
                            image = ControlSymbols.Coil,
                            nameRes = R.string.ref_sym_coil_name,
                            designation = "K",
                            noteRes = R.string.ref_sym_coil_note,
                        ),
                        DrawingSymbol(
                            key = "coil_timed_on",
                            image = ControlSymbols.CoilTimedOn,
                            nameRes = R.string.ref_sym_coil_timed_on_name,
                            designation = "K",
                        ),
                        DrawingSymbol(
                            key = "coil_timed_off",
                            image = ControlSymbols.CoilTimedOff,
                            nameRes = R.string.ref_sym_coil_timed_off_name,
                            designation = "K",
                        ),
                        DrawingSymbol(
                            key = "thermal_overload",
                            image = ControlSymbols.ThermalOverload,
                            nameRes = R.string.ref_sym_thermal_overload_name,
                            designation = "F",
                            noteRes = R.string.ref_sym_thermal_overload_note,
                        ),
                        DrawingSymbol(
                            key = "indicator_lamp",
                            image = ControlSymbols.IndicatorLamp,
                            nameRes = R.string.ref_sym_indicator_lamp_name,
                            designation = "H",
                        ),
                        DrawingSymbol(
                            key = "horn",
                            image = ControlSymbols.Horn,
                            nameRes = R.string.ref_sym_horn_name,
                            designation = "H",
                        ),
                        DrawingSymbol(
                            key = "ammeter",
                            image = ControlSymbols.Ammeter,
                            nameRes = R.string.ref_sym_ammeter_name,
                            designation = "P",
                        ),
                        DrawingSymbol(
                            key = "voltmeter",
                            image = ControlSymbols.Voltmeter,
                            nameRes = R.string.ref_sym_voltmeter_name,
                            designation = "P",
                        ),
                        DrawingSymbol(
                            key = "energy_meter",
                            image = ControlSymbols.EnergyMeter,
                            nameRes = R.string.ref_sym_energy_meter_name,
                            designation = "P",
                        ),
                        DrawingSymbol(
                            key = "sensor",
                            image = ControlSymbols.Sensor,
                            nameRes = R.string.ref_sym_sensor_name,
                            designation = "B",
                        ),
                        DrawingSymbol(
                            key = "plc",
                            image = ControlSymbols.ProgrammableController,
                            nameRes = R.string.ref_sym_plc_name,
                            designation = "A",
                        ),
                        ),
                    ),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sym_letters_title,
                blocks = listOf(
                    ReferenceBlock.Prose(listOf(R.string.ref_sym_letters_intro)),
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("A"), Localized(R.string.ref_sym_letter_a)),
                            ReferenceRow(Symbol("B"), Localized(R.string.ref_sym_letter_b)),
                            ReferenceRow(Symbol("C"), Localized(R.string.ref_sym_letter_c)),
                            ReferenceRow(Symbol("E"), Localized(R.string.ref_sym_letter_e)),
                            ReferenceRow(Symbol("F"), Localized(R.string.ref_sym_letter_f)),
                            ReferenceRow(Symbol("G"), Localized(R.string.ref_sym_letter_g)),
                            ReferenceRow(Symbol("H"), Localized(R.string.ref_sym_letter_h)),
                            ReferenceRow(Symbol("K"), Localized(R.string.ref_sym_letter_k)),
                            ReferenceRow(Symbol("L"), Localized(R.string.ref_sym_letter_l)),
                            ReferenceRow(Symbol("M"), Localized(R.string.ref_sym_letter_m)),
                            ReferenceRow(Symbol("P"), Localized(R.string.ref_sym_letter_p)),
                            ReferenceRow(Symbol("Q"), Localized(R.string.ref_sym_letter_q)),
                            ReferenceRow(Symbol("R"), Localized(R.string.ref_sym_letter_r)),
                            ReferenceRow(Symbol("T"), Localized(R.string.ref_sym_letter_t)),
                            ReferenceRow(Symbol("W"), Localized(R.string.ref_sym_letter_w)),
                            ReferenceRow(Symbol("X"), Localized(R.string.ref_sym_letter_x)),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val installationSymbols = ReferenceTopic(
        key = "symbols_installation",
        category = DRAWING_SYMBOLS,
        titleRes = R.string.ref_sym_plan_title,
        descriptionRes = R.string.ref_sym_plan_description,
        sourceRes = R.string.ref_sym_plan_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sym_plan_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sym_plan_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sym_plan_section,
                blocks = listOf(
                    ReferenceBlock.SymbolGrid(
                        listOf(
                        DrawingSymbol(
                            key = "socket",
                            image = InstallationSymbols.Socket,
                            nameRes = R.string.ref_sym_socket_name,
                        ),
                        DrawingSymbol(
                            key = "socket_earthed",
                            image = InstallationSymbols.SocketEarthed,
                            nameRes = R.string.ref_sym_socket_earthed_name,
                            noteRes = R.string.ref_sym_socket_earthed_note,
                        ),
                        DrawingSymbol(
                            key = "socket_double",
                            image = InstallationSymbols.SocketDouble,
                            nameRes = R.string.ref_sym_socket_double_name,
                        ),
                        DrawingSymbol(
                            key = "socket_weatherproof",
                            image = InstallationSymbols.SocketWeatherproof,
                            nameRes = R.string.ref_sym_socket_weatherproof_name,
                        ),
                        DrawingSymbol(
                            key = "socket_switched",
                            image = InstallationSymbols.SocketSwitched,
                            nameRes = R.string.ref_sym_socket_switched_name,
                        ),
                        DrawingSymbol(
                            key = "data_outlet",
                            image = InstallationSymbols.DataOutlet,
                            nameRes = R.string.ref_sym_data_outlet_name,
                        ),
                        DrawingSymbol(
                            key = "switch_one_way",
                            image = InstallationSymbols.SwitchOneWay,
                            nameRes = R.string.ref_sym_switch_one_way_name,
                        ),
                        DrawingSymbol(
                            key = "switch_two_way",
                            image = InstallationSymbols.SwitchTwoWay,
                            nameRes = R.string.ref_sym_switch_two_way_name,
                            noteRes = R.string.ref_sym_switch_two_way_note,
                        ),
                        DrawingSymbol(
                            key = "switch_intermediate",
                            image = InstallationSymbols.SwitchIntermediate,
                            nameRes = R.string.ref_sym_switch_intermediate_name,
                            noteRes = R.string.ref_sym_switch_intermediate_note,
                        ),
                        DrawingSymbol(
                            key = "switch_two_gang",
                            image = InstallationSymbols.SwitchTwoGang,
                            nameRes = R.string.ref_sym_switch_two_gang_name,
                        ),
                        DrawingSymbol(
                            key = "dimmer",
                            image = InstallationSymbols.Dimmer,
                            nameRes = R.string.ref_sym_dimmer_name,
                        ),
                        DrawingSymbol(
                            key = "pull_switch",
                            image = InstallationSymbols.PullSwitch,
                            nameRes = R.string.ref_sym_pull_switch_name,
                        ),
                        DrawingSymbol(
                            key = "luminaire",
                            image = InstallationSymbols.Luminaire,
                            nameRes = R.string.ref_sym_luminaire_name,
                            designation = "E",
                        ),
                        DrawingSymbol(
                            key = "luminaire_fluorescent",
                            image = InstallationSymbols.LuminaireFluorescent,
                            nameRes = R.string.ref_sym_luminaire_fluorescent_name,
                            designation = "E",
                        ),
                        DrawingSymbol(
                            key = "downlight",
                            image = InstallationSymbols.Downlight,
                            nameRes = R.string.ref_sym_downlight_name,
                            designation = "E",
                        ),
                        DrawingSymbol(
                            key = "emergency_luminaire",
                            image = InstallationSymbols.EmergencyLuminaire,
                            nameRes = R.string.ref_sym_emergency_luminaire_name,
                            designation = "E",
                            noteRes = R.string.ref_sym_emergency_luminaire_note,
                        ),
                        DrawingSymbol(
                            key = "floodlight",
                            image = InstallationSymbols.Floodlight,
                            nameRes = R.string.ref_sym_floodlight_name,
                            designation = "E",
                        ),
                        DrawingSymbol(
                            key = "exit",
                            image = InstallationSymbols.Exit,
                            nameRes = R.string.ref_sym_exit_name,
                            designation = "E",
                        ),
                        DrawingSymbol(
                            key = "junction_box",
                            image = InstallationSymbols.JunctionBox,
                            nameRes = R.string.ref_sym_junction_box_name,
                        ),
                        DrawingSymbol(
                            key = "distribution_board",
                            image = InstallationSymbols.DistributionBoard,
                            nameRes = R.string.ref_sym_distribution_board_name,
                        ),
                        DrawingSymbol(
                            key = "riser",
                            image = InstallationSymbols.Riser,
                            nameRes = R.string.ref_sym_riser_name,
                        ),
                        DrawingSymbol(
                            key = "conduit",
                            image = InstallationSymbols.Conduit,
                            nameRes = R.string.ref_sym_conduit_name,
                        ),
                        DrawingSymbol(
                            key = "bell",
                            image = InstallationSymbols.Bell,
                            nameRes = R.string.ref_sym_bell_name,
                            designation = "H",
                        ),
                        DrawingSymbol(
                            key = "intercom",
                            image = InstallationSymbols.Intercom,
                            nameRes = R.string.ref_sym_intercom_name,
                        ),
                        DrawingSymbol(
                            key = "detector",
                            image = InstallationSymbols.Detector,
                            nameRes = R.string.ref_sym_detector_name,
                            designation = "B",
                        ),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val ansiComparison = ReferenceTopic(
        key = "symbols_ansi",
        category = DRAWING_SYMBOLS,
        titleRes = R.string.ref_sym_ansi_title,
        descriptionRes = R.string.ref_sym_ansi_description,
        sourceRes = R.string.ref_sym_ansi_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_sym_ansi_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_sym_ansi_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_sym_ansi_section,
                blocks = listOf(
                    ReferenceBlock.SymbolComparison(
                        leftLabelRes = R.string.ref_sym_col_iec,
                        rightLabelRes = R.string.ref_sym_col_ansi,
                        pairs = listOf(
                        SymbolPair(
                            key = "resistor",
                            nameRes = R.string.ref_sym_pair_resistor_name,
                            left = SingleLineSymbols.Resistor,
                            right = AnsiSymbols.Resistor,
                            noteRes = R.string.ref_sym_pair_resistor_note,
                        ),
                        SymbolPair(
                            key = "inductor",
                            nameRes = R.string.ref_sym_pair_inductor_name,
                            left = SingleLineSymbols.Inductor,
                            right = AnsiSymbols.Inductor,
                        ),
                        SymbolPair(
                            key = "fuse",
                            nameRes = R.string.ref_sym_pair_fuse_name,
                            left = SingleLineSymbols.Fuse,
                            right = AnsiSymbols.Fuse,
                            noteRes = R.string.ref_sym_pair_fuse_note,
                        ),
                        SymbolPair(
                            key = "circuit_breaker",
                            nameRes = R.string.ref_sym_pair_circuit_breaker_name,
                            left = SingleLineSymbols.CircuitBreaker,
                            right = AnsiSymbols.CircuitBreaker,
                            noteRes = R.string.ref_sym_pair_circuit_breaker_note,
                        ),
                        SymbolPair(
                            key = "transformer",
                            nameRes = R.string.ref_sym_pair_transformer_name,
                            left = SingleLineSymbols.Transformer,
                            right = AnsiSymbols.Transformer,
                            noteRes = R.string.ref_sym_pair_transformer_note,
                        ),
                        SymbolPair(
                            key = "lamp",
                            nameRes = R.string.ref_sym_pair_lamp_name,
                            left = SingleLineSymbols.Lamp,
                            right = AnsiSymbols.Lamp,
                        ),
                        SymbolPair(
                            key = "contact_no",
                            nameRes = R.string.ref_sym_pair_contact_no_name,
                            left = ControlSymbols.ContactNormallyOpen,
                            right = AnsiSymbols.ContactNormallyOpen,
                            noteRes = R.string.ref_sym_pair_contact_no_note,
                        ),
                        SymbolPair(
                            key = "battery",
                            nameRes = R.string.ref_sym_pair_battery_name,
                            left = SingleLineSymbols.Battery,
                            right = AnsiSymbols.Battery,
                        ),
                        SymbolPair(
                            key = "capacitor",
                            nameRes = R.string.ref_sym_pair_capacitor_name,
                            left = SingleLineSymbols.Capacitor,
                            right = AnsiSymbols.Capacitor,
                            noteRes = R.string.ref_sym_pair_capacitor_note,
                        ),
                        SymbolPair(
                            key = "earth",
                            nameRes = R.string.ref_sym_pair_earth_name,
                            left = SingleLineSymbols.Earth,
                            right = AnsiSymbols.Ground,
                            noteRes = R.string.ref_sym_pair_earth_note,
                        ),
                        ),
                    ),
                ),
            ),
        ),
    )

    // -- Reading a cable\'s name --------------------------------------------------------

    private val cableTypeCodes = ReferenceTopic(
        key = "cable_type_codes",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_code_title,
        descriptionRes = R.string.ref_code_description,
        sourceRes = R.string.ref_code_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_code_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_code_intro))),
            ),
            tableSection(
                titleRes = R.string.ref_code_s1,
                rows = listOf(
                    ReferenceRow(Symbol("H"), Localized(R.string.ref_code_h_0)),
                    ReferenceRow(Symbol("A"), Localized(R.string.ref_code_h_1)),
                    ReferenceRow(Symbol("03"), Localized(R.string.ref_code_h_2)),
                    ReferenceRow(Symbol("05"), Localized(R.string.ref_code_h_3)),
                    ReferenceRow(Symbol("07"), Localized(R.string.ref_code_h_4)),
                    ReferenceRow(Symbol("V"), Localized(R.string.ref_code_h_5)),
                    ReferenceRow(Symbol("R"), Localized(R.string.ref_code_h_6)),
                    ReferenceRow(Symbol("N"), Localized(R.string.ref_code_h_7)),
                    ReferenceRow(Symbol("-U"), Localized(R.string.ref_code_h_8)),
                    ReferenceRow(Symbol("-R"), Localized(R.string.ref_code_h_9)),
                    ReferenceRow(Symbol("-K"), Localized(R.string.ref_code_h_10)),
                    ReferenceRow(Symbol("-F"), Localized(R.string.ref_code_h_11)),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_code_s2,
                rows = listOf(
                    ReferenceRow(Symbol("N"), Localized(R.string.ref_code_g_0)),
                    ReferenceRow(Symbol("Y"), Localized(R.string.ref_code_g_1)),
                    ReferenceRow(Symbol("2X"), Localized(R.string.ref_code_g_2)),
                    ReferenceRow(Symbol("M"), Localized(R.string.ref_code_g_3)),
                    ReferenceRow(Symbol("A"), Localized(R.string.ref_code_g_4)),
                    ReferenceRow(Symbol("F"), Localized(R.string.ref_code_g_5)),
                    ReferenceRow(Symbol("-J"), Localized(R.string.ref_code_g_6)),
                    ReferenceRow(Symbol("-O"), Localized(R.string.ref_code_g_7)),
                    ReferenceRow(Symbol("RE"), Localized(R.string.ref_code_g_8)),
                    ReferenceRow(Symbol("SE"), Localized(R.string.ref_code_g_9)),
                ),
            ),
            ReferenceSection(
                titleRes = R.string.ref_code_s3,
                blocks = listOf(
                    ReferenceBlock.Table(
                        listOf(
                            ReferenceRow(Symbol("H07V-U"), Localized(R.string.ref_code_ex_h07vu)),
                            ReferenceRow(Symbol("NYM-J"), Localized(R.string.ref_code_ex_nymj)),
                            ReferenceRow(Symbol("NYY-J"), Localized(R.string.ref_code_ex_nyyj)),
                            ReferenceRow(Symbol("NYAF"), Localized(R.string.ref_code_ex_nyaf)),
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_code_callout),
                ),
            ),
        ),
    )

    // -- The figures the cable calculator applies -----------------------------------------

    private val correctionFactorTables = ReferenceTopic(
        key = "correction_factors",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_derate_title,
        descriptionRes = R.string.ref_derate_description,
        sourceRes = R.string.ref_derate_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_derate_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_derate_intro))),
            ),
            // Read from the model the calculator uses, so the page and the
            // result can never disagree about a factor.
            tableSection(
                titleRes = R.string.ref_derate_s1,
                rows = CorrectionFactors.PVC_AMBIENT.keys.sorted().map { ambient ->
                    ReferenceRow(
                        label = Quantity(ambient, "°C", 3),
                        value = Quantity(CorrectionFactors.PVC_AMBIENT.getValue(ambient), "", 3),
                        note = CorrectionFactors.XLPE_AMBIENT[ambient]
                            ?.let { Quantity(it, "", 3) },
                    )
                },
                footnoteRes = R.string.ref_derate_note1,
            ),
            ReferenceSection(
                titleRes = R.string.ref_derate_s2,
                blocks = listOf(
                    ReferenceBlock.Table(
                        CorrectionFactors.GROUPING.map { (circuits, factor) ->
                            ReferenceRow(
                                label = Quantity(circuits.toDouble(), "", 2),
                                value = Quantity(factor, "", 3),
                            )
                        },
                    ),
                    ReferenceBlock.Callout(CalloutKind.WARNING, R.string.ref_derate_callout),
                ),
                footnoteRes = R.string.ref_derate_note2,
            ),
        ),
    )

    // -- Before treating anything as dead ---------------------------------------------------

    private val safetyRules = ReferenceTopic(
        key = "safety_rules",
        category = COMMISSIONING_AND_DIAGNOSIS,
        titleRes = R.string.ref_safety_title,
        descriptionRes = R.string.ref_safety_description,
        sourceRes = R.string.ref_safety_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_safety_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_safety_intro))),
            ),
            ReferenceSection(
                titleRes = R.string.ref_safety_s1,
                blocks = listOf(
                    ReferenceBlock.Ordered(
                        listOf(
                            R.string.ref_safety_1,
                            R.string.ref_safety_2,
                            R.string.ref_safety_3,
                            R.string.ref_safety_4,
                            R.string.ref_safety_5,
                        ),
                    ),
                    ReferenceBlock.Callout(CalloutKind.SAFETY, R.string.ref_safety_callout),
                ),
            ),
        ),
    )

    // -- Every equation in one place ----------------------------------------------------------

    private val formulaSummary = ReferenceTopic(
        key = "formula_summary",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_formula_title,
        descriptionRes = R.string.ref_formula_description,
        sourceRes = R.string.ref_formula_source,
        sections = listOf(
            tableSection(
                titleRes = R.string.ref_formula_s1,
                rows = listOf(
                    ReferenceRow(Symbol("ΔU = k · I · ρ(θ) · L · cos φ / (A · n)"), Localized(R.string.ref_formula_0)),
                    ReferenceRow(Symbol("I_z ≥ I_b / (n · Ca · Cg)"), Localized(R.string.ref_formula_1)),
                    ReferenceRow(Symbol("I = S / (k · U)"), Localized(R.string.ref_formula_2)),
                    ReferenceRow(Symbol("I = P_in / (k · U · cos φ)"), Localized(R.string.ref_formula_3)),
                    ReferenceRow(Symbol("S² = P² + Q²"), Localized(R.string.ref_formula_4)),
                    ReferenceRow(Symbol("Q_c = P · (tan φ₁ − tan φ₂)"), Localized(R.string.ref_formula_5)),
                    ReferenceRow(Symbol("t = H · (I_r / I)^k · DoD"), Localized(R.string.ref_formula_6)),
                    ReferenceRow(Symbol("I = c · U / (k · (Z_s + √(R² + X²)))"), Localized(R.string.ref_formula_7)),
                    ReferenceRow(Symbol("Z_s = Z_e + R₁ + R₂"), Localized(R.string.ref_formula_8)),
                    ReferenceRow(Symbol("Z_s,max = c · U₀ / I_a"), Localized(R.string.ref_formula_9)),
                    ReferenceRow(Symbol("S ≥ √(I² · t) / k"), Localized(R.string.ref_formula_10)),
                    ReferenceRow(Symbol("m = n · A · L · δ / 1000"), Localized(R.string.ref_formula_11)),
                    ReferenceRow(Symbol("fill = Σ(n · π · d² / 4) / (π · D² / 4)"), Localized(R.string.ref_formula_12)),
                    ReferenceRow(Symbol("W_req = Σ(n · d) + s · (N − 1)"), Localized(R.string.ref_formula_13)),
                    ReferenceRow(Symbol("N = (E · A) / (Φ · UF · MF)"), Localized(R.string.ref_formula_14)),
                    ReferenceRow(Symbol("K = (L · W) / (Hm · (L + W))"), Localized(R.string.ref_formula_15)),
                    ReferenceRow(Symbol("Voc(T) = Voc · (1 + β · (T − 25) / 100)"), Localized(R.string.ref_formula_16)),
                    ReferenceRow(Symbol("I_N = √(I_u² + (3 · I₃)²)"), Localized(R.string.ref_formula_17)),
                    ReferenceRow(Symbol("C = P · h · d / 1000 · tarife"), Localized(R.string.ref_formula_18)),
                ),
                footnoteRes = R.string.ref_formula_note,
            ),
        ),
    )

    // -- Which document answers which question --------------------------------------------------

    private val standardsMap = ReferenceTopic(
        key = "standards_map",
        category = TABLES_AND_CODES,
        titleRes = R.string.ref_standards_title,
        descriptionRes = R.string.ref_standards_description,
        sourceRes = R.string.ref_standards_source,
        sections = listOf(
            ReferenceSection(
                titleRes = R.string.ref_standards_intro_title,
                blocks = listOf(ReferenceBlock.Prose(listOf(R.string.ref_standards_intro))),
            ),
            tableSection(
                titleRes = R.string.ref_standards_s1,
                rows = listOf(
                    ReferenceRow(Symbol("60364-4-41"), Localized(R.string.ref_std_a_0)),
                    ReferenceRow(Symbol("60364-4-43"), Localized(R.string.ref_std_a_1)),
                    ReferenceRow(Symbol("60364-5-52"), Localized(R.string.ref_std_a_2)),
                    ReferenceRow(Symbol("60364-5-54"), Localized(R.string.ref_std_a_3)),
                    ReferenceRow(Symbol("60364-6"), Localized(R.string.ref_std_a_4)),
                    ReferenceRow(Symbol("60364-7"), Localized(R.string.ref_std_a_5)),
                ),
            ),
            tableSection(
                titleRes = R.string.ref_standards_s2,
                rows = listOf(
                    ReferenceRow(Symbol("60038"), Localized(R.string.ref_std_b_0)),
                    ReferenceRow(Symbol("60228"), Localized(R.string.ref_std_b_1)),
                    ReferenceRow(Symbol("60269"), Localized(R.string.ref_std_b_2)),
                    ReferenceRow(Symbol("60529"), Localized(R.string.ref_std_b_3)),
                    ReferenceRow(Symbol("60617"), Localized(R.string.ref_std_b_4)),
                    ReferenceRow(Symbol("60898"), Localized(R.string.ref_std_b_5)),
                    ReferenceRow(Symbol("60909"), Localized(R.string.ref_std_b_6)),
                    ReferenceRow(Symbol("60947-2"), Localized(R.string.ref_std_b_7)),
                    ReferenceRow(Symbol("61008"), Localized(R.string.ref_std_b_8)),
                    ReferenceRow(Symbol("61439"), Localized(R.string.ref_std_b_9)),
                    ReferenceRow(Symbol("62262"), Localized(R.string.ref_std_b_10)),
                    ReferenceRow(Symbol("81346"), Localized(R.string.ref_std_b_11)),
                    ReferenceRow(Symbol("EN 12464-1"), Localized(R.string.ref_std_b_12)),
                    ReferenceRow(Symbol("EN 50110-1"), Localized(R.string.ref_std_b_13)),
                ),
            ),
        ),
    )

    val all: List<ReferenceTopic> = listOf(
        breakerCurves,
        residualDevices,
        earthingSystems,
        disconnectionTimes,
        ratingSeries,
        commissioningTests,
        faultDiagnosis,
        commonMistakes,
        safetyRules,
        selectionStarting,
        selectionRcdtype,
        selectionInsulation,
        selectionCabletype,
        selectionMaterial,
        selectionEnclosure,
        selectionBattery,
        primerHarmonics,
        primerSelectivity,
        primerPowerquality,
        primerBonding,
        primerCableanatomy,
        primerNameplate,
        primerVectorgroup,
        primerHazardous,
        primerSwitchboard,
        singleLineSymbols,
        controlSymbols,
        installationSymbols,
        ansiComparison,
        ingressProtection,
        impactProtection,
        conductorColours,
        materials,
        standardVoltages,
        symbols,
        powerFactors,
        cableTypeCodes,
        correctionFactorTables,
        formulaSummary,
        standardsMap,
    )

    /** The topic [key] names, or null if there is none. */
    fun topicOrNull(key: String): ReferenceTopic? = all.firstOrNull { it.key == key }

    /**
     * The title resource for [key], for naming a link to it from elsewhere.
     *
     * A link that says "Selectivity" has to say it in the reader's language, and
     * the caller has only the key. Falls back to the library's own name rather
     * than throwing, so a mistyped key renders a link to the index instead of
     * crashing a calculator.
     */
    fun titleResOf(key: String): Int =
        topicOrNull(key)?.titleRes ?: R.string.dashboard_references_title

    // -- Builders ---------------------------------------------------------------------------

    private const val ENERGY_DIGITS = 2


    private fun row(label: String, valueRes: Int, noteRes: Int? = null) = ReferenceRow(
        label = Symbol(label),
        value = Localized(valueRes),
        note = noteRes?.let { Localized(it) },
    )

    /**
     * One IK row: the code, the energy, and the test hammer that delivers it.
     *
     * The hammer's mass goes through the formatter like every other number, so
     * "0,5 kg · 400 mm" reads correctly beside the Turkish prose above it.
     */
    private fun impact(code: String, joules: Double, hammerKg: Double, dropMm: Int) = ReferenceRow(
        label = Symbol(code),
        value = Quantity(joules, "J", ENERGY_DIGITS),
        note = Quantity(hammerKg, "kg · $dropMm mm", ENERGY_DIGITS),
    )

    private fun ConductorMaterial.labelRes(): Int = when (this) {
        ConductorMaterial.COPPER -> R.string.common_material_copper
        ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
    }

    /** Mass of one mm² of this material over a kilometre, the cable-schedule figure. */
    private fun ConductorMaterial.oneSquareMillimetrePerKilometre(): Double = densityKgPerDm3
}
