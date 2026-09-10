# VoltageBoard — design language

_Last updated 19 August 2026._

One app, two platforms, the same screen on both. This is the reasoning behind
the tokens; the tokens themselves live in
`core/designsystem/theme/` and are the authority. Where this document and the
code disagree, the code is right and this document is stale.

The audience is whoever is looking at a screen and asking "why is it like
that". Everything below is a decision that was made rather than a default that
was inherited — the defaults are noted where they were rejected, because
knowing what was turned down is most of what a design language is.

---

## The one rule

**Same code, same pixels.** The app does not adapt its look to the platform it
is running on. It has no wallpaper-derived colours, no platform system font, no
Cupertino variants. A user who has it on a phone and a tablet, or who moves from
Android to iOS, should recognise the same product.

This is a deliberate trade. The app gives up feeling native-by-default on iOS in
exchange for being recognisable as itself. It is the right trade for a tool
somebody uses on a job every day and wrong for an app they open twice a year.

Where the *platform* has a genuine convention about behaviour rather than
appearance — hiding the tab bar behind a keyboard, drawing a separator under a
scrolled title bar — the app follows it, because both platforms agree and the
user is right either way.

---

## Colour

`core/designsystem/theme/Color.kt`.

### The palette

A **steel navy** base, the colour of instrument housings and switchgear, and a
**muted copper** accent — the one material every reader of this app handles
daily. Surfaces are neutral-cool greys with no hue cast, so a screen full of
figures reads as paper rather than as tinted glass.

| Role | Light | Dark |
|---|---|---|
| primary | `#14496F` | `#9DC6EE` |
| primaryContainer | `#CFE3F7` | `#0E3C5D` |
| secondary (slate) | `#4A5A6B` | `#B5C4D2` |
| tertiary (copper) | `#8A4B2A` | `#F0B394` |
| background / surface | `#FBFCFD` | `#0E1419` |
| onSurface | `#12181F` | `#E3E8EE` |
| onSurfaceVariant | `#414B55` | `#BAC5D0` |
| outline / outlineVariant | `#6F7B86` / `#C6CED6` | `#85919C` / `#3B444D` |

The dark scheme is not the light one inverted. Its surface is a near-black with
a cool cast rather than the neutral charcoal a generator produces, because an
unlit workshop at night is where it is actually read.

### What the accents mean

Colour on the dashboard encodes *what a section is for*, and encodes only that:

- **primary** — the app's own material: tools, reference, theory, notes
- **secondary** — where the reader's work meets other people's: Projects, Forum
- **tertiary** — built from the reader's own activity: Favourites, History

Assigned any other way it means nothing, and alternating hues at random is what
makes a set of cards look assembled rather than designed.

### Status colours are a separate system

Material has no slot for "this result passes its limits" or "this one is
marginal", so the app carries `success`, `warning` and `info` of its own
(`ElecSemanticColors`).

They are the **loud** half of the palette. Brand colours are muted so they can
sit under a screen of numbers all day; status colours exist to be noticed the
moment a result crosses a limit, and run at full saturation.

That is also how the copper accent and the amber warning stay apart despite
sitting 17° from each other on the wheel: **the accent is 53 % saturated and the
warning is 100 %.** A status may shout; a brand may not. `info` was moved from
blue to teal for the same reason — it used to sit within a few degrees of the
primary, so "this is a note" and "this is the app" were the same colour.

### Contrast

Every foreground/background pair used for text clears **4.5:1** in both schemes;
outlines clear **3:1**. The tightest pair is the light warning at 5.01:1.
Re-check with `scripts/` if the palette moves.

---

## Type

`core/designsystem/theme/Type.kt` and `ElecFonts.kt`.

**Inter** for language, **JetBrains Mono** for figures, both bundled. This is
the single most important thing in this document: `FontFamily.SansSerif`
resolves to Roboto on Android and SF or Helvetica on iOS, and the two have
different metrics — the same paragraph wraps in different places and a column of
figures that lines up on one platform does not on the other. A shared design
language that breaks on its first line is not one.

Inter was chosen for screens at small sizes and because it keeps `1`, `l` and
`I` distinct, which matters when the text reads "l = 30 m" or "IEC 60364-1".
JetBrains Mono is tabular by construction, so a result changing from 5.17 to
18.40 does not shift the digits beside it.

