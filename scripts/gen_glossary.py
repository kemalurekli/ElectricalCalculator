#!/usr/bin/env python3
"""Single source for the glossary's structure and its English text.

### What this owns, and what it must never touch

The app ships in more than two languages, so this script owns exactly two
things: the Kotlin catalog, and the **English** strings. Every translation lives
in its own `values-<tag>/strings.xml`, is written by whoever speaks that
language, and is never generated, overwritten or fabricated here. A generator
that emitted all eight languages would either need eight columns per term — a
table no translator can work in — or would quietly seed English text under a
Turkish key, which is worse than an obvious gap.

Re-running is therefore safe: the English block is rebuilt from this table, and
the translation files are only *read*, to report what each language is still
missing.

### Definitions

Written from scratch. IEC 60050 is the authority on this vocabulary and its
wording is copyrighted, so where a standard defines a term the standard is named
rather than quoted.

Usage:
    python3 scripts/gen_glossary.py
"""
from dataclasses import dataclass, field
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent / "app/src/main"
RES = ROOT / "res"


@dataclass
class T:
    """One term: its key, its English text, and its structure."""

    key: str
    en_term: str
    en: str
    symbol: str | None = None
    unit: str | None = None
    see: list[str] = field(default_factory=list)
    calc: str | None = None
    ref: str | None = None


