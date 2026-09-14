#!/usr/bin/env python3
"""Play Store screenshots: headline + phone, 1080x1920, rendered by Chrome.

One HTML page per shot, the app's own Inter face and palette, and a phone
frame the raw emulator capture sits in. The status bar is hidden under the
bezel so every image shows the same thing: the app, from its own title bar
down.

    python3 store/screenshots/compose.py en           # all eight into play/en/
    python3 store/screenshots/compose.py tr 3 7       # just those, Turkish
    python3 store/screenshots/compose.py en-tablet    # 7-inch tablet frame

Raw captures live in raw/<lang>/ (1080x2400, the Pixel_8 emulator at 420 dpi,
the app switched to that language with `adb shell cmd locale set-app-locales`).
To change a slogan, edit SHOTS and re-run; to change a screen, re-capture with
`adb shell screencap` and drop the file in raw/<lang>/. Chrome is the renderer
because it is the only thing on this machine that lays out text properly
and reads the app's own .ttf files without a library.
"""
from __future__ import annotations

import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
FONTS = os.path.join(REPO, "core/designsystem/src/commonMain/composeResources/font")
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

# (raw capture, headline, sub-line), per store language
SHOTS = {
    "en": [
        ("01-vd.png",       "Calculate on site.",            "The result, the limit that applies and the verdict — no connection needed."),
        ("02-steps.png",    "See every step.",               "Formula, intermediate values and units, as you would on paper."),
        ("07-calcs.png",    "20 calculators, one place.",    "Power, protection, cables, motors, lighting, solar, EV charging."),
        ("03-theory.png",   "Learn the why, not just the formula.", "24 theory topics: diagram, derivation and the conditions it holds under."),
        ("08-quiz.png",     "Test yourself.",                "Hundreds of questions across the topics; type an answer, check it at once."),
        ("04-reftable.png", "Tables that cite their source.", "IEC and EN references — every row names the standard it came from."),
        ("05-project.png",  "Design the board.",             "Add a circuit; size, protective device and voltage drop come by themselves."),
        ("06-thread.png",   "Ask people in the trade.",      "A forum for faults, protection, wiring and standards."),
    ],
    "tr": [
        ("01-vd.png",       "Sahada hesapla.",              "Sonuç, standardın sınırı ve kararıyla birlikte — bağlantı gerekmez."),
        ("02-steps.png",    "Her adımı gör.",               "Formül, ara değerler ve birimler; kâğıtta çözer gibi."),
        ("07-calcs.png",    "20 hesaplayıcı, tek yerde.",   "Güç, koruma, kablo, motor, aydınlatma, güneş, EV şarj."),
        ("03-theory.png",   "Formülü değil, sebebini öğren.", "24 teori konusu: şema, türetme ve geçerlilik koşulları."),
        ("08-quiz.png",     "Kendini dene.",                "Her konuda yüzlerce soru; cevabını yaz, anında kontrol et."),
        ("04-reftable.png", "Kaynağı yazılı tablolar.",     "IEC ve EN referansları — her satırın hangi standarttan geldiği belli."),
        ("05-project.png",  "Panoyu tasarla.",              "Devre ekle; kesit, koruma cihazı ve gerilim düşümü kendiliğinden gelsin."),
        ("06-thread.png",   "Meslektaşlarına sor.",         "Türkçe forum: arıza, koruma, tesisat, standartlar."),
    ],
}

STATUS_BAR_PX = 96   # hidden under the bezel

