package com.kemalurekli.electricalcalculator.features.fieldnotes.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_voltage_indicator_is_not_a_prover_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_voltage_indicator_is_not_a_prover_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_phantom_voltage_reads_high_delivers_nothing_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_phantom_voltage_reads_high_delivers_nothing_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_capacitors_stay_charged_after_isolation_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_capacitors_stay_charged_after_isolation_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_rcd_test_button_proves_only_the_mechanism_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_rcd_test_button_proves_only_the_mechanism_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_neutral_is_a_live_conductor_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_neutral_is_a_live_conductor_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_broken_pen_energises_metalwork_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_broken_pen_energises_metalwork_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_borrowed_neutral_survives_isolation_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_borrowed_neutral_survives_isolation_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_meter_category_matches_where_you_stand_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_meter_category_matches_where_you_stand_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_replace_a_fuse_like_for_like_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_replace_a_fuse_like_for_like_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_emergency_stop_is_not_an_isolator_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_emergency_stop_is_not_an_isolator_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_isolate_upstream_of_what_you_touch_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_isolate_upstream_of_what_you_touch_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_label_before_you_leave_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_label_before_you_leave_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_volt_drop_bites_before_ampacity_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_volt_drop_bites_before_ampacity_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_doubling_length_doubles_drop_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_doubling_length_doubles_drop_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_single_phase_kilowatt_is_four_amps_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_single_phase_kilowatt_is_four_amps_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_three_phase_kilowatt_is_one_and_a_half_amps_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_three_phase_kilowatt_is_one_and_a_half_amps_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_transformer_kva_to_secondary_amps_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_transformer_kva_to_secondary_amps_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_dol_start_draws_six_times_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_dol_start_draws_six_times_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_copper_resistance_rises_with_temperature_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_copper_resistance_rises_with_temperature_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_grouping_costs_more_than_people_expect_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_grouping_costs_more_than_people_expect_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_protection_sits_between_two_currents_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_protection_sits_between_two_currents_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_power_factor_correction_pays_on_current_not_energy_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_power_factor_correction_pays_on_current_not_energy_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_lighting_design_starts_from_the_task_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_lighting_design_starts_from_the_task_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_diversity_is_a_judgement_not_a_discount_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_diversity_is_a_judgement_not_a_discount_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_dead_tests_come_before_live_ones_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_dead_tests_come_before_live_ones_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_insulation_test_destroys_electronics_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_insulation_test_destroys_electronics_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_measure_loop_impedance_at_the_far_end_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_measure_loop_impedance_at_the_far_end_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_polarity_is_cheap_to_check_and_lethal_to_miss_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_polarity_is_cheap_to_check_and_lethal_to_miss_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_rcd_ramp_test_before_the_trip_test_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_rcd_ramp_test_before_the_trip_test_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_prove_continuity_of_the_protective_conductor_first_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_prove_continuity_of_the_protective_conductor_first_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_a_clamp_meter_reads_zero_around_a_whole_cable_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_a_clamp_meter_reads_zero_around_a_whole_cable_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_true_rms_matters_on_distorted_current_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_true_rms_matters_on_distorted_current_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_record_the_reading_not_the_verdict_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_record_the_reading_not_the_verdict_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_test_leads_have_resistance_too_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_test_leads_have_resistance_too_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_an_open_neutral_hides_from_a_two_lead_test_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_an_open_neutral_hides_from_a_two_lead_test_body
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_energise_one_circuit_at_a_time_title
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_energise_one_circuit_at_a_time_body

/**
 * Every field note the app ships.
 *
 * GENERATED — edit `scripts/gen_field_notes.py` and re-run it rather than this
 * file. The script owns the structure and the English text; each translation
 * lives in its own `values-<tag>/strings.xml` and is never generated.
 *
 * Order here is the authoring order. The screen groups by category and keeps
 * this order within each, so a category reads as it was written rather than
 * being alphabetised into nonsense.
 */
object FieldNoteCatalog {

