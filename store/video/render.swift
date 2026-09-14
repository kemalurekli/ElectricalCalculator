// Assembles the promo video, frame by frame.
//
// Each scene is a still background (rendered by Chrome) and, for the app
// scenes, a screen recording drawn into the phone's screen rectangle with
// rounded corners. Scenes dissolve into each other over 0.4 s; the whole thing
// fades from and to black. No audio.
//
// Frame-by-frame rather than AVMutableComposition because the composition
// API's layer instructions cannot clip to a rounded rectangle or hold a
// clip's last frame past its end, and both are needed here — screenrecord
// stops writing frames when the screen stops changing, so every clip is
// shorter than its scene.
//
//   swiftc -O -o render render.swift && ./render <dir> <out.mp4>

import AVFoundation
import AppKit
import CoreImage

let fps = 30
let W = 1920, H = 1080
let screen = CGRect(x: 1212, y: 112, width: 476, height: 1058)   // top-left origin
let cornerRadius: CGFloat = 48
let dissolve = 0.4
let fadeEdge = 0.5

struct Scene {
    let bg: String          // bg/<name>.png
    let clip: String?       // clips/<name>.mp4
    let duration: Double
    let clipOffset: Double  // seconds of the clip to skip at the start
}

let scenes: [Scene] = [
    Scene(bg: "title", clip: nil,     duration: 3.0,  clipOffset: 0),
    Scene(bg: "s1",    clip: "clip1", duration: 13.0, clipOffset: 0.6),
    Scene(bg: "s2",    clip: "clip2", duration: 9.5,  clipOffset: 0.4),
    Scene(bg: "s3",    clip: "clip3", duration: 11.0, clipOffset: 0.4),
    Scene(bg: "s4",    clip: "clip4", duration: 14.0, clipOffset: 0.4),
    Scene(bg: "s5",    clip: "clip5", duration: 11.0, clipOffset: 0.4),
    Scene(bg: "s6",    clip: "clip6", duration: 7.0,  clipOffset: 3.2),
    Scene(bg: "end",   clip: nil,     duration: 3.5,  clipOffset: 0),
]

let dir = URL(fileURLWithPath: CommandLine.arguments[1])
let out = URL(fileURLWithPath: CommandLine.arguments[2])

func loadPNG(_ name: String) -> CGImage {
    let url = dir.appendingPathComponent("bg/\(name).png")
    let src = CGImageSourceCreateWithURL(url as CFURL, nil)!
    return CGImageSourceCreateImageAtIndex(src, 0, nil)!
}

/// Walks a clip forwards, handing out the frame that is current at a time.
/// Holds the last frame once the clip has run out.
final class ClipReader {
    private let reader: AVAssetReader
    private let output: AVAssetReaderTrackOutput
    private let ci = CIContext(options: [.useSoftwareRenderer: false])
    private var pending: (time: Double, image: CGImage)?
    private var current: CGImage?
    private var finished = false

    init(_ name: String) throws {
        let asset = AVURLAsset(url: dir.appendingPathComponent("clips/\(name).mp4"))
        let track = asset.tracks(withMediaType: .video).first!
        reader = try AVAssetReader(asset: asset)
        output = AVAssetReaderTrackOutput(track: track, outputSettings: [
            kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA,
        ])
        output.alwaysCopiesSampleData = false
        reader.add(output)
        reader.startReading()
    }

    private func next() -> (Double, CGImage)? {
        guard !finished, let sb = output.copyNextSampleBuffer(),
              let pb = CMSampleBufferGetImageBuffer(sb) else { finished = true; return nil }
        let t = CMTimeGetSeconds(CMSampleBufferGetPresentationTimeStamp(sb))
        let img = ci.createCGImage(CIImage(cvPixelBuffer: pb), from: CGRect(x: 0, y: 0,
            width: CVPixelBufferGetWidth(pb), height: CVPixelBufferGetHeight(pb)))!
        return (t, img)
    }

    func frame(at t: Double) -> CGImage? {
        if pending == nil, let n = next() { pending = (n.0, n.1) }
        while let p = pending, p.time <= t {
            current = p.image
            if let n = next() { pending = (n.0, n.1) } else { pending = nil }
        }
        return current ?? pending?.image
    }
}

// -- Writer -------------------------------------------------------------------

try? FileManager.default.removeItem(at: out)
let writer = try AVAssetWriter(outputURL: out, fileType: .mp4)
let input = AVAssetWriterInput(mediaType: .video, outputSettings: [
    AVVideoCodecKey: AVVideoCodecType.h264,
    AVVideoWidthKey: W, AVVideoHeightKey: H,
    AVVideoCompressionPropertiesKey: [
        AVVideoAverageBitRateKey: 12_000_000,
        AVVideoProfileLevelKey: AVVideoProfileLevelH264HighAutoLevel,
        AVVideoMaxKeyFrameIntervalKey: fps * 2,
    ],
])
input.expectsMediaDataInRealTime = false
let adaptor = AVAssetWriterInputPixelBufferAdaptor(assetWriterInput: input, sourcePixelBufferAttributes: [
    kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA,
    kCVPixelBufferWidthKey as String: W, kCVPixelBufferHeightKey as String: H,
])
writer.add(input)
writer.startWriting()
writer.startSession(atSourceTime: .zero)

