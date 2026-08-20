# Moving ElecToolkit to Compose Multiplatform

_Written 19 August 2026. Revised the same day with the toolchain verified by
building it, and with a correction — see "What the first draft missed"._

The app is one Gradle module targeting Android. The decision has been taken to
ship iOS from the same Compose UI code. This is the inventory of what stands in
the way, so the size of the job is known before anyone starts it.

---

## The toolchain, verified

Not researched — **compiled.** A throwaway KMP module was built against
`iosSimulatorArm64` with the app's entire dependency set, at the versions the
project already uses:

| Dependency | Version | iOS |
|---|---|---|
| Kotlin | 2.3.21 (current) | ✅ |
| Compose Multiplatform | 1.11.1 | ✅ |
| `material3`, `material-icons-extended`, `components.resources` | via CMP | ✅ |
| `material3-adaptive-navigation-suite` | 1.12.0-alpha03 | ✅ the tab shell ports |
| `org.jetbrains.androidx.navigation:navigation-compose` | 2.9.2 | ✅ |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.0 | ✅ |
| Koin (`core`, `compose-viewmodel`) | 4.2.2 | ✅ |
| Room | **2.8.4 — already in use** | ✅ full iOS variants |
| DataStore | **1.2.1 — already in use** | ✅ |
| supabase-kt (`postgrest`, `auth`) | **3.1.4 — already in use** | ✅ |
| Ktor Darwin engine | **3.1.3 — already in use** | ✅ |
| kotlinx-datetime | 0.8.0 (new) | ✅ |

`BUILD SUCCESSFUL`. **No version bumps are required** — Room, DataStore,
supabase-kt and Ktor are already at versions that publish iOS artifacts, which
is a consequence of having chosen multiplatform libraries when the forum was
built.

Two notes. Room's iOS variants are on **Google's Maven**, not Maven Central, so
`google()` must be in `dependencyResolutionManagement`. And CMP's library
artifacts are versioned independently of the CMP plugin — `1.12.0-alpha03`
libraries under plugin `1.11.1` is normal, not a mistake.

---

## What is already portable

- **`core/designsystem/theme/`** — `Color.kt`, `Shape.kt`, `Spacing.kt` and
  `Theme.kt` contain no Android API at all. `Theme.kt` was cleaned out when the
  wallpaper-derived colour scheme was removed; that was the last of it.
- **The domain layer** — catalogs, use cases, validation — is plain Kotlin.
- **Material 3, the icon set and the app shell**, per the table above.

---

## What the first draft missed

This document's first draft looked for `android.*` imports and Compose APIs and
declared the domain layer "plain Kotlin with no Android imports". That was true
and beside the point: **it never looked for the JVM standard library**, which is
just as absent from Kotlin/Native.

| API | Files | Where |
|---|---|---|
| `java.util.Locale` | 37 | Everywhere; most are `uppercase(Locale)` and mechanical |
| `java.time.*` | 12 | Domain models, repositories, formatting |
| `java.text` + `java.math` | 1 | **`NumberFormatter.kt`** — `DecimalFormat`, `BigDecimal` |
| `java.text.Collator` / `Normalizer` | 2 | `SearchNormalizer`, `GlossaryViewModel` |
| `java.security` | 1 | `GoogleCredentialProvider` (deferred with the forum) |

Concentrated rather than scattered, which is the good news: **all number
formatting is in one file**, and that file is on the critical path of the first
screen to be ported. The first problem to solve is therefore both the most
necessary and the most representative.

### The trap inside NumberFormatter

`NumberFormatterTest` asserts `format(2.345, decimals = 2) == "2.35"`. As a
double, `2.345` is `2.34499999999999997…`, so arithmetic rounding yields
`2.34`. The JDK's `DecimalFormat` gets `2.35` because it rounds the double's
**shortest round-tripping decimal representation** — `"2.345"` — not its exact
binary value.