TERMS: list[T] = [
    T('active_power', 'Active power',
      en='The part of the supplied power that does real work — turns a shaft, makes heat, gives light. It is what an energy meter bills.',
      symbol='P', unit='W', see=['reactive_power', 'apparent_power', 'cos_phi'], calc='POWER'),
    T('adiabatic_check', 'Adiabatic check',
      en='A check that a conductor survives a fault long enough for the protective device to clear it, using S ≥ √(I²t)/k from IEC 60364-5-54. It assumes none of the heat escapes during the fault, which is why it is called adiabatic.',
      see=['fault_current', 'let_through_energy', 'protective_conductor'], calc='EARTH_FAULT_LOOP'),
    T('ambient_temperature', 'Ambient temperature',
      en='The air temperature around a cable or a device, before it heats anything itself. Capacity tables assume 30 °C in air; hotter surroundings leave a conductor less room to shed its own heat, so its rating falls.',
      symbol='θa', unit='°C', see=['correction_factor', 'derating', 'ampacity'], calc='CABLE_SIZE'),
    T('ampacity', 'Ampacity',
      en='The current a conductor can carry continuously without its insulation exceeding its rated temperature. It is a property of the installation, not just the cable: the same conductor buried, in a tray or in insulation has three different figures.',
      symbol='Iz', unit='A', see=['derating', 'reference_method', 'correction_factor'], calc='CABLE_SIZE'),
    T('apparent_power', 'Apparent power',
      en='Voltage times current, without regard to how much of it does work. Cables and transformers are sized on this, because they carry the whole current whether it is useful or not.',
      symbol='S', unit='VA', see=['active_power', 'reactive_power', 'cos_phi'], calc='POWER'),
    T('arc_fault', 'Arc fault',
      en='A fault where current jumps a gap instead of flowing through metal, at a damaged terminal or a broken strand. The current can stay below what an overcurrent device would trip on while the arc itself is hot enough to start a fire.',
      see=['short_circuit', 'overcurrent']),
    T('armour', 'Armour',
      en="A steel wire or tape layer under a cable's outer sheath, there to take mechanical damage. It also gives the cable an earthed metallic path, which is why armoured cable is common where a separate protective conductor would be awkward.",
      see=['sheath', 'protective_conductor']),
    T('automatic_disconnection', 'Automatic disconnection of supply',
      en='The standard protective measure of IEC 60364-4-41: an earth fault must raise enough current to operate the protective device within a stated time, so a dangerous touch voltage cannot persist.',
      see=['disconnection_time', 'earth_fault_loop_impedance', 'touch_voltage'], calc='EARTH_FAULT_LOOP', ref='disconnection_times'),
    T('awg', 'AWG',
      en='American Wire Gauge, the North American conductor size scale. The numbers run backwards — larger gauge means thinner wire — and each step of three roughly halves the area.',
      see=['cross_sectional_area']),
    T('back_up_protection', 'Back-up protection',
      en='An upstream device that assists a downstream one whose breaking capacity is lower than the fault current at its terminals. The pair is only valid in combinations the manufacturer has tested.',
      see=['breaking_capacity', 'selectivity', 'cascading']),
    T('basic_protection', 'Basic protection',
      en='Protection against touching a part that is live in normal service — insulation, enclosures, barriers. It is the layer that has to fail before fault protection is called on.',
      see=['fault_protection', 'live_part']),
    T('breaking_capacity', 'Breaking capacity',
      en='The largest fault current a device can interrupt and still be safe afterwards. It is selected against the maximum prospective short-circuit current at the point of installation, not against the load.',
      symbol='Icu', unit='kA', see=['prospective_short_circuit_current', 'short_circuit'], calc='SHORT_CIRCUIT'),
    T('busbar', 'Busbar',
      en='A rigid conductor, usually a copper or aluminium bar, that distributes current to several outgoing circuits inside a board.',
      see=['distribution_board', 'switchgear']),
    T('c_rate', 'C-rate',
      en="Discharge or charge current expressed as a multiple of a battery's rated capacity. A 100 Ah bank at 0,5 C is being worked at 50 A.",
      symbol='C', see=['depth_of_discharge', 'peukert_law'], calc='BATTERY_RUNTIME'),
    T('cascading', 'Cascading',
      en="Deliberately letting an upstream breaker help clear a fault that exceeds a downstream one's own breaking capacity. It saves cost but gives up selectivity, since both devices open.",
      see=['back_up_protection', 'selectivity', 'breaking_capacity']),
    T('circuit_breaker', 'Circuit breaker',
      en='A switching device that carries current normally and interrupts it automatically under overload or short circuit. Unlike a fuse it is reset rather than replaced.',
      see=['mcb', 'mccb', 'trip_curve', 'fuse'], ref='breaker_curves'),
    T('clearance', 'Clearance',
      en='The shortest distance through air between two conductive parts. Together with creepage distance it decides how close live parts may sit at a given voltage.',
      unit='mm', see=['creepage_distance']),
    T('conductor', 'Conductor',
      en='The metal part of a cable that carries current — copper or aluminium in practice. Everything else in the cable exists to insulate it, protect it or hold it together.',
      see=['cross_sectional_area', 'resistivity', 'line_conductor'], ref='materials'),
    T('contactor', 'Contactor',
      en='An electrically operated switch built to make and break load current repeatedly, and to drop out when its coil loses supply. That last property is what a relay is not designed for.',
      see=['relay', 'interlock']),
    T('continuity_test', 'Continuity test',
      en='A dead test measuring the resistance of protective and line conductors end to end, to prove they are unbroken and correctly connected. The reading is compared against the calculated R1+R2.',
      see=['insulation_resistance', 'earth_fault_loop_impedance']),
    T('correction_factor', 'Correction factor',
      en='A multiplier applied to a tabulated current capacity to account for conditions worse than the table assumed — hot air, bunched circuits, soil thermal resistivity. Factors multiply together, so two mild ones can bite harder than either alone.',
      symbol='Ca · Cg', see=['ambient_temperature', 'grouping_factor', 'derating'], calc='CABLE_SIZE'),
    T('cos_phi', 'Power factor (cos φ)',
      en='The ratio of active power to apparent power. At cos φ = 0,8 a cable carries a quarter more current than the useful power alone would suggest, and that extra current is what gets billed as reactive energy.',
      symbol='cos φ', see=['power_factor_correction', 'reactive_power', 'phase_angle'], calc='POWER_FACTOR_CORRECTION', ref='power_factors'),
    T('creepage_distance', 'Creepage distance',
      en='The shortest path along an insulating surface between two conductive parts. Dirt and damp make a surface conduct long before air breaks down, so this is usually the larger requirement.',
      unit='mm', see=['clearance', 'ip_code']),
    T('cross_sectional_area', 'Cross-sectional area',
      en="The conductor's area in mm², the number that names a cable in IEC countries. It sets both resistance and current capacity, but not in the same proportion — doubling the area halves resistance while capacity rises by rather less.",
      symbol='A', unit='mm²', see=['ampacity', 'awg', 'conductor'], calc='CABLE_SIZE'),
    T('current_transformer', 'Current transformer',
      en='An instrument transformer that produces a small secondary current proportional to a large primary one, for metering and protection. Its secondary must never be left open-circuit while primary current flows — the voltage that appears is dangerous.',
      see=['transformer', 'switchgear']),
    T('demand_factor', 'Demand factor',
      en='Maximum demand divided by total connected load. It recognises that not everything installed runs at once, and it is regional and regulation-specific rather than universal.',
      see=['diversity_factor', 'load_factor']),
    T('depth_of_discharge', 'Depth of discharge',
      en="The share of a battery's capacity actually used before recharging. Lead-acid life falls sharply past about 50 %; lithium tolerates 80–90 %.",
      symbol='DoD', see=['c_rate', 'peukert_law'], calc='BATTERY_RUNTIME'),
    T('derating', 'Derating',
      en="Reducing a component's usable rating because the conditions are worse than the ones it was rated in. For cables it is the ambient and grouping factors; for a device it may be temperature inside an enclosure.",
      see=['correction_factor', 'ambient_temperature', 'grouping_factor'], calc='CABLE_SIZE'),
    T('disconnection_time', 'Disconnection time',
      en='How long a protective device may take to clear an earth fault. IEC 60364-4-41 requires 0,4 s for final circuits up to 32 A on a TN system and 5 s for distribution circuits.',
      symbol='t', unit='s', see=['automatic_disconnection', 'earth_fault_loop_impedance', 'touch_voltage'], calc='EARTH_FAULT_LOOP', ref='disconnection_times'),
    T('distribution_board', 'Distribution board',
      en='The enclosure where an incoming supply is split into outgoing circuits, each with its own protective device.',
      see=['busbar', 'switchgear', 'form_of_separation']),
    T('diversity_factor', 'Diversity factor',
      en='An allowance for loads that do not peak together, used to size a supply for less than the arithmetic sum of its circuits. Like demand factor it is set by local practice, not by physics.',
      see=['demand_factor', 'load_factor']),
    T('earth_electrode', 'Earth electrode',
      en="The metal in contact with soil that gives an installation its connection to earth — a rod, plate, tape or the building's foundation steel. Its resistance depends on soil far more than on the metal.",
      symbol='RA', unit='Ω', see=['earthing', 'tt_system', 'equipotential_bonding'], ref='earthing_systems'),
    T('earth_fault_loop_impedance', 'Earth fault loop impedance',
      en='The total impedance of the path a fault current takes from the transformer, out along the line conductor, through the fault, and back along the protective conductor. It decides how much current flows, and therefore whether the device operates.',
      symbol='Zs', unit='Ω', see=['disconnection_time', 'external_loop_impedance', 'automatic_disconnection'], calc='EARTH_FAULT_LOOP'),
    T('earthing', 'Earthing',
      en='Connecting conductive parts to the general mass of earth so that a fault produces a current large enough to be detected, and so that exposed metal cannot sit at a dangerous voltage.',
      see=['earth_electrode', 'equipotential_bonding', 'tn_system', 'tt_system', 'it_system'], ref='earthing_systems'),
    T('efficiency', 'Efficiency',
      en='Output power divided by input power. The difference is loss, and it is why a 5,5 kW motor draws more than 5,5 kW from the supply.',
      symbol='η', see=['active_power', 'no_load_loss'], calc='MOTOR_CURRENT'),
    T('equipotential_bonding', 'Equipotential bonding',
      en='Joining metalwork together so that no dangerous voltage can appear between two things a person might touch at once. It works by removing the difference rather than by removing the fault.',
      see=['earthing', 'extraneous_conductive_part', 'touch_voltage']),
    T('exposed_conductive_part', 'Exposed conductive part',
      en='Metal belonging to equipment that is not live in normal service but could become live under a fault — a motor frame, a metal enclosure. These are what the protective conductor connects.',
      see=['extraneous_conductive_part', 'protective_conductor', 'fault_protection']),
    T('external_loop_impedance', 'External earth loop impedance',
      en="The part of the earth fault loop that lies outside the installation, from the origin back through the supply network. Measured at the origin or taken from the distributor's declared maximum.",
      symbol='Ze', unit='Ω', see=['earth_fault_loop_impedance'], calc='EARTH_FAULT_LOOP'),
    T('extraneous_conductive_part', 'Extraneous conductive part',
      en='Metal that is not part of the electrical installation but can bring a potential into it — water pipes, structural steel, gas pipes. Bonded, not earthed as a protective conductor.',
      see=['exposed_conductive_part', 'equipotential_bonding']),
    T('fault_current', 'Fault current',
      en='The current that flows when insulation fails and a live conductor reaches earth or another conductor. Its size decides both whether the protection operates and how much damage is done before it does.',
      symbol='If', unit='A', see=['short_circuit', 'residual_current', 'adiabatic_check'], calc='EARTH_FAULT_LOOP'),
    T('fault_protection', 'Fault protection',
      en='Protection against electric shock from a part that has become live because of a fault. Usually delivered by automatic disconnection of supply.',
      see=['basic_protection', 'automatic_disconnection', 'rcd']),
    T('form_of_separation', 'Form of separation',
      en='How far an assembly separates its busbars, functional units and terminals from one another, graded Form 1 to Form 4 in IEC 61439. Higher forms let one circuit be worked on while others stay live.',
      see=['switchgear', 'distribution_board']),
    T('frequency', 'Frequency',
      en='How many times per second an alternating supply completes a cycle. 50 Hz across most of the world, 60 Hz in North America and parts of Asia; it changes reactance, motor speed and transformer sizing.',
      symbol='f', unit='Hz', see=['inductive_reactance', 'slip'], ref='standard_voltages'),
    T('functional_earth', 'Functional earth',
      en='An earth connection made so equipment works properly — screening, reference potential for electronics — rather than for safety. It does not substitute for a protective earth.',
      see=['earthing', 'protective_conductor']),
    T('fuse', 'Fuse',
      en='A protective device that clears a fault by melting. Its speed rises steeply with current, so it can be very fast on a short circuit while tolerating a brief overload.',
      see=['gg_fuse', 'circuit_breaker', 'let_through_energy'], ref='rating_series'),
    T('gg_fuse', 'gG fuse',
      en='A general-purpose fuse link that protects against both overload and short circuit. An aM link protects only against short circuit and must be paired with a separate overload device.',
      see=['fuse', 'overload'], ref='rating_series'),
    T('grouping_factor', 'Grouping factor',
      en="The derating applied when circuits are bunched together, because each one's heat makes it harder for its neighbours to cool. Nine circuits in a bundle can lose nearly half the rating of one on its own.",
      symbol='Cg', see=['correction_factor', 'derating', 'ampacity'], calc='CABLE_SIZE'),
    T('harmonic', 'Harmonic',
      en='A current or voltage component at a whole multiple of the supply frequency, drawn by electronic loads. The third and its multiples add up in the neutral instead of cancelling, so a neutral can carry more than any line.',
      see=['neutral_conductor', 'rcd', 'power_factor_correction']),
    T('ik_code', 'IK code',
      en='A two-digit rating from IEC 62262 for how much mechanical impact an enclosure withstands, from IK00 (unprotected) to IK10 (20 joules).',
      see=['ip_code'], ref='ik_rating'),
    T('illuminance', 'Illuminance',
      en='Luminous flux arriving per square metre of a surface. It is what a light meter reads and what a lighting design is specified in — an office typically 500 lx.',
      symbol='E', unit='lx', see=['luminous_flux']),
    T('impedance', 'Impedance',
      en='The total opposition an AC circuit presents to current: resistance and reactance combined. Unlike resistance it depends on frequency.',
      symbol='Z', unit='Ω', see=['inductive_reactance', 'capacitive_reactance', 'resistivity']),
    T('impedance_voltage', 'Impedance voltage (uk)',
      en='The percentage of rated primary voltage needed to drive rated current through a transformer with its secondary short-circuited. It is on the nameplate, and it sets the fault current the transformer can deliver.',
      symbol='uk', unit='%', see=['transformer', 'prospective_short_circuit_current'], calc='TRANSFORMER_CURRENT'),
    T('indirect_contact', 'Indirect contact',
      en='Touching metal that has become live through a fault, rather than a conductor that is live by design. The older name for what the standards now cover as fault protection.',
      see=['fault_protection', 'exposed_conductive_part', 'touch_voltage']),
    T('inductive_reactance', 'Inductive reactance',
      en='The opposition an inductance presents to alternating current, rising with frequency. In LV cable it is small next to resistance until the conductor gets large, at which point neglecting it understates voltage drop.',
      symbol='XL', unit='Ω', see=['impedance', 'capacitive_reactance', 'voltage_drop']),
    T('capacitive_reactance', 'Capacitive reactance',
      en='The opposition a capacitance presents to alternating current, falling as frequency rises. It is what a correction capacitor uses to supply reactive power.',
      symbol='XC', unit='Ω', see=['impedance', 'inductive_reactance', 'power_factor_correction']),
    T('inrush_current', 'Inrush current',
      en='The brief, large current a transformer, motor or capacitive load draws at the instant it is switched on. It is what a Type C or D breaker exists to tolerate.',
      see=['trip_curve', 'soft_starter', 'star_delta_starting'], ref='breaker_curves'),
    T('instantaneous_trip', 'Instantaneous trip',
      en="The magnetic part of a breaker's operation, which opens it within milliseconds once current exceeds a multiple of its rating — 5× for Type B, 10× for C, 20× for D.",
      see=['trip_curve', 'thermal_magnetic_trip', 'disconnection_time'], ref='breaker_curves'),
    T('insulation_resistance', 'Insulation resistance',
      en='Resistance measured between conductors, and between conductors and earth, with a DC test voltage. IEC 60364-6 wants at least 1,0 MΩ on a 230/400 V circuit tested at 500 V.',
      unit='MΩ', see=['continuity_test']),
    T('interlock', 'Interlock',
      en='A mechanical or electrical arrangement that makes an unsafe combination impossible — two contactors that can never close together, a door that cannot open while a circuit is live.',
      see=['contactor', 'isolation']),
    T('ip_code', 'IP code',
      en='Two digits from IEC 60529: the first is protection against solids and contact, the second against water. IP54 is dust-protected and splash-proof.',
      see=['ik_code', 'creepage_distance'], ref='ip_rating'),
    T('isolation', 'Isolation',
      en='Cutting an installation off from every source of supply, with a visible or positively indicated gap, so it can be worked on safely. Switching off a load is not isolation.',
      see=['interlock', 'live_part', 'switchgear']),
    T('it_system', 'IT system',
      en='An earthing arrangement with no direct connection between supply and earth, or one through a high impedance. A first earth fault does not produce enough current to trip, which is why the system keeps running and why insulation monitoring is mandatory.',
      see=['tn_system', 'tt_system', 'earthing'], ref='earthing_systems'),
    T('let_through_energy', 'Let-through energy (I²t)',
      en="The energy a protective device allows past before it clears a fault, in A²s. The cable's withstand must exceed it — that is the adiabatic check, written the other way round.",
      symbol='I²t', unit='A²s', see=['adiabatic_check', 'fuse', 'breaking_capacity']),
    T('line_conductor', 'Line conductor',
      en='A conductor that carries current from the source to the load and is live in normal service. Marked L1, L2, L3 on a three-phase system; brown, black and grey under IEC 60446.',
      symbol='L', see=['neutral_conductor', 'protective_conductor', 'live_part'], ref='conductor_colours'),
    T('live_part', 'Live part',
      en='A conductor or conductive part intended to be energised in normal use, including the neutral. The neutral counts, which is why it is switched and isolated like any other live conductor.',
      see=['basic_protection', 'isolation', 'neutral_conductor']),
    T('load_factor', 'Load factor',
      en='Average load over a period divided by the peak load in that period. It describes how evenly a supply is used, and it is a tariff and sizing input rather than a safety one.',
      see=['demand_factor', 'diversity_factor']),
    T('lszh', 'LSZH',
      en='Low smoke zero halogen cable compound. It burns without producing the dense acidic smoke that PVC does, which matters in tunnels, ships and public buildings where people evacuate through the smoke.',
      see=['pvc', 'xlpe']),
    T('luminous_flux', 'Luminous flux',
      en='The total light a source emits, in lumens. A lamp is sold by this figure; how much of it reaches the working plane depends on the luminaire, the room and its surfaces.',
      symbol='Φ', unit='lm', see=['illuminance']),
    T('mcb', 'MCB',
      en='Miniature circuit breaker: the DIN-rail device protecting final circuits, made to IEC 60898. Its trip curve — B, C or D — is what decides whether an earth fault disconnects fast enough.',
      see=['mccb', 'trip_curve', 'circuit_breaker', 'rcbo'], ref='breaker_curves'),
    T('mccb', 'MCCB',
      en='Moulded case circuit breaker, made to IEC 60947-2 for larger currents than an MCB. Its trip settings are usually adjustable, which is what makes selectivity achievable in a distribution board.',
      see=['mcb', 'selectivity', 'breaking_capacity']),
    T('nameplate', 'Nameplate',
      en="The plate on a machine stating the conditions it was designed and tested for. Every rating on it is conditional — a motor's current is at its rated voltage, frequency and duty, not at whatever it happens to see.",
      see=['rated_current', 'rated_voltage', 'service_factor']),
    T('neutral_conductor', 'Neutral conductor',
      en='The conductor connected to the star point that carries the unbalance of a three-phase system back to source. It is a live conductor, and on a harmonic-rich supply it can carry more current than any line.',
      symbol='N', see=['line_conductor', 'pen_conductor', 'harmonic'], ref='conductor_colours'),
    T('no_load_loss', 'No-load loss',
      en='The power a transformer consumes just by being energised — iron loss in the core. It runs 24 hours a day whether or not anything is connected, which is why it dominates the lifetime cost of a lightly loaded transformer.',
      unit='W', see=['transformer', 'efficiency']),
    T('overcurrent', 'Overcurrent',
      en='Any current above the rated value, whether from an overload or a short circuit. The two need different protection responses, which is why devices have both a thermal and a magnetic element.',
      see=['overload', 'short_circuit', 'thermal_magnetic_trip']),
    T('overload', 'Overload',
      en='Excess current in a circuit that is otherwise healthy — too much load, not a fault. It is a thermal problem, so protection may take minutes rather than milliseconds.',
      see=['overcurrent', 'short_circuit', 'thermal_magnetic_trip']),
    T('pen_conductor', 'PEN conductor',
      en='A single conductor doing the job of both neutral and protective earth, as in a TN-C system. It must never be switched or broken — losing it puts full line voltage on every exposed metal part it served.',
      symbol='PEN', see=['protective_conductor', 'neutral_conductor', 'tn_system'], ref='earthing_systems'),
    T('peukert_law', "Peukert's law",
      en='The rule that a battery gives less than its nameplate capacity when discharged faster than the rate it was rated at. A lead-acid bank rated over 20 hours delivers noticeably less over 2.',
      symbol='k', see=['c_rate', 'depth_of_discharge'], calc='BATTERY_RUNTIME'),
    T('phase_angle', 'Phase angle',
      en='How far current lags or leads voltage in an AC circuit. Its cosine is the power factor, and its sign says whether the load is inductive or capacitive.',
      symbol='φ', unit='°', see=['cos_phi', 'reactive_power'], calc='POWER'),
    T('phase_sequence', 'Phase sequence',
      en='The order in which the three line voltages reach their peaks. Reversing it reverses a motor, which is why it is verified before a machine is first run.',
      see=['polarity', 'slip']),
    T('polarity', 'Polarity',
      en='A check that switches and protective devices are in the line conductor and not in the neutral. Get it wrong and a circuit stays live with its switch off.',
      see=['line_conductor', 'neutral_conductor', 'isolation']),
    T('power_factor_correction', 'Power factor correction',
      en='Adding capacitors to supply reactive power locally so it is not drawn from the network. Active power is unchanged; what falls is current, and with it the load on cables and transformers.',
      symbol='Qc', unit='var', see=['cos_phi', 'reactive_power', 'harmonic'], calc='POWER_FACTOR_CORRECTION'),
    T('prospective_short_circuit_current', 'Prospective short-circuit current',
      en='The current that would flow into a bolted fault at a given point. Two figures matter: the maximum, which selects breaking capacity, and the minimum, which verifies the device trips at all.',
      symbol='Ipf', unit='kA', see=['breaking_capacity', 'short_circuit', 'impedance_voltage'], calc='SHORT_CIRCUIT'),
    T('protective_conductor', 'Protective conductor (PE)',
      en='The conductor that connects exposed metal to earth so a fault current has a path back to source. Green-and-yellow, and never used for anything else.',
      symbol='PE', see=['pen_conductor', 'earthing', 'adiabatic_check'], calc='EARTH_FAULT_LOOP', ref='conductor_colours'),
    T('pvc', 'PVC',
      en='Polyvinyl chloride cable insulation, rated to a 70 °C conductor temperature. Cheap and common; it softens with heat and gives off dense acidic smoke in a fire.',
      see=['xlpe', 'lszh'], ref='materials'),
    T('rated_current', 'Rated current',
      en='The current a device is designed to carry continuously, In on its nameplate. For a protective device it is also the base that trip multiples are measured from.',
      symbol='In', unit='A', see=['ampacity', 'nameplate', 'trip_curve'], ref='rating_series'),
    T('rated_voltage', 'Rated voltage',
      en='The voltage equipment is designed for. IEC 60038 sets 230/400 V for LV distribution, with tolerances that a design has to work inside rather than at.',
      symbol='Un', unit='V', see=['nameplate', 'voltage_drop'], ref='standard_voltages'),
    T('rcbo', 'RCBO',
      en='One device combining residual current protection with overcurrent protection for a single circuit. An RCCB has no overcurrent element and still needs a breaker or fuse alongside it.',
      see=['rccb', 'rcd', 'mcb'], ref='rcd_types'),
    T('rccb', 'RCCB',
      en='A residual current circuit breaker with no overcurrent protection of its own. It detects earth leakage only, so the circuit still needs a device for overload and short circuit.',
      see=['rcbo', 'rcd'], ref='rcd_types'),
    T('rcd', 'RCD',
      en='A device that compares the current going out with the current coming back and opens when the difference exceeds its rating. It responds to leakage, not to how large the fault current is — which is why it protects where an overcurrent device cannot.',
      symbol='IΔn', unit='mA', see=['residual_current', 'rcbo', 'rccb', 'touch_voltage'], calc='EARTH_FAULT_LOOP', ref='rcd_types'),
    T('reactive_power', 'Reactive power',
      en='Power that flows back and forth between source and load without doing net work, exchanged by inductance and capacitance. It does no work but it does occupy the cable.',
      symbol='Q', unit='var', see=['active_power', 'apparent_power', 'power_factor_correction'], calc='POWER'),
    T('reference_method', 'Reference method',
      en='The installation arrangement a capacity table is written for — clipped direct, in conduit, buried, in insulation. Choosing the wrong one is the most common way a correct-looking cable calculation ends up wrong.',
      see=['ampacity', 'correction_factor'], calc='CABLE_SIZE'),
    T('relay', 'Relay',
      en='An electrically operated switch for control signals rather than load current. A contactor is built to break motor current; a relay generally is not.',
      see=['contactor', 'interlock']),
    T('residual_current', 'Residual current',
      en='The vector sum of the currents in all live conductors of a circuit. In a healthy circuit it is zero; anything else is current escaping to earth.',
      symbol='IΔ', unit='mA', see=['rcd', 'fault_current'], ref='rcd_types'),
    T('resistivity', 'Resistivity',
      en='How strongly a material resists current, independent of its shape. It rises with temperature, so a hot conductor drops more voltage than the same cable cold — about 20 % more for copper between 20 °C and 70 °C.',
      symbol='ρ', unit='Ω·mm²/m', see=['conductor', 'voltage_drop'], ref='materials'),
    T('rms', 'RMS',
      en='Root mean square: the DC value that would produce the same heating. Every AC voltage and current quoted without qualification is an RMS figure — 230 V RMS peaks at about 325 V.',
      see=['rated_voltage', 'frequency']),
    T('selectivity', 'Selectivity (discrimination)',
      en="Arranging protection so only the device nearest a fault opens, leaving the rest of the installation running. Achieved by current, by time, or by energy — and a manufacturer's table is the only proof for the last of these.",
      see=['cascading', 'back_up_protection', 'trip_curve']),
    T('service_factor', 'Service factor',
      en="A multiplier on a motor's rated power saying how much continuous overload it tolerates. A 1,15 service factor is headroom for occasional excess, not a licence to run there.",
      see=['nameplate', 'overload'], calc='MOTOR_CURRENT'),
    T('sheath', 'Sheath',
      en='The outermost layer of a cable, protecting everything inside from moisture, chemicals and abrasion. It is not insulation and must never be relied on as such.',
      see=['armour', 'pvc', 'lszh'], calc='CABLE_WEIGHT'),
    T('short_circuit', 'Short circuit',
      en='A fault of negligible impedance between conductors, producing the largest current a circuit can deliver. It is limited only by the impedance of the supply and the cable between it and the fault.',
      see=['prospective_short_circuit_current', 'breaking_capacity', 'fault_current'], calc='SHORT_CIRCUIT'),
    T('skin_effect', 'Skin effect',
      en='The tendency of alternating current to crowd into the outside of a conductor, leaving the centre carrying less. It is negligible in small LV cable and is the reason very large conductors are used in parallel rather than as one.',
      see=['conductor', 'inductive_reactance']),
    T('slip', 'Slip',
      en="The difference between an induction motor's synchronous speed and its actual speed, as a fraction. A motor with no slip produces no torque — the slip is what induces rotor current in the first place.",
      symbol='s', see=['frequency', 'vfd']),
    T('soft_starter', 'Soft starter',
      en='A device that ramps voltage up so a motor accelerates gently, cutting inrush and mechanical shock. It reduces starting torque as well, so it does not suit every load.',
      see=['inrush_current', 'star_delta_starting', 'vfd']),
    T('spd', 'Surge protective device (SPD)',
      en='A device that limits transient overvoltages from lightning or switching by diverting the surge to earth. It protects against voltage spikes, not against overcurrent, and has no role in fault protection.',
      see=['earthing', 'equipotential_bonding']),
    T('star_delta_starting', 'Star-delta starting',
      en='Starting a motor with its windings in star, then switching to delta once it is up to speed. Starting current drops to a third — and so does starting torque, which is the catch.',
      see=['inrush_current', 'soft_starter', 'vfd'], calc='MOTOR_CURRENT'),
    T('switchgear', 'Switchgear',
      en='The collective name for the devices that switch, protect and isolate circuits, together with the assemblies housing them.',
      see=['distribution_board', 'busbar', 'form_of_separation']),
    T('thermal_magnetic_trip', 'Thermal magnetic trip',
      en='The two mechanisms in a common breaker: a bimetal that responds slowly to sustained overload, and an electromagnet that responds in milliseconds to short circuit. Overload and short circuit need different speeds, so they get different mechanisms.',
      see=['instantaneous_trip', 'overload', 'short_circuit'], ref='breaker_curves'),
    T('tn_system', 'TN system',
      en="An earthing arrangement where exposed metal is connected back to the source's earthed point by a protective conductor. The fault path is metallic, so fault current is high and overcurrent devices can clear an earth fault.",
      see=['tt_system', 'it_system', 'pen_conductor', 'earthing'], ref='earthing_systems'),
    T('touch_voltage', 'Touch voltage',
      en='The voltage appearing between parts a person can touch simultaneously during a fault. IEC 60364-4-41 takes 50 V AC as the limit that may persist in dry conditions.',
      symbol='UL', unit='V', see=['equipotential_bonding', 'rcd', 'automatic_disconnection'], calc='EARTH_FAULT_LOOP'),
    T('transformer', 'Transformer',
      en='A machine that changes voltage between two windings by magnetic coupling, with no moving parts. It transfers apparent power, which is why it is rated in kVA and not in kW.',
      unit='kVA', see=['impedance_voltage', 'vector_group', 'no_load_loss'], calc='TRANSFORMER_CURRENT'),
    T('trip_curve', 'Trip curve',
      en='The graph of operating time against current for a protective device. Reading it is how you answer both questions that matter: will it hold the load, and will it clear the fault in time.',
      see=['instantaneous_trip', 'mcb', 'selectivity'], ref='breaker_curves'),
    T('tt_system', 'TT system',
      en="An earthing arrangement where the installation has its own earth electrode, separate from the supply's. Loop impedance is high, so an RCD rather than an overcurrent device provides fault protection.",
      see=['tn_system', 'it_system', 'earth_electrode', 'rcd'], ref='earthing_systems'),
    T('vector_group', 'Vector group',
      en="A code such as Dyn11 giving a transformer's winding connections and the phase shift between them. Transformers must match on this before they can be paralleled.",
      see=['transformer', 'phase_sequence']),
    T('vfd', 'Variable frequency drive (VFD)',
      en="An electronic drive that varies a motor's speed by varying supply frequency. It draws a distorted, high-frequency-rich current, which is why it needs a Type B RCD and careful cable screening.",
      see=['soft_starter', 'harmonic', 'rcd', 'slip'], ref='rcd_types'),
    T('voltage_drop', 'Voltage drop',
      en='The voltage lost along a cable because the conductor has resistance. IEC 60364-5-52 Annex G suggests keeping it within 3 % for lighting and 5 % for other circuits, measured from the origin.',
      symbol='ΔU', unit='V', see=['resistivity', 'cross_sectional_area', 'rated_voltage'], calc='VOLTAGE_DROP'),
    T('voltage_factor', 'Voltage factor',
      en='A tolerance IEC 60909 applies to nominal voltage so each fault calculation errs the safe way: 1,05 for a maximum current, 0,95 for a minimum.',
      symbol='c', see=['prospective_short_circuit_current', 'short_circuit'], calc='SHORT_CIRCUIT'),
    T('withstand_current', 'Withstand current',
      en="The current a component can carry for a stated short time without damage — a busbar's 1-second rating, for instance. It is a thermal and mechanical limit, not an operating rating.",
      unit='kA', see=['adiabatic_check', 'let_through_energy', 'busbar']),
    T('xlpe', 'XLPE',
      en='Cross-linked polyethylene cable insulation, rated to a 90 °C conductor temperature. The higher rating means more current for the same section than PVC, but the cable is stiffer to install.',
      see=['pvc', 'lszh', 'ampacity'], ref='materials'),
]