    val all: List<FieldNote> = listOf(
        FieldNote(
            key = "voltage_indicator_is_not_a_prover",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_voltage_indicator_is_not_a_prover_title,
            body = Res.string.fn_voltage_indicator_is_not_a_prover_body,
            glossaryTerms = listOf("live_part", "isolation"),
            referenceTopic = "safety_rules",
        ),
        FieldNote(
            key = "phantom_voltage_reads_high_delivers_nothing",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_phantom_voltage_reads_high_delivers_nothing_title,
            body = Res.string.fn_phantom_voltage_reads_high_delivers_nothing_body,
            glossaryTerms = listOf("live_part", "impedance"),
        ),
        FieldNote(
            key = "capacitors_stay_charged_after_isolation",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_capacitors_stay_charged_after_isolation_title,
            body = Res.string.fn_capacitors_stay_charged_after_isolation_body,
            glossaryTerms = listOf("power_factor_correction", "vfd", "isolation"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
        ),
        FieldNote(
            key = "rcd_test_button_proves_only_the_mechanism",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_rcd_test_button_proves_only_the_mechanism_title,
            body = Res.string.fn_rcd_test_button_proves_only_the_mechanism_body,
            glossaryTerms = listOf("rcd", "earth_fault_loop_impedance", "protective_conductor"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "rcd_types",
        ),
        FieldNote(
            key = "neutral_is_a_live_conductor",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_neutral_is_a_live_conductor_title,
            body = Res.string.fn_neutral_is_a_live_conductor_body,
            glossaryTerms = listOf("neutral_conductor", "live_part", "phase_sequence"),
            calculator = CalculatorId.NEUTRAL_CURRENT,
        ),
        FieldNote(
            key = "broken_pen_energises_metalwork",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_broken_pen_energises_metalwork_title,
            body = Res.string.fn_broken_pen_energises_metalwork_body,
            glossaryTerms = listOf("pen_conductor", "tn_system", "earthing", "touch_voltage"),
            referenceTopic = "earthing_systems",
        ),
        FieldNote(
            key = "borrowed_neutral_survives_isolation",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_borrowed_neutral_survives_isolation_title,
            body = Res.string.fn_borrowed_neutral_survives_isolation_body,
            glossaryTerms = listOf("neutral_conductor", "isolation"),
            referenceTopic = "common_mistakes",
        ),
        FieldNote(
            key = "meter_category_matches_where_you_stand",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_meter_category_matches_where_you_stand_title,
            body = Res.string.fn_meter_category_matches_where_you_stand_body,
            glossaryTerms = listOf("prospective_short_circuit_current", "breaking_capacity"),
            calculator = CalculatorId.SHORT_CIRCUIT,
        ),
        FieldNote(
            key = "replace_a_fuse_like_for_like",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_replace_a_fuse_like_for_like_title,
            body = Res.string.fn_replace_a_fuse_like_for_like_body,
            glossaryTerms = listOf("gg_fuse", "fuse", "breaking_capacity", "rated_current"),
            referenceTopic = "rating_series",
        ),
        FieldNote(
            key = "emergency_stop_is_not_an_isolator",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_emergency_stop_is_not_an_isolator_title,
            body = Res.string.fn_emergency_stop_is_not_an_isolator_body,
            glossaryTerms = listOf("isolation", "interlock", "switchgear"),
            referenceTopic = "safety_rules",
        ),
        FieldNote(
            key = "isolate_upstream_of_what_you_touch",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_isolate_upstream_of_what_you_touch_title,
            body = Res.string.fn_isolate_upstream_of_what_you_touch_body,
            glossaryTerms = listOf("isolation", "switchgear", "distribution_board"),
            referenceTopic = "safety_rules",
        ),
        FieldNote(
            key = "label_before_you_leave",
            category = FieldNoteCategory.SAFETY_AND_PRACTICE,
            title = Res.string.fn_label_before_you_leave_title,
            body = Res.string.fn_label_before_you_leave_body,
            glossaryTerms = listOf("distribution_board", "isolation"),
            referenceTopic = "common_mistakes",
        ),
        FieldNote(
            key = "volt_drop_bites_before_ampacity",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_volt_drop_bites_before_ampacity_title,
            body = Res.string.fn_volt_drop_bites_before_ampacity_body,
            glossaryTerms = listOf("voltage_drop", "ampacity"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),
        FieldNote(
            key = "doubling_length_doubles_drop",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_doubling_length_doubles_drop_title,
            body = Res.string.fn_doubling_length_doubles_drop_body,
            glossaryTerms = listOf("voltage_drop", "cross_sectional_area", "resistivity"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),
        FieldNote(
            key = "single_phase_kilowatt_is_four_amps",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_single_phase_kilowatt_is_four_amps_title,
            body = Res.string.fn_single_phase_kilowatt_is_four_amps_body,
            glossaryTerms = listOf("active_power", "cos_phi", "rated_current"),
            calculator = CalculatorId.POWER,
        ),
        FieldNote(
            key = "three_phase_kilowatt_is_one_and_a_half_amps",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_three_phase_kilowatt_is_one_and_a_half_amps_title,
            body = Res.string.fn_three_phase_kilowatt_is_one_and_a_half_amps_body,
            glossaryTerms = listOf("active_power", "line_conductor"),
            calculator = CalculatorId.POWER,
        ),
        FieldNote(
            key = "transformer_kva_to_secondary_amps",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_transformer_kva_to_secondary_amps_title,
            body = Res.string.fn_transformer_kva_to_secondary_amps_body,
            glossaryTerms = listOf("apparent_power", "transformer", "rated_current"),
            calculator = CalculatorId.TRANSFORMER_CURRENT,
        ),
        FieldNote(
            key = "dol_start_draws_six_times",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_dol_start_draws_six_times_title,
            body = Res.string.fn_dol_start_draws_six_times_body,
            glossaryTerms = listOf("inrush_current", "star_delta_starting", "soft_starter", "gg_fuse"),
            calculator = CalculatorId.MOTOR_CURRENT,
            referenceTopic = "selection_starting",
        ),
        FieldNote(
            key = "copper_resistance_rises_with_temperature",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_copper_resistance_rises_with_temperature_title,
            body = Res.string.fn_copper_resistance_rises_with_temperature_body,
            glossaryTerms = listOf("resistivity", "ambient_temperature", "voltage_drop"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),
        FieldNote(
            key = "grouping_costs_more_than_people_expect",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_grouping_costs_more_than_people_expect_title,
            body = Res.string.fn_grouping_costs_more_than_people_expect_body,
            glossaryTerms = listOf("grouping_factor", "derating", "correction_factor"),
            calculator = CalculatorId.CABLE_SIZE,
            referenceTopic = "correction_factors",
        ),
        FieldNote(
            key = "protection_sits_between_two_currents",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_protection_sits_between_two_currents_title,
            body = Res.string.fn_protection_sits_between_two_currents_body,
            glossaryTerms = listOf("rated_current", "ampacity", "overload", "overcurrent"),
            calculator = CalculatorId.CABLE_SIZE,
            referenceTopic = "rating_series",
        ),
        FieldNote(
            key = "power_factor_correction_pays_on_current_not_energy",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_power_factor_correction_pays_on_current_not_energy_title,
            body = Res.string.fn_power_factor_correction_pays_on_current_not_energy_body,
            glossaryTerms = listOf("power_factor_correction", "reactive_power", "apparent_power", "cos_phi"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
        ),
        FieldNote(
            key = "lighting_design_starts_from_the_task",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_lighting_design_starts_from_the_task_title,
            body = Res.string.fn_lighting_design_starts_from_the_task_body,
            glossaryTerms = listOf("illuminance", "luminous_flux"),
            calculator = CalculatorId.LIGHTING_LUMEN,
        ),
        FieldNote(
            key = "diversity_is_a_judgement_not_a_discount",
            category = FieldNoteCategory.RULES_OF_THUMB,
            title = Res.string.fn_diversity_is_a_judgement_not_a_discount_title,
            body = Res.string.fn_diversity_is_a_judgement_not_a_discount_body,
            glossaryTerms = listOf("diversity_factor", "demand_factor", "load_factor"),
        ),
        FieldNote(
            key = "dead_tests_come_before_live_ones",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_dead_tests_come_before_live_ones_title,
            body = Res.string.fn_dead_tests_come_before_live_ones_body,
            glossaryTerms = listOf("continuity_test", "insulation_resistance"),
            referenceTopic = "commissioning_tests",
        ),
        FieldNote(
            key = "insulation_test_destroys_electronics",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_insulation_test_destroys_electronics_title,
            body = Res.string.fn_insulation_test_destroys_electronics_body,
            glossaryTerms = listOf("insulation_resistance", "spd"),
            referenceTopic = "commissioning_tests",
        ),
        FieldNote(
            key = "measure_loop_impedance_at_the_far_end",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_measure_loop_impedance_at_the_far_end_title,
            body = Res.string.fn_measure_loop_impedance_at_the_far_end_body,
            glossaryTerms = listOf("earth_fault_loop_impedance", "disconnection_time", "external_loop_impedance"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "disconnection_times",
        ),
        FieldNote(
            key = "polarity_is_cheap_to_check_and_lethal_to_miss",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_polarity_is_cheap_to_check_and_lethal_to_miss_title,
            body = Res.string.fn_polarity_is_cheap_to_check_and_lethal_to_miss_body,
            glossaryTerms = listOf("polarity", "line_conductor", "neutral_conductor"),
            referenceTopic = "commissioning_tests",
        ),
        FieldNote(
            key = "rcd_ramp_test_before_the_trip_test",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_rcd_ramp_test_before_the_trip_test_title,
            body = Res.string.fn_rcd_ramp_test_before_the_trip_test_body,
            glossaryTerms = listOf("rcd", "residual_current", "disconnection_time"),
            referenceTopic = "rcd_types",
        ),
        FieldNote(
            key = "prove_continuity_of_the_protective_conductor_first",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_prove_continuity_of_the_protective_conductor_first_title,
            body = Res.string.fn_prove_continuity_of_the_protective_conductor_first_body,
            glossaryTerms = listOf("protective_conductor", "continuity_test", "equipotential_bonding"),
            referenceTopic = "primer_bonding",
        ),
        FieldNote(
            key = "a_clamp_meter_reads_zero_around_a_whole_cable",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_a_clamp_meter_reads_zero_around_a_whole_cable_title,
            body = Res.string.fn_a_clamp_meter_reads_zero_around_a_whole_cable_body,
            glossaryTerms = listOf("residual_current", "rcd", "current_transformer"),
        ),
        FieldNote(
            key = "true_rms_matters_on_distorted_current",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_true_rms_matters_on_distorted_current_title,
            body = Res.string.fn_true_rms_matters_on_distorted_current_body,
            glossaryTerms = listOf("rms", "harmonic"),
            calculator = CalculatorId.NEUTRAL_CURRENT,
            referenceTopic = "primer_harmonics",
        ),
        FieldNote(
            key = "record_the_reading_not_the_verdict",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_record_the_reading_not_the_verdict_title,
            body = Res.string.fn_record_the_reading_not_the_verdict_body,
            glossaryTerms = listOf("earth_fault_loop_impedance", "insulation_resistance"),
            referenceTopic = "commissioning_tests",
        ),
        FieldNote(
            key = "test_leads_have_resistance_too",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_test_leads_have_resistance_too_title,
            body = Res.string.fn_test_leads_have_resistance_too_body,
            glossaryTerms = listOf("continuity_test", "protective_conductor"),
        ),
        FieldNote(
            key = "an_open_neutral_hides_from_a_two_lead_test",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_an_open_neutral_hides_from_a_two_lead_test_title,
            body = Res.string.fn_an_open_neutral_hides_from_a_two_lead_test_body,
            glossaryTerms = listOf("insulation_resistance", "continuity_test", "neutral_conductor"),
        ),
        FieldNote(
            key = "energise_one_circuit_at_a_time",
            category = FieldNoteCategory.MEASUREMENT_AND_TESTING,
            title = Res.string.fn_energise_one_circuit_at_a_time_title,
            body = Res.string.fn_energise_one_circuit_at_a_time_body,
            glossaryTerms = listOf("inrush_current", "overcurrent"),
            referenceTopic = "commissioning_tests",
        ),
    )

    private val byKey: Map<String, FieldNote> = all.associateBy { it.key }

    /** The note for [key], or null for a stale cross-reference or deep link. */
    fun noteOrNull(key: String): FieldNote? = byKey[key]

    /** The notes filed under [category], in authoring order. */
    fun inCategory(category: FieldNoteCategory): List<FieldNote> =
        all.filter { it.category == category }
}
