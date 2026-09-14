// frame <movie> <seconds> <out.png> — one frame of a movie, for looking at clips.
import AVFoundation
import AppKit

let args = CommandLine.arguments
let asset = AVURLAsset(url: URL(fileURLWithPath: args[1]))
let gen = AVAssetImageGenerator(asset: asset)
gen.requestedTimeToleranceBefore = .zero
gen.requestedTimeToleranceAfter = CMTime(value: 1, timescale: 10)
gen.appliesPreferredTrackTransform = true
let t = CMTime(seconds: Double(args[2])!, preferredTimescale: 600)
let cg = try gen.copyCGImage(at: t, actualTime: nil)
let rep = NSBitmapImageRep(cgImage: cg)
try rep.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: args[3]))
print("\(cg.width)x\(cg.height)  duration \(CMTimeGetSeconds(asset.duration))s")