def escape(text: str) -> str:
    """XML-escape, and protect the characters Android string resources treat specially."""
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "\\'")
        .replace('"', '\\"')
    )


def kotlin() -> str:
    lines = [
        "package com.kemalurekli.electricalcalculator.features.glossary.domain",
        "",
        "import com.kemalurekli.electricalcalculator.R",
        "import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId",
        "",
        "/**",
        " * Every glossary term the app knows.",
        " *",
        " * GENERATED — edit `scripts/gen_glossary.py` and re-run it rather than this",
        " * file. The script owns the structure and the English text; each translation",
        " * lives in its own `values-<tag>/strings.xml` and is never generated.",
        " *",
        " * Order here is the authoring order. The screen sorts by the reader's language",
        " * with a locale collator, because A–Z is not the same list in Turkish as in",
        " * English, and not a list at all in the same sense in Arabic.",
        " */",
        "object GlossaryCatalog {",
        "",
        "    val all: List<GlossaryTerm> = listOf(",
    ]

    for t in TERMS:
        lines.append("        GlossaryTerm(")
        lines.append(f'            key = "{t.key}",')
        lines.append(f'            englishTerm = "{t.en_term}",')
        lines.append(f"            termRes = R.string.gl_{t.key}_term,")
        lines.append(f"            definitionRes = R.string.gl_{t.key}_def,")
        if t.symbol:
            lines.append(f'            symbol = "{t.symbol}",')
        if t.unit:
            lines.append(f'            unit = "{t.unit}",')
        if t.see:
            joined = ", ".join(f'"{k}"' for k in t.see)
            lines.append(f"            seeAlso = listOf({joined}),")
        if t.calc:
            lines.append(f"            calculator = CalculatorId.{t.calc},")
        if t.ref:
            lines.append(f'            referenceTopic = "{t.ref}",')
        lines.append("        ),")

    lines += [
        "    )",
        "",
        "    private val byKey: Map<String, GlossaryTerm> = all.associateBy { it.key }",
        "",
        "    /** The term for [key], or null for a stale cross-reference or deep link. */",
        "    fun termOrNull(key: String): GlossaryTerm? = byKey[key]",
        "}",
        "",
    ]
    return "\n".join(lines)


