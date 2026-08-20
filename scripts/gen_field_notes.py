#!/usr/bin/env python3
"""Single source for the field notes' structure and their English text.

### What this owns, and what it must never touch

Exactly two things: the Kotlin catalog, and the **English** strings. Every
translation lives in its own `values-<tag>/strings.xml`, is written by someone
who works in that language, and is never generated here. This is the same rule
`gen_glossary.py` states and for the same reason: a generator that emitted every
language would either need one column per language — a table no translator can
work in — or would quietly seed English text under a Turkish key, which is worse
than an obvious gap.

Re-running is therefore safe. The English block is rebuilt from the table below;
the translation files are only *read*, to report what each language is missing.

### What belongs in this file

A note makes a claim you can act on. If it defines a word it belongs in the
glossary; if it quotes a tabulated figure it belongs in a reference topic. The
cross-link fields are what make that boundary cheap to hold: a note that wants
to define a term links to the term instead of restating it.

Before adding a note, read the reference topic it is closest to. `safety_rules`
already carries the five rules, prove-test-prove, lock-and-label and barriering
adjacent live parts, and a note repeating any of those is a note in the wrong
place.

### These are claims, not transcriptions

Nothing here is copied from a standard, which makes it more dangerous than the
tables rather than less: a wrong table entry misleads a reader, a wrong rule of
thumb makes them act and feel sure about it. Registered in
`docs/verification-backlog.md` as `field-note-claims`.

Usage:
    python3 scripts/gen_field_notes.py
"""
from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
import re

MODULE = Path(__file__).resolve().parent.parent / "feature/fieldnotes/src/commonMain"
ROOT = MODULE / "kotlin"
RES = MODULE / "composeResources"

# Declared in feature/fieldnotes/build.gradle.kts and repeated here because the
# catalog imports from it.
RES_PACKAGE = "com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources"


@dataclass
class N:
    """One note: its key, its category, and its English text."""

    key: str
    cat: str
    en_title: str
    en: str
    terms: list[str] = field(default_factory=list)
    calc: str | None = None
    ref: str | None = None


