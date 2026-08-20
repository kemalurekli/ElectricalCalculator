package com.kemalurekli.electricalcalculator.features.theory.domain

import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_assumption_dc
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_assumption_one_way
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_assumption_uniform
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_example_radial
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_example_submain
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_material_aluminium
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_material_copper
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_conductor_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_assumption_resistive
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_assumption_two_branches
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_example_equal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_example_uneven
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_solve_branches
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_current_divider_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_assumption_dc
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_assumption_resistive
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_example_kettle
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_example_led
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_example_motor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_example_nameplate
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_solve_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_solve_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_solve_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_dc_power_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_assumption_no_load
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_assumption_not_a_supply
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_assumption_stiff_source
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_example_loaded
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_example_stiff
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_example_unloaded
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_solve_output
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_divider_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_assumption_no_leakage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_assumption_saturation
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_example_sixty
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_example_small
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_solve_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_emf_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_active_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_area
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_average
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_base_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_base_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_capacitance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_core_area
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_current_in_one
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_current_in_two
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_current_out
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_delta_ab
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_delta_bc
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_delta_ca
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_divider_lower
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_divider_upper
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_drop_one
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_drop_two
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_flux
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_frequency
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_impedance_ohms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_inductance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_length
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_line_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_line_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_load_hint
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_load_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_optional_hint
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_path_length
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_peak
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_permeability
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_power_factor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_primary_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_primary_turns
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_primary_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_resistance_one
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_resistance_three
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_resistance_two
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_rms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_secondary_turns
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_star_a
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_star_b
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_star_c
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_supply_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_supply_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_temperature
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_temperature_hint
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_thevenin_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_thevenin_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_time
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_turns
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_field_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_assumption_lumped
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_assumption_signs
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_example_drops
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_example_junction
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_solve_loop
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_solve_node
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_kirchhoff_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_assumption_linear
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_assumption_no_gap
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_assumption_uniform
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_example_air
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_example_steel
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_solve_flux
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_magnetic_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_assumption_efficiency
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_assumption_fixed_source
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_assumption_signal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_example_efficient
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_example_matched
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_mpt_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_assumption_internal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_assumption_same
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_example_converted
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_norton_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_assumption_dc
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_assumption_linear
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_assumption_temperature
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_example_element
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_example_heater
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_example_lamp
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_example_shunt
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_solve_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_solve_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_solve_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ohm_law_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_assumption_ideal_wire
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_assumption_same_nodes
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_example_mixed
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_example_two
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_solve_total
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_parallel_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_assumption_same_base
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_assumption_three_phase
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_example_feeder
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_example_lv
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_solve_pu
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_per_unit_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_assumption_never
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_assumption_step
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_example_debounce
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_example_timer
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_solve_charging
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rc_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_assumption_series
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_assumption_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_example_filter
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_example_sharp
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_resonance_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_assumption_series
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_example_capacitive
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_example_inductive
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rlc_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_assumption_meter
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_example_mains
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_example_meter
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_example_scope
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_solve_from_average
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_solve_from_peak
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_solve_from_rms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_rms_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_selector_connection
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_selector_direction
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_selector_known
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_selector_law
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_selector_material
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_assumption_ideal_wire
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_assumption_one_path
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_example_three
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_example_two
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_solve_total
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_series_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_assumption_internal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_assumption_linear
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_assumption_terminals
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_example_divider
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_thevenin_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_assumption_balanced
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_example_board
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_example_motor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_solve_delta
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_solve_star
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_three_phase_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_assumption_no_magnetising
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_example_step_down
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_example_step_up
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_solve_secondary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_transformer_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_assumption_displacement
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_example_corrected
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_example_motor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_solve
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_triangle_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_active_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_apparent_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_area
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_average
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_bandwidth
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_base_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_base_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_branch_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_branch_drop
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_branch_one_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_branch_two_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_capacitance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_capacitive_reactance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_capacitor_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_conductor_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_current_out
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_current_unknown
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_currents_in
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_delivered_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_delta_arms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_drop_unknown
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_efficiency
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_elapsed_time
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_emf
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_flux
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_flux_density
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_frequency
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_impedance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_inductance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_inductive_reactance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_internal_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_known_drops
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_length
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_line_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_line_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_load_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_mmf
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_open_circuit_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_output_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_peak
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_phase_angle
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_phase_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_phase_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_power_factor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_primary_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_quality_factor
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_reactive_power
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_reluctance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_resistivity
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_resonant_frequency
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_rms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_secondary_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_short_circuit_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_star_arms
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_supply_current
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_supply_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_time_constant
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_total_resistance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_turns
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_turns_ratio
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_var_voltage
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_assumption_phase
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_example_correction
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_example_small
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_solve_reactance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xc_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_assumption_ideal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_assumption_saturation
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_assumption_sinusoidal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_example_choke
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_example_coil
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_solve_reactance
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_xl_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_assumption_not_supply
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_assumption_resistive
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_assumption_terminals
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_example_delta_equal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_example_equal
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_example_uneven
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_solve_to_delta
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_solve_to_star
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_summary
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_ydelta_title

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
            title = Res.string.th_ohm_law_title,
            summary = Res.string.th_ohm_law_summary,
            theory = Res.string.th_ohm_law_theory,
            assumptions = listOf(
                Res.string.th_ohm_law_assumption_linear,
                Res.string.th_ohm_law_assumption_temperature,
                Res.string.th_ohm_law_assumption_dc,
            ),
            diagram = TheoryDiagram.SIMPLE_LOOP,
            solutions = listOf(
                TheorySolution(
                    key = "current",
                    targetLabel = Res.string.th_ohm_law_solve_current,
                    formula = "I = U / R",
                    variables = listOf(
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                        TheoryVariable("R", Res.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("r", Res.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "lamp", Res.string.th_ohm_law_example_lamp,
                            mapOf("u" to "230", "r" to "529"),
                        ),
                        TheoryExample(
                            "heater", Res.string.th_ohm_law_example_heater,
                            mapOf("u" to "230", "r" to "26.5"),
                        ),
                    ),
                    solve = ::ohmForCurrent,
                ),
                TheorySolution(
                    key = "voltage",
                    targetLabel = Res.string.th_ohm_law_solve_voltage,
                    formula = "U = I · R",
                    variables = listOf(
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                        TheoryVariable("R", Res.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("i", Res.string.th_field_current, "A", min = 0.0),
                        TheoryField("r", Res.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "shunt", Res.string.th_ohm_law_example_shunt,
                            mapOf("i" to "10", "r" to "0.01"),
                        ),
                    ),
                    solve = ::ohmForVoltage,
                ),
                TheorySolution(
                    key = "resistance",
                    targetLabel = Res.string.th_ohm_law_solve_resistance,
                    formula = "R = U / I",
                    variables = listOf(
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                        TheoryVariable("R", Res.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("i", Res.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "element", Res.string.th_ohm_law_example_element,
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
            title = Res.string.th_dc_power_title,
            summary = Res.string.th_dc_power_summary,
            theory = Res.string.th_dc_power_theory,
            assumptions = listOf(
                Res.string.th_dc_power_assumption_dc,
                Res.string.th_dc_power_assumption_resistive,
            ),
            diagram = TheoryDiagram.SIMPLE_LOOP,
            solutions = listOf(
                TheorySolution(
                    key = "power",
                    targetLabel = Res.string.th_dc_power_solve_power,
                    formula = "P = U · I = I² · R = U² / R",
                    variables = listOf(
                        TheoryVariable("P", Res.string.th_var_power, "W"),
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                        TheoryVariable("R", Res.string.th_var_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("i", Res.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "led_strip", Res.string.th_dc_power_example_led,
                            mapOf("u" to "12", "i" to "1.5"),
                        ),
                        TheoryExample(
                            "kettle", Res.string.th_dc_power_example_kettle,
                            mapOf("u" to "230", "i" to "9.5"),
                        ),
                    ),
                    solve = ::powerFromVoltageAndCurrent,
                ),
                TheorySolution(
                    key = "current",
                    targetLabel = Res.string.th_dc_power_solve_current,
                    formula = "I = P / U",
                    variables = listOf(
                        TheoryVariable("P", Res.string.th_var_power, "W"),
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("p", Res.string.th_field_power, "W", min = 0.0),
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "nameplate", Res.string.th_dc_power_example_nameplate,
                            mapOf("p" to "2200", "u" to "230"),
                        ),
                    ),
                    solve = ::currentFromPower,
                ),
                TheorySolution(
                    key = "voltage",
                    targetLabel = Res.string.th_dc_power_solve_voltage,
                    formula = "U = P / I",
                    variables = listOf(
                        TheoryVariable("P", Res.string.th_var_power, "W"),
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("p", Res.string.th_field_power, "W", min = 0.0),
                        TheoryField("i", Res.string.th_field_current, "A", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "motor", Res.string.th_dc_power_example_motor,
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
            title = Res.string.th_series_title,
            summary = Res.string.th_series_summary,
            theory = Res.string.th_series_theory,
            assumptions = listOf(
                Res.string.th_series_assumption_one_path,
                Res.string.th_series_assumption_ideal_wire,
            ),
            diagram = TheoryDiagram.SERIES_RESISTORS,
            solutions = listOf(
                TheorySolution(
                    key = "total",
                    targetLabel = Res.string.th_series_solve_total,
                    formula = "R_t = R₁ + R₂ + R₃\nI = U / R_t\nU_n = I · R_n",
                    variables = listOf(
                        TheoryVariable("R_t", Res.string.th_var_total_resistance, "Ω"),
                        TheoryVariable("I", Res.string.th_var_current, "A"),
                        TheoryVariable("U_n", Res.string.th_var_branch_drop, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "24"),
                        TheoryField("r1", Res.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", Res.string.th_field_resistance_two, "Ω", min = 0.0),
                        TheoryField(
                            "r3", Res.string.th_field_resistance_three, "Ω", min = 0.0,
                            optional = true, hint = Res.string.th_field_optional_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "two_resistors", Res.string.th_series_example_two,
                            mapOf("u" to "24", "r1" to "100", "r2" to "220"),
                        ),
                        TheoryExample(
                            "three_resistors", Res.string.th_series_example_three,
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
            title = Res.string.th_parallel_title,
            summary = Res.string.th_parallel_summary,
            theory = Res.string.th_parallel_theory,
            assumptions = listOf(
                Res.string.th_parallel_assumption_same_nodes,
                Res.string.th_parallel_assumption_ideal_wire,
            ),
            diagram = TheoryDiagram.PARALLEL_RESISTORS,
            solutions = listOf(
                TheorySolution(
                    key = "total",
                    targetLabel = Res.string.th_parallel_solve_total,
                    formula = "1/R_t = 1/R₁ + 1/R₂ + 1/R₃\nI_n = U / R_n",
                    variables = listOf(
                        TheoryVariable("R_t", Res.string.th_var_total_resistance, "Ω"),
                        TheoryVariable("I_n", Res.string.th_var_branch_current, "A"),
                        TheoryVariable("U", Res.string.th_var_voltage, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("r1", Res.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", Res.string.th_field_resistance_two, "Ω", min = 0.0),
                        TheoryField(
                            "r3", Res.string.th_field_resistance_three, "Ω", min = 0.0,
                            optional = true, hint = Res.string.th_field_optional_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "two_lamps", Res.string.th_parallel_example_two,
                            mapOf("u" to "230", "r1" to "529", "r2" to "529"),
                        ),
                        TheoryExample(
                            "mixed_load", Res.string.th_parallel_example_mixed,
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
            title = Res.string.th_divider_title,
            summary = Res.string.th_divider_summary,
            theory = Res.string.th_divider_theory,
            assumptions = listOf(
                Res.string.th_divider_assumption_no_load,
                Res.string.th_divider_assumption_stiff_source,
                Res.string.th_divider_assumption_not_a_supply,
            ),
            diagram = TheoryDiagram.VOLTAGE_DIVIDER,
            solutions = listOf(
                TheorySolution(
                    key = "output",
                    targetLabel = Res.string.th_divider_solve_output,
                    formula = "U_out = U · R₂ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("U", Res.string.th_var_supply_voltage, "V"),
                        TheoryVariable("U_out", Res.string.th_var_output_voltage, "V"),
                        TheoryVariable("R_L", Res.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("r1", Res.string.th_field_divider_upper, "Ω", min = 0.0),
                        TheoryField("r2", Res.string.th_field_divider_lower, "Ω", min = 0.0),
                        TheoryField(
                            "r_load", Res.string.th_field_load_resistance, "Ω", min = 0.0,
                            optional = true, hint = Res.string.th_field_load_hint,
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "unloaded", Res.string.th_divider_example_unloaded,
                            mapOf("u" to "12", "r1" to "10000", "r2" to "10000"),
                        ),
                        TheoryExample(
                            "loaded", Res.string.th_divider_example_loaded,
                            mapOf("u" to "12", "r1" to "10000", "r2" to "10000", "r_load" to "10000"),
                        ),
                        TheoryExample(
                            "stiff", Res.string.th_divider_example_stiff,
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
            title = Res.string.th_current_divider_title,
            summary = Res.string.th_current_divider_summary,
            theory = Res.string.th_current_divider_theory,
            assumptions = listOf(
                Res.string.th_current_divider_assumption_two_branches,
                Res.string.th_current_divider_assumption_resistive,
            ),
            diagram = TheoryDiagram.CURRENT_DIVIDER,
            solutions = listOf(
                TheorySolution(
                    key = "branches",
                    targetLabel = Res.string.th_current_divider_solve_branches,
                    formula = "I₁ = I · R₂ / (R₁ + R₂)\nI₂ = I · R₁ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("I", Res.string.th_var_supply_current, "A"),
                        TheoryVariable("I₁", Res.string.th_var_branch_one_current, "A"),
                        TheoryVariable("I₂", Res.string.th_var_branch_two_current, "A"),
                    ),
                    fields = listOf(
                        TheoryField("i", Res.string.th_field_supply_current, "A", min = 0.0),
                        TheoryField("r1", Res.string.th_field_resistance_one, "Ω", min = 0.0),
                        TheoryField("r2", Res.string.th_field_resistance_two, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", Res.string.th_current_divider_example_equal,
                            mapOf("i" to "10", "r1" to "100", "r2" to "100"),
                        ),
                        TheoryExample(
                            "uneven", Res.string.th_current_divider_example_uneven,
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
            title = Res.string.th_conductor_title,
            summary = Res.string.th_conductor_summary,
            theory = Res.string.th_conductor_theory,
            assumptions = listOf(
                Res.string.th_conductor_assumption_dc,
                Res.string.th_conductor_assumption_uniform,
                Res.string.th_conductor_assumption_one_way,
            ),
            diagram = TheoryDiagram.CONDUCTOR_RUN,
            selectorLabel = Res.string.th_selector_material,
            solutions = listOf(
                conductorSolution("copper", Res.string.th_conductor_material_copper, ::copperResistance),
                conductorSolution(
                    "aluminium", Res.string.th_conductor_material_aluminium, ::aluminiumResistance,
                ),
            ),
            glossaryTerms = listOf("resistivity", "conductor"),
            calculator = CalculatorId.VOLTAGE_DROP,
        ),

        // ---- Intermediate -------------------------------------------------

        TheoryTopic(
            key = "kirchhoff_laws",
            level = TheoryLevel.INTERMEDIATE,
            title = Res.string.th_kirchhoff_title,
            summary = Res.string.th_kirchhoff_summary,
            theory = Res.string.th_kirchhoff_theory,
            assumptions = listOf(
                Res.string.th_kirchhoff_assumption_lumped,
                Res.string.th_kirchhoff_assumption_signs,
            ),
            diagram = TheoryDiagram.NODE_AND_LOOP,
            selectorLabel = Res.string.th_selector_law,
            solutions = listOf(
                TheorySolution(
                    key = "node",
                    targetLabel = Res.string.th_kirchhoff_solve_node,
                    formula = "ΣI_in = ΣI_out",
                    variables = listOf(
                        TheoryVariable("I₁, I₂", Res.string.th_var_currents_in, "A"),
                        TheoryVariable("I₃", Res.string.th_var_current_out, "A"),
                        TheoryVariable("I₄", Res.string.th_var_current_unknown, "A"),
                    ),
                    fields = listOf(
                        TheoryField("i_in1", Res.string.th_field_current_in_one, "A", allowNegative = true),
                        TheoryField("i_in2", Res.string.th_field_current_in_two, "A", allowNegative = true),
                        TheoryField("i_out1", Res.string.th_field_current_out, "A", allowNegative = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "junction", Res.string.th_kirchhoff_example_junction,
                            mapOf("i_in1" to "10", "i_in2" to "4", "i_out1" to "6"),
                        ),
                    ),
                    solve = ::kirchhoffNode,
                ),
                TheorySolution(
                    key = "loop",
                    targetLabel = Res.string.th_kirchhoff_solve_loop,
                    formula = "ΣU_source = ΣU_drop",
                    variables = listOf(
                        TheoryVariable("U", Res.string.th_var_supply_voltage, "V"),
                        TheoryVariable("U₁, U₂", Res.string.th_var_known_drops, "V"),
                        TheoryVariable("U₃", Res.string.th_var_drop_unknown, "V"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "230"),
                        TheoryField("u1", Res.string.th_field_drop_one, "V", allowZero = true),
                        TheoryField("u2", Res.string.th_field_drop_two, "V", allowZero = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "three_drops", Res.string.th_kirchhoff_example_drops,
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
            title = Res.string.th_rms_title,
            summary = Res.string.th_rms_summary,
            theory = Res.string.th_rms_theory,
            assumptions = listOf(
                Res.string.th_rms_assumption_sinusoidal,
                Res.string.th_rms_assumption_meter,
            ),
            diagram = TheoryDiagram.SINE_WAVE,
            selectorLabel = Res.string.th_selector_known,
            solutions = listOf(
                sineSolution(
                    "from_rms", Res.string.th_rms_solve_from_rms,
                    "Û = U · √2", "u_rms", Res.string.th_field_rms,
                    "mains", Res.string.th_rms_example_mains, "230", ::sineFromRms,
                ),
                sineSolution(
                    "from_peak", Res.string.th_rms_solve_from_peak,
                    "U = Û / √2", "u_peak", Res.string.th_field_peak,
                    "scope", Res.string.th_rms_example_scope, "325", ::sineFromPeak,
                ),
                sineSolution(
                    "from_average", Res.string.th_rms_solve_from_average,
                    "Û = U̅ · π / 2", "u_avg", Res.string.th_field_average,
                    "meter", Res.string.th_rms_example_meter, "207", ::sineFromAverage,
                ),
            ),
            calculator = CalculatorId.POWER,
        ),

        TheoryTopic(
            key = "capacitive_reactance",
            level = TheoryLevel.INTERMEDIATE,
            title = Res.string.th_xc_title,
            summary = Res.string.th_xc_summary,
            theory = Res.string.th_xc_theory,
            assumptions = listOf(
                Res.string.th_xc_assumption_sinusoidal,
                Res.string.th_xc_assumption_ideal,
                Res.string.th_xc_assumption_phase,
            ),
            diagram = TheoryDiagram.AC_CAPACITOR,
            solutions = listOf(
                TheorySolution(
                    key = "reactance",
                    targetLabel = Res.string.th_xc_solve_reactance,
                    formula = "X_C = 1 / (2 · π · f · C)",
                    variables = listOf(
                        TheoryVariable("X_C", Res.string.th_var_capacitive_reactance, "Ω"),
                        TheoryVariable("f", Res.string.th_var_frequency, "Hz"),
                        TheoryVariable("C", Res.string.th_var_capacitance, "F"),
                    ),
                    fields = listOf(
                        TheoryField("f", Res.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("c", Res.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "correction", Res.string.th_xc_example_correction,
                            mapOf("f" to "50", "c" to "100", "u" to "230"),
                        ),
                        TheoryExample(
                            "small", Res.string.th_xc_example_small,
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
            title = Res.string.th_xl_title,
            summary = Res.string.th_xl_summary,
            theory = Res.string.th_xl_theory,
            assumptions = listOf(
                Res.string.th_xl_assumption_sinusoidal,
                Res.string.th_xl_assumption_ideal,
                Res.string.th_xl_assumption_saturation,
            ),
            diagram = TheoryDiagram.AC_INDUCTOR,
            solutions = listOf(
                TheorySolution(
                    key = "reactance",
                    targetLabel = Res.string.th_xl_solve_reactance,
                    formula = "X_L = 2 · π · f · L",
                    variables = listOf(
                        TheoryVariable("X_L", Res.string.th_var_inductive_reactance, "Ω"),
                        TheoryVariable("f", Res.string.th_var_frequency, "Hz"),
                        TheoryVariable("L", Res.string.th_var_inductance, "H"),
                    ),
                    fields = listOf(
                        TheoryField("f", Res.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("l", Res.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "choke", Res.string.th_xl_example_choke,
                            mapOf("f" to "50", "l" to "100", "u" to "230"),
                        ),
                        TheoryExample(
                            "coil", Res.string.th_xl_example_coil,
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
            title = Res.string.th_triangle_title,
            summary = Res.string.th_triangle_summary,
            theory = Res.string.th_triangle_theory,
            assumptions = listOf(
                Res.string.th_triangle_assumption_sinusoidal,
                Res.string.th_triangle_assumption_displacement,
            ),
            diagram = TheoryDiagram.POWER_TRIANGLE,
            solutions = listOf(
                TheorySolution(
                    key = "triangle",
                    targetLabel = Res.string.th_triangle_solve,
                    formula = "S = P / cos φ\nQ = √(S² − P²)",
                    variables = listOf(
                        TheoryVariable("S", Res.string.th_var_apparent_power, "VA"),
                        TheoryVariable("P", Res.string.th_var_active_power, "W"),
                        TheoryVariable("Q", Res.string.th_var_reactive_power, "var"),
                        TheoryVariable("cos φ", Res.string.th_var_power_factor, "—"),
                    ),
                    fields = listOf(
                        TheoryField("p", Res.string.th_field_active_power, "kW", min = 0.0),
                        TheoryField(
                            "pf", Res.string.th_field_power_factor, "—",
                            min = 0.0, max = 1.0, default = "0.85",
                        ),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "motor", Res.string.th_triangle_example_motor,
                            mapOf("p" to "7.5", "pf" to "0.85"),
                        ),
                        TheoryExample(
                            "corrected", Res.string.th_triangle_example_corrected,
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
            title = Res.string.th_three_phase_title,
            summary = Res.string.th_three_phase_summary,
            theory = Res.string.th_three_phase_theory,
            assumptions = listOf(
                Res.string.th_three_phase_assumption_balanced,
                Res.string.th_three_phase_assumption_sinusoidal,
            ),
            diagram = TheoryDiagram.STAR_DELTA_SUPPLY,
            selectorLabel = Res.string.th_selector_connection,
            solutions = listOf(
                threePhaseSolution(
                    "star", Res.string.th_three_phase_solve_star,
                    "U_ph = U_L / √3\nI_ph = I_L",
                    "board", Res.string.th_three_phase_example_board, "32", ::starConnection,
                ),
                threePhaseSolution(
                    "delta", Res.string.th_three_phase_solve_delta,
                    "U_ph = U_L\nI_ph = I_L / √3",
                    "motor", Res.string.th_three_phase_example_motor, "14", ::deltaConnection,
                ),
            ),
            glossaryTerms = listOf("apparent_power"),
            calculator = CalculatorId.POWER,
        ),

        TheoryTopic(
            key = "rc_time_constant",
            level = TheoryLevel.INTERMEDIATE,
            title = Res.string.th_rc_title,
            summary = Res.string.th_rc_summary,
            theory = Res.string.th_rc_theory,
            assumptions = listOf(
                Res.string.th_rc_assumption_step,
                Res.string.th_rc_assumption_ideal,
                Res.string.th_rc_assumption_never,
            ),
            diagram = TheoryDiagram.RC_CHARGING,
            solutions = listOf(
                TheorySolution(
                    key = "charging",
                    targetLabel = Res.string.th_rc_solve_charging,
                    formula = "τ = R · C\nu(t) = U · (1 − e^(−t/τ))",
                    variables = listOf(
                        TheoryVariable("τ", Res.string.th_var_time_constant, "s"),
                        TheoryVariable("u(t)", Res.string.th_var_capacitor_voltage, "V"),
                        TheoryVariable("t", Res.string.th_var_elapsed_time, "s"),
                    ),
                    fields = listOf(
                        TheoryField("r", Res.string.th_field_resistance, "Ω", min = 0.0),
                        TheoryField("c", Res.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("t", Res.string.th_field_time, "s", min = 0.0, allowZero = true),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "timer", Res.string.th_rc_example_timer,
                            mapOf("r" to "100000", "c" to "10", "u" to "12", "t" to "1"),
                        ),
                        TheoryExample(
                            "debounce", Res.string.th_rc_example_debounce,
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
            key = "induced_emf",
            level = TheoryLevel.INTERMEDIATE,
            title = Res.string.th_emf_title,
            summary = Res.string.th_emf_summary,
            theory = Res.string.th_emf_theory,
            assumptions = listOf(
                Res.string.th_emf_assumption_sinusoidal,
                Res.string.th_emf_assumption_no_leakage,
                Res.string.th_emf_assumption_saturation,
            ),
            solutions = listOf(
                TheorySolution(
                    key = "emf",
                    targetLabel = Res.string.th_emf_solve_voltage,
                    formula = "E = 4.44 · f · N · Φ",
                    variables = listOf(
                        TheoryVariable("E", Res.string.th_var_emf, "V"),
                        TheoryVariable("f", Res.string.th_var_frequency, "Hz"),
                        TheoryVariable("N", Res.string.th_var_turns, ""),
                        TheoryVariable("Φ", Res.string.th_var_flux, "Wb"),
                    ),
                    fields = listOf(
                        TheoryField("f", Res.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("n", Res.string.th_field_turns, "", min = 0.0, default = "500"),
                        TheoryField("phi", Res.string.th_field_flux, "Wb", min = 0.0, default = "0.002"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "small_transformer", Res.string.th_emf_example_small,
                            mapOf("f" to "50", "n" to "500", "phi" to "0.002"),
                        ),
                        TheoryExample(
                            "sixty_hertz", Res.string.th_emf_example_sixty,
                            mapOf("f" to "60", "n" to "500", "phi" to "0.002"),
                        ),
                    ),
                    solve = ::inducedEmf,
                ),
            ),
        ),
        TheoryTopic(
            key = "transformer_ratio",
            level = TheoryLevel.INTERMEDIATE,
            title = Res.string.th_transformer_title,
            summary = Res.string.th_transformer_summary,
            theory = Res.string.th_transformer_theory,
            assumptions = listOf(
                Res.string.th_transformer_assumption_ideal,
                Res.string.th_transformer_assumption_no_magnetising,
            ),
            solutions = listOf(
                TheorySolution(
                    key = "secondary",
                    targetLabel = Res.string.th_transformer_solve_secondary,
                    formula = "Up / Us = Np / Ns = Is / Ip",
                    variables = listOf(
                        TheoryVariable("a", Res.string.th_var_turns_ratio, ""),
                        TheoryVariable("U_p", Res.string.th_var_primary_voltage, "V"),
                        TheoryVariable("U_s", Res.string.th_var_secondary_voltage, "V"),
                    ),
                    fields = listOf(
                        TheoryField("np", Res.string.th_field_primary_turns, "", min = 0.0, default = "1000"),
                        TheoryField("ns", Res.string.th_field_secondary_turns, "", min = 0.0, default = "50"),
                        TheoryField("up", Res.string.th_field_primary_voltage, "V", min = 0.0, default = "400"),
                        TheoryField("ip", Res.string.th_field_primary_current, "A", min = 0.0, default = "1"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "step_down", Res.string.th_transformer_example_step_down,
                            mapOf("np" to "1000", "ns" to "50", "up" to "400", "ip" to "1"),
                        ),
                        TheoryExample(
                            "step_up", Res.string.th_transformer_example_step_up,
                            mapOf("np" to "50", "ns" to "1000", "up" to "20", "ip" to "20"),
                        ),
                    ),
                    solve = ::transformerRatio,
                ),
            ),
            calculator = CalculatorId.TRANSFORMER_CURRENT,
            glossaryTerms = listOf("transformer"),
        ),
        TheoryTopic(
            key = "rlc_impedance",
            level = TheoryLevel.ADVANCED,
            title = Res.string.th_rlc_title,
            summary = Res.string.th_rlc_summary,
            theory = Res.string.th_rlc_theory,
            assumptions = listOf(
                Res.string.th_rlc_assumption_series,
                Res.string.th_rlc_assumption_sinusoidal,
                Res.string.th_rlc_assumption_ideal,
            ),
            diagram = TheoryDiagram.SERIES_RLC,
            solutions = listOf(
                TheorySolution(
                    key = "impedance",
                    targetLabel = Res.string.th_rlc_solve,
                    formula = "Z = √(R² + (X_L − X_C)²)\nφ = arctan((X_L − X_C) / R)",
                    variables = listOf(
                        TheoryVariable("Z", Res.string.th_var_impedance, "Ω"),
                        TheoryVariable("X_L", Res.string.th_var_inductive_reactance, "Ω"),
                        TheoryVariable("X_C", Res.string.th_var_capacitive_reactance, "Ω"),
                        TheoryVariable("φ", Res.string.th_var_phase_angle, "°"),
                    ),
                    fields = listOf(
                        TheoryField("r", Res.string.th_field_resistance, "Ω", min = 0.0),
                        TheoryField("l", Res.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("c", Res.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("f", Res.string.th_field_frequency, "Hz", min = 0.0, default = "50"),
                        TheoryField("u", Res.string.th_field_voltage, "V", min = 0.0, default = "230"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "inductive", Res.string.th_rlc_example_inductive,
                            mapOf("r" to "10", "l" to "200", "c" to "100", "f" to "50", "u" to "230"),
                        ),
                        TheoryExample(
                            "capacitive", Res.string.th_rlc_example_capacitive,
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
            title = Res.string.th_resonance_title,
            summary = Res.string.th_resonance_summary,
            theory = Res.string.th_resonance_theory,
            assumptions = listOf(
                Res.string.th_resonance_assumption_series,
                Res.string.th_resonance_assumption_ideal,
                Res.string.th_resonance_assumption_voltage,
            ),
            diagram = TheoryDiagram.SERIES_RLC,
            solutions = listOf(
                TheorySolution(
                    key = "resonance",
                    targetLabel = Res.string.th_resonance_solve,
                    formula = "f₀ = 1 / (2 · π · √(L · C))\nQ = (1/R) · √(L / C)",
                    variables = listOf(
                        TheoryVariable("f₀", Res.string.th_var_resonant_frequency, "Hz"),
                        TheoryVariable("Q", Res.string.th_var_quality_factor, "—"),
                        TheoryVariable("B", Res.string.th_var_bandwidth, "Hz"),
                    ),
                    fields = listOf(
                        TheoryField("l", Res.string.th_field_inductance, "mH", min = 0.0),
                        TheoryField("c", Res.string.th_field_capacitance, "µF", min = 0.0),
                        TheoryField("r", Res.string.th_field_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "filter", Res.string.th_resonance_example_filter,
                            mapOf("l" to "10", "c" to "1", "r" to "5"),
                        ),
                        TheoryExample(
                            "sharp", Res.string.th_resonance_example_sharp,
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
            title = Res.string.th_thevenin_title,
            summary = Res.string.th_thevenin_summary,
            theory = Res.string.th_thevenin_theory,
            assumptions = listOf(
                Res.string.th_thevenin_assumption_linear,
                Res.string.th_thevenin_assumption_terminals,
                Res.string.th_thevenin_assumption_internal,
            ),
            diagram = TheoryDiagram.THEVENIN,
            solutions = listOf(
                TheorySolution(
                    key = "equivalent",
                    targetLabel = Res.string.th_thevenin_solve,
                    formula = "U_th = U · R₂ / (R₁ + R₂)\nR_th = R₁ · R₂ / (R₁ + R₂)",
                    variables = listOf(
                        TheoryVariable("U_th", Res.string.th_var_open_circuit_voltage, "V"),
                        TheoryVariable("R_th", Res.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("R_L", Res.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u", Res.string.th_field_supply_voltage, "V", min = 0.0, default = "12"),
                        TheoryField("r1", Res.string.th_field_divider_upper, "Ω", min = 0.0),
                        TheoryField("r2", Res.string.th_field_divider_lower, "Ω", min = 0.0),
                        TheoryField("r_load", Res.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "divider", Res.string.th_thevenin_example_divider,
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
            title = Res.string.th_norton_title,
            summary = Res.string.th_norton_summary,
            theory = Res.string.th_norton_theory,
            assumptions = listOf(
                Res.string.th_norton_assumption_same,
                Res.string.th_norton_assumption_internal,
            ),
            diagram = TheoryDiagram.NORTON,
            solutions = listOf(
                TheorySolution(
                    key = "equivalent",
                    targetLabel = Res.string.th_norton_solve,
                    formula = "I_N = U_th / R_th\nR_N = R_th",
                    variables = listOf(
                        TheoryVariable("I_N", Res.string.th_var_short_circuit_current, "A"),
                        TheoryVariable("R_N", Res.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("R_L", Res.string.th_var_load_resistance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("u_th", Res.string.th_field_thevenin_voltage, "V", min = 0.0),
                        TheoryField("r_th", Res.string.th_field_thevenin_resistance, "Ω", min = 0.0),
                        TheoryField("r_load", Res.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "converted", Res.string.th_norton_example_converted,
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
            title = Res.string.th_mpt_title,
            summary = Res.string.th_mpt_summary,
            theory = Res.string.th_mpt_theory,
            assumptions = listOf(
                Res.string.th_mpt_assumption_signal,
                Res.string.th_mpt_assumption_fixed_source,
                Res.string.th_mpt_assumption_efficiency,
            ),
            diagram = TheoryDiagram.THEVENIN,
            solutions = listOf(
                TheorySolution(
                    key = "transfer",
                    targetLabel = Res.string.th_mpt_solve,
                    formula = "P_L = I² · R_L\nP_max = U_th² / (4 · R_th)  at  R_L = R_th",
                    variables = listOf(
                        TheoryVariable("P_L", Res.string.th_var_delivered_power, "W"),
                        TheoryVariable("R_th", Res.string.th_var_internal_resistance, "Ω"),
                        TheoryVariable("η", Res.string.th_var_efficiency, "%"),
                    ),
                    fields = listOf(
                        TheoryField("u_th", Res.string.th_field_thevenin_voltage, "V", min = 0.0),
                        TheoryField("r_th", Res.string.th_field_thevenin_resistance, "Ω", min = 0.0),
                        TheoryField("r_load", Res.string.th_field_load_resistance, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "matched", Res.string.th_mpt_example_matched,
                            mapOf("u_th" to "12", "r_th" to "50", "r_load" to "50"),
                        ),
                        TheoryExample(
                            "efficient", Res.string.th_mpt_example_efficient,
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
            title = Res.string.th_ydelta_title,
            summary = Res.string.th_ydelta_summary,
            theory = Res.string.th_ydelta_theory,
            assumptions = listOf(
                Res.string.th_ydelta_assumption_terminals,
                Res.string.th_ydelta_assumption_resistive,
                Res.string.th_ydelta_assumption_not_supply,
            ),
            diagram = TheoryDiagram.Y_DELTA_TRANSFORM,
            selectorLabel = Res.string.th_selector_direction,
            solutions = listOf(
                TheorySolution(
                    key = "star_to_delta",
                    targetLabel = Res.string.th_ydelta_solve_to_delta,
                    formula = "R_ab = (R_a·R_b + R_b·R_c + R_c·R_a) / R_c",
                    variables = listOf(
                        TheoryVariable("R_a, R_b, R_c", Res.string.th_var_star_arms, "Ω"),
                        TheoryVariable("R_ab …", Res.string.th_var_delta_arms, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("ra", Res.string.th_field_star_a, "Ω", min = 0.0),
                        TheoryField("rb", Res.string.th_field_star_b, "Ω", min = 0.0),
                        TheoryField("rc", Res.string.th_field_star_c, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", Res.string.th_ydelta_example_equal,
                            mapOf("ra" to "10", "rb" to "10", "rc" to "10"),
                        ),
                        TheoryExample(
                            "uneven", Res.string.th_ydelta_example_uneven,
                            mapOf("ra" to "10", "rb" to "20", "rc" to "30"),
                        ),
                    ),
                    solve = ::starToDelta,
                ),
                TheorySolution(
                    key = "delta_to_star",
                    targetLabel = Res.string.th_ydelta_solve_to_star,
                    formula = "R_a = R_ab · R_ca / (R_ab + R_bc + R_ca)",
                    variables = listOf(
                        TheoryVariable("R_ab …", Res.string.th_var_delta_arms, "Ω"),
                        TheoryVariable("R_a, R_b, R_c", Res.string.th_var_star_arms, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("rab", Res.string.th_field_delta_ab, "Ω", min = 0.0),
                        TheoryField("rbc", Res.string.th_field_delta_bc, "Ω", min = 0.0),
                        TheoryField("rca", Res.string.th_field_delta_ca, "Ω", min = 0.0),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "equal", Res.string.th_ydelta_example_delta_equal,
                            mapOf("rab" to "30", "rbc" to "30", "rca" to "30"),
                        ),
                    ),
                    solve = ::deltaToStar,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

        // ---- Electromagnetism ---------------------------------------------


        TheoryTopic(
            key = "magnetic_circuit",
            level = TheoryLevel.ADVANCED,
            title = Res.string.th_magnetic_title,
            summary = Res.string.th_magnetic_summary,
            theory = Res.string.th_magnetic_theory,
            assumptions = listOf(
                Res.string.th_magnetic_assumption_linear,
                Res.string.th_magnetic_assumption_uniform,
                Res.string.th_magnetic_assumption_no_gap,
            ),
            solutions = listOf(
                TheorySolution(
                    key = "flux",
                    targetLabel = Res.string.th_magnetic_solve_flux,
                    formula = "Φ = N·I / (l / μ₀μrA)",
                    variables = listOf(
                        TheoryVariable("F", Res.string.th_var_mmf, "A"),
                        TheoryVariable("ℛ", Res.string.th_var_reluctance, "1/H"),
                        TheoryVariable("Φ", Res.string.th_var_flux, "Wb"),
                        TheoryVariable("B", Res.string.th_var_flux_density, "T"),
                    ),
                    fields = listOf(
                        TheoryField("n", Res.string.th_field_turns, "", min = 0.0, default = "150"),
                        TheoryField("i", Res.string.th_field_current, "A", min = 0.0, default = "1"),
                        TheoryField("l", Res.string.th_field_path_length, "mm", min = 0.0, default = "300"),
                        TheoryField("a", Res.string.th_field_core_area, "mm²", min = 0.0, default = "900"),
                        TheoryField("mur", Res.string.th_field_permeability, "", min = 0.0, default = "2000"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "steel_core", Res.string.th_magnetic_example_steel,
                            mapOf("n" to "150", "i" to "1", "l" to "300", "a" to "900", "mur" to "2000"),
                        ),
                        TheoryExample(
                            "air_core", Res.string.th_magnetic_example_air,
                            mapOf("n" to "150", "i" to "1", "l" to "300", "a" to "900", "mur" to "1"),
                        ),
                    ),
                    solve = ::magneticCircuit,
                ),
            ),
        ),


        TheoryTopic(
            key = "per_unit",
            level = TheoryLevel.ADVANCED,
            title = Res.string.th_per_unit_title,
            summary = Res.string.th_per_unit_summary,
            theory = Res.string.th_per_unit_theory,
            assumptions = listOf(
                Res.string.th_per_unit_assumption_three_phase,
                Res.string.th_per_unit_assumption_same_base,
            ),
            solutions = listOf(
                TheorySolution(
                    key = "pu",
                    targetLabel = Res.string.th_per_unit_solve_pu,
                    formula = "Zpu = Z / (U² / S)",
                    variables = listOf(
                        TheoryVariable("S", Res.string.th_var_base_power, "MVA"),
                        TheoryVariable("U", Res.string.th_var_base_voltage, "kV"),
                        TheoryVariable("Z", Res.string.th_var_impedance, "Ω"),
                    ),
                    fields = listOf(
                        TheoryField("s", Res.string.th_field_base_power, "MVA", min = 0.0, default = "100"),
                        TheoryField("u", Res.string.th_field_base_voltage, "kV", min = 0.0, default = "33"),
                        TheoryField("z", Res.string.th_field_impedance_ohms, "Ω", min = 0.0, default = "5"),
                    ),
                    examples = listOf(
                        TheoryExample(
                            "feeder", Res.string.th_per_unit_example_feeder,
                            mapOf("s" to "100", "u" to "33", "z" to "5"),
                        ),
                        TheoryExample(
                            "low_voltage", Res.string.th_per_unit_example_lv,
                            mapOf("s" to "1", "u" to "0.4", "z" to "0.02"),
                        ),
                    ),
                    solve = ::perUnit,
                ),
            ),
            glossaryTerms = listOf("impedance"),
        ),

    )

    /** The two conductor materials differ only in which solver they call. */
    private fun conductorSolution(
        key: String,
        title: StringResource,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabel = title,
        formula = "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]\nR = ρ(θ) · L / A",
        variables = listOf(
            TheoryVariable("R", Res.string.th_var_conductor_resistance, "Ω"),
            TheoryVariable("ρ(θ)", Res.string.th_var_resistivity, "Ω·mm²/m"),
            TheoryVariable("L", Res.string.th_var_length, "m"),
            TheoryVariable("A", Res.string.th_var_area, "mm²"),
        ),
        fields = listOf(
            TheoryField("l", Res.string.th_field_length, "m", min = 0.0),
            TheoryField("a", Res.string.th_field_area, "mm²", min = 0.0),
            TheoryField(
                "theta", Res.string.th_field_temperature, "°C",
                min = -50.0, max = 250.0, allowZero = true, allowNegative = true,
                default = "20", hint = Res.string.th_field_temperature_hint,
            ),
        ),
        examples = listOf(
            TheoryExample(
                "radial", Res.string.th_conductor_example_radial,
                mapOf("l" to "25", "a" to "2.5", "theta" to "70"),
            ),
            TheoryExample(
                "submain", Res.string.th_conductor_example_submain,
                mapOf("l" to "60", "a" to "16", "theta" to "30"),
            ),
        ),
        solve = solve,
    )

    /** The three sine solutions differ only in which level the reader has. */
    private fun sineSolution(
        key: String,
        targetLabel: StringResource,
        formula: String,
        fieldKey: String,
        fieldLabel: StringResource,
        exampleKey: String,
        exampleTitle: StringResource,
        exampleValue: String,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabel = targetLabel,
        formula = formula,
        variables = listOf(
            TheoryVariable("U", Res.string.th_var_rms, "V"),
            TheoryVariable("Û", Res.string.th_var_peak, "V"),
            TheoryVariable("U̅", Res.string.th_var_average, "V"),
        ),
        fields = listOf(TheoryField(fieldKey, fieldLabel, "V", min = 0.0)),
        examples = listOf(
            TheoryExample(exampleKey, exampleTitle, mapOf(fieldKey to exampleValue)),
        ),
        solve = solve,
    )

    /** Star and delta take the same readings and differ in what they mean. */
    private fun threePhaseSolution(
        key: String,
        targetLabel: StringResource,
        formula: String,
        exampleKey: String,
        exampleTitle: StringResource,
        exampleCurrent: String,
        solve: (TheoryInputs) -> TheorySolutionResult,
    ) = TheorySolution(
        key = key,
        targetLabel = targetLabel,
        formula = formula,
        variables = listOf(
            TheoryVariable("U_L", Res.string.th_var_line_voltage, "V"),
            TheoryVariable("U_ph", Res.string.th_var_phase_voltage, "V"),
            TheoryVariable("I_L", Res.string.th_var_line_current, "A"),
            TheoryVariable("I_ph", Res.string.th_var_phase_current, "A"),
        ),
        fields = listOf(
            TheoryField("u_line", Res.string.th_field_line_voltage, "V", min = 0.0, default = "400"),
            TheoryField("i_line", Res.string.th_field_line_current, "A", min = 0.0),
            TheoryField(
                "pf", Res.string.th_field_power_factor, "—",
                min = 0.0, max = 1.0, default = "0.85",
            ),
        ),
        examples = listOf(
            TheoryExample(
                exampleKey, exampleTitle,
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
