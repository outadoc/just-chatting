//
//  ChatAutoCompleteRow.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatAutoCompleteRow: View {
    let items: [AutoCompleteItem]
    let onChatterClick: (Chatter) -> Void
    let onEmoteClick: (Emote) -> Void

    @ScaledMetric(relativeTo: .callout) private var emoteHeight: CGFloat = 24

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            LazyHStack(spacing: 8) {
                ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                    switch onEnum(of: item) {
                    case .user(let user):
                        Button {
                            onChatterClick(user.chatter)
                        } label: {
                            Text(verbatim: "@\(user.chatter.displayName)")
                                .font(.callout)
                                .padding(.horizontal, 10)
                                .frame(height: emoteHeight + 8)
                                .background(Color(.secondarySystemBackground), in: Capsule())
                        }
                        .buttonStyle(.plain)

                    case .emote(let emote):
                        Button {
                            onEmoteClick(emote.emote)
                        } label: {
                            EmoteView(emote: emote.emote, height: emoteHeight)
                                .padding(.horizontal, 8)
                                .frame(height: emoteHeight + 8)
                                .background(Color(.secondarySystemBackground), in: Capsule())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .frame(height: emoteHeight + 8)
    }
}

#Preview {
    ChatAutoCompleteRow(
        items: PreviewData.autoCompleteItems,
        onChatterClick: { _ in },
        onEmoteClick: { _ in }
    )
    .padding()
}
