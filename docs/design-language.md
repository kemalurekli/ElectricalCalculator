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
- **Icons.** Material Outlined is the set. Two glyphs will read as foreign on
  iOS — the back arrow, where iOS uses a chevron, and the vertical-ellipsis
  overflow — and should be revisited during the port rather than guessed at now.
- **Illustration.** There is none, and empty states use a single tinted glyph.