Any reimplementation has to do the same or it will silently change results
across the whole app. The approach is therefore string-based decimal rounding
of `value.toString()`, in common Kotlin, with `Locale` reduced to the two
characters that actually vary — the decimal and grouping separators.

Writing it in common code rather than as `expect`/`actual` over `DecimalFormat`
and `NSNumberFormatter` is deliberate: two platform formatters would be free to
disagree about the same number, and "same code, same pixels" is the one rule the
design language has.

---

## Real blockers, in order of cost

### 1. Dependency injection — Hilt does not exist off Android

94 files use `dagger.hilt` or `javax.inject`. Hilt is Android-only and has no
multiplatform port. The options are Koin, kotlin-inject, or hand-written
factories. This is the largest single item and it touches every ViewModel.

### 2. Room

12 files, and cheaper than it looks: **2.8.4 — the version already in the
project — publishes full iOS variants**, so this is a migration rather than a
replacement and needs no upgrade. The schema, the DAOs and the migration tests
move with it, and that migration suite must stay green.

### 3. `PlatformTextStyle(includeFontPadding = false)`

`core/designsystem/theme/Type.kt`, one call, inside `elecTextStyle`. The
`includeFontPadding` constructor is Android-only; the common `PlatformTextStyle`
has no such parameter.

Deleting it is one line. The reason it is still there is that it changes the
vertical metrics of every piece of text in the app, so it wants to be done
deliberately and looked at, not swept along inside a migration.

### 4. `android.text.format.DateUtils`

`features/home/presentation/HomeScreen.kt`, in `toRelativeTime()`. Renders "2
minutes ago" with the platform's own plural rules for the current locale, which
is a lot of correctness for one call.

There is no common equivalent. The replacement is `kotlinx-datetime` plus plural
string resources per language — which means writing the plural rules the
platform is currently supplying for free, in every locale the app ships.

### 5. `LocalContext` — 41 usages

Almost all of them are in feature code doing genuinely Android things: sharing a
result via `Intent`, copying to the clipboard, writing a PDF, reading the
licence texts out of assets, resolving the current `Activity` for Credential
Manager. These need `expect`/`actual` implementations, one per capability, not a
mechanical swap.

`core/designsystem` has none, which is the part that matters.

### 6. `@Preview`

39 files import `androidx.compose.ui.tooling.preview.Preview`. Compose
Multiplatform has its own `org.jetbrains.compose.ui.tooling.preview.Preview`. An
import swap, but it is 39 files of it.

---

## What is *not* a blocker, despite appearances

### `stringResource(R.string.x)` — 62 files

The plan this work followed proposed stripping `stringResource` out of the
design-system components and passing every label in as a parameter, on the
grounds that it made `core/designsystem` "resource-free and therefore movable".

**That was wrong, and it was not done.** Compose Multiplatform ships
`org.jetbrains.compose.resources`, whose `stringResource(Res.string.x)` is a
one-for-one replacement for the Android one. The migration is an import change
and a resource-directory move, not an API redesign.

Doing it the other way would have made the API worse in exchange for nothing:
`ElecSearchBar` would take a placeholder and a clear-button label,
`ElecListItem` two favourite labels, `ElecStates` three more — a dozen new
parameters at every call site, so that a mechanical change could be avoided
later. The strings stay where they are.

The one caveat is that `values-tr/` and `values/` become
`composeResources/values/` and the accessor becomes generated — so the existing
`StringResourceIntegrityTest`, which checks the two locales stay in step, needs
rewriting against the new layout.

### `R.font.*` — one file

`core/designsystem/theme/ElecFonts.kt`, and it is the only file in the design
system that names an Android resource. That is deliberate: `R.font.inter_regular`
becomes `Res.font.inter_regular`, five lines change, and the type scale next
door — where the design decisions actually live — moves untouched.

---

## The order, as agreed

The first draft suggested splitting every module while still Android-only and
adding iOS at step 4. That was rejected in favour of proving iOS works first,
on the grounds that a month of restructuring against an unproven target is a
month you cannot get back if the target does not hold.

