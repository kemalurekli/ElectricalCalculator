import AVFoundation
let a = AVURLAsset(url: URL(fileURLWithPath: CommandLine.arguments[1]))
print("duration", CMTimeGetSeconds(a.duration), "readable", a.isReadable, "playable", a.isPlayable)
for t in a.tracks { print(" track", t.mediaType.rawValue, t.naturalSize, "fps", t.nominalFrameRate, "range", CMTimeGetSeconds(t.timeRange.duration), "fmt", t.formatDescriptions) }
