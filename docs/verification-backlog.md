# Verification backlog

Every figure in this app is one of two things: **derived** or **transcribed**.

A derived figure is computed from inputs by a formula, and a unit test can prove it.
Voltage drop, fault current, adiabatic section, AWG diameters — if one of these is
wrong, a test fails.

A transcribed figure is a number read out of a standard, a catalogue or common
practice and typed into a Kotlin file. **No test can prove it right.** A test can only
prove it is self-consistent — that capacity rises with cross-section, that XLPE sits
above PVC, that copper sits above aluminium. A single mistyped digit that respects
those relationships passes every check and ships.

This file is the register of those figures. It is the last gate before release.

---

## How to use this

1. Work top-down. The table is sorted by confidence, **lowest first** — the rows most
   likely to be wrong are the rows that come first.
2. For each row, open the file, read the values against the cited source, and set
   **Status**:
   - `unverified` — nobody has checked it against the source
   - `verified` — checked against the cited edition, correct as written
   - `corrected` — was wrong, has been fixed, and the fix is covered by a test
3. When a figure changes, change the test that locks it in the same commit. The tests
   pin the values deliberately; a value that can change without a test failing is a
   value nobody is guarding.
4. When you add a new transcribed table anywhere in the app, **add a row here**.
   `VerificationBacklogTest` fails the build if a known data file has no row.

### On editions

The values were transcribed against the current IEC editions at the time of writing.
Standards are revised. A row marked `verified` is verified **against a stated edition**
— write the edition in the Notes column when you verify, and re-open the row when that
edition is superseded.

### The one that matters most

`ampacity-aluminium`. An undersized conductor is a fire risk, aluminium is the table
transcribed with the least confidence, and it is the table a user is least likely to
sanity-check by eye because few people carry aluminium ratings in their head.

---

## Register