Three weights of each are shipped (Regular, Medium, SemiBold). The files are
static instances subset to the characters the app displays, built by
`scripts/build_fonts.py` — 516 KB for six faces. A character outside the set
falls back to a system face for that run of text, so a forum post in Cyrillic
still reads; it is simply not set in Inter.

**They must be packaged as Android assets, and only `:app` can do it.** Compose
Resources reads every other resource type through a reader that tries the assets
and falls back to the classpath; `Font()` on Android skips the reader and hands
the path to `AssetManager`. A build that ships the fonts on the classpath alone
renders the entire app in Roboto, without an exception or a log line, and looks
fine at a glance. AGP 9's Kotlin Multiplatform library variant has no assets to
add to, so the copy lives in `app/build.gradle.kts`. `ElecFontLoadingTest` is
what stands between that and a release: it measures a comma against a digit
through the real accessors, and no proportional face passes.

### The scale

Material 3's sizes, with two changes:

- **Tracking is tightened.** M3 sets body text at 0.5sp of letter spacing, tuned
  for Roboto, and it is the most recognisable thing about a default Compose app
  after its colours: paragraphs that read slightly airy, the way a consumer
  app's do. Inter is drawn tight and does not need the help. Body and label
  tracking is 0.1–0.2sp.
- **`titleLarge` is 20sp, not 22.** It is the top bar's size and Turkish screen
  titles were a character from being ellipsized.

Everything is in `sp`, so the reader's own font-size setting still scales it.

---

## Space and shape

- **4dp grid.** `ElecSpacing` — `xs` 4, `sm` 8, `md` 12, `lg` 16, `xl` 24,
  `xxl` 32. Screen inset is 16dp; the gap between major sections is 24dp.
  Nothing measures its own padding in raw dp.
- **Corner radii** 6 / 10 / 14 / 20 / 28dp, softer than Material's defaults at
  the large end. That softness is what gives the dashboard cards their calm.
- **No shadows anywhere.** Containers separate by tone plus a 1dp hairline
  outline. At the density of an engineering dashboard, stacked shadows read as
  noise, and tone alone is not enough: in the light scheme a card sits about two
  per cent off the page, which has no findable boundary. The hairline is what
  makes the shape deliberate; the tone still does the grouping.

---

## Components

The rules that are not obvious from the code:

**Screens** are built with `ElecScreenScaffold` — all 43 of them. It owns the
title bar, the window insets, the snackbar host and the width cap. A screen that
wants something else has to say so out loud.

**Width cap.** Content is capped at 840dp on a medium window and 1240dp on an
expanded one, and centred. Full-bleed rows on a tablet strand the trailing
control an uncomfortable reach from the label it belongs to, and paragraphs get
too long to find the start of the next line.

**Top bar** is compact on every screen, never large-title, with a hairline that
fades in as content scrolls under it. The large-title bar is iOS's default for a
root screen; the app declines it, on both platforms, so that one question has
one answer.

**Choices wrap, they never ellipsize.** `ElecOptionSelector` lays options out as
pills in a `FlowRow`. Ellipsis is acceptable in a summary and never in a choice
— a reader cannot pick between options the layout has stopped naming, which is
what a segmented button did to "Alüminyum" in Turkish. No selected-item
checkmark either; that detail is unmistakably Material.

**One empty state.** `ElecEmptyState` covers "this list is empty", "the network
is down" and "this build has no backend", because to the reader those are the
same event: they came for something and it is not here. It takes an optional
action, offered only where doing the thing could change the answer.

**A card is an object; a row is a doorway.** If the entry carries state worth
reading or comparing — a project's supply and when it was last touched, a forum
section's thread count, a glossary entry — it is a card. If it exists only to
open the page behind it — a calculator, a reference, a theory topic, the More
list — it is a row. Rows are separated by `ElecListDivider`, drawn between rows
and never after the last one in a group.

