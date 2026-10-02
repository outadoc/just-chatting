//
//  MentionText.swift
//  JustChatting
//

import SwiftUI

/// An `@login` mention. Mentions of the app user are drawn as a tonal pill, like in Compose.
struct MentionText: View {
    let text: String
    let isMentionOfAppUser: Bool

    var body: some View {
        if isMentionOfAppUser {
            Text(verbatim: text)
                .fontWeight(.bold)
                .foregroundStyle(Color.accentColor)
                .padding(.horizontal, 6)
                .padding(.vertical, 1)
                .background(Color.accentColor.opacity(0.2), in: Capsule())
        } else {
            // Keeps the inherited foreground style, which differs in message bodies and replies
            Text(verbatim: text)
                .fontWeight(.bold)
        }
    }
}
