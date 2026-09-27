//
//  ChatMessageView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Everything a chat message needs to render, precomputed once per chat state update.
struct ChatMessageContext {
    let emotesByName: [String: Emote]
    let badges: [String: TwitchBadge]
    let sourceChannelBadges: [String: [String: TwitchBadge]]
    let sourceChannels: [String: User]
    let pronouns: [Chatter: Pronoun]
    let richEmbeds: [String: ChatListItemRichEmbed]
    let removedContent: [ChatListItemRemoveContent]
    let appUserLogin: String
    var showTimestamps: Bool

    init(chatting: ChatViewModel.StateChatting, showTimestamps: Bool) {
        emotesByName = chatting.allEmotesMap
            .merging(chatting.cheerEmotes) { _, cheer in cheer }
        badges = Self.index(badges: Array(chatting.globalBadges) + Array(chatting.channelBadges))
        sourceChannelBadges = chatting.sourceChannelBadges.mapValues { Self.index(badges: Array($0)) }
        sourceChannels = chatting.sourceChannels
        var pronouns: [Chatter: Pronoun] = [:]
        for (chatter, pronoun) in chatting.pronouns {
            if let pronoun = pronoun as? Pronoun {
                pronouns[chatter] = pronoun
            }
        }
        self.pronouns = pronouns
        richEmbeds = chatting.richEmbeds
        removedContent = Array(chatting.removedContent)
        appUserLogin = chatting.appUser.userLogin
        self.showTimestamps = showTimestamps
    }

    // Channel badges override global badges with the same set id and version.
    private static func index(badges: [TwitchBadge]) -> [String: TwitchBadge] {
        Dictionary(badges.map { (key(setId: $0.setId, version: $0.version), $0) }) { _, last in last }
    }

    static func key(setId: String, version: String) -> String {
        "\(setId)/\(version)"
    }
}

struct ChatMessageRow: View {
    let message: ChatListItemMessage
    let context: ChatMessageContext

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            if context.showTimestamps {
                Text(message.date, format: .dateTime.hour().minute())
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .monospacedDigit()
            }

            switch onEnum(of: message) {
            case .simple(let simple):
                ChatMessageBodyView(messageBody: simple.body, context: context)
                    .padding(.vertical, 4)

            case .highlighted(let highlighted):
                HighlightedMessageView(highlighted: highlighted, context: context)

            case .notice(let notice):
                NoticeMessageView(notice: notice)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct HighlightedMessageView: View {
    let highlighted: ChatListItemMessage.Highlighted
    let context: ChatMessageContext

    var body: some View {
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
                    ChatMessageBodyView(messageBody: body, context: context)
                }
            }
            .padding(8)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemBackground))
        }
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .padding(.vertical, 4)
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
}

private struct NoticeMessageView: View {
    let notice: ChatListItemMessage.Notice

    var body: some View {
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
}

struct ChatMessageBodyView: View {
    let messageBody: ChatListItemMessage.Body
    let context: ChatMessageContext

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.openURL) private var openURL
    @ScaledMetric(relativeTo: .callout) private var emoteHeight: CGFloat = 29
    @ScaledMetric(relativeTo: .callout) private var badgeHeight: CGFloat = 18

    var body: some View {
        let chatterColor = ChatterColors.color(for: messageBody.chatter, hex: messageBody.color, colorScheme: colorScheme)
        let embeddedEmotes = Dictionary(
            messageBody.embeddedEmotes.map { ($0.name, $0) }
        ) { _, last in last }
        let tokens = ChatMessageTokenizer.tokenize(message: messageBody.message) { word in
            embeddedEmotes[word] ?? context.emotesByName[word]
        }

        VStack(alignment: .leading, spacing: 4) {
            if let inReplyTo = messageBody.inReplyTo {
                InReplyToView(
                    mentions: Array(inReplyTo.mentions),
                    message: inReplyTo.message
                )
            }

            FlowLayout(spacing: 3) {
                if let roomId = messageBody.sourceRoomId, let sourceChannel = context.sourceChannels[roomId] {
                    AsyncImage(url: URL(string: sourceChannel.profileImageUrl)) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.secondary.opacity(0.3)
                    }
                    .frame(width: badgeHeight, height: badgeHeight)
                    .clipShape(Circle())
                    .accessibilityLabel(sourceChannel.displayName)
                }

                if let pronoun = context.pronouns[messageBody.chatter] {
                    Text(verbatim: "(\(pronoun.displayPronoun))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                ForEach(resolvedBadges, id: \.self) { badge in
                    AnimatedImageView(url: badge.urls.url(for: colorScheme))
                        .frame(width: badgeHeight, height: badgeHeight)
                        .accessibilityLabel(badge.title ?? badge.setId)
                }

                Text(chatterName)
                    .fontWeight(.bold)
                    .foregroundStyle(chatterColor)
                    .font(.callout)

                ForEach(Array(tokens.enumerated()), id: \.offset) { _, token in
                    tokenView(token)
                }
            }

            ForEach(Array(messageBody.gifs), id: \.id) { gif in
                AnimatedImageView(url: URL(string: gif.url))
                    .frame(maxWidth: .infinity, maxHeight: 200, alignment: .leading)
                    .frame(height: 200)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .accessibilityLabel(gif.description_)
            }

            if let messageId = messageBody.messageId, let embed = context.richEmbeds[messageId] {
                RichEmbedView(embed: embed)
            }
        }
    }