# The device the raw captures came from. Play wants tablet screenshots in the
# same 9:16 canvas, so the only thing that changes is the frame: a 7-inch
# tablet is 1600x2560 at 320 dpi (`adb shell wm size 1600x2560; wm density
# 320` on the phone emulator — the app lays itself out by dp and grows a
# navigation rail), and its screen is 5:8 rather than 9:20.
DEVICES = {
    "phone":  dict(capture_w=1080, screen_w=848, radius_outer=88, radius_inner=64, pad=26, width=900, status=96),
    "tablet": dict(capture_w=1600, screen_w=900, radius_outer=56, radius_inner=36, pad=24, width=948, status=112),
    # Landscape, 2560x1600 at 320 dpi, on a 16:9 canvas with the caption above
    # the screen rather than beside it. Chromebooks are landscape machines and
    # a portrait phone frame there would be the wrong shape twice over.
    "chromebook": dict(capture_w=2560, screen_w=1560, radius_outer=28, radius_inner=14, pad=18, width=1596, status=104, landscape=True),
}

PAGE = """<!doctype html>
<html lang="{lang}"><head><meta charset="utf-8">
<style>
@font-face {{ font-family: Inter; font-weight: 600; src: url("file://{fonts}/inter_semibold.ttf"); }}
@font-face {{ font-family: Inter; font-weight: 500; src: url("file://{fonts}/inter_medium.ttf"); }}
@font-face {{ font-family: Inter; font-weight: 400; src: url("file://{fonts}/inter_regular.ttf"); }}
* {{ margin: 0; padding: 0; box-sizing: border-box; }}
html, body {{ width: 1080px; height: 1920px; overflow: hidden; }}
body {{
  font-family: Inter, system-ui, sans-serif;
  background:
    radial-gradient(ellipse 900px 700px at 50% 108%, #d6e6f6 0%, rgba(214,230,246,0) 70%),
    linear-gradient(180deg, #f7f9fb 0%, #eef3f8 100%);
  color: #14496F;
  position: relative;
}}
.brand {{
  position: absolute; left: 96px; top: 84px;
  font-size: 30px; font-weight: 500; letter-spacing: .02em; color: #4A5A6B;
}}
.brand b {{ color: #14496F; font-weight: 600; }}
.brand .n {{ color: #8a97a5; font-weight: 400; margin-left: 14px; }}
h1 {{
  position: absolute; left: 96px; right: 96px; top: 168px;
  font-size: 84px; line-height: 1.06; font-weight: 600; letter-spacing: -0.022em;
  color: #14496F;
}}
p {{
  position: absolute; left: 96px; right: 96px; top: {sub_top}px;
  font-size: 36px; line-height: 1.32; font-weight: 400; color: #4A5A6B;
}}
.phone {{
  position: absolute; left: 50%; top: {phone_top}px; transform: translateX(-50%);
  width: {frame_w}px; height: 1980px;
  background: #1b1f24; border-radius: {r_outer}px;
  padding: {pad}px;
  box-shadow: 0 60px 120px -30px rgba(20,73,111,.45), 0 20px 40px -20px rgba(0,0,0,.35);
}}
.phone::before {{
  content: ""; position: absolute; inset: 0; border-radius: {r_outer}px;
  box-shadow: inset 0 0 0 3px rgba(255,255,255,.08);
}}
.screen {{
  width: 100%; height: 100%; border-radius: {r_inner}px; overflow: hidden;
  background: #f7f9fb; position: relative;
}}
.screen img {{
  position: absolute; left: 0; top: -{status}px; width: {screen_w}px; height: auto;
}}
</style></head>
<body>
  <div class="brand"><b>VoltageBoard</b></div>
  <h1>{headline}</h1>
  <p>{sub}</p>
  <div class="phone"><div class="screen"><img src="file://{shot}"></div></div>
</body></html>
"""


