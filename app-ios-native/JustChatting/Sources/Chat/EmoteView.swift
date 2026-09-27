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
    @State private var loadedRatio: CGFloat?

    var body: some View {
        // Emote.ratio is set explicitly for known non-square emotes; otherwise fall back to the
        // loaded image's aspect ratio.
        let ratio = loadedRatio ?? CGFloat(emote.ratio)
        ZStack {
            AnimatedImageView(url: emote.urls.url(colorScheme: colorScheme, displayScale: displayScale)) { size in
                guard size.height > 0, emote.ratio == 1 else { return }
                loadedRatio = size.width / size.height
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
