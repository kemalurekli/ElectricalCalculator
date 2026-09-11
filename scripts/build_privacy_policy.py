#!/usr/bin/env python3
"""Turns docs/privacy-policy.md into the page that gets uploaded.

The policy is written and reviewed as Markdown, but the app links to an HTML
address and both stores want a URL rather than a repository file. Keeping the
conversion here means the published page cannot drift from the reviewed text:
edit the Markdown, run this, upload the result.

Not a general Markdown engine, deliberately. It handles exactly what this one
document uses — headings, paragraphs, bold, italic, links, bullet lists and one
table — and raises on anything it does not recognise rather than silently
dropping it. A privacy policy that quietly loses a sentence on its way to being
published is the failure worth guarding against.

    python3 scripts/build_privacy_policy.py            # writes build/privacypolicy.html
    python3 scripts/build_privacy_policy.py <path>     # writes where you say
"""

from __future__ import annotations

import html
import os
import re
import sys

SOURCE = "docs/privacy-policy.md"
DEFAULT_OUTPUT = "build/privacypolicy.html"

# Inline spans, applied in this order. Links first: a link's text may contain
# bold, and its URL must not be scanned for underscores.
LINK = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
BOLD = re.compile(r"\*\*([^*]+)\*\*")
ITALIC = re.compile(r"(?<!\*)\*([^*]+)\*(?!\*)")
UNDERSCORE_ITALIC = re.compile(r"(?<!\w)_([^_]+)_(?!\w)")
CODE = re.compile(r"`([^`]+)`")


def inline(text: str) -> str:
    """Escapes the text, then puts the few tags this document uses back."""
    out = html.escape(text, quote=False)
    out = CODE.sub(lambda m: f"<code>{m.group(1)}</code>", out)
    out = LINK.sub(
        lambda m: f'<a href="{html.escape(m.group(2), quote=True)}">{m.group(1)}</a>',
        out,
    )
    out = BOLD.sub(lambda m: f"<strong>{m.group(1)}</strong>", out)
    out = ITALIC.sub(lambda m: f"<em>{m.group(1)}</em>", out)
    out = UNDERSCORE_ITALIC.sub(lambda m: f"<em>{m.group(1)}</em>", out)
    return out


def convert(markdown: str) -> tuple[str, str]:
    """Returns the page title and its body."""
    # Comments are notes to whoever edits the policy, not to whoever reads it.
    markdown = re.sub(r"<!--.*?-->", "", markdown, flags=re.S)

    title = ""
    body: list[str] = []
    paragraph: list[str] = []
    bullets: list[str] = []
    table: list[list[str]] = []

    def flush_paragraph() -> None:
        if paragraph:
            body.append("<p>" + inline(" ".join(paragraph)) + "</p>")
            paragraph.clear()

    def flush_bullets() -> None:
        if bullets:
            items = "".join(f"<li>{inline(b)}</li>" for b in bullets)
            body.append(f"<ul>{items}</ul>")
            bullets.clear()

    def flush_table() -> None:
        if not table:
            return
        head, rows = table[0], table[1:]
        # The alignment row markdown requires, which carries no content.
        rows = [r for r in rows if not all(set(c) <= set("-: ") for c in r)]
        head_html = "".join(f"<th>{inline(c)}</th>" for c in head)
        rows_html = "".join(
            "<tr>" + "".join(f"<td>{inline(c)}</td>" for c in r) + "</tr>"
            for r in rows
        )
        body.append(
            f"<table><thead><tr>{head_html}</tr></thead><tbody>{rows_html}</tbody></table>"
        )
        table.clear()

    def flush_all() -> None:
        flush_paragraph()
        flush_bullets()
        flush_table()

    for raw in markdown.split("\n"):
        line = raw.rstrip()

        if not line.strip():
            flush_all()
            continue

        if line.startswith("#"):
            flush_all()
            level = len(line) - len(line.lstrip("#"))
            text = line[level:].strip()
            if level == 1 and not title:
                title = text
            body.append(f"<h{level}>{inline(text)}</h{level}>")
            continue

        if line.startswith("|"):
            flush_paragraph()
            flush_bullets()
            table.append([c.strip() for c in line.strip("|").split("|")])
            continue

        if line.startswith("- "):
            flush_paragraph()
            flush_table()
            bullets.append(line[2:].strip())
            continue

        if line.startswith("  ") and bullets:
            # A bullet wrapped onto the next line.
            bullets[-1] += " " + line.strip()
            continue

        # Bullet syntax, blockquotes and fenced or indented code. A line that
        # merely *starts* with emphasis is a paragraph, which is why this looks
        # for the space after the star rather than the star.
        if line.startswith(("* ", "> ", "```", "    ")):
            raise SystemExit(f"build_privacy_policy: unhandled Markdown: {line!r}")

        flush_bullets()
        flush_table()
        paragraph.append(line.strip())

    flush_all()
    return title or "Privacy Policy", "\n".join(body)


# Deliberately one file with no external anything: no font, no stylesheet, no
# script. It has to render on a ten-year-old phone in a store reviewer's hand
# with a bad connection, and every request it does not make is one that cannot
# fail.
PAGE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<style>
  :root {{ color-scheme: light dark; }}
  body {{
    margin: 0 auto; padding: 2rem 1.25rem 4rem; max-width: 42rem;
    font: 16px/1.65 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #14181d; background: #fbfcfd;
  }}
  h1 {{ font-size: 1.7rem; line-height: 1.25; margin: 0 0 .25rem; }}
  h2 {{ font-size: 1.15rem; margin: 2.25rem 0 .5rem; }}
  p, li {{ margin: .7rem 0; }}
  a {{ color: #1c4f7c; }}
  code {{ font-size: .9em; background: #eceff3; padding: .1em .35em; border-radius: 3px; }}
  table {{ border-collapse: collapse; width: 100%; margin: 1rem 0; font-size: .95rem; }}
  th, td {{ text-align: left; vertical-align: top; padding: .5rem .6rem; border-bottom: 1px solid #d8dde3; }}
  th {{ background: #f1f4f7; }}
  @media (prefers-color-scheme: dark) {{
    body {{ color: #e6eaef; background: #14181d; }}
    a {{ color: #7fb2e0; }}
    code {{ background: #232a32; }}
    th {{ background: #1d232a; }}
    th, td {{ border-bottom-color: #2b333c; }}
  }}
</style>
</head>
<body>
{body}
</body>
</html>
"""


def main() -> None:
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    source = os.path.join(root, SOURCE)
    output = sys.argv[1] if len(sys.argv) > 1 else os.path.join(root, DEFAULT_OUTPUT)

    with open(source, encoding="utf-8") as handle:
        title, body = convert(handle.read())

    os.makedirs(os.path.dirname(output), exist_ok=True)
    with open(output, "w", encoding="utf-8") as handle:
        handle.write(PAGE.format(title=html.escape(title, quote=False), body=body))

    print(f"{output}  ({os.path.getsize(output)} bytes)")


if __name__ == "__main__":
    main()
