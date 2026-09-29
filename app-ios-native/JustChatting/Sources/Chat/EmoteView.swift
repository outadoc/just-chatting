//
//  EmoteView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct EmoteView: View {
    let emote: Emote
    var overlays: [Emote] = []
    let height: CGFloat

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.displayScale) private var displayScale
    @State private var loadedRatio: LoadedRatio?

    /// Aspect ratio of the image loaded from `url`. Keeping the URL means that a view reused for
    /// another emote doesn't keep the previous one's ratio.
    private struct LoadedRatio: Equatable {
        let url: URL?
        let ratio: CGFloat
    }

    var body: some View {
        let url = emote.urls.url(colorScheme: colorScheme, displayScale: displayScale)
        // Emote.ratio is set explicitly for known non-square emotes; otherwise fall back to the
        // loaded image's aspect ratio.
        let ratio = loadedRatio.flatMap { $0.url == url ? $0.ratio : nil } ?? CGFloat(emote.ratio)
        ZStack {
            AnimatedImageView(url: url) { size in
                guard size.height > 0, emote.ratio == 1 else { return }
                loadedRatio = LoadedRatio(url: url, ratio: size.width / size.height)
            }
            // Zero-width emotes are drawn on top of the preceding emote.
            ForEach(Array(overlays.enumerated()), id: \.offset) { _, overlay in
                AnimatedImageView(url: overlay.urls.url(colorScheme: colorScheme, displayScale: displayScale))
            }
        }
        .frame(width: height * ratio, height: height)
        .accessibilityLabel(([emote] + overlays).map(\.name).joined(separator: " "))
    }
}
