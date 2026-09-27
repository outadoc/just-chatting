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

    @State private var userInfoViewModel = KoinHelper().getUserInfoViewModel()
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
                    if messageBody.canBeRepliedTo {
                        Button {
                            onReply(message)
                            dismiss()
                        } label: {
                            Label("Reply", systemImage: "arrowshape.turn.up.left")
                        }
                    }

                    Button {
                        onCopy(message)
                        dismiss()
                    } label: {
                        Label("Copy message", systemImage: "doc.on.doc")
                    }
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
                HStack(spacing: 12) {
                    AsyncImage(url: URL(string: user.profileImageUrl)) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.secondary.opacity(0.3)
                    }
                    .frame(width: 52, height: 52)
                    .clipShape(Circle())

                    VStack(alignment: .leading, spacing: 2) {
                        Text(user.displayName)
                            .font(.title3.weight(.semibold))
                        Text(verbatim: "@\(user.login)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

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

extension ChatMessageContext {
    var withoutTimestamps: ChatMessageContext {
        var copy = self
        copy.showTimestamps = false
        return copy
    }
}