**1. The proof — the Converter screen on the iOS simulator.**
Six files, no Android APIs, one dependency-free use case, seventeen
`stringResource` calls, and it uses the whole design system. Porting it
exercises the module setup, Compose Multiplatform, the palette and bundled
fonts, Compose Resources, Koin, the KMP ViewModel and the iOS shell — in one
screen. The modules it needs (`:core:common` → `:core:designsystem` →
`:feature:converter`) are the first three of the layered structure, so the proof
is not a detour; it is the foundation with something running on top of it.

`NumberFormatter` is the one hard piece and it lands here.

**2. Shared infrastructure.** `:core:domain` (`java.time` → `kotlinx-datetime`),
`:core:database` (Room to KMP), `:core:datastore`, `:core:data` (Ktor Darwin
engine on iOS), and one `expect`/`actual` per platform capability — sharing,
clipboard, locale, file export.

**3. Features**, fewest dependencies first: glossary, references, theory, field
notes → favourites, history → calculators (129 files, the largest single lump
but a repeating pattern) → projects → home, more and the navigation shell.

**4. Forum and the store.** Last, because it holds the only platform-specific
authentication. Credential Manager has no iOS counterpart, and **App Store
guideline 4.8 requires Sign in with Apple wherever third-party sign-in is
offered** — Supabase supports Apple, so the work is in the iOS client and the
dashboard. `SchedulePdf.kt` (Core Graphics instead of `android.graphics.pdf`)
is independent and can ship later than the first iOS release.

Throughout: **Android keeps working.** There is a published app; a port that
breaks it to make progress is not making progress.

## Where this has got to

Step 1 is done: the converter runs on the simulator in both languages, and
`NumberFormatter` was harder than expected — see the trap above.

Step 2 is done, `SchedulePdf` excepted — that one is Core Graphics work and
belongs with the projects screen. `:core:common`, `:core:domain`,
`:core:database`, `:core:datastore` and `:core:data` all build for iOS, and
`HistoryRepositoryIosTest` puts a record through Room on a simulator and reads
it back.

`RegionProvider` returns a country code now rather than a `java.util.Locale` —
every caller read `.country` off it anyway, so the narrower type says what it is
for and drops a JVM type from shared code in the same move.

`ResultSharing` stopped being an object taking a `Context` and became
`rememberResultSharing()`, obtained from composition. Neither platform can share
from nothing: Android needs a `Context`, iOS needs the view controller to
present from. Putting the split at the implementation removed the same
dependency from 34 call sites, and both sides answer the copy-confirmation
question honestly — Android says no from 13 onwards because the system draws its
own clipboard preview, iOS says yes because it draws nothing.

Step 3 has begun, and not in the order the plan named. History went before the
read-only screens because it is the most *informative* — the first screen that
reads the database, so it puts the whole of step 2 on screen rather than each
layer passing its own test separately. The glossary went next because it is the
root of the feature dependency tree: field notes, theory and favourites all
reach into `glossary.domain`, and nothing reaches into them from below.

Field notes followed the glossary, and were the first move that had nothing new
in it: the generator retargeted, the strings decoded, the `@StringRes Int`
fields renamed. Four screens run on iOS now, under a real tab bar.

One trap, which the next generated catalogue will hit too. The screen's own
strings share the `fn_` prefix with the generated ones, so stripping the
generated block by prefix took nine hand-written strings with it — invisible in
Turkish, because those had been copied to the module already, and visible only
in English. Check what a prefix sweep actually matched before deleting.

### What the glossary settled

**Catalogues carry `StringResource`, not `@StringRes Int`.** An `R.string` id is
an Android build artefact, and it was in 48 places. `GlossaryTerm` now holds
handles, and `scripts/gen_glossary.py` writes the catalogue and the English
strings into the module together — so the next regeneration cannot undo it.