NOTES: list[N] = [
    # -- Safety and working practice -----------------------------------------
    N('voltage_indicator_is_not_a_prover', 'SAFETY_AND_PRACTICE',
      'A non-contact voltage detector cannot prove a circuit dead',
      en="It senses the field around a conductor, so it reports what is nearby rather than what "
         "is in front of it. Screened cable, a conductor at the back of a bunch, a low battery or "
         "a gloved hand between pen and cable all give the same result as a dead circuit: nothing. "
         "Use it to find a live conductor, never to establish that one is dead — that needs a "
         "two-pole tester, proved on a known source before and after.",
      terms=['live_part', 'isolation'], ref='safety_rules'),

    N('phantom_voltage_reads_high_delivers_nothing', 'SAFETY_AND_PRACTICE',
      'A disconnected conductor can read most of the supply voltage',
      en="A long run beside a live cable couples to it capacitively. A digital multimeter draws "
         "almost no current, so that coupling is enough to show a hundred volts or more on a "
         "conductor that is disconnected at both ends. The reading is real; the source behind it "
         "cannot deliver current. A two-pole tester loads the circuit and collapses the phantom, "
         "which is the whole reason it is the tool for proving dead.",
      terms=['live_part', 'impedance']),

    N('capacitors_stay_charged_after_isolation', 'SAFETY_AND_PRACTICE',
      'Isolating a circuit does not discharge what is in it',
      en="Power-factor correction banks and the DC link inside a drive hold their charge after the "
         "supply is gone, and both store enough energy to be lethal. Discharge resistors take time "
         "and they fail silently. Wait for the time the manufacturer states, then prove it dead at "
         "the terminals — the wait is a prerequisite for the test, not a substitute for it.",
      terms=['power_factor_correction', 'vfd', 'isolation'], calc='POWER_FACTOR_CORRECTION'),

    N('rcd_test_button_proves_only_the_mechanism', 'SAFETY_AND_PRACTICE',
      'The test button on an RCD proves the RCD, not the installation',
      en="It drives a current through an internal winding to check that the mechanism still "
         "releases. It says nothing about the earth path, the trip time under a real fault, or "
         "whether the device is wired the right way round. A button that works on an installation "
         "with no earth connection still works — and the installation is still lethal.",
      terms=['rcd', 'earth_fault_loop_impedance', 'protective_conductor'],
      calc='EARTH_FAULT_LOOP', ref='rcd_types'),

    N('neutral_is_a_live_conductor', 'SAFETY_AND_PRACTICE',
      'A neutral is a live conductor, not a safe one',
      en="It sits near earth potential only while the circuit is intact and reasonably balanced. "
         "Open it while load is connected and the far end rises towards line voltage; unbalance a "
         "three-phase circuit and it carries current that has to go somewhere. Treat it as live "
         "until proved otherwise, and never disconnect one on a circuit you have not isolated.",
      terms=['neutral_conductor', 'live_part', 'phase_sequence'], calc='NEUTRAL_CURRENT'),

    N('broken_pen_energises_metalwork', 'SAFETY_AND_PRACTICE',
      'A broken PEN conductor puts line voltage on every earthed surface',
      en="In a TN-C-S supply the incoming conductor is neutral and earth in one. Lose it upstream "
         "and load current returns through the only path left — the earthing system — so exposed "
         "metalwork throughout the installation rises towards line potential while every device "
         "keeps working normally. Nothing trips, nothing looks wrong. It is the reason TN-C-S is "
         "not permitted for some installations at all.",
      terms=['pen_conductor', 'tn_system', 'earthing', 'touch_voltage'], ref='earthing_systems'),

    N('borrowed_neutral_survives_isolation', 'SAFETY_AND_PRACTICE',
      'A borrowed neutral stays live after you isolate its circuit',
      en="Where an installation has grown, a circuit's neutral is sometimes connected to a "
         "different circuit's line. Switch off the breaker you believe feeds the work and the line "
         "goes dead while the neutral keeps carrying another circuit's current. Common in lighting "
         "on two-way switching and in old rewires. If a supposedly dead neutral reads a voltage or "
         "sparks on disconnection, stop and find where it goes.",
      terms=['neutral_conductor', 'isolation'], ref='common_mistakes'),

    N('meter_category_matches_where_you_stand', 'SAFETY_AND_PRACTICE',
      'Choose the meter by where you are standing, not by the voltage',
      en="Measurement category rates a tester for the transient energy at a location, not for the "
         "steady voltage it will read. Nearer the origin, the impedance behind the fault is lower "
         "and the transients are larger: a socket outlet is CAT II, a distribution board CAT III, "
         "the incomer and the supply side CAT IV. A CAT II meter reading 230 V perfectly well at "
         "an intake can still fail as an arc across your hands.",
      terms=['prospective_short_circuit_current', 'breaking_capacity'], calc='SHORT_CIRCUIT'),

    N('replace_a_fuse_like_for_like', 'SAFETY_AND_PRACTICE',
      'A fuse is chosen for its breaking capacity as much as its rating',
      en="Two fuses of the same amperage are not interchangeable. The class sets how it behaves "
         "before it clears — gG for general circuits, aM for motors that must survive inrush — and "
         "the breaking capacity sets the fault current it can interrupt without rupturing. Fitting "
         "a higher rating to stop nuisance operation removes the protection the cable was sized "
         "around, and the cable is now the fuse.",
      terms=['gg_fuse', 'fuse', 'breaking_capacity', 'rated_current'], ref='rating_series'),

    N('emergency_stop_is_not_an_isolator', 'SAFETY_AND_PRACTICE',
      'An emergency stop is not an isolator',
      en="It is designed to bring machinery to a safe state quickly, and it can often be reset from "
         "somewhere you cannot see. It may leave control circuits, heaters or a drive's DC link "
         "energised, and on many machines it does. Isolation means a device intended for isolation, "
         "locked in the off position, with the key in your pocket.",
      terms=['isolation', 'interlock', 'switchgear'], ref='safety_rules'),

    N('isolate_upstream_of_what_you_touch', 'SAFETY_AND_PRACTICE',
      'Switching a device off leaves its own terminals live',
      en="Open a breaker and the load side is dead while its supply terminals stay at full voltage "
         "— that is what it is for. The same is true of an isolator, a contactor and a switched "
         "socket. To work on the device itself you have to isolate the thing feeding it, which is "
         "usually one board further back than people expect.",
      terms=['isolation', 'switchgear', 'distribution_board'], ref='safety_rules'),

    N('label_before_you_leave', 'SAFETY_AND_PRACTICE',
      'An unlabelled board is a hazard you leave behind for someone else',
      en="Every hour saved by not labelling is paid back with interest by whoever next has to "
         "isolate a circuit in a hurry. Guessing which breaker feeds what ends either in the wrong "
         "circuit being switched off — sometimes one that mattered — or in the right one being "
         "left on. Label as you go: the information is in your head only while you are standing "
         "there.",
      terms=['distribution_board', 'isolation'], ref='common_mistakes'),
    N('volt_drop_bites_before_ampacity', 'RULES_OF_THUMB',
      'On a long run the volt drop limit is reached before the current rating',
      en="Ampacity is set by how hot the conductor gets, which does not care how far it goes. Volt "
         "drop accumulates with every metre. Past roughly thirty or forty metres on a final circuit "
         "the cable that carries the current comfortably is already outside the drop limit, so size "
         "on drop first and check the rating afterwards — doing it the other way round means sizing "
         "twice.",
      terms=['voltage_drop', 'ampacity'], calc='VOLTAGE_DROP'),

    N('doubling_length_doubles_drop', 'RULES_OF_THUMB',
      'Double the length and you double the drop; double the area and you halve it',
      en="Resistance is proportional to length and inversely proportional to cross-sectional area, "
         "so both relationships are exactly linear and both are worth having in your head. A run "
         "that drops 3 % at 25 m drops 6 % at 50 m. Going up one preferred size is not a halving "
         "though — the series steps by roughly 1.5 to 1.6 at a time, so one size buys about a third "
         "off the drop, not half.",
      terms=['voltage_drop', 'cross_sectional_area', 'resistivity'], calc='VOLTAGE_DROP'),

    N('single_phase_kilowatt_is_four_amps', 'RULES_OF_THUMB',
      'At 230 V single phase, a kilowatt is about 4.3 A',
      en="For a resistive load — heating, filament lighting, a kettle — current is simply power over "
         "voltage. It is the fastest sanity check there is on a nameplate or a schedule of loads. "
         "For anything with a motor or a switched-mode supply, divide by the power factor as well, "
         "which for a small motor takes 4.3 A closer to 5.5 A.",
      terms=['active_power', 'cos_phi', 'rated_current'], calc='POWER'),

    N('three_phase_kilowatt_is_one_and_a_half_amps', 'RULES_OF_THUMB',
      'At 400 V three phase, a kilowatt is about 1.44 A',
      en="The √3 in the three-phase formula is what makes the same power need less current per "
         "conductor than a single-phase supply of the same voltage. Carrying 1.44 A per kilowatt "
         "lets you size a submain in your head and catch a schedule of loads that is out by a "
         "factor rather than a percent.",
      terms=['active_power', 'line_conductor'], calc='POWER'),

    N('transformer_kva_to_secondary_amps', 'RULES_OF_THUMB',
      'A 400 V transformer gives about 1.44 A on the secondary per kVA',
      en="The same √3 arithmetic, run on apparent power instead of active: a 630 kVA unit delivers "
         "roughly 910 A at full load. Useful standing in front of a transformer with no nameplate "
         "legible, and the number the incoming device and busbar have to be rated for.",
      terms=['apparent_power', 'transformer', 'rated_current'], calc='TRANSFORMER_CURRENT'),

    N('dol_start_draws_six_times', 'RULES_OF_THUMB',
      'A motor started direct-on-line draws six to eight times its full load current',
      en="For the second or two it takes to reach speed. It is why the protective device has to "
         "tolerate an inrush it would otherwise treat as a fault, why a gG fuse is the wrong choice "
         "in front of a motor, and why a supply that runs the motor perfectly well can still dim "
         "the lights every time it starts.",
      terms=['inrush_current', 'star_delta_starting', 'soft_starter', 'gg_fuse'],
      calc='MOTOR_CURRENT', ref='selection_starting'),

    N('copper_resistance_rises_with_temperature', 'RULES_OF_THUMB',
      'Copper gains about 0.4 % resistance per kelvin',
      en="A conductor at its 70 °C limit has roughly 20 % more resistance than the same conductor at "
         "20 °C, and therefore 20 % more volt drop. Calculating drop at ambient and then loading the "
         "cable to its rating is how a design that passed on paper fails on site.",
      terms=['resistivity', 'ambient_temperature', 'voltage_drop'], calc='VOLTAGE_DROP'),

    N('grouping_costs_more_than_people_expect', 'RULES_OF_THUMB',
      'Bunching circuits together costs more capacity than the ambient ever does',
      en="Six circuits in one containment lose over 40 % of their tabulated rating; the same cables "
         "would have to reach about 55 °C ambient to lose as much. Grouping is also the factor most "
         "often left out, because it is a property of the installation rather than of the cable in "
         "front of you.",
      terms=['grouping_factor', 'derating', 'correction_factor'],
      calc='CABLE_SIZE', ref='correction_factors'),

    N('protection_sits_between_two_currents', 'RULES_OF_THUMB',
      'The device rating has to sit between the load and the cable',
      en="Design current ≤ device rating ≤ cable capacity, in that order, with no exceptions worth "
         "learning as a beginner. Every overload argument reduces to those two inequalities: a "
         "device smaller than the load nuisance-trips, a device larger than the cable leaves the "
         "cable unprotected.",
      terms=['rated_current', 'ampacity', 'overload', 'overcurrent'],
      calc='CABLE_SIZE', ref='rating_series'),

    N('power_factor_correction_pays_on_current_not_energy', 'RULES_OF_THUMB',
      'Correcting power factor cuts current, not the energy you are billed for',
      en="Capacitors return reactive current locally instead of dragging it up the supply, so cables "
         "and transformers see less current and the losses in them fall. The active energy the meter "
         "records barely moves. The saving is in released capacity and in avoiding a reactive charge, "
         "which is a different argument from a smaller electricity bill.",
      terms=['power_factor_correction', 'reactive_power', 'apparent_power', 'cos_phi'],
      calc='POWER_FACTOR_CORRECTION'),

    N('lighting_design_starts_from_the_task', 'RULES_OF_THUMB',
      'Light the task, not the room',
      en="Illuminance targets are written against what is being done — a workshop bench and the "
         "corridor outside it differ by a factor of five. Designing to one figure for a whole "
         "building over-lights the circulation and under-lights the work, and the lumen method "
         "gives an average across a plane, so a compliant average can still leave a dark bench "
         "under a badly placed fitting.",
      terms=['illuminance', 'luminous_flux'], calc='LIGHTING_LUMEN'),

    N('diversity_is_a_judgement_not_a_discount', 'RULES_OF_THUMB',
      'Diversity is an argument about behaviour, not a coefficient to apply',
      en="It says that not every load runs at once, which is true of sockets in a house and false of "
         "a machine line built to run flat out. Applying a habitual factor to a load that has no "
         "diversity is how a submain ends up undersized, and the failure appears at commissioning "
         "under real load rather than on paper.",
      terms=['diversity_factor', 'demand_factor', 'load_factor']),

    # -- Measurement and testing ---------------------------------------------
    N('dead_tests_come_before_live_ones', 'MEASUREMENT_AND_TESTING',
      'Every dead test comes before the first live one',
      en="Continuity and insulation resistance are done on an isolated installation, and they are "
         "what tells you it is safe to energise. Running them afterwards inverts the point: the "
         "first fault they would have found has already been found by putting voltage on it. The "
         "order is the test, not a preference.",
      terms=['continuity_test', 'insulation_resistance'], ref='commissioning_tests'),

    N('insulation_test_destroys_electronics', 'MEASUREMENT_AND_TESTING',
      'An insulation test at 500 V will destroy anything left connected',
      en="Dimmers, LED drivers, surge protection and any electronic control are not built to survive "
         "the test voltage. Disconnect them or link out the circuit, and remember that a device "
         "damaged this way often keeps working for a while — the failure arrives weeks later and "
         "nobody connects it to the test.",
      terms=['insulation_resistance', 'spd'], ref='commissioning_tests'),

    N('measure_loop_impedance_at_the_far_end', 'MEASUREMENT_AND_TESTING',
      'Loop impedance is only meaningful at the furthest point of the circuit',
      en="It rises with distance, so the reading at the board is the best case and the reading at "
         "the last socket is the one the disconnection time depends on. A circuit that passes at the "
         "board and fails at its end is a circuit that will not disconnect in time under a fault "
         "where a fault is most likely.",
      terms=['earth_fault_loop_impedance', 'disconnection_time', 'external_loop_impedance'],
      calc='EARTH_FAULT_LOOP', ref='disconnection_times'),

    N('polarity_is_cheap_to_check_and_lethal_to_miss', 'MEASUREMENT_AND_TESTING',
      'A reversed line and neutral leaves everything switched on the neutral',
      en="Lamps light, sockets work, and every appliance behaves normally — the fault shows up only "
         "when someone opens a switched fitting and finds the line conductor still connected to the "
         "part they are holding. It takes seconds to check and there is no symptom to notice later.",
      terms=['polarity', 'line_conductor', 'neutral_conductor'], ref='commissioning_tests'),

    N('rcd_ramp_test_before_the_trip_test', 'MEASUREMENT_AND_TESTING',
      'A residual current device that trips at the right current can still be too slow',
      en="Trip current and trip time are separate results. A device operating at 25 mA on a 30 mA "
         "rating has the sensitivity but might exceed 300 ms, which is what determines whether it "
         "protects a person. Record both; a pass on one is not a pass.",
      terms=['rcd', 'residual_current', 'disconnection_time'], ref='rcd_types'),

    N('prove_continuity_of_the_protective_conductor_first', 'MEASUREMENT_AND_TESTING',
      'The protective conductor is the first thing to prove and the easiest to forget',
      en="Every other protective measure assumes it is there. A high-resistance joint in it — a "
         "painted enclosure, an overtightened terminal, a strand cut through — leaves the "
         "installation looking normal and disconnecting slowly or not at all, and no test of the "
         "line conductors reveals it.",
      terms=['protective_conductor', 'continuity_test', 'equipotential_bonding'], ref='primer_bonding'),

    N('a_clamp_meter_reads_zero_around_a_whole_cable', 'MEASUREMENT_AND_TESTING',
      'A clamp around a whole cable reads nearly zero, and that is correct',
      en="Line and neutral currents are equal and opposite, so their fields cancel and the "
         "instrument reports the difference — which is the leakage. To measure load current, clamp "
         "one conductor. Reading a few milliamps around the whole cable is not a broken meter; it "
         "is the earth leakage, and a rising one is worth investigating.",
      terms=['residual_current', 'rcd', 'current_transformer']),

    N('true_rms_matters_on_distorted_current', 'MEASUREMENT_AND_TESTING',
      'An averaging meter under-reads distorted current badly',
      en="Meters that are not true RMS assume a sine wave and scale an average to suit it. On the "
         "current drawn by LED drivers, drives and switched-mode supplies that assumption is wrong "
         "and the reading can be tens of per cent low — precisely on the circuits whose neutral "
         "current is the thing you were worried about.",
      terms=['rms', 'harmonic'], calc='NEUTRAL_CURRENT', ref='primer_harmonics'),

    N('record_the_reading_not_the_verdict', 'MEASUREMENT_AND_TESTING',
      'Write down the number, not "pass"',
      en="A recorded 0.35 Ω is comparable with next year's 0.55 Ω and shows a joint going high "
         "before it fails. A recorded \"pass\" tells the next person only that someone once thought "
         "it was fine. The value of a test schedule is almost entirely in the trend.",
      terms=['earth_fault_loop_impedance', 'insulation_resistance'], ref='commissioning_tests'),

    N('test_leads_have_resistance_too', 'MEASUREMENT_AND_TESTING',
      'Null the leads, or their resistance lands in every continuity reading',
      en="A pair of leads is easily 0.1 Ω, which is a large fraction of the figure a short final "
         "circuit should show. Nulling takes a moment at the start of the day; skipping it "
         "systematically inflates every reading and turns a genuinely low resistance into a "
         "borderline one.",
      terms=['continuity_test', 'protective_conductor']),

    N('an_open_neutral_hides_from_a_two_lead_test', 'MEASUREMENT_AND_TESTING',
      'A circuit can pass insulation testing and still be broken',
      en="Insulation resistance proves conductors are separated from each other and from earth. It "
         "says nothing about whether a conductor is continuous. A neutral broken inside a joint "
         "gives an excellent insulation reading — infinite, in fact — because a disconnected "
         "conductor is very well insulated indeed.",
      terms=['insulation_resistance', 'continuity_test', 'neutral_conductor']),

    N('energise_one_circuit_at_a_time', 'MEASUREMENT_AND_TESTING',
      'Energise one circuit at a time, and know what each one should draw',
      en="Closing everything at once means an inrush that is the sum of every load and a fault that "
         "could be anywhere. Bringing circuits up one by one localises a problem to the last thing "
         "you switched, and it lets you compare the current drawn against what that circuit was "
         "designed for while there is still time to act on the difference.",
      terms=['inrush_current', 'overcurrent'], ref='commissioning_tests'),
]


