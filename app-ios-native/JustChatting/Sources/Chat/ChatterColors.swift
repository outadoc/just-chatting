//
//  ChatterColors.swift
//  JustChatting
//

import JCShared
import SwiftUI

enum ChatterColors {
    /// The chatter's own color, or a fallback from the palette if they haven't picked one.
    static func color(for chatter: Chatter, hex: String?) -> Color {
        if let color = Color(hex: hex) {
            return color
        }
        let palette = Color.chatterPalette
        // Stable per-chatter pick; String.hashValue is randomized per launch, so hash manually.
        let seed = chatter.id.unicodeScalars.reduce(UInt32(5381)) { ($0 &* 33) &+ $1.value }
        return palette[Int(seed % UInt32(palette.count))]
    }
}
