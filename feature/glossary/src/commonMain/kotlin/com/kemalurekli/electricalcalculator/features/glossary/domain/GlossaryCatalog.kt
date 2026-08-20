package com.kemalurekli.electricalcalculator.features.glossary.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_active_power_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_active_power_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_adiabatic_check_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_adiabatic_check_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ambient_temperature_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ambient_temperature_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ampacity_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ampacity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_apparent_power_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_apparent_power_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_arc_fault_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_arc_fault_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_armour_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_armour_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_automatic_disconnection_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_automatic_disconnection_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_awg_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_awg_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_back_up_protection_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_back_up_protection_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_basic_protection_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_basic_protection_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_breaking_capacity_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_breaking_capacity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_busbar_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_busbar_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_c_rate_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_c_rate_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cascading_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cascading_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_circuit_breaker_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_circuit_breaker_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_clearance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_clearance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_conductor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_conductor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_contactor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_contactor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_continuity_test_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_continuity_test_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_correction_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_correction_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cos_phi_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cos_phi_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_creepage_distance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_creepage_distance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cross_sectional_area_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_cross_sectional_area_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_current_transformer_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_current_transformer_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_demand_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_demand_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_depth_of_discharge_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_depth_of_discharge_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_derating_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_derating_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_disconnection_time_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_disconnection_time_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_distribution_board_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_distribution_board_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_diversity_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_diversity_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earth_electrode_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earth_electrode_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earth_fault_loop_impedance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earth_fault_loop_impedance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earthing_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_earthing_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_efficiency_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_efficiency_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_equipotential_bonding_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_equipotential_bonding_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_exposed_conductive_part_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_exposed_conductive_part_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_external_loop_impedance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_external_loop_impedance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_extraneous_conductive_part_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_extraneous_conductive_part_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fault_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fault_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fault_protection_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fault_protection_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_form_of_separation_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_form_of_separation_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_frequency_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_frequency_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_functional_earth_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_functional_earth_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fuse_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_fuse_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_gg_fuse_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_gg_fuse_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_grouping_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_grouping_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_harmonic_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_harmonic_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ik_code_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ik_code_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_illuminance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_illuminance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_impedance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_impedance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_impedance_voltage_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_impedance_voltage_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_indirect_contact_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_indirect_contact_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_inductive_reactance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_inductive_reactance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_capacitive_reactance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_capacitive_reactance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_inrush_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_inrush_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_instantaneous_trip_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_instantaneous_trip_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_insulation_resistance_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_insulation_resistance_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_interlock_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_interlock_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ip_code_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ip_code_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_isolation_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_isolation_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_it_system_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_it_system_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_let_through_energy_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_let_through_energy_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_line_conductor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_line_conductor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_live_part_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_live_part_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_load_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_load_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_lszh_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_lszh_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_luminous_flux_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_luminous_flux_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_mcb_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_mcb_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_mccb_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_mccb_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_nameplate_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_nameplate_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_neutral_conductor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_neutral_conductor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_no_load_loss_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_no_load_loss_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_overcurrent_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_overcurrent_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_overload_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_overload_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_pen_conductor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_pen_conductor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_peukert_law_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_peukert_law_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_phase_angle_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_phase_angle_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_phase_sequence_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_phase_sequence_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_polarity_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_polarity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_power_factor_correction_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_power_factor_correction_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_prospective_short_circuit_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_prospective_short_circuit_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_protective_conductor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_protective_conductor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_pvc_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_pvc_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rated_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rated_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rated_voltage_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rated_voltage_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rcbo_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rcbo_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rccb_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rccb_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rcd_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rcd_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_reactive_power_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_reactive_power_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_reference_method_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_reference_method_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_relay_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_relay_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_residual_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_residual_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_resistivity_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_resistivity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rms_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_rms_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_selectivity_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_selectivity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_service_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_service_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_sheath_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_sheath_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_short_circuit_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_short_circuit_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_skin_effect_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_skin_effect_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_slip_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_slip_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_soft_starter_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_soft_starter_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_spd_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_spd_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_star_delta_starting_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_star_delta_starting_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_switchgear_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_switchgear_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_thermal_magnetic_trip_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_thermal_magnetic_trip_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_tn_system_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_tn_system_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_touch_voltage_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_touch_voltage_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_transformer_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_transformer_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_trip_curve_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_trip_curve_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_tt_system_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_tt_system_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_vector_group_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_vector_group_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_vfd_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_vfd_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_voltage_drop_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_voltage_drop_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_voltage_factor_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_voltage_factor_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_withstand_current_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_withstand_current_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_xlpe_term
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_xlpe_def

/**
 * Every glossary term the app knows.
 *
 * GENERATED — edit `scripts/gen_glossary.py` and re-run it rather than this
 * file. The script owns the structure and the English text; each translation
 * lives in its own `values-<tag>/strings.xml` and is never generated.
 *
 * Order here is the authoring order. The screen sorts by the reader's language
 * with a locale collator, because A–Z is not the same list in Turkish as in
 * English, and not a list at all in the same sense in Arabic.
 */
object GlossaryCatalog {

