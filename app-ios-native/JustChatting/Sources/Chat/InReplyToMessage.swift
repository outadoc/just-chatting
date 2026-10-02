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
        Label {
            HStack(alignment: .firstTextBaseline, spacing: 3) {
                ForEach(Array(mentions.enumerated()), id: \.offset) { _, mention in
                    let isMentionOfAppUser = appUserLogin.map {
                        MessageTokenKt.isMentionOf(mention: mention, login: $0)
                    } ?? false
                    MentionText(text: "@\(mention)", isMentionOfAppUser: isMentionOfAppUser)
                }

                if let message {
                    Text(verbatim: ": \(message)")
                        .lineLimit(2)
                }
            }
        } icon: {
            Image(systemName: "arrowshape.turn.up.left.fill")
        }
        .font(.caption)
        .foregroundStyle(.secondary)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
