//
//  EmoteView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-23.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SDWebImageSwiftUI
import SwiftUI

struct EmoteView: View {
    var emote: Emote
    var onClick: () -> Void

    var size: CGFloat = 24

    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        let url = emote.urls.getBestUrl(
            screenDensity: Float(UIScreen.main.scale),
            isDarkTheme: colorScheme == .dark
        )

        Button(
            action: onClick,
            label: {
                AnimatedImage(url: URL(string: url)!)
                    .resizable()
                    .frame(width: size, height: size)
            }
        )
    }
}

extension EmoteUrls {
    func getBestUrl(screenDensity: Float, isDarkTheme: Bool) -> String {
        let list = isDarkTheme ? dark : light
        let best = list.min { a, b in
            screenDensity - a.key.floatValue < screenDensity - b.key.floatValue
        }
        return best!.value
    }
}
