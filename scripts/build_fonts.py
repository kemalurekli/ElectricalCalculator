#!/usr/bin/env python3
"""Builds the app's bundled font files from the upstream variable fonts.

The app ships its own typefaces so that an Android build and an iOS build
render the same screen the same way. `FontFamily.SansSerif` resolves to Roboto
on one platform and to SF/Helvetica on the other, which means the same text
wraps in different places — a shared design language that breaks on its first
line is not one.

What this does, per weight:

  1. **Instantiates** the variable font at that weight, producing a static file.
     Static instances rather than one variable file because Compose
     Multiplatform's font loading is uniform for static faces across targets,
     while variable-axis support is not; the point of this exercise is not to
     find out on iOS which axes survived.
  2. **Subsets** it to the characters this app can actually display. The full
     Inter covers scripts the app has no translations for. Dropping them takes
     the family from 876 KB to roughly a tenth of that.

Run it only when the character set or the upstream fonts change; the outputs
are committed, so an ordinary build never needs Python or a network.

    python3 -m venv .fontenv && ./.fontenv/bin/pip install fonttools
    ./.fontenv/bin/python scripts/build_fonts.py <download-dir> <res-font-dir>

Upstream sources (both SIL Open Font License 1.1):
    https://github.com/google/fonts/tree/main/ofl/inter
    https://github.com/google/fonts/tree/main/ofl/jetbrainsmono
"""

from __future__ import annotations

import sys
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

# What the app can put on screen.
#
# Three contiguous ranges plus a hand-written list, rather than every block an
# engineering app might conceivably touch. Taking the whole of Greek, Latin
# Extended-B, the arrows and the maths operators cost 192 KB per face for a few
# hundred glyphs nothing renders.
#
# A character outside this set is not lost: both platforms fall back to a system
# face for the run of text containing it. That matters because forum posts are
# written by people, and a post in Cyrillic must still be readable — it will
# simply be set in the platform's own font rather than in Inter.
UNICODE_RANGES = [
    (0x0020, 0x007E),  # ASCII printable
    (0x00A0, 0x00FF),  # Latin-1 Supplement — accented Latin, °, ², ³, µ, ×, ÷
    (0x0100, 0x017F),  # Latin Extended-A — Turkish ğĞıİşŞ, and European names
]

# Everything else the app actually displays, named one at a time so that adding
# a glyph is a deliberate act rather than a block import.
EXTRA_CHARACTERS = (
    # Greek used as engineering notation, not as language
    "ΔΘΛΞΠΣΦΩ"
    "αβγδεηθλμνπρστφχψω"
    # Punctuation the copy is written with
    "‐‑‒–—―"  # hyphens and dashes
    "‘’‚“”„"  # curly quotes
    "†‡•…‰′″"  # dagger, bullet, ellipsis, ‰, ′, ″
    "   ​"  # narrow/figure/thin/zero-width spaces
    # Superscripts and subscripts — mm², m³, cos φ₁
    "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻ⁿ"
    "₀₁₂₃₄₅₆₇₈₉₋"
    # Currency — the energy-cost calculator
    "€₺₹₤"  # € ₺ ₹ ₤
    # Letterlike and units
    "Ω№℃℉K"  # Ω (ohm sign) № ℃ ℉ K
    # Arrows used in flow text and diagrams
    "←↑→↓↔⇒"
    # Mathematics
    "∂∆∏∑−∓√∞∫"
    "≈≠≡≤≥≪≫⊕⋅"
    # Shapes used as list markers and status glyphs
    "■□▲▶▼◆○●✓✗"
)

# The weights the type scale asks for, and nothing more. Each one is a file, so
# an unused weight is dead bytes in every install.
#
# The monospace family needs three, not two: the result figure is SemiBold, the
# secondary rows and table cells are Medium, and rendered formulas are Regular.
INTER_WEIGHTS = {400: "inter_regular", 500: "inter_medium", 600: "inter_semibold"}
MONO_WEIGHTS = {
    400: "jetbrains_mono_regular",
    500: "jetbrains_mono_medium",
    600: "jetbrains_mono_semibold",
}

# Inter carries an optical-size axis as well as weight. UI text is set between
# 11 and 32 sp, so the axis is pinned at the size most of the app's text
# actually is rather than left to a default that suits neither headline nor
# caption.
INTER_OPSZ = 16


def unicode_set() -> set[int]:
    codepoints = {cp for lo, hi in UNICODE_RANGES for cp in range(lo, hi + 1)}
    codepoints.update(ord(ch) for ch in EXTRA_CHARACTERS)
    return codepoints


def build(source: Path, out: Path, axes: dict[str, float]) -> None:
    font = TTFont(source)
    # Names are left as the variable font's defaults. Rewriting them means
    # consulting the STAT table for a name at the pinned weight, and JetBrains
    # Mono has no named value at 600 — which is the weight the result figure is
    # set in. The outlines are what render; nothing loads these by family name,
    # because Android resolves them by resource id and Compose Resources by file.
    instantiateVariableFont(font, axes, inplace=True, updateFontNames=False)

    options = subset.Options()
    # Layout features are kept: Inter's kerning and its tabular-figure feature
    # are the reasons to bundle it rather than any nearby grotesque.
    options.layout_features = ["*"]
    options.name_IDs = ["*"]
    options.notdef_outline = True
    options.recalc_bounds = True
    # `glyf` hinting is dropped; Android renders these unhinted anyway and it is
    # a measurable share of the file.
    options.hinting = False

    subsetter = subset.Subsetter(options=options)
    subsetter.populate(unicodes=unicode_set())
    subsetter.subset(font)

    out.parent.mkdir(parents=True, exist_ok=True)
    font.save(out)
    font.close()


def main(argv: list[str]) -> int:
    if len(argv) != 3:
        print(__doc__)
        return 2

    downloads, res_font = Path(argv[1]), Path(argv[2])
    total_in = total_out = 0

    jobs = [
        (downloads / "Inter-var.ttf", INTER_WEIGHTS, lambda w: {"wght": w, "opsz": INTER_OPSZ}),
        (downloads / "JetBrainsMono-var.ttf", MONO_WEIGHTS, lambda w: {"wght": w}),
    ]

    for source, weights, axes_for in jobs:
        total_in += source.stat().st_size
        for weight, name in weights.items():
            out = res_font / f"{name}.ttf"
            build(source, out, axes_for(weight))
            size = out.stat().st_size
            total_out += size
            print(f"  {out.name:<28} {weight:>4}  {size / 1024:6.1f} KB")

    print(f"\n{total_in / 1024:.0f} KB of variable fonts in, {total_out / 1024:.0f} KB of static faces out")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
