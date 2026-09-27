//
//  EmoteUrls+ColorScheme.swift
//  JustChatting
//

import JCShared
import SwiftUI

extension EmoteUrls {
    /// The URL best suited to this screen, picked by the same rules as in the Compose app.
    func url(colorScheme: ColorScheme, displayScale: CGFloat) -> URL? {
        URL(string: getBestUrl(screenDensity: Float(displayScale), isDarkTheme: colorScheme == .dark))
    }
}
