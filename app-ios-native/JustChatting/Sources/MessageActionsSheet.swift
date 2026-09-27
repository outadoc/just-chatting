//
//  MessageActionsSheet.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Sheet shown when tapping a chat message: author info, the message itself, the emotes it
/// uses, and reply / copy actions.
struct MessageActionsSheet: View {
    let message: ChatListItemMessage
    let context: ChatMessageContext
    let onReply: (ChatListItemMessage) -> Void
    let onCopy: (ChatListItemMessage) -> Void

    @SharedViewModel(\.userInfoViewModel) private var userInfoViewModel
    @Environment(\.dismiss) private var dismiss
    @ScaledMetric(relativeTo: .body) private var emoteHeight: CGFloat = 32

    var body: some View {
        if let messageBody = message.body {
            List {
                Section {
                    Observing(userInfoViewModel.state) { state in
                        userHeader(state: state, chatter: messageBody.chatter)
                    }
                }

                Section {
                    ChatMessageRow(message: message, context: context.withoutTimestamps)
                } footer: {
                    Text(message.date, format: .dateTime.day().month().year().hour().minute().second())
                }

                let emotes = usedEmotes(in: messageBody)
                if !emotes.isEmpty {
                    Section {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(emotes, id: \.name) { emote in
                                    Button {
                                        UIPasteboard.general.string = emote.name
                                    } label: {
                                        VStack(spacing: 4) {
                                            EmoteView(emote: emote, height: emoteHeight)
                                            Text(emote.name)
                                                .font(.caption2)
                                                .lineLimit(1)
                                        }
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityHint("Copies the emote name")
                                }
                            }
                        }
                    } header: {
                        Text("Emotes in this message")
                    }
                }

                Section {
                    MessageActionButtons(
                        message: message,
                        onReply: { onReply($0); dismiss() },
                        onCopy: { onCopy($0); dismiss() }
                    )
                }
            }
            .onAppear {
                userInfoViewModel.load(userId: messageBody.chatter.id)
            }
        }
    }

    @ViewBuilder
    private func userHeader(state: UserInfoViewModel.State, chatter: Chatter) -> some View {
        switch onEnum(of: state) {
        case .loaded(let loaded):
            let user = loaded.user
            VStack(alignment: .leading, spacing: 8) {
                UserHeaderView(user: user, size: .large)

                if !user.description_.isEmpty {
                    Text(user.description_)
                        .font(.callout)
                }

                let createdAt = Date(
                    epochSeconds: user.createdAt.epochSeconds,
                    nanosecondsOfSecond: user.createdAt.nanosecondsOfSecond
                )
                Label {
                    Text("Created on \(createdAt.formatted(date: .long, time: .omitted))")
                } icon: {
                    Image(systemName: "calendar")
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
            .padding(.vertical, 4)

        case .loading, .error:
            Text(chatter.displayName)
                .font(.title3.weight(.semibold))
        }
    }

    // `embeddedEmotes` only covers first-party Twitch emotes; third-party emotes are plain words
    // that match an emote name, so look those up too.
    private func usedEmotes(in messageBody: ChatListItemMessage.Body) -> [Emote] {
        var seen = Set<String>()
        let words = messageBody.message?.split(separator: " ").map(String.init) ?? []
        let candidates = Array(messageBody.embeddedEmotes) + words.compactMap { context.emotesByName[$0] }
        return candidates.filter { seen.insert($0.name).inserted }
    }
}
