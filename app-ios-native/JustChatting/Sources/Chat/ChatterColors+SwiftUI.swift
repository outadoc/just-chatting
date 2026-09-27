//
//  ChatterColors+SwiftUI.swift
//  JustChatting
//

import JCShared
import SwiftUI

extension ChatterColors {
    /// Color of `chatter`'s name: the one they picked if it can be made readable, or one from the
    /// fallback palette otherwise. Same rules, and same fallback pick, as in the Compose app.
    func color(for chatter: Chatter, hex: String?, colorScheme: ColorScheme) -> Color {
        let background = UIColor.systemBackground.argb(in: colorScheme)
        if let color = accessibleColor(hexColor: hex, background: background) {
            return Color(argb: color.int32Value)
        }
        let palette = Color.chatterPalette
        return palette[Int(fallbackIndex(chatter: chatter, paletteSize: Int32(palette.count)))]
    }
}
