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
    @Environment(\.animateEmotes) private var animateEmotes
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
            AnimatedImageView(url: url, animated: animateEmotes) { size in
                guard size.height > 0, emote.ratio == 1 else { return }
                loadedRatio = LoadedRatio(url: url, ratio: size.width / size.height)
            }
            // A new image view for every image, so that one is never reused to show another.
            .id(url)
            // Zero-width emotes are drawn on top of the preceding emote.
            ForEach(Array(overlays.enumerated()), id: \.offset) { _, overlay in
                AnimatedImageView(
                    url: overlay.urls.url(colorScheme: colorScheme, displayScale: displayScale),
                    animated: animateEmotes
                )
            }
        }
        .frame(width: height * ratio, height: height)
        .accessibilityLabel(([emote] + overlays).map(\.name).joined(separator: " "))
    }
}
