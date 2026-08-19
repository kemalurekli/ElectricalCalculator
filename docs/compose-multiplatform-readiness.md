# Moving ElecToolkit to Compose Multiplatform

_Written 19 August 2026, against the tree at the "Build every screen the same
way" commit._

The app is one Gradle module targeting Android. The decision has been taken to
ship iOS from the same Compose UI code. This is the inventory of what stands in
the way, gathered by reading the tree rather than by guessing — so the size of
the job is known before anyone starts it.

**Nothing here is scheduled.** The design work that preceded this document
deliberately stopped at "do not make the problem worse".

---

## What is already portable

- **`core/designsystem/theme/`** — `Color.kt`, `Shape.kt`, `Spacing.kt` and
  `Theme.kt` contain no Android API at all. `Theme.kt` was cleaned out when the
  wallpaper-derived colour scheme was removed; that was the last of it.
- **The data layer's two network dependencies.** supabase-kt and Ktor are
  Kotlin Multiplatform already, which is why they were chosen. DataStore is
  multiplatform too.
- **The domain layer** — catalogs, use cases, formatters, validation — is plain
  Kotlin with no Android imports.
- **Material 3 and the icon set.** `material3`, `material-icons-extended` and
  `material3-adaptive-navigation-suite` all publish multiplatform artifacts, so
  the app shell and every component survive the move.

---

## Real blockers, in order of cost

### 1. Dependency injection — Hilt does not exist off Android

94 files use `dagger.hilt` or `javax.inject`. Hilt is Android-only and has no
multiplatform port. The options are Koin, kotlin-inject, or hand-written
factories. This is the largest single item and it touches every ViewModel.

### 2. Room

12 files. Room has supported KMP since 2.7, so this is a migration rather than a
replacement — but the schema, the DAOs and the migration tests all move with it,
and the app has a migration test suite that must keep passing.

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

## Suggested order

1. **Split the module.** `:core:designsystem` first, since it is nearly clean;
   then `:core:domain`, then data, then features. Do this while still
   Android-only — a module split that compiles is a checkpoint worth having.
2. **Replace Hilt.** The biggest job, and everything downstream depends on it.
3. **Room to KMP**, keeping the migration tests green.
4. **Add the iOS target** to `:core:designsystem` alone and get the previews
   rendering. Nothing else needs to work yet.
5. **Resources**, then `@Preview`, then the `expect`/`actual` platform
   capabilities.
6. **`PlatformTextStyle` and `DateUtils` last**, when there is a device to
   compare against.