def escape(text: str) -> str:
    """XML-escape, and nothing else.

    `aapt` also wanted `\\'` and `\\"`; Compose Resources does not, and passes
    a backslash written here straight to the reader. See gen_glossary.py.
    """
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def kotlin() -> str:
    imports = [f"import {RES_PACKAGE}.Res"]
    for n in NOTES:
        imports.append(f"import {RES_PACKAGE}.fn_{n.key}_title")
        imports.append(f"import {RES_PACKAGE}.fn_{n.key}_body")

    lines = [
        "package com.kemalurekli.electricalcalculator.features.fieldnotes.domain",
        "",
        "import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId",
        *imports,
        "",
        "/**",
        " * Every field note the app ships.",
        " *",
        " * GENERATED — edit `scripts/gen_field_notes.py` and re-run it rather than this",
        " * file. The script owns the structure and the English text; each translation",
        " * lives in its own `values-<tag>/strings.xml` and is never generated.",
        " *",
        " * Order here is the authoring order. The screen groups by category and keeps",
        " * this order within each, so a category reads as it was written rather than",
        " * being alphabetised into nonsense.",
        " */",
        "object FieldNoteCatalog {",
        "",
        "    val all: List<FieldNote> = listOf(",
    ]

    for n in NOTES:
        lines.append("        FieldNote(")
        lines.append(f'            key = "{n.key}",')
        lines.append(f"            category = FieldNoteCategory.{n.cat},")
        lines.append(f"            title = Res.string.fn_{n.key}_title,")
        lines.append(f"            body = Res.string.fn_{n.key}_body,")
        if n.terms:
            joined = ", ".join(f'"{k}"' for k in n.terms)
            lines.append(f"            glossaryTerms = listOf({joined}),")
        if n.calc:
            lines.append(f"            calculator = CalculatorId.{n.calc},")
        if n.ref:
            lines.append(f'            referenceTopic = "{n.ref}",')
        lines.append("        ),")

    lines += [
        "    )",
        "",
        "    private val byKey: Map<String, FieldNote> = all.associateBy { it.key }",
        "",
        "    /** The note for [key], or null for a stale cross-reference or deep link. */",
        "    fun noteOrNull(key: String): FieldNote? = byKey[key]",
        "",
        "    /** The notes filed under [category], in authoring order. */",
        "    fun inCategory(category: FieldNoteCategory): List<FieldNote> =",
        "        all.filter { it.category == category }",
        "}",
        "",
    ]
    return "\n".join(lines)