def english_block() -> str:
    lines = ["", "    <!-- Glossary -->"]
    for t in TERMS:
        lines.append(f'    <string name="gl_{t.key}_term">{escape(t.en_term)}</string>')
        # A literal % with no positional argument must declare formatted="false",
        # or String.format sees a conversion specifier and lint sees a bug.
        # `StringResourceIntegrityTest` enforces this.
        attr = ' formatted="false"' if "%" in t.en else ""
        lines.append(f'    <string name="gl_{t.key}_def"{attr}>{escape(t.en)}</string>')
    return "\n".join(lines) + "\n"


def rewrite_english() -> None:
    """Rebuild the generated block in the base strings file, leaving the rest alone."""
    path = RES / "values/strings.xml"
    text = path.read_text()
    close = "</resources>"
    assert text.count(close) == 1, f"{path} has {text.count(close)} closing tags"

    start = text.find("    <!-- Glossary -->")
    if start != -1:
        text = text[:start].rstrip("\n") + "\n\n" + close + "\n"
    path.write_text(text.replace(close, english_block().lstrip("\n") + "\n" + close))


def translation_coverage() -> dict[str, set[str]]:
    """Which glossary keys each shipped language is still missing.

    Read-only. A missing key is not an error here — Android falls back to the
    English string, which is honest — but it is something the person adding a
    language needs a list of.
    """
    wanted = {f"gl_{t.key}_term" for t in TERMS} | {f"gl_{t.key}_def" for t in TERMS}
    name_attr = re.compile(r'<string\s+name="([^"]+)"')

    coverage: dict[str, set[str]] = {}
    for folder in sorted(RES.glob("values-*")):
        strings = folder / "strings.xml"
        # values-night and friends carry no strings.
        if not strings.exists():
            continue
        present = set(name_attr.findall(strings.read_text()))
        coverage[folder.name.removeprefix("values-")] = wanted - present
    return coverage


keys = [t.key for t in TERMS]
assert len(keys) == len(set(keys)), "duplicate glossary key"
unknown = {k for t in TERMS for k in t.see} - set(keys)
assert not unknown, f"see-also points at missing terms: {sorted(unknown)}"

catalog = (
    ROOT
    / "java/com/kemalurekli/electricalcalculator/features/glossary/domain/GlossaryCatalog.kt"
)
catalog.write_text(kotlin())
rewrite_english()

print(f"{len(TERMS)} terms -> catalog + {2 * len(TERMS)} English strings")
for tag, missing in translation_coverage().items():
    if missing:
        print(f"  {tag}: {len(missing)} strings not yet translated")
    else:
        print(f"  {tag}: complete")
