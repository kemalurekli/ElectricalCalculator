import AppKit

// Builds the three 1024px iOS app icons from one description of the mark.
//
// The icon is drawn rather than dropped in as a binary so that a change to the
// palette is a change to two hex values here, and so that the light, dark and
// tinted variants cannot drift apart — they are the same geometry with
// different colours, which is the only way iOS's three appearances stay
// recognisably one icon.
//
// Android does not use this. Its adaptive icon is a pair of vector drawables
// in `app/src/main/res/drawable/`, carrying the same path at the scale the
// 108dp layer needs; a raster there would only be resampled by the launcher.
//
//     swiftc -O -o /tmp/build_icon scripts/build_icon.swift
//     /tmp/build_icon iosApp/iosApp/Assets.xcassets/AppIcon.appiconset
//
// Full bleed on purpose: both platforms apply their own mask, and a corner
// radius baked in here would show up as a second, wrong rounding inside it.
/// One appearance: iOS asks for light, dark and tinted, and rejects a set
/// where any of them is missing.
struct Variant { let name: String; let bg: NSColor; let fg: NSColor }

func color(_ hex: UInt32) -> NSColor {
    NSColor(srgbRed: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
}

// Six vertices in a 0..100 box: two arms meeting at a waist. Kept as data so
// the Android vector path can be derived from the same numbers.
let bolt: [(Double, Double)] = [(62, 4), (22, 54), (46, 54), (38, 96), (78, 44), (54, 44)]

func draw(_ v: Variant, side: Int, to url: URL) {
    let rep = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: side, pixelsHigh: side,
                               bitsPerSample: 8, samplesPerPixel: 4, hasAlpha: true,
                               isPlanar: false, colorSpaceName: .deviceRGB,
                               bytesPerRow: side * 4, bitsPerPixel: 32)!
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: rep)
    let s = Double(side)
    v.bg.setFill()
    NSRect(x: 0, y: 0, width: s, height: s).fill()

    // Sixty per cent of the side. Larger starts to crowd the mask on iOS;
    // smaller stops reading at the sizes that matter.
    let scale = (s * 0.60) / 92.0
    let ox = s / 2 - 50 * scale
    let oy = s / 2 - 50 * scale
    let path = NSBezierPath()
    for (i, p) in bolt.enumerated() {
        // NSBezierPath counts y upwards and the design coordinates downwards.
        let pt = NSPoint(x: ox + p.0 * scale, y: s - (oy + p.1 * scale))
        i == 0 ? path.move(to: pt) : path.line(to: pt)
    }
    path.close()
    v.fg.setFill()
    path.fill()

    NSGraphicsContext.restoreGraphicsState()
    try! rep.representation(using: .png, properties: [:])!.write(to: url)
}

let out = URL(fileURLWithPath: CommandLine.arguments[1])
let variants = [
    Variant(name: "icon-light", bg: color(0x14496F), fg: color(0xCFE3F7)),
    Variant(name: "icon-dark",  bg: color(0x0E1419), fg: color(0x9DC6EE)),
    Variant(name: "icon-tinted", bg: color(0x1A1A1A), fg: color(0xE8E8E8)),
]
for v in variants {
    draw(v, side: 1024, to: out.appendingPathComponent("\(v.name).png"))
    print("wrote \(v.name).png")
}
// The mark has to survive 40pt on an iPhone home screen and 48dp in an Android
// launcher. Rendered small here so that is checked at build time rather than
// discovered on a device.
draw(variants[0], side: 80, to: out.appendingPathComponent("icon-small.png"))