**Three glyphs are drawn rather than taken from Material.** The set is Material
Outlined, and three of its marks meant nothing on iOS: the back arrow, where
every iOS app has ever used a bare chevron; the vertical-ellipsis overflow,
which iOS has never had; and the three-node share graph, against iOS's tray
with an ascending arrow. The one rule forbids a Cupertino variant, so each is
replaced by a **single glyph that is right on iOS and not wrong on Android** —
`ElecChromeIcons`, drawn on Material's own 24 grid at a 2px stroke so they sit
beside the rest of the set. The overflow keeps its ring: `MoreTab` is a bare
horizontal ellipsis, which is also iOS's own More tab, and the two have to stay
apart.

**A leading glyph has to differ between rows.** `ElecListItem` takes an optional
icon, and three lists used to be required to supply one: references drew twenty
identical books, theory twenty identical sigmas, projects a column of folders.
A glyph the same on every row is not an icon, it is a 56dp indent. The slot
takes an icon where the rows genuinely differ, and nothing where they do not.
What the row knows instead goes in `badge` at the trailing edge — kept there so
every title still starts at the same x — or in `caption`, a third line for a
provenance such as the standard a reference transcribes.

**A floating action button needs `spacing.fabClearance` under the list.** It
floats above the content rather than beside it, so without that the last row is
permanently underneath it. Three screens had this and none had noticed, because
it only appears once the list is long enough to reach the bottom.

**Waiting draws the shape of what is coming.** Where the shape is known — the
forum's two lists — `ElecSkeleton*` stands in for it, built from the same cards
and insets as the real rows so nothing moves when the answer lands. Still, not
shimmering: motion is undecided below and a loading state is no place to decide
it. Everything else reads from the device and arrives at once; those keep the
spinner.

**A result goes above the form that produced it,** on every screen that has one.
The circuit editor was the exception and it was the screen where it mattered
most: whether the circuit passed sat under twelve fields.

**A name the user chose is edited in the bar that shows it.** `ElecEditableTitle`
in `ElecScreenScaffold`'s `titleContent`. A project and a circuit used to hold
the name in a field halfway down a form while the bar above showed the
placeholder for it — one fact in two places, and the readable one was not the
one you could change.

**Results** use `ElecResultCard`: the figure in tabular type, coloured by
whether it passes its limits, with a plain-language verdict beneath. The whole
card is a polite live region, so a recalculation is announced rather than left
to be discovered.

---

## Navigation

Five tabs — **Home · Calculators · Projects · Forum · More** — as a bottom bar on
a phone and a rail on a wide window, from one `NavigationSuiteScaffold`
declaration.

Which sections earn a tab is a question about what people open repeatedly, not
about what the app contains: a job in progress, the work itself, and the one
part that changes while you are not looking. Reference tables, the glossary,
theory and saved work are looked up rather than lived in, and live behind More.

Five is also the ceiling both platforms set — a sixth tab does not fit a phone's
width at an accessible label size.

**Tab labels are their own strings**, separate from the screen titles they open.
A tab is about 72dp wide and a label that does not fit wraps to two lines and
throws the bar out of alignment. Turkish uses "Araçlar" where English uses
"Calculators".

**Tab roots draw no back arrow.** The reader did not arrive from somewhere; they
switched tabs, and the bar below is what takes them back.

**Each tab keeps its own back stack**, so returning to a half-written forum
thread finds it half-written. Tapping around the bar does not accumulate history
— one back gesture still leaves the app.

**The tab bar hides while the keyboard is up.** A row of controls behind a
keyboard is a row nobody can reach. On this codebase it also removes an entire
class of inset bug: with the bar gone the content is the whole window again, and
`safeDrawing` is measured against the thing it claims to measure.

---

## Accessibility

Not a section that gets skipped, because a lot of the layout above only makes
sense with it:

- Touch targets are at least 48dp, even where the glyph inside is smaller.
- Composite items are one node: a dashboard card announces "title. badge.
  subtitle" as one target, matching how it behaves for touch.
- Decorative icons are `contentDescription = null` — the text beside them
  already names the thing.
- Results and error states are polite live regions.
- Groups of choices are `selectableGroup()`, so a screen reader can say "2 of
  3".

---

## Things deliberately not decided here

- **Motion.** Beyond the option pill's colour transition and the top-bar
  separator fade, the app has no motion language yet. It should not get one
  until the palette and the structure have been lived with.
- **Illustration.** There is none, and empty states use a single tinted glyph.
