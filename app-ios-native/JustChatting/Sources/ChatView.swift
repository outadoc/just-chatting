//
//  ChatView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatView: View {
    let userId: String

    @State private var viewModel = KoinHelper().getChatViewModel()

    // TODO use VM state instead
    @State private var messageText = ""

    @Environment(\.colorScheme) var colorScheme
    @State private var isAtBottom = true

    var body: some View {
        Observing(viewModel.state) { state in
            switch onEnum(of: state) {
            case .initial, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .navigationTitle("Chat")
                    .navigationBarTitleDisplayMode(.inline)

            case .failed:
                ContentUnavailableView("Failed to load chat", systemImage: "exclamationmark.triangle")
                    .navigationTitle("Chat")
                    .navigationBarTitleDisplayMode(.inline)

            case .chatting(let chatting):
                chattingView(chatting: chatting)
            }
        }
        .onAppear {
            viewModel.loadChat(userId: userId)
        }
        .onChange(of: userId) { _, newUserId in
            viewModel.loadChat(userId: newUserId)
        }
    }

    @ViewBuilder
    private func chattingView(chatting: ChatViewModel.StateChatting) -> some View {
        let messages = chatting.chatMessages.reversed()
        let globalEmotes = chatting.allEmotesMap
            .merging(chatting.cheerEmotes) { _, cheer in cheer }
        let allBadges = Array(chatting.globalBadges) + Array(chatting.channelBadges)
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(messages.enumerated()), id: \.offset) { index, message in
                        messageRow(message: message, globalEmotes: globalEmotes, allBadges: allBadges, index: index)
                            .id(index)
                    }
                    Color.clear
                        .frame(height: 0)
                        .id("bottom")
                        .onAppear { isAtBottom = true }
                        .onDisappear { isAtBottom = false }
                }
                .padding(.vertical, 4)
            }
            .onAppear {
                proxy.scrollTo("bottom")
            }
            .onChange(of: messages.count) { _, _ in
                guard isAtBottom else { return }
                proxy.scrollTo("bottom")
            }
        }
        .navigationTitle(chatting.user.displayName)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Circle()
                    .fill(chatting.connectionStatus.isAlive ? Color.green : Color.red)
                    .frame(width: 10, height: 10)
            }
            ToolbarItem(placement: .bottomBar) {
                HStack(alignment: .center, spacing: 8) {
                    TextField("Send a message", text: $messageText, axis: .vertical)
                        .textFieldStyle(.plain)
                        .lineLimit(1...5)
                        .frame(maxWidth: .infinity)
                        .onChange(of: messageText) { _, newValue in
                            viewModel.onMessageInputChanged(
                                message: newValue,
                                selectionRange: KotlinIntRange(
                                    start: 0,
                                    endInclusive: Int32(newValue.count)
                                )
                            )
                        }

                    Button {
                        viewModel.submit(
                            screenDensity: Float(UIScreen.main.scale),
                            isDarkTheme: colorScheme == .dark
                        )
                        messageText = ""
                    } label: {
                        Image(systemName: "arrow.up")
                            .fontWeight(.semibold)
                            .foregroundStyle(.white)
                            .frame(width: 32, height: 32)
                            .background(
                                messageText.isEmpty ? Color(.systemGray3) : Color.accentColor,
                                in: Circle()
                            )
                    }
                    .disabled(messageText.isEmpty)
                }
            }
        }
    }

    @ViewBuilder
    private func messageRow(message: ChatListItemMessage, globalEmotes: [String: Emote], allBadges: [TwitchBadge], index: Int) -> some View {
        let rowBackground: Color = index.isMultiple(of: 2) ? Color(.systemBackground) : Color(.secondarySystemBackground)
        switch onEnum(of: message) {
        case .simple(let simple):
            chatMessageView(body: simple.body, globalEmotes: globalEmotes, allBadges: allBadges)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(rowBackground)

        case .highlighted(let highlighted):
            highlightedMessageView(highlighted: highlighted, globalEmotes: globalEmotes, allBadges: allBadges)
                .padding(.horizontal, 12)
                .padding(.vertical, 2)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(rowBackground)

        case .notice(let notice):
            noticeMessageView(notice: notice)
                .padding(.horizontal, 12)
                .padding(.vertical, 2)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(rowBackground)
        }
    }

    @ViewBuilder
    private func highlightedMessageView(highlighted: ChatListItemMessage.Highlighted, globalEmotes: [String: Emote], allBadges: [TwitchBadge]) -> some View {
        let accentColor = color(forLevel: highlighted.metadata.level)
        HStack(spacing: 0) {
            accentColor.frame(width: 4)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    if let icon = highlighted.metadata.titleIcon {
                        Image(systemName: symbolName(forIcon: icon))
                            .font(.callout)
                    }
                    Text(highlighted.metadata.title.localizedString())
                        .font(.callout)
                        .fontWeight(.semibold)
                }
                .foregroundStyle(accentColor)

                if let subtitle = highlighted.metadata.subtitle {
                    Text(subtitle.localizedString())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if let body = highlighted.body {
                    chatMessageView(body: body, globalEmotes: globalEmotes, allBadges: allBadges)
                }
            }
            .padding(8)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemBackground))
        }
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private func noticeMessageView(notice: ChatListItemMessage.Notice) -> some View {
        HStack(spacing: 0) {
            Color.accentColor.frame(width: 4)
            Text(notice.text.localizedString())
                .font(.callout)
                .padding(8)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.secondarySystemBackground))
        }
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private func chatMessageView(body: ChatListItemMessage.Body, globalEmotes: [String: Emote], allBadges: [TwitchBadge]) -> some View {
        let emotesByName = globalEmotes
            .merging(body.embeddedEmotes.map { ($0.name, $0) }) { _, specific in specific }
        let tokens = tokenize(message: body.message, emotesByName: emotesByName)
        let resolvedBadges = Array(body.badges).compactMap { badge in
            allBadges.first { $0.setId == badge.id && $0.version == badge.version }
        }

        VStack(alignment: .leading, spacing: 4) {
            if let inReplyTo = body.inReplyTo {
                inReplyToView(inReplyTo: inReplyTo)
            }

            FlowLayout(spacing: 2) {
                ForEach(resolvedBadges, id: \.setId) { badge in
                    badgeView(badge: badge)
                }

                Text(body.chatter.displayName + ": ")
                    .fontWeight(.semibold)
                    .foregroundStyle(color(from: body.color))
                    .font(.callout)
                    .fixedSize()

                ForEach(Array(tokens.enumerated()), id: \.offset) { _, token in
                    switch token {
                    case .text(let word):
                        Text(word)
                            .font(.callout)
                            .fixedSize()
                    case .emote(let emote):
                        emoteView(emote: emote)
                    case .link(let url):
                        Link(url.absoluteString, destination: url)
                            .font(.callout)
                            .fixedSize()
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func inReplyToView(inReplyTo: ChatListItemMessage.BodyInReplyTo) -> some View {
        let mentions = Array(inReplyTo.mentions).map { "@\($0)" }.joined(separator: " ")
        let text = inReplyTo.message.map { ": \($0)" } ?? ""
        Label(mentions + text, systemImage: "arrowshape.turn.up.left.fill")
            .font(.caption)
            .foregroundStyle(.secondary)
            .lineLimit(2)
            .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func badgeView(badge: TwitchBadge) -> some View {
        AsyncImage(url: imageUrl(urls: badge.urls)) { phase in
            switch phase {
            case .success(let image):
                image.resizable().scaledToFit()
            default:
                Color.clear
            }
        }
        .frame(height: 18)
    }

    @ViewBuilder
    private func emoteView(emote: Emote) -> some View {
        EmoteView(url: imageUrl(urls: emote.urls))
    }

    private struct EmoteView: View {
        let url: URL?
        @State private var aspectRatio: CGFloat = 1
        @ScaledMetric(relativeTo: .callout) private var emoteHeight: CGFloat = 30

        var body: some View {
            AnimatedImageView(url: url) { size in
                guard size.height > 0 else { return }
                aspectRatio = size.width / size.height
            }
            .frame(width: emoteHeight * aspectRatio, height: emoteHeight)
        }
    }

    private func imageUrl(urls: EmoteUrls) -> URL? {
        let dict = colorScheme == .dark ? urls.dark : urls.light
        for scale: Float in [2.0, 1.0, 4.0] {
            if let urlStr = dict[KotlinFloat(value: scale)] as? String {
                return URL(string: urlStr)
            }
        }
        return dict.values.first.flatMap { URL(string: $0) }
    }

    private enum MessageToken {
        case text(String)
        case emote(Emote)
        case link(URL)
    }

    private func tokenize(message: String?, emotesByName: [String: Emote]) -> [MessageToken] {
        guard let message else { return [] }
        return message.split(separator: " ", omittingEmptySubsequences: false).map { word in
            let w = String(word)
            if let emote = emotesByName[w] { return .emote(emote) }
            if let url = URL(string: w), UIApplication.shared.canOpenURL(url) { return .link(url) }
            return .text(w)
        }
    }

    private func color(forLevel level: ChatListItemMessage.HighlightedLevel) -> Color {
        switch level {
        case .base:  return .accentColor
        case .one:   return Color(red: 0x6b/255, green: 0x81/255, blue: 0x6e/255)
        case .two:   return Color(red: 0x32/255, green: 0x84/255, blue: 0x3b/255)
        case .three: return Color(red: 0x00/255, green: 0x7a/255, blue: 0x6c/255)
        case .four:  return Color(red: 0x00/255, green: 0x80/255, blue: 0xa9/255)
        case .five:  return Color(red: 0x00/255, green: 0x70/255, blue: 0xdb/255)
        case .six:   return Color(red: 0x01/255, green: 0x6c/255, blue: 0xd9/255)
        case .seven: return Color(red: 0x73/255, green: 0x1a/255, blue: 0xcb/255)
        case .eight: return Color(red: 0xbe/255, green: 0x0b/255, blue: 0xb7/255)
        case .nine:  return Color(red: 0xab/255, green: 0x20/255, blue: 0x78/255)
        case .ten:   return Color(red: 0xc9/255, green: 0x02/255, blue: 0x16/255)
        default:     return .accentColor
        }
    }

    private func symbolName(forIcon icon: Icon) -> String {
        switch icon {
        case .callReceived:      return "phone.arrow.down.left"
        case .campaign:          return "megaphone.fill"
        case .cancel:            return "xmark.circle.fill"
        case .fastForward:       return "forward.fill"
        case .gavel:             return "hammer.fill"
        case .highlight:         return "star.fill"
        case .redeem:            return "gift.fill"
        case .reply:             return "arrowshape.turn.up.left.fill"
        case .send:              return "paperplane.fill"
        case .star:              return "star.fill"
        case .toll:              return "bell.fill"
        case .volunteerActivism: return "heart.fill"
        case .wavingHand:        return "hand.wave.fill"
        default:                 return "info.circle.fill"
        }
    }

    private func color(from hex: String?) -> Color {
        guard let hex, hex.hasPrefix("#"), hex.count == 7,
              let rgb = UInt64(hex.dropFirst(), radix: 16) else {
            return .primary
        }
        return Color(
            red: Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8) & 0xFF) / 255,
            blue: Double(rgb & 0xFF) / 255
        )
    }
}
