//
//  AppColors.swift
//  JustChatting
//

import SwiftUI

/// Colors from the asset catalog. Asset symbol generation is disabled in the project, so they
/// are looked up by name here, and only here.
extension Color {
    /// Live indicators and badges.
    static let live = Color("Live")
    /// Backgrounds for warnings, like connection issues or canceled streams.
    static let warning = Color("Warning")
    /// Content drawn on top of a tinted background, like `live` or `warning`.
    static let onTint = Color("OnTint")

    /// Fallback colors for chatters who haven't picked one, mirroring `CustomColors.kt`.
    static let chatterPalette: [Color] = (1...15).map { Color("Chatter\($0)") }

    /// Accent colors of highlighted messages, by level (1 to 10).
    static func highlightLevel(_ level: Int) -> Color {
        Color("HighlightLevel\(level)")
    }
}