Two traps came with that. Compose Resources does not read `aapt`'s escapes, so
`\'` reaches the screen as a backslash; the generator's `escape()` now does XML
and nothing else. And `formatted="false"` is gone — it exists to stop `aapt`
reading a bare `%` as a specifier, and Compose Resources does no formatting
unless the caller passes arguments.

**`StringResolver` split in two.** The multiplatform half is in `:core:common`
and takes a `StringResource`. The Android half, `ResourceIdResolver`, adds the
integer overload — so the number of ViewModels injecting *that* is a running
count of the migration left, and the type is deleted when the count is zero.

**Collation, grapheme boundaries and upper-casing are language questions.** All
three are `expect`/`actual` in `:core:common` now. The JDK's `Collator` and
Foundation's `compare(options:range:locale:)` both implement the Unicode
collation algorithm, so the two platforms file *Çalışma* in the same place
without either being handed a list of rules — the simulator lists *Açık, Açma,
Adyabatik, Akım, Aktif*, which is ç < d < k and then ı < t.

## The calculators, and everything that had to move with them

The plan called this the largest single lump and expected it late. It came
earlier because everything else was waiting behind it: theory shares
`WorkedExample` and `CalculationStep` with the calculators, favourites reads
theory and references, and references and the calculators used to reach into
each other.

That last one had to be untangled first. `CorrectionFactors` went down to
`:core:domain` — it is table data with no feature of its own, and two features
read it — which turned a cycle into a line. Then references, then the
calculators, then theory in the same breath, because the shared worked-solution
card cannot carry `StringResource` for one caller and `Int` for the other.

`:app` is 72 files now. It was 318.

### What is left in `:app`, and why

Favourites, home and more followed immediately — favourites because it reads
every catalogue and all five had finally moved, home and more because the route
types and the five top-level destinations went into `:core:navigation`.

`ElecNavHost` did not go with them, and cannot: it names every feature's entry
composable, so a module holding it would depend on all of them. `ElecAppShell`
stayed with it, because the shell calls the graph.

Projects went next, and took the circuit designer and the inspection feature
with it — three packages that only make sense together, and nothing else in the
app uses either of the first two.

`ElecNavHost` and `ElecAppShell` then moved to `:shell` — the one module that
depends on every feature, which is why it is not under `core/`: `core` is what
features build on, and this is what builds on features.

The forum and the settings screen are slots rather than dependencies.
`:app` passes them in through `platformDestinations`; iOS passes nothing.
`hasPlatformScreens` is one flag with two effects because it is one fact: a
platform without those screens must show neither the Forum tab nor the settings
gear, and a tab pointing at a route with no destination throws rather than doing
nothing.

**iOS runs the same shell Android does.** There is no separate iOS UI left.

The forum went last, as the plan always said it would. Its data layer was never
the problem — supabase-kt and Ktor are multiplatform, and the threads, replies
and reports are plain suspend calls. Signing in was, and `ForumSignIn` is the
seam: Android asks Credential Manager for a Google ID token; iOS reports itself
unconfigured, so the forum is readable there and not postable.

`ForumConfig` is generated from `local.properties` into shared code, because
`BuildConfig` is an Android build artefact and useless to the iOS half. The anon
key is public by design and the Google *web* client id is already in the shipped
APK; the `service_role` key is not there, has never been in this repository, and
must not be.

**`:app` is 18 files.** It was 318. Fifteen of them are Hilt modules,
`MainActivity`, the Application class, and the settings screen. Nothing else in
the app is Android-only any more.

### Hilt is gone

`:app` is **8 files**: the Application, `MainActivity`, `MainViewModel`, the
settings screen and its ViewModel, the language repository, the Android
destinations, and one Koin module for the two things that genuinely need
Android — the device region from `LocaleManager` and the per-app language
through `AppCompatDelegate`.

Nine Hilt modules and the entry point that bridged them are deleted. Both
platforms now start the same graph: `coreCommonModule`, `coreDataModule`, and
one module per feature.