def english_block() -> str:
    lines = ["", "    <!-- Field notes -->"]
    for n in NOTES:
        # No formatted="false": Compose Resources does no formatting unless the
        # caller passes arguments, so there is nothing to opt out of.
        lines.append(f'    <string name="fn_{n.key}_title">{escape(n.en_title)}</string>')
        lines.append(f'    <string name="fn_{n.key}_body">{escape(n.en)}</string>')
    return "\n".join(lines) + "\n"


def rewrite_english() -> None:
    """Rebuild the generated block in the base strings file, leaving the rest alone."""
    path = RES / "values/strings.xml"
    text = path.read_text()
    close = "</resources>"
    assert text.count(close) == 1, f"{path} has {text.count(close)} closing tags"

    start = text.find("    <!-- Field notes -->")
    if start != -1:
        text = text[:start].rstrip("\n") + "\n\n" + close + "\n"
    path.write_text(text.replace(close, english_block().lstrip("\n") + "\n" + close))


def translation_coverage() -> dict[str, set[str]]:
    """Which note keys each shipped language is still missing.

    Read-only. Android falls back to the English string for a missing key, which
    is honest, but it is something the person adding a language needs a list of.
    """
    wanted = {f"fn_{n.key}_title" for n in NOTES} | {f"fn_{n.key}_body" for n in NOTES}
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


keys = [n.key for n in NOTES]
assert len(keys) == len(set(keys)), "duplicate note key"

catalog = (
    ROOT
    / "com/kemalurekli/electricalcalculator/features/fieldnotes/domain/FieldNoteCatalog.kt"
)
catalog.write_text(kotlin())
rewrite_english()

print(f"{len(NOTES)} notes -> {catalog.relative_to(ROOT.parent.parent.parent)}")
for lang, missing in translation_coverage().items():
    state = "complete" if not missing else f"{len(missing)} strings missing"
    print(f"  {lang}: {state}")
