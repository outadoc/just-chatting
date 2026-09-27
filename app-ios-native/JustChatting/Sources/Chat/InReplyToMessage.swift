//
//  InReplyToMessage.swift
//  JustChatting
//

import SwiftUI

struct InReplyToMessage: View {
    let mentions: [String]
    let message: String?

    var body: some View {
        let mentionText = mentions.map { "@\($0)" }.joined(separator: " ")
        let text = message.map { ": \($0)" } ?? ""
        Label(mentionText + text, systemImage: "arrowshape.turn.up.left.fill")
            .font(.caption)
            .foregroundStyle(.secondary)
            .lineLimit(2)
            .frame(maxWidth: .infinity, alignment: .leading)
    }
}
