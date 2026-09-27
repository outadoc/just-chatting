//
//  RichEmbedView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct RichEmbedView: View {
    let embed: ChatListItemRichEmbed

    @Environment(\.openURL) private var openURL

    var body: some View {
        Button {
            if let url = URL(string: embed.requestUrl) {
                openURL(url)
            }
        } label: {
            HStack(spacing: 8) {
                AsyncImage(url: URL(string: embed.thumbnailUrl)) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.secondary.opacity(0.2)
                }
                .frame(width: 96, height: 54)
                .clipShape(RoundedRectangle(cornerRadius: 6))

                VStack(alignment: .leading, spacing: 2) {
                    Text(embed.title)
                        .font(.callout.weight(.semibold))
                        .lineLimit(2)
                    Text(embed.authorName)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                Spacer(minLength: 0)
            }
            .padding(6)
            .background(Color(.tertiarySystemBackground), in: RoundedRectangle(cornerRadius: 8))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(.isLink)
    }
}