LANDSCAPE = """<!doctype html>
<html lang="{lang}"><head><meta charset="utf-8">
<style>
@font-face {{ font-family: Inter; font-weight: 600; src: url("file://{fonts}/inter_semibold.ttf"); }}
@font-face {{ font-family: Inter; font-weight: 500; src: url("file://{fonts}/inter_medium.ttf"); }}
@font-face {{ font-family: Inter; font-weight: 400; src: url("file://{fonts}/inter_regular.ttf"); }}
* {{ margin: 0; padding: 0; box-sizing: border-box; }}
html, body {{ width: 1920px; height: 1080px; overflow: hidden; }}
body {{
  font-family: Inter, system-ui, sans-serif;
  background:
    radial-gradient(ellipse 1400px 700px at 50% 112%, #d6e6f6 0%, rgba(214,230,246,0) 70%),
    linear-gradient(180deg, #f7f9fb 0%, #eef3f8 100%);
  color: #14496F; position: relative;
}}
.brand {{ position: absolute; left: 160px; top: 64px; font-size: 26px; font-weight: 600; color: #14496F; }}
h1 {{ position: absolute; left: 160px; top: 118px; font-size: 68px; line-height: 1.06; font-weight: 600; letter-spacing: -0.022em; }}
p  {{ position: absolute; left: 160px; top: 206px; width: 1600px; font-size: 30px; line-height: 1.3; font-weight: 400; color: #4A5A6B; }}
.phone {{
  position: absolute; left: 50%; top: 292px; transform: translateX(-50%);
  width: {frame_w}px; height: 1200px; background: #1b1f24; border-radius: {r_outer}px; padding: {pad}px;
  box-shadow: 0 50px 100px -30px rgba(20,73,111,.45), 0 20px 40px -20px rgba(0,0,0,.35);
}}
.phone::before {{ content: ""; position: absolute; inset: 0; border-radius: {r_outer}px; box-shadow: inset 0 0 0 3px rgba(255,255,255,.08); }}
.screen {{ width: 100%; height: 100%; border-radius: {r_inner}px; overflow: hidden; background: #f7f9fb; position: relative; }}
.screen img {{ position: absolute; left: 0; top: -{status}px; width: {screen_w}px; height: auto; }}
</style></head>
<body>
  <div class="brand">VoltageBoard</div>
  <h1>{headline}</h1>
  <p>{sub}</p>
  <div class="phone"><div class="screen"><img src="file://{shot}"></div></div>
</body></html>
"""


def main() -> None:
    lang = sys.argv[1] if len(sys.argv) > 1 else "en"
    device = lang.split("-", 1)[1] if "-" in lang else "phone"
    dev = DEVICES[device]
    raw_dir = os.path.join(HERE, "raw", lang)
    out = os.path.join(HERE, "play", lang)
    os.makedirs(out, exist_ok=True)
    only = sys.argv[2:]  # optional: indices to rebuild
    lang = lang.split("-")[0]
    for i, (raw, headline, sub) in enumerate(SHOTS[lang], start=1):
        if only and str(i) not in only:
            continue
        if not os.path.exists(os.path.join(raw_dir, raw)):
            continue
        # Two-line headlines push the sub-line and the phone down together.
        two_lines = len(headline) > 22
        sub_top = 380 if two_lines else 290
        phone_top = 520 if two_lines else 430
        # The capture is 1080 wide and sits in an 848 px screen: scale
        # the hidden status bar by the same ratio.
        status = round(dev["status"] * dev["screen_w"] / dev["capture_w"])
        template = LANDSCAPE if dev.get("landscape") else PAGE
        size = "1920,1080" if dev.get("landscape") else "1080,1920"
        html = template.format(
            fonts=FONTS, index=i, lang=lang, headline=headline.replace("<br>", " ") if dev.get("landscape") else headline, sub=sub,
            shot=os.path.join(raw_dir, raw), sub_top=sub_top, phone_top=phone_top,
            status=status, frame_w=dev["width"], pad=dev["pad"],
            r_outer=dev["radius_outer"], r_inner=dev["radius_inner"], screen_w=dev["screen_w"],
        )
        page = os.path.join(HERE, f".{i:02d}.html")
        png = os.path.join(out, f"{i:02d}.png")
        with open(page, "w", encoding="utf-8") as f:
            f.write(html)
        subprocess.run([
            CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
            "--force-device-scale-factor=1", f"--window-size={size}",
            "--no-sandbox", "--disable-logging", f"--screenshot={png}",
            "file://" + page,
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        os.remove(page)
        print(png)


if __name__ == "__main__":
    main()