Two checks replace what Hilt gave at compile time. `KoinGraphTest` verifies that
everything registered can be built; `RegisteredViewModelsTest` verifies that
nothing was left out. The second was written after three of the forum's
ViewModels shipped unregistered — they sit on sections rather than screens, so
they are reached by a scroll rather than a tap, and no manual sweep found them.

`ResourceIdResolver` is deleted too, which was its own success condition: the
number of ViewModels injecting it was the count of screens that could not leave
`:app`, and it reached zero.

### What is genuinely left

**Sign in with Apple.** App Store guideline 4.8 requires an equivalent option
beside any third-party sign-in, so this gates the forum on iOS rather than being
optional. Supabase supports the provider; the work is in the iOS client and the
dashboard.

**The Supabase client on iOS.** `ForumConfig` has the values now, but nothing
builds a client from them there. It is a few lines once sign-in exists.

**The schedule PDF on iOS**, via Core Graphics. Independent of everything else.

**The settings screen**, which renders two of the forum's sections and so waits
on the same work.

### The schedule PDF is not on iOS, deliberately

`SchedulePdf` draws with `android.graphics.pdf`: a table with measured column
widths, wrapped cells and page breaks. Core Graphics can do all of it, but that
is a rewrite rather than a port. `isSchedulePdfSupported` is false on iOS and
the menu item is absent — not disabled, because a greyed-out item invites the
reader to work out what unlocks it and nothing does. The CSV export works on
both and carries the same figures.

`FileSharing` is the other half, and takes bytes. The Android version used to
take a `ScheduleReport` and an `OutputStream` so the PDF could be drawn
straight into the file — the right trade when both halves lived in one module,
but a `ScheduleReport` is a projects concept and a stream is a JVM type.

### The check Hilt used to make

Five missing Koin registrations were found the same way: open the app, tap
through to a screen, read `NoDefinitionFoundException` out of logcat. That is
not a process. `KoinGraphTest` runs `Module.verify()` over every feature
module, which walks each definition's constructor by reflection and reports
what is unbuildable — the compile-time guarantee Hilt gave, bought back as a
unit test.

`formatAsRelativeTime` is the third and last of the deliberate divergences,
after dates and collation: "3 dakika önce", "yesterday", "2 weeks ago". Both
platforms ship the plural rules and the thresholds; writing our own would mean
getting Turkish, Arabic and Russian right unaided.

### Three things this taught that the smaller moves did not

**A generated Koin module is only correct at the moment it is generated.** Two
ViewModels arrived after `CalculatorsModule` was written, and neither was a
compile error — Koin resolves by type at runtime, so the screens threw
`NoDefinitionFoundException` the first time anyone opened them. Hilt made that
a compile error. `CalculatorsModuleTest` gets it back by reading the sources.

**`Math.toDegrees(acos(pf))` does not survive a regex.** Replacing it with a
multiplication caught the first closing parenthesis rather than the matching
one and produced `acos(pf * DEGREES_PER_RADIAN)` — which compiles, runs, and
is wrong by a factor no reviewer would spot in a diff. `TheorySolverTest`
failed on the phase angle, which is what those tests are for.

**A test fake has to lie the same way the real thing does.** The module's
`StringResolver` fake appended its arguments instead of substituting them, so
every assertion about *which* string was asked for passed and the ones about
what came out failed. It uses `formatPositional` now, the same as production.

### The one thing worth knowing before porting the next feature

Android has one object graph, not two. A ported feature's Koin module names the
repositories it needs, and on iOS `coreDataModule` builds them; registering that
same module on Android would build a **second** `ElecToolkitDatabase` over the
same file, and two connections with their own write-ahead logs is how a saved
calculation goes missing. `core/di/HiltBridgeModule.kt` is the answer: a Hilt
`@EntryPoint` that hands Koin the objects Hilt already made. Each feature that
moves adds a line to it; the file goes when Hilt does.