| ID | Item | Where | Source | Confidence | Status | How to verify |
|---|---|---|---|---|---|---|
| `field-note-claims` | Every field note — the practical claims, not definitions or tabulated figures | `feature/fieldnotes/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/fieldnotes/domain/FieldNoteCatalog.kt` | Written in our own words; no standard transcribed | low | unverified | **One of the two rows here that are not transcription at all** — see `theory-claims` for the other. These are assertions about how installations behave and how work should be done, and that makes them more dangerous than a mistyped table cell, not less: a wrong figure misleads a reader, a wrong rule makes them act on it and feel certain. Read each note against the standard or the practice it leans on, and check the two failure modes a plausible note hides — a claim that is true of one earthing arrangement or one market but stated generally, and a claim that was true of older equipment. Notes carry no `sourceRes`, so provenance lives only in review. Check the Turkish separately: see `turkish-terminology`. |
| `theory-claims` | Every theory topic — the derivations and the conditions each formula holds under | `feature/theory/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/theory/domain` | Written in our own words; standard textbook relationships, no standard transcribed | low | unverified | Two separate things to check, and the second is the riskier. **The arithmetic** in `TheorySolvers.kt` is covered by `TheorySolverTest`, which works a sample out by hand — read those expected figures against your own working rather than trusting that a green test means a right formula. **The prose** is the part no test reaches: each topic states where its formula comes from and, in `assumptionsRes`, when it stops being true. A wrong assumption is worse than a wrong figure, because a reader who acts on it does so confidently — the failure mode to hunt is a condition stated too broadly, such as a rule that holds for one earthing arrangement written as though it held everywhere. Check the Turkish separately: see `turkish-terminology`. |
| `ampacity-aluminium` | Aluminium current-carrying capacities, all methods | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/cablesize/domain/AmpacityTable.kt` | IEC 60364-5-52 Tables B.52.3 (PVC) and B.52.5 (XLPE/EPR) | low | unverified | One row per cross-section, laid out to be read side by side with the printed table. Four columns, in this order: **B1, B2, C, E**. The printed table also carries A1, A2 and F columns that the app does not model, so read the column heading on every pass rather than reading across by position — an off-by-one column is the error this layout is most exposed to. 240 cells in all: 15 cross-sections × 4 methods × 2 insulations × the 2- and 3-loaded-conductor cases. Confirm no row exists below 2.5 mm², a size aluminium is not manufactured in. |
| `conduit-fill-percentages` | 53 % / 31 % / 40 % fill limits | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/conduitfill/domain/ConduitFillModels.kt` | NEC Chapter 9, Table 1 | low | unverified | **Non-IEC source in an otherwise IEC app.** IEC 61386 is a product standard and specifies no fill percentage, so the NEC figures were used as the only widely codified rule. Decide whether this is acceptable for the target market, and confirm the on-screen note says where the rule comes from. |
| `tray-fill-fraction` | 40 % default tray fill | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/trayfill/domain/TrayFillModels.kt` | Industry practice; IEC 61537 gives no figure | low | unverified | Same question as the conduit rule: a convention, not a standard. Confirm the default is presented as a convention the user can change, not as a requirement. |
| `lighting-example-targets` | Illuminance targets in the worked examples (500 lx office, 150 lx warehouse, …) | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/lighting/presentation/LightingExamples.kt` | EN 12464-1 | low | unverified | The calculator itself has **no illuminance table** — the target is a user input, deliberately, because EN 12464-1 is task-specific. Only the worked examples state figures. Check each example's lux against the task it names. |
| `ampacity-copper` | Copper current-carrying capacities, all methods | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/cablesize/domain/AmpacityTable.kt` | IEC 60364-5-52 Tables B.52.2 (PVC) and B.52.4 (XLPE/EPR) | medium | unverified | As `ampacity-aluminium`, with one difference: copper carries the 1.5 mm² row aluminium omits, so it is 16 cross-sections and 256 cells. Higher confidence only because copper ratings are more familiar and a gross error is more likely to be noticed. |
| `correction-ambient` | Ambient temperature factors Ca, PVC and XLPE | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/table/CorrectionFactors.kt` | IEC 60364-5-52 Table B.52.14 | medium | unverified | Check the tabulated 5 K steps. The code interpolates linearly between them, which is accepted practice, and clamps rather than extrapolating — confirm the top of the range matches the table for each insulation. |
| `correction-grouping` | Grouping factors Cg | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/table/CorrectionFactors.kt` | IEC 60364-5-52 Table B.52.17 | medium | unverified | Check each circuit count. Confirm counts above the tabulated range clamp to the last entry rather than continuing toward zero. |
| `mcb-magnetic-bands` | Type B / C / D instantaneous multipliers 5 / 10 / 20 × In | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/earthfault/domain/EarthFaultModels.kt` | IEC 60898-1 | medium | unverified | These are the **upper** limits of each magnetic band, which is the value that must be used for a disconnection check. Confirm the code uses the upper limit and not the band midpoint. |
| `insulation-temperatures` | PVC 70 °C, XLPE/EPR 90 °C conductor limits | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/model/CableModels.kt` | IEC 60502-1 / IEC 60364-5-52 | medium | unverified | Widely known values; check anyway because they drive both the resistivity at operating temperature and the adiabatic check. |
| `insulation-densities` | PVC 1.40, XLPE 0.92 kg/dm³ | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/model/CableModels.kt` | Manufacturer typical values | medium | unverified | Used only for cable weight. Compare a computed weight against a manufacturer catalogue for two or three common cables; the KDoc records one such comparison (PVC copper ≈ 1.44 kg/m against a catalogue 1.4–1.5). |
| `standard-cross-sections` | Preferred conductor cross-sections | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/model/CableModels.kt` | IEC 60228 | medium | unverified | Check the series is complete and in order, and that no non-preferred size has crept in. |
| `reference-tables` | The tabulated content of all 40 reference topics | `feature/references/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/references/domain/ReferenceCatalog.kt` | Per topic — each carries its own `sourceRes` | medium | unverified | The largest row by volume. Every topic states its source on screen; work topic by topic against that source. Protection and earthing topics first, since they are the ones a user acts on. |
| `glossary-definitions` | 117 term definitions | `feature/glossary/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/glossary/domain/GlossaryCatalog.kt` | Written in our own words; IEC 60050 (IEV) consulted, never copied | medium | unverified | Not a transcription problem but a correctness problem: a definition can be well written and still wrong. Check the English definitions for technical accuracy, then the Turkish for terminology (see `turkish-terminology`). |
| `turkish-terminology` | 3144 Turkish strings, across `:app` and the modules that have left it | `app/src/main/res/values-tr/strings.xml` | — | medium | unverified | Needs an electrical engineer who works in Turkish, not a translator. The risk is not mistranslation but *plausible* mistranslation — a term that reads fine and is not what the trade says. |
| `symbol-geometry` | Drawn symbols | `core/designsystem/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/designsystem/symbol` | Drawn to the geometric description in IEC 60617; **not** copied from it | medium | unverified | Visual check only — no test can do this. Open each symbol topic on a device in both light and dark themes. Look for: wrong proportions, missing terminals, a symbol that reads as a different device. |
| `adiabatic-k` | k = 115 / 143 / 76 / 94 | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/earthfault/domain/AdiabaticFactors.kt` | IEC 60364-5-54 Tables 54.2–54.6 | high | unverified | Four values, widely reproduced, easy to check. Confirm each is the *line conductor / protective conductor* case the code claims and not a bunched or bare-conductor variant. |
| `pe-section-table` | S ≤ 16 → S; 16 < S ≤ 35 → 16; S > 35 → S/2 | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/earthfault/domain/AdiabaticFactors.kt` | IEC 60364-5-54 Table 54.7 | high | unverified | Three-line rule, easy to check. Confirm it is offered *alongside* the adiabatic result and not instead of it — the standard permits either route. |
| `voltage-factor-c` | c = 1.05 (max) and 0.95 (min) | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/shortcircuit/domain/ShortCircuitModels.kt` | IEC 60909-0 Table 1, low voltage | high | unverified | Confirm these are the LV values (there are separate HV values) and that c_max pairs with 20 °C while c_min pairs with the conductor at operating temperature. |
| `touch-voltage-limit` | 50 V AC conventional limit | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/earthfault/domain/EarthFaultModels.kt` | IEC 60364-4-41 | high | unverified | Dry-location value. Confirm the on-screen note says so — 25 V applies to some special locations. |
| `conductor-resistivity` | ρ₂₀ 0.017241 / 0.028264 Ω·mm²/m, α 0.00393 / 0.00403, δ 8.89 / 2.70 kg/dm³ | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/model/ConductorMaterial.kt` | IEC 60228 / IEC 60287 | high | unverified | Physical constants, low risk. ρ₂₀ for copper is 1/58 by definition of the IEC reference conductivity; aluminium is 1/35.4. |
| `standard-voltages` | 230 V single phase, 400 V three phase | `core/domain/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/core/domain/model/SystemVoltageDefaults.kt` | IEC 60038 | high | unverified | Defaults only, always editable. Confirm they are right for the target market. |
| `cable-reactance-default` | X = 0.08 Ω/km | `feature/calculators/src/commonMain/kotlin/com/kemalurekli/electricalcalculator/features/calculators/shortcircuit/domain/ShortCircuitModels.kt` | Typical LV cable value | n/a | unverified | **Not a hidden constant** — it is the pre-filled value of an editable input, and the calculator says so. Listed here for completeness. Confirm it stays editable. |

---

## Not on this list, and why

**Derived values.** Voltage drop, fault current, adiabatic cross-section, power,
lumen method, PV string limits, neutral current, energy cost, AWG diameters, cable
weight, conduit and tray occupancy. Each is computed from a formula and pinned by a
worked example plus physical-property tests. If one is wrong, the build is red.

**Formula symbols.** Language-neutral by design and identical in every locale.

**Calculator defaults other than those listed.** Every pre-filled value is visible in
its field and editable; none of them enters a result without the user seeing it.
