# Promo video

`voltageboard-promo.mp4` — 1920 × 1080, 30 fps, 71 s, silent. Not committed
(38 MB); rebuild it:

    cd store/video
    swiftc -O -o render render.swift && ./render "$PWD" voltageboard-promo.mp4

Needs Xcode's toolchain (`DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`
if `xcode-select` points at the command-line tools).

## What is in it

| Scene | Source | Caption |
|---|---|---|
| title card | `bg/title.png` | VoltageBoard — Elektrik hesaplayıcıları, adımlarıyla. |
| clip1 | search → voltage drop → worked example → result → steps | Her adımı gör. |
| clip2 | the calculator list, scrolled | 20 hesaplayıcı, tek yerde. |
| clip3 | theory: voltage divider, diagram and prose | Formülü değil, sebebini öğren. |
| clip4 | a project: add a circuit, watch it get sized | Panoyu tasarla. |
| clip5 | a reference with its source line | Kaynağı yazılı tablolar. |
| clip6 | a forum thread | Meslektaşlarına sor. |
| end card | `bg/end.png` | Çevrimdışı çalışır. 12 dil. |

## How it was made

- **Clips**: `adb shell screenrecord --size 720x1600` on the Pixel_8 emulator
  with the status bar in demo mode (9:00, full signal), driven by `adb shell
  input`. screenrecord writes a frame only when the screen changes, so every
  clip ends at its last change; the renderer holds the final frame for the
  rest of the scene.
- **Backgrounds**: `bg/*.html`, rendered by headless Chrome at 1920 × 1080 —
  the app's own Inter and its blue, the same layout as the store screenshots.
  The phone's screen area is left blank for the clip.
- **Assembly**: `render.swift` draws every frame with Core Graphics — background,
  then the clip inside the screen rectangle with rounded corners and the status
  bar cropped away — and hands it to AVAssetWriter. Scenes dissolve over 0.4 s;
  the whole thing fades from and to black.

`frame.swift` and `probe.swift` are the two tools used to look at clips
without ffmpeg: one frame as PNG, and a track's duration and format.

## Uploading

YouTube → unlisted, **monetisation off**, embedding allowed, no age
restriction. Paste the watch URL into Play Console → Store listing → Video.