// -- Compositing --------------------------------------------------------------

let colorSpace = CGColorSpaceCreateDeviceRGB()

func makeContext() -> CGContext {
    CGContext(data: nil, width: W, height: H, bitsPerComponent: 8, bytesPerRow: W * 4, space: colorSpace,
              bitmapInfo: CGImageAlphaInfo.premultipliedFirst.rawValue | CGBitmapInfo.byteOrder32Little.rawValue)!
}

/// Draws one scene at a local time into a fresh image. Core Graphics has its
/// origin at the bottom left, so the screen rectangle is flipped once here.
func composite(_ scene: Scene, reader: ClipReader?, at local: Double, bg: CGImage) -> CGImage {
    let ctx = makeContext()
    ctx.draw(bg, in: CGRect(x: 0, y: 0, width: W, height: H))
    if let reader = reader, let frame = reader.frame(at: local + scene.clipOffset) {
        let r = CGRect(x: screen.minX, y: CGFloat(H) - screen.maxY, width: screen.width, height: screen.height)
        ctx.saveGState()
        ctx.addPath(CGPath(roundedRect: r, cornerWidth: cornerRadius, cornerHeight: cornerRadius, transform: nil))
        ctx.clip()
        ctx.interpolationQuality = .high
        // The status bar is hidden under the bezel, as in the store
        // screenshots: the frame is drawn taller than the screen and its top
        // 64 px (of 1600) fall outside the clip.
        let hidden = 64.0 / 1600.0
        let tall = r.height / (1 - hidden)
        ctx.draw(frame, in: CGRect(x: r.minX, y: r.minY, width: r.width, height: tall))
        ctx.restoreGState()
    }
    return ctx.makeImage()!
}

let total = scenes.reduce(0) { $0 + $1.duration }
let frameCount = Int(total * Double(fps))
var readers: [Int: ClipReader] = [:]
var backgrounds: [Int: CGImage] = [:]
var lastOfPrevious: CGImage?      // the previous scene's final frame, for the dissolve
var sceneIndex = 0
var sceneStart = 0.0

print("scenes \(scenes.count), \(String(format: "%.1f", total)) s, \(frameCount) frames")

for n in 0..<frameCount {
    let t = Double(n) / Double(fps)
    while sceneIndex + 1 < scenes.count, t >= sceneStart + scenes[sceneIndex].duration {
        lastOfPrevious = composite(scenes[sceneIndex], reader: readers[sceneIndex],
                                   at: scenes[sceneIndex].duration, bg: backgrounds[sceneIndex]!)
        readers[sceneIndex] = nil
        sceneStart += scenes[sceneIndex].duration
        sceneIndex += 1
    }
    let scene = scenes[sceneIndex]
    if backgrounds[sceneIndex] == nil { backgrounds[sceneIndex] = loadPNG(scene.bg) }
    if let clip = scene.clip, readers[sceneIndex] == nil { readers[sceneIndex] = try ClipReader(clip) }
    let local = t - sceneStart

    let frame = composite(scene, reader: readers[sceneIndex], at: local, bg: backgrounds[sceneIndex]!)

    var pb: CVPixelBuffer?
    CVPixelBufferPoolCreatePixelBuffer(nil, adaptor.pixelBufferPool!, &pb)
    CVPixelBufferLockBaseAddress(pb!, [])
    let ctx = CGContext(data: CVPixelBufferGetBaseAddress(pb!), width: W, height: H, bitsPerComponent: 8,
                        bytesPerRow: CVPixelBufferGetBytesPerRow(pb!), space: colorSpace,
                        bitmapInfo: CGImageAlphaInfo.premultipliedFirst.rawValue | CGBitmapInfo.byteOrder32Little.rawValue)!
    let full = CGRect(x: 0, y: 0, width: W, height: H)
    ctx.draw(frame, in: full)
    // Dissolve: the previous scene lingers on top and fades away.
    if local < dissolve, let prev = lastOfPrevious {
        ctx.setAlpha(CGFloat(1 - local / dissolve))
        ctx.draw(prev, in: full)
        ctx.setAlpha(1)
    }
    // Fade from black at the very start, to black at the very end.
    let edge = min(t, total - t)
    if edge < fadeEdge {
        ctx.setFillColor(CGColor(red: 0, green: 0, blue: 0, alpha: CGFloat(1 - edge / fadeEdge)))
        ctx.fill(full)
    }
    CVPixelBufferUnlockBaseAddress(pb!, [])

    while !input.isReadyForMoreMediaData { Thread.sleep(forTimeInterval: 0.005) }
    adaptor.append(pb!, withPresentationTime: CMTime(value: CMTimeValue(n), timescale: CMTimeScale(fps)))
    if n % 300 == 0 { print("  \(n)/\(frameCount)") }
}

input.markAsFinished()
let done = DispatchSemaphore(value: 0)
writer.finishWriting { done.signal() }
done.wait()
print(writer.status == .completed ? "wrote \(out.path)" : "failed: \(String(describing: writer.error))")