    val all: List<GlossaryTerm> = listOf(
        GlossaryTerm(
            key = "active_power",
            englishTerm = "Active power",
            term = Res.string.gl_active_power_term,
            definition = Res.string.gl_active_power_def,
            symbol = "P",
            unit = "W",
            seeAlso = listOf("reactive_power", "apparent_power", "cos_phi"),
            calculator = CalculatorId.POWER,
        ),
        GlossaryTerm(
            key = "adiabatic_check",
            englishTerm = "Adiabatic check",
            term = Res.string.gl_adiabatic_check_term,
            definition = Res.string.gl_adiabatic_check_def,
            seeAlso = listOf("fault_current", "let_through_energy", "protective_conductor"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
        ),
        GlossaryTerm(
            key = "ambient_temperature",
            englishTerm = "Ambient temperature",
            term = Res.string.gl_ambient_temperature_term,
            definition = Res.string.gl_ambient_temperature_def,
            symbol = "θa",
            unit = "°C",
            seeAlso = listOf("correction_factor", "derating", "ampacity"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "ampacity",
            englishTerm = "Ampacity",
            term = Res.string.gl_ampacity_term,
            definition = Res.string.gl_ampacity_def,
            symbol = "Iz",
            unit = "A",
            seeAlso = listOf("derating", "reference_method", "correction_factor"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "apparent_power",
            englishTerm = "Apparent power",
            term = Res.string.gl_apparent_power_term,
            definition = Res.string.gl_apparent_power_def,
            symbol = "S",
            unit = "VA",
            seeAlso = listOf("active_power", "reactive_power", "cos_phi"),
            calculator = CalculatorId.POWER,
        ),
        GlossaryTerm(
            key = "arc_fault",
            englishTerm = "Arc fault",
            term = Res.string.gl_arc_fault_term,
            definition = Res.string.gl_arc_fault_def,
            seeAlso = listOf("short_circuit", "overcurrent"),
        ),
        GlossaryTerm(
            key = "armour",
            englishTerm = "Armour",
            term = Res.string.gl_armour_term,
            definition = Res.string.gl_armour_def,
            seeAlso = listOf("sheath", "protective_conductor"),
        ),
        GlossaryTerm(
            key = "automatic_disconnection",
            englishTerm = "Automatic disconnection of supply",
            term = Res.string.gl_automatic_disconnection_term,
            definition = Res.string.gl_automatic_disconnection_def,
            seeAlso = listOf("disconnection_time", "earth_fault_loop_impedance", "touch_voltage"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "disconnection_times",
        ),
        GlossaryTerm(
            key = "awg",
            englishTerm = "AWG",
            term = Res.string.gl_awg_term,
            definition = Res.string.gl_awg_def,
            seeAlso = listOf("cross_sectional_area"),
        ),
        GlossaryTerm(
            key = "back_up_protection",
            englishTerm = "Back-up protection",
            term = Res.string.gl_back_up_protection_term,
            definition = Res.string.gl_back_up_protection_def,
            seeAlso = listOf("breaking_capacity", "selectivity", "cascading"),
        ),
        GlossaryTerm(
            key = "basic_protection",
            englishTerm = "Basic protection",
            term = Res.string.gl_basic_protection_term,
            definition = Res.string.gl_basic_protection_def,
            seeAlso = listOf("fault_protection", "live_part"),
        ),
        GlossaryTerm(
            key = "breaking_capacity",
            englishTerm = "Breaking capacity",
            term = Res.string.gl_breaking_capacity_term,
            definition = Res.string.gl_breaking_capacity_def,
            symbol = "Icu",
            unit = "kA",
            seeAlso = listOf("prospective_short_circuit_current", "short_circuit"),
            calculator = CalculatorId.SHORT_CIRCUIT,
        ),
        GlossaryTerm(
            key = "busbar",
            englishTerm = "Busbar",
            term = Res.string.gl_busbar_term,
            definition = Res.string.gl_busbar_def,
            seeAlso = listOf("distribution_board", "switchgear"),
        ),
        GlossaryTerm(
            key = "c_rate",
            englishTerm = "C-rate",
            term = Res.string.gl_c_rate_term,
            definition = Res.string.gl_c_rate_def,
            symbol = "C",
            seeAlso = listOf("depth_of_discharge", "peukert_law"),
            calculator = CalculatorId.BATTERY_RUNTIME,
        ),
        GlossaryTerm(
            key = "cascading",
            englishTerm = "Cascading",
            term = Res.string.gl_cascading_term,
            definition = Res.string.gl_cascading_def,
            seeAlso = listOf("back_up_protection", "selectivity", "breaking_capacity"),
        ),
        GlossaryTerm(
            key = "circuit_breaker",
            englishTerm = "Circuit breaker",
            term = Res.string.gl_circuit_breaker_term,
            definition = Res.string.gl_circuit_breaker_def,
            seeAlso = listOf("mcb", "mccb", "trip_curve", "fuse"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "clearance",
            englishTerm = "Clearance",
            term = Res.string.gl_clearance_term,
            definition = Res.string.gl_clearance_def,
            unit = "mm",
            seeAlso = listOf("creepage_distance"),
        ),
        GlossaryTerm(
            key = "conductor",
            englishTerm = "Conductor",
            term = Res.string.gl_conductor_term,
            definition = Res.string.gl_conductor_def,
            seeAlso = listOf("cross_sectional_area", "resistivity", "line_conductor"),
            referenceTopic = "materials",
        ),
        GlossaryTerm(
            key = "contactor",
            englishTerm = "Contactor",
            term = Res.string.gl_contactor_term,
            definition = Res.string.gl_contactor_def,
            seeAlso = listOf("relay", "interlock"),
        ),
        GlossaryTerm(
            key = "continuity_test",
            englishTerm = "Continuity test",
            term = Res.string.gl_continuity_test_term,
            definition = Res.string.gl_continuity_test_def,
            seeAlso = listOf("insulation_resistance", "earth_fault_loop_impedance"),
        ),
        GlossaryTerm(
            key = "correction_factor",
            englishTerm = "Correction factor",
            term = Res.string.gl_correction_factor_term,
            definition = Res.string.gl_correction_factor_def,
            symbol = "Ca · Cg",
            seeAlso = listOf("ambient_temperature", "grouping_factor", "derating"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "cos_phi",
            englishTerm = "Power factor (cos φ)",
            term = Res.string.gl_cos_phi_term,
            definition = Res.string.gl_cos_phi_def,
            symbol = "cos φ",
            seeAlso = listOf("power_factor_correction", "reactive_power", "phase_angle"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
            referenceTopic = "power_factors",
        ),
        GlossaryTerm(
            key = "creepage_distance",
            englishTerm = "Creepage distance",
            term = Res.string.gl_creepage_distance_term,
            definition = Res.string.gl_creepage_distance_def,
            unit = "mm",
            seeAlso = listOf("clearance", "ip_code"),
        ),
        GlossaryTerm(
            key = "cross_sectional_area",
            englishTerm = "Cross-sectional area",
            term = Res.string.gl_cross_sectional_area_term,
            definition = Res.string.gl_cross_sectional_area_def,
            symbol = "A",
            unit = "mm²",
            seeAlso = listOf("ampacity", "awg", "conductor"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "current_transformer",
            englishTerm = "Current transformer",
            term = Res.string.gl_current_transformer_term,
            definition = Res.string.gl_current_transformer_def,
            seeAlso = listOf("transformer", "switchgear"),
        ),
        GlossaryTerm(
            key = "demand_factor",
            englishTerm = "Demand factor",
            term = Res.string.gl_demand_factor_term,
            definition = Res.string.gl_demand_factor_def,
            seeAlso = listOf("diversity_factor", "load_factor"),
        ),
        GlossaryTerm(
            key = "depth_of_discharge",
            englishTerm = "Depth of discharge",
            term = Res.string.gl_depth_of_discharge_term,
            definition = Res.string.gl_depth_of_discharge_def,
            symbol = "DoD",
            seeAlso = listOf("c_rate", "peukert_law"),
            calculator = CalculatorId.BATTERY_RUNTIME,
        ),
        GlossaryTerm(
            key = "derating",
            englishTerm = "Derating",
            term = Res.string.gl_derating_term,
            definition = Res.string.gl_derating_def,
            seeAlso = listOf("correction_factor", "ambient_temperature", "grouping_factor"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "disconnection_time",
            englishTerm = "Disconnection time",
            term = Res.string.gl_disconnection_time_term,
            definition = Res.string.gl_disconnection_time_def,
            symbol = "t",
            unit = "s",
            seeAlso = listOf("automatic_disconnection", "earth_fault_loop_impedance", "touch_voltage"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "disconnection_times",
        ),
        GlossaryTerm(
            key = "distribution_board",
            englishTerm = "Distribution board",
            term = Res.string.gl_distribution_board_term,
            definition = Res.string.gl_distribution_board_def,
            seeAlso = listOf("busbar", "switchgear", "form_of_separation"),
        ),
        GlossaryTerm(
            key = "diversity_factor",
            englishTerm = "Diversity factor",
            term = Res.string.gl_diversity_factor_term,
            definition = Res.string.gl_diversity_factor_def,
            seeAlso = listOf("demand_factor", "load_factor"),
        ),
        GlossaryTerm(
            key = "earth_electrode",
            englishTerm = "Earth electrode",
            term = Res.string.gl_earth_electrode_term,
            definition = Res.string.gl_earth_electrode_def,
            symbol = "RA",
            unit = "Ω",
            seeAlso = listOf("earthing", "tt_system", "equipotential_bonding"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "earth_fault_loop_impedance",
            englishTerm = "Earth fault loop impedance",
            term = Res.string.gl_earth_fault_loop_impedance_term,
            definition = Res.string.gl_earth_fault_loop_impedance_def,
            symbol = "Zs",
            unit = "Ω",
            seeAlso = listOf("disconnection_time", "external_loop_impedance", "automatic_disconnection"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
        ),
        GlossaryTerm(
            key = "earthing",
            englishTerm = "Earthing",
            term = Res.string.gl_earthing_term,
            definition = Res.string.gl_earthing_def,
            seeAlso = listOf("earth_electrode", "equipotential_bonding", "tn_system", "tt_system", "it_system"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "efficiency",
            englishTerm = "Efficiency",
            term = Res.string.gl_efficiency_term,
            definition = Res.string.gl_efficiency_def,
            symbol = "η",
            seeAlso = listOf("active_power", "no_load_loss"),
            calculator = CalculatorId.MOTOR_CURRENT,
        ),
        GlossaryTerm(
            key = "equipotential_bonding",
            englishTerm = "Equipotential bonding",
            term = Res.string.gl_equipotential_bonding_term,
            definition = Res.string.gl_equipotential_bonding_def,
            seeAlso = listOf("earthing", "extraneous_conductive_part", "touch_voltage"),
        ),
        GlossaryTerm(
            key = "exposed_conductive_part",
            englishTerm = "Exposed conductive part",
            term = Res.string.gl_exposed_conductive_part_term,
            definition = Res.string.gl_exposed_conductive_part_def,
            seeAlso = listOf("extraneous_conductive_part", "protective_conductor", "fault_protection"),
        ),
        GlossaryTerm(
            key = "external_loop_impedance",
            englishTerm = "External earth loop impedance",
            term = Res.string.gl_external_loop_impedance_term,
            definition = Res.string.gl_external_loop_impedance_def,
            symbol = "Ze",
            unit = "Ω",
            seeAlso = listOf("earth_fault_loop_impedance"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
        ),
        GlossaryTerm(
            key = "extraneous_conductive_part",
            englishTerm = "Extraneous conductive part",
            term = Res.string.gl_extraneous_conductive_part_term,
            definition = Res.string.gl_extraneous_conductive_part_def,
            seeAlso = listOf("exposed_conductive_part", "equipotential_bonding"),
        ),
        GlossaryTerm(
            key = "fault_current",
            englishTerm = "Fault current",
            term = Res.string.gl_fault_current_term,
            definition = Res.string.gl_fault_current_def,
            symbol = "If",
            unit = "A",
            seeAlso = listOf("short_circuit", "residual_current", "adiabatic_check"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
        ),
        GlossaryTerm(
            key = "fault_protection",
            englishTerm = "Fault protection",
            term = Res.string.gl_fault_protection_term,
            definition = Res.string.gl_fault_protection_def,
            seeAlso = listOf("basic_protection", "automatic_disconnection", "rcd"),
        ),
        GlossaryTerm(
            key = "form_of_separation",
            englishTerm = "Form of separation",
            term = Res.string.gl_form_of_separation_term,
            definition = Res.string.gl_form_of_separation_def,
            seeAlso = listOf("switchgear", "distribution_board"),
        ),
        GlossaryTerm(
            key = "frequency",
            englishTerm = "Frequency",
            term = Res.string.gl_frequency_term,
            definition = Res.string.gl_frequency_def,
            symbol = "f",
            unit = "Hz",
            seeAlso = listOf("inductive_reactance", "slip"),
            referenceTopic = "standard_voltages",
        ),
        GlossaryTerm(
            key = "functional_earth",
            englishTerm = "Functional earth",
            term = Res.string.gl_functional_earth_term,
            definition = Res.string.gl_functional_earth_def,
            seeAlso = listOf("earthing", "protective_conductor"),
        ),
        GlossaryTerm(
            key = "fuse",
            englishTerm = "Fuse",
            term = Res.string.gl_fuse_term,
            definition = Res.string.gl_fuse_def,
            seeAlso = listOf("gg_fuse", "circuit_breaker", "let_through_energy"),
            referenceTopic = "rating_series",
        ),
        GlossaryTerm(
            key = "gg_fuse",
            englishTerm = "gG fuse",
            term = Res.string.gl_gg_fuse_term,
            definition = Res.string.gl_gg_fuse_def,
            seeAlso = listOf("fuse", "overload"),
            referenceTopic = "rating_series",
        ),
        GlossaryTerm(
            key = "grouping_factor",
            englishTerm = "Grouping factor",
            term = Res.string.gl_grouping_factor_term,
            definition = Res.string.gl_grouping_factor_def,
            symbol = "Cg",
            seeAlso = listOf("correction_factor", "derating", "ampacity"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "harmonic",
            englishTerm = "Harmonic",
            term = Res.string.gl_harmonic_term,
            definition = Res.string.gl_harmonic_def,
            seeAlso = listOf("neutral_conductor", "rcd", "power_factor_correction"),
        ),
        GlossaryTerm(
            key = "ik_code",
            englishTerm = "IK code",
            term = Res.string.gl_ik_code_term,
            definition = Res.string.gl_ik_code_def,
            seeAlso = listOf("ip_code"),
            referenceTopic = "ik_rating",
        ),
        GlossaryTerm(
            key = "illuminance",
            englishTerm = "Illuminance",
            term = Res.string.gl_illuminance_term,
            definition = Res.string.gl_illuminance_def,
            symbol = "E",
            unit = "lx",
            seeAlso = listOf("luminous_flux"),
        ),
        GlossaryTerm(
            key = "impedance",
            englishTerm = "Impedance",
            term = Res.string.gl_impedance_term,
            definition = Res.string.gl_impedance_def,
            symbol = "Z",
            unit = "Ω",
            seeAlso = listOf("inductive_reactance", "capacitive_reactance", "resistivity"),
        ),
        GlossaryTerm(
            key = "impedance_voltage",
            englishTerm = "Impedance voltage (uk)",
            term = Res.string.gl_impedance_voltage_term,
            definition = Res.string.gl_impedance_voltage_def,
            symbol = "uk",
            unit = "%",
            seeAlso = listOf("transformer", "prospective_short_circuit_current"),
            calculator = CalculatorId.TRANSFORMER_CURRENT,
        ),
        GlossaryTerm(
            key = "indirect_contact",
            englishTerm = "Indirect contact",
            term = Res.string.gl_indirect_contact_term,
            definition = Res.string.gl_indirect_contact_def,
            seeAlso = listOf("fault_protection", "exposed_conductive_part", "touch_voltage"),
        ),
        GlossaryTerm(
            key = "inductive_reactance",
            englishTerm = "Inductive reactance",
            term = Res.string.gl_inductive_reactance_term,
            definition = Res.string.gl_inductive_reactance_def,
            symbol = "XL",
            unit = "Ω",
            seeAlso = listOf("impedance", "capacitive_reactance", "voltage_drop"),
        ),
        GlossaryTerm(
            key = "capacitive_reactance",
            englishTerm = "Capacitive reactance",
            term = Res.string.gl_capacitive_reactance_term,
            definition = Res.string.gl_capacitive_reactance_def,
            symbol = "XC",
            unit = "Ω",
            seeAlso = listOf("impedance", "inductive_reactance", "power_factor_correction"),
        ),
        GlossaryTerm(
            key = "inrush_current",
            englishTerm = "Inrush current",
            term = Res.string.gl_inrush_current_term,
            definition = Res.string.gl_inrush_current_def,
            seeAlso = listOf("trip_curve", "soft_starter", "star_delta_starting"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "instantaneous_trip",
            englishTerm = "Instantaneous trip",
            term = Res.string.gl_instantaneous_trip_term,
            definition = Res.string.gl_instantaneous_trip_def,
            seeAlso = listOf("trip_curve", "thermal_magnetic_trip", "disconnection_time"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "insulation_resistance",
            englishTerm = "Insulation resistance",
            term = Res.string.gl_insulation_resistance_term,
            definition = Res.string.gl_insulation_resistance_def,
            unit = "MΩ",
            seeAlso = listOf("continuity_test"),
        ),
        GlossaryTerm(
            key = "interlock",
            englishTerm = "Interlock",
            term = Res.string.gl_interlock_term,
            definition = Res.string.gl_interlock_def,
            seeAlso = listOf("contactor", "isolation"),
        ),
        GlossaryTerm(
            key = "ip_code",
            englishTerm = "IP code",
            term = Res.string.gl_ip_code_term,
            definition = Res.string.gl_ip_code_def,
            seeAlso = listOf("ik_code", "creepage_distance"),
            referenceTopic = "ip_rating",
        ),
        GlossaryTerm(
            key = "isolation",
            englishTerm = "Isolation",
            term = Res.string.gl_isolation_term,
            definition = Res.string.gl_isolation_def,
            seeAlso = listOf("interlock", "live_part", "switchgear"),
        ),
        GlossaryTerm(
            key = "it_system",
            englishTerm = "IT system",
            term = Res.string.gl_it_system_term,
            definition = Res.string.gl_it_system_def,
            seeAlso = listOf("tn_system", "tt_system", "earthing"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "let_through_energy",
            englishTerm = "Let-through energy (I²t)",
            term = Res.string.gl_let_through_energy_term,
            definition = Res.string.gl_let_through_energy_def,
            symbol = "I²t",
            unit = "A²s",
            seeAlso = listOf("adiabatic_check", "fuse", "breaking_capacity"),
        ),
        GlossaryTerm(
            key = "line_conductor",
            englishTerm = "Line conductor",
            term = Res.string.gl_line_conductor_term,
            definition = Res.string.gl_line_conductor_def,
            symbol = "L",
            seeAlso = listOf("neutral_conductor", "protective_conductor", "live_part"),
            referenceTopic = "conductor_colours",
        ),
        GlossaryTerm(
            key = "live_part",
            englishTerm = "Live part",
            term = Res.string.gl_live_part_term,
            definition = Res.string.gl_live_part_def,
            seeAlso = listOf("basic_protection", "isolation", "neutral_conductor"),
        ),
        GlossaryTerm(
            key = "load_factor",
            englishTerm = "Load factor",
            term = Res.string.gl_load_factor_term,
            definition = Res.string.gl_load_factor_def,
            seeAlso = listOf("demand_factor", "diversity_factor"),
        ),
        GlossaryTerm(
            key = "lszh",
            englishTerm = "LSZH",
            term = Res.string.gl_lszh_term,
            definition = Res.string.gl_lszh_def,
            seeAlso = listOf("pvc", "xlpe"),
        ),
        GlossaryTerm(
            key = "luminous_flux",
            englishTerm = "Luminous flux",
            term = Res.string.gl_luminous_flux_term,
            definition = Res.string.gl_luminous_flux_def,
            symbol = "Φ",
            unit = "lm",
            seeAlso = listOf("illuminance"),
        ),
        GlossaryTerm(
            key = "mcb",
            englishTerm = "MCB",
            term = Res.string.gl_mcb_term,
            definition = Res.string.gl_mcb_def,
            seeAlso = listOf("mccb", "trip_curve", "circuit_breaker", "rcbo"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "mccb",
            englishTerm = "MCCB",
            term = Res.string.gl_mccb_term,
            definition = Res.string.gl_mccb_def,
            seeAlso = listOf("mcb", "selectivity", "breaking_capacity"),
        ),
        GlossaryTerm(
            key = "nameplate",
            englishTerm = "Nameplate",
            term = Res.string.gl_nameplate_term,
            definition = Res.string.gl_nameplate_def,
            seeAlso = listOf("rated_current", "rated_voltage", "service_factor"),
        ),
        GlossaryTerm(
            key = "neutral_conductor",
            englishTerm = "Neutral conductor",
            term = Res.string.gl_neutral_conductor_term,
            definition = Res.string.gl_neutral_conductor_def,
            symbol = "N",
            seeAlso = listOf("line_conductor", "pen_conductor", "harmonic"),
            referenceTopic = "conductor_colours",
        ),
        GlossaryTerm(
            key = "no_load_loss",
            englishTerm = "No-load loss",
            term = Res.string.gl_no_load_loss_term,
            definition = Res.string.gl_no_load_loss_def,
            unit = "W",
            seeAlso = listOf("transformer", "efficiency"),
        ),
        GlossaryTerm(
            key = "overcurrent",
            englishTerm = "Overcurrent",
            term = Res.string.gl_overcurrent_term,
            definition = Res.string.gl_overcurrent_def,
            seeAlso = listOf("overload", "short_circuit", "thermal_magnetic_trip"),
        ),
        GlossaryTerm(
            key = "overload",
            englishTerm = "Overload",
            term = Res.string.gl_overload_term,
            definition = Res.string.gl_overload_def,
            seeAlso = listOf("overcurrent", "short_circuit", "thermal_magnetic_trip"),
        ),
        GlossaryTerm(
            key = "pen_conductor",
            englishTerm = "PEN conductor",
            term = Res.string.gl_pen_conductor_term,
            definition = Res.string.gl_pen_conductor_def,
            symbol = "PEN",
            seeAlso = listOf("protective_conductor", "neutral_conductor", "tn_system"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "peukert_law",
            englishTerm = "Peukert's law",
            term = Res.string.gl_peukert_law_term,
            definition = Res.string.gl_peukert_law_def,
            symbol = "k",
            seeAlso = listOf("c_rate", "depth_of_discharge"),
            calculator = CalculatorId.BATTERY_RUNTIME,
        ),
        GlossaryTerm(
            key = "phase_angle",
            englishTerm = "Phase angle",
            term = Res.string.gl_phase_angle_term,
            definition = Res.string.gl_phase_angle_def,
            symbol = "φ",
            unit = "°",
            seeAlso = listOf("cos_phi", "reactive_power"),
            calculator = CalculatorId.POWER,
        ),
        GlossaryTerm(
            key = "phase_sequence",
            englishTerm = "Phase sequence",
            term = Res.string.gl_phase_sequence_term,
            definition = Res.string.gl_phase_sequence_def,
            seeAlso = listOf("polarity", "slip"),
        ),
        GlossaryTerm(
            key = "polarity",
            englishTerm = "Polarity",
            term = Res.string.gl_polarity_term,
            definition = Res.string.gl_polarity_def,
            seeAlso = listOf("line_conductor", "neutral_conductor", "isolation"),
        ),
        GlossaryTerm(
            key = "power_factor_correction",
            englishTerm = "Power factor correction",
            term = Res.string.gl_power_factor_correction_term,
            definition = Res.string.gl_power_factor_correction_def,
            symbol = "Qc",
            unit = "var",
            seeAlso = listOf("cos_phi", "reactive_power", "harmonic"),
            calculator = CalculatorId.POWER_FACTOR_CORRECTION,
        ),
        GlossaryTerm(
            key = "prospective_short_circuit_current",
            englishTerm = "Prospective short-circuit current",
            term = Res.string.gl_prospective_short_circuit_current_term,
            definition = Res.string.gl_prospective_short_circuit_current_def,
            symbol = "Ipf",
            unit = "kA",
            seeAlso = listOf("breaking_capacity", "short_circuit", "impedance_voltage"),
            calculator = CalculatorId.SHORT_CIRCUIT,
        ),
        GlossaryTerm(
            key = "protective_conductor",
            englishTerm = "Protective conductor (PE)",
            term = Res.string.gl_protective_conductor_term,
            definition = Res.string.gl_protective_conductor_def,
            symbol = "PE",
            seeAlso = listOf("pen_conductor", "earthing", "adiabatic_check"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "conductor_colours",
        ),
        GlossaryTerm(
            key = "pvc",
            englishTerm = "PVC",
            term = Res.string.gl_pvc_term,
            definition = Res.string.gl_pvc_def,
            seeAlso = listOf("xlpe", "lszh"),
            referenceTopic = "materials",
        ),
        GlossaryTerm(
            key = "rated_current",
            englishTerm = "Rated current",
            term = Res.string.gl_rated_current_term,
            definition = Res.string.gl_rated_current_def,
            symbol = "In",
            unit = "A",
            seeAlso = listOf("ampacity", "nameplate", "trip_curve"),
            referenceTopic = "rating_series",
        ),
        GlossaryTerm(
            key = "rated_voltage",
            englishTerm = "Rated voltage",
            term = Res.string.gl_rated_voltage_term,
            definition = Res.string.gl_rated_voltage_def,
            symbol = "Un",
            unit = "V",
            seeAlso = listOf("nameplate", "voltage_drop"),
            referenceTopic = "standard_voltages",
        ),
        GlossaryTerm(
            key = "rcbo",
            englishTerm = "RCBO",
            term = Res.string.gl_rcbo_term,
            definition = Res.string.gl_rcbo_def,
            seeAlso = listOf("rccb", "rcd", "mcb"),
            referenceTopic = "rcd_types",
        ),
        GlossaryTerm(
            key = "rccb",
            englishTerm = "RCCB",
            term = Res.string.gl_rccb_term,
            definition = Res.string.gl_rccb_def,
            seeAlso = listOf("rcbo", "rcd"),
            referenceTopic = "rcd_types",
        ),
        GlossaryTerm(
            key = "rcd",
            englishTerm = "RCD",
            term = Res.string.gl_rcd_term,
            definition = Res.string.gl_rcd_def,
            symbol = "IΔn",
            unit = "mA",
            seeAlso = listOf("residual_current", "rcbo", "rccb", "touch_voltage"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
            referenceTopic = "rcd_types",
        ),
        GlossaryTerm(
            key = "reactive_power",
            englishTerm = "Reactive power",
            term = Res.string.gl_reactive_power_term,
            definition = Res.string.gl_reactive_power_def,
            symbol = "Q",
            unit = "var",
            seeAlso = listOf("active_power", "apparent_power", "power_factor_correction"),
            calculator = CalculatorId.POWER,
        ),
        GlossaryTerm(
            key = "reference_method",
            englishTerm = "Reference method",
            term = Res.string.gl_reference_method_term,
            definition = Res.string.gl_reference_method_def,
            seeAlso = listOf("ampacity", "correction_factor"),
            calculator = CalculatorId.CABLE_SIZE,
        ),
        GlossaryTerm(
            key = "relay",
            englishTerm = "Relay",
            term = Res.string.gl_relay_term,
            definition = Res.string.gl_relay_def,
            seeAlso = listOf("contactor", "interlock"),
        ),
        GlossaryTerm(
            key = "residual_current",
            englishTerm = "Residual current",
            term = Res.string.gl_residual_current_term,
            definition = Res.string.gl_residual_current_def,
            symbol = "IΔ",
            unit = "mA",
            seeAlso = listOf("rcd", "fault_current"),
            referenceTopic = "rcd_types",
        ),
        GlossaryTerm(
            key = "resistivity",
            englishTerm = "Resistivity",
            term = Res.string.gl_resistivity_term,
            definition = Res.string.gl_resistivity_def,
            symbol = "ρ",
            unit = "Ω·mm²/m",
            seeAlso = listOf("conductor", "voltage_drop"),
            referenceTopic = "materials",
        ),
        GlossaryTerm(
            key = "rms",
            englishTerm = "RMS",
            term = Res.string.gl_rms_term,
            definition = Res.string.gl_rms_def,
            seeAlso = listOf("rated_voltage", "frequency"),
        ),
        GlossaryTerm(
            key = "selectivity",
            englishTerm = "Selectivity (discrimination)",
            term = Res.string.gl_selectivity_term,
            definition = Res.string.gl_selectivity_def,
            seeAlso = listOf("cascading", "back_up_protection", "trip_curve"),
        ),
        GlossaryTerm(
            key = "service_factor",
            englishTerm = "Service factor",
            term = Res.string.gl_service_factor_term,
            definition = Res.string.gl_service_factor_def,
            seeAlso = listOf("nameplate", "overload"),
            calculator = CalculatorId.MOTOR_CURRENT,
        ),
        GlossaryTerm(
            key = "sheath",
            englishTerm = "Sheath",
            term = Res.string.gl_sheath_term,
            definition = Res.string.gl_sheath_def,
            seeAlso = listOf("armour", "pvc", "lszh"),
            calculator = CalculatorId.CABLE_WEIGHT,
        ),
        GlossaryTerm(
            key = "short_circuit",
            englishTerm = "Short circuit",
            term = Res.string.gl_short_circuit_term,
            definition = Res.string.gl_short_circuit_def,
            seeAlso = listOf("prospective_short_circuit_current", "breaking_capacity", "fault_current"),
            calculator = CalculatorId.SHORT_CIRCUIT,
        ),
        GlossaryTerm(
            key = "skin_effect",
            englishTerm = "Skin effect",
            term = Res.string.gl_skin_effect_term,
            definition = Res.string.gl_skin_effect_def,
            seeAlso = listOf("conductor", "inductive_reactance"),
        ),
        GlossaryTerm(
            key = "slip",
            englishTerm = "Slip",
            term = Res.string.gl_slip_term,
            definition = Res.string.gl_slip_def,
            symbol = "s",
            seeAlso = listOf("frequency", "vfd"),
        ),
        GlossaryTerm(
            key = "soft_starter",
            englishTerm = "Soft starter",
            term = Res.string.gl_soft_starter_term,
            definition = Res.string.gl_soft_starter_def,
            seeAlso = listOf("inrush_current", "star_delta_starting", "vfd"),
        ),
        GlossaryTerm(
            key = "spd",
            englishTerm = "Surge protective device (SPD)",
            term = Res.string.gl_spd_term,
            definition = Res.string.gl_spd_def,
            seeAlso = listOf("earthing", "equipotential_bonding"),
        ),
        GlossaryTerm(
            key = "star_delta_starting",
            englishTerm = "Star-delta starting",
            term = Res.string.gl_star_delta_starting_term,
            definition = Res.string.gl_star_delta_starting_def,
            seeAlso = listOf("inrush_current", "soft_starter", "vfd"),
            calculator = CalculatorId.MOTOR_CURRENT,
        ),
        GlossaryTerm(
            key = "switchgear",
            englishTerm = "Switchgear",
            term = Res.string.gl_switchgear_term,
            definition = Res.string.gl_switchgear_def,
            seeAlso = listOf("distribution_board", "busbar", "form_of_separation"),
        ),
        GlossaryTerm(
            key = "thermal_magnetic_trip",
            englishTerm = "Thermal magnetic trip",
            term = Res.string.gl_thermal_magnetic_trip_term,
            definition = Res.string.gl_thermal_magnetic_trip_def,
            seeAlso = listOf("instantaneous_trip", "overload", "short_circuit"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "tn_system",
            englishTerm = "TN system",
            term = Res.string.gl_tn_system_term,
            definition = Res.string.gl_tn_system_def,
            seeAlso = listOf("tt_system", "it_system", "pen_conductor", "earthing"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "touch_voltage",
            englishTerm = "Touch voltage",
            term = Res.string.gl_touch_voltage_term,
            definition = Res.string.gl_touch_voltage_def,
            symbol = "UL",
            unit = "V",
            seeAlso = listOf("equipotential_bonding", "rcd", "automatic_disconnection"),
            calculator = CalculatorId.EARTH_FAULT_LOOP,
        ),
        GlossaryTerm(
            key = "transformer",
            englishTerm = "Transformer",
            term = Res.string.gl_transformer_term,
            definition = Res.string.gl_transformer_def,
            unit = "kVA",
            seeAlso = listOf("impedance_voltage", "vector_group", "no_load_loss"),
            calculator = CalculatorId.TRANSFORMER_CURRENT,
        ),
        GlossaryTerm(
            key = "trip_curve",
            englishTerm = "Trip curve",
            term = Res.string.gl_trip_curve_term,
            definition = Res.string.gl_trip_curve_def,
            seeAlso = listOf("instantaneous_trip", "mcb", "selectivity"),
            referenceTopic = "breaker_curves",
        ),
        GlossaryTerm(
            key = "tt_system",
            englishTerm = "TT system",
            term = Res.string.gl_tt_system_term,
            definition = Res.string.gl_tt_system_def,
            seeAlso = listOf("tn_system", "it_system", "earth_electrode", "rcd"),
            referenceTopic = "earthing_systems",
        ),
        GlossaryTerm(
            key = "vector_group",
            englishTerm = "Vector group",
            term = Res.string.gl_vector_group_term,
            definition = Res.string.gl_vector_group_def,
            seeAlso = listOf("transformer", "phase_sequence"),
        ),
        GlossaryTerm(
            key = "vfd",
            englishTerm = "Variable frequency drive (VFD)",
            term = Res.string.gl_vfd_term,
            definition = Res.string.gl_vfd_def,
            seeAlso = listOf("soft_starter", "harmonic", "rcd", "slip"),
            referenceTopic = "rcd_types",
        ),
        GlossaryTerm(
            key = "voltage_drop",
            englishTerm = "Voltage drop",
            term = Res.string.gl_voltage_drop_term,
            definition = Res.string.gl_voltage_drop_def,
            symbol = "ΔU",
            unit = "V",
            seeAlso = listOf("resistivity", "cross_sectional_area", "rated_voltage"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),
        GlossaryTerm(
            key = "voltage_factor",
            englishTerm = "Voltage factor",
            term = Res.string.gl_voltage_factor_term,
            definition = Res.string.gl_voltage_factor_def,
            symbol = "c",
            seeAlso = listOf("prospective_short_circuit_current", "short_circuit"),
            calculator = CalculatorId.SHORT_CIRCUIT,
        ),
        GlossaryTerm(
            key = "withstand_current",
            englishTerm = "Withstand current",
            term = Res.string.gl_withstand_current_term,
            definition = Res.string.gl_withstand_current_def,
            unit = "kA",
            seeAlso = listOf("adiabatic_check", "let_through_energy", "busbar"),
        ),
        GlossaryTerm(
            key = "xlpe",
            englishTerm = "XLPE",
            term = Res.string.gl_xlpe_term,
            definition = Res.string.gl_xlpe_def,
            seeAlso = listOf("pvc", "lszh", "ampacity"),
            referenceTopic = "materials",
        ),
    )

    private val byKey: Map<String, GlossaryTerm> = all.associateBy { it.key }

    /** The term for [key], or null for a stale cross-reference or deep link. */
    fun termOrNull(key: String): GlossaryTerm? = byKey[key]
}