    private var chatterName: String {
        let chatter = messageBody.chatter
        let name = chatter.hasLocalizedDisplayName
            ? "\(chatter.displayName) (\(chatter.login))"
            : chatter.displayName
        // Action messages (/me) are rendered as "Name message" instead of "Name: message".
        return messageBody.isAction ? name : "\(name):"
    }

    private var resolvedBadges: [TwitchBadge] {
        // Messages relayed from another channel in a shared chat carry that channel's badges.
        if let roomId = messageBody.sourceRoomId, !messageBody.sourceBadges.isEmpty {
            let sourceBadges = context.sourceChannelBadges[roomId] ?? [:]
            return Array(messageBody.sourceBadges).compactMap { badge in
                sourceBadges[ChatMessageContext.key(setId: badge.id, version: badge.version)]
            }
        }
        return Array(messageBody.badges).compactMap { badge in
            context.badges[ChatMessageContext.key(setId: badge.id, version: badge.version)]
        }
    }

    @ViewBuilder
    private func tokenView(_ token: ChatMessageToken) -> some View {
        switch token {
        case .text(let word):
            Text(word)
                .font(.callout)
                .italic(messageBody.isAction)

        case .emote(let emote, let overlays):
            EmoteView(
                emote: emote,
                overlays: overlays,
                height: messageBody.isGigantifiedEmote ? emoteHeight * 4.5 : emoteHeight
            )

        case .mention(let mention):
            let isMentioningMe = mention
                .dropFirst()
                .trimmingCharacters(in: .punctuationCharacters)
                .caseInsensitiveCompare(context.appUserLogin) == .orderedSame
            Text(mention)
                .font(.callout)
                .fontWeight(.bold)
                .foregroundStyle(isMentioningMe ? Color(.systemBackground) : .primary)
                .padding(.horizontal, isMentioningMe ? 3 : 0)
                .background(isMentioningMe ? Color.primary : .clear, in: RoundedRectangle(cornerRadius: 3))

        case .link(let text, let url):
            Text(text)
                .font(.callout)
                .foregroundStyle(Color.accentColor)
                .underline()
                .onTapGesture { openURL(url) }
                .accessibilityAddTraits(.isLink)
        }
    }
}

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

struct EmoteView: View {
    let emote: Emote
    var overlays: [Emote] = []
    let height: CGFloat

    @Environment(\.colorScheme) private var colorScheme
    @State private var loadedRatio: CGFloat?

    var body: some View {
        // Emote.ratio is set explicitly for known non-square emotes; otherwise fall back to the
        // loaded image's aspect ratio.
        let ratio = loadedRatio ?? CGFloat(emote.ratio)
        ZStack {
            AnimatedImageView(url: emote.urls.url(for: colorScheme)) { size in
                guard size.height > 0, emote.ratio == 1 else { return }
                loadedRatio = size.width / size.height
            }
            // Zero-width emotes are drawn on top of the preceding emote.
            ForEach(Array(overlays.enumerated()), id: \.offset) { _, overlay in
                AnimatedImageView(url: overlay.urls.url(for: colorScheme))
            }
        }
        .frame(width: height * ratio, height: height)
        .accessibilityLabel(([emote] + overlays).map(\.name).joined(separator: " "))
    }
}

private struct RichEmbedView: View {
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

enum ChatMessageToken {
    case text(String)
    case emote(Emote, overlays: [Emote])
    case mention(String)
    case link(text: String, url: URL)
}

enum ChatMessageTokenizer {
    private static let linkDetector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)

    static func tokenize(message: String?, emoteNamed: (String) -> Emote?) -> [ChatMessageToken] {
        guard let message else { return [] }
        let words = message.split(separator: " ", omittingEmptySubsequences: true).map(String.init)
        var tokens: [ChatMessageToken] = []
        var index = 0

        while index < words.count {
            let word = words[index]

            if let emote = emoteNamed(word) {
                // Collect any zero-width emotes that should overlay this one.
                var overlays: [Emote] = []
                if !emote.isZeroWidth {
                    var overlayIndex = index + 1
                    while overlayIndex < words.count,
                          let overlay = emoteNamed(words[overlayIndex]),
                          overlay.isZeroWidth {
                        overlays.append(overlay)
                        overlayIndex += 1
                    }
                }
                tokens.append(.emote(emote, overlays: overlays))
                index += 1 + overlays.count
                continue
            }

            if word.hasPrefix("@"), word.count > 1 {
                tokens.append(.mention(word))
            } else if let url = link(in: word) {
                tokens.append(.link(text: word, url: url))
            } else {
                tokens.append(.text(word))
            }
            index += 1
        }

        return tokens
    }

    private static func link(in word: String) -> URL? {
        let range = NSRange(word.startIndex..., in: word)
        guard let match = linkDetector?.firstMatch(in: word, range: range),
              match.range == range,
              let url = match.url,
              url.scheme == "http" || url.scheme == "https" else {
            return nil
        }
        return url
    }
}
