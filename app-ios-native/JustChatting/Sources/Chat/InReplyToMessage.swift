//
//  InReplyToMessage.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct InReplyToMessage: View {
    let mentions: [String]
    let message: String?
    var appUserLogin: String? = nil

    var body: some View {
        // A single Text, so that the message flows right after the mentions and wraps under them.
        Label {
            Text(attributedText)
        } icon: {
            Image(systemName: "arrowshape.turn.up.left.fill")
        }
        .font(.caption)
        .foregroundStyle(.secondary)
        .lineLimit(2)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var attributedText: AttributedString {
        var result = AttributedString()

        for (index, mention) in mentions.enumerated() {
            if index > 0 {
                result.append(AttributedString(" "))
            }

            var mentionText = AttributedString("@\(mention)")
            let isMentionOfAppUser = appUserLogin.map {
                MessageTokenKt.isMentionOf(mention: mention, login: $0)
            } ?? false

            // Text can't draw a rounded background, so this is the closest to the pill in MentionText.
            if isMentionOfAppUser {
                mentionText.inlinePresentationIntent = .stronglyEmphasized
                mentionText.swiftUI.foregroundColor = Color.accentColor
                mentionText.swiftUI.backgroundColor = Color.accentColor.opacity(0.2)
            }

            result.append(mentionText)
        }

        if let message {
            result.append(AttributedString(": \(message)"))
        }

        return result
    }
}
