//
//  InReplyToView.swift
//  JustChatting
//

import SwiftUI

struct InReplyToView: View {
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

#Preview {
    VStack(spacing: 12) {
        InReplyToView(mentions: ["djessy728"], message: "Salut Antoine, est tu encore en contact avec Mathieu?")
        InReplyToView(
            mentions: ["djessy728", "hiccoz"],
            message: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin."
        )
        InReplyToView(mentions: ["djessy728"], message: nil)
    }
    .padding()
}
