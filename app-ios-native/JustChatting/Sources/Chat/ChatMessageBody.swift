//
//  ChatMessageBody.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatMessageBody: View {
    let messageBody: ChatListItemMessage.Body
    let context: ChatMessageContext

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.openURL) private var openURL
    @ScaledMetric(relativeTo: .callout) private var emoteHeight: CGFloat = 29
    @ScaledMetric(relativeTo: .callout) private var badgeHeight: CGFloat = 18

    var body: some View {
        let chatterColor = ChatterColors.color(for: messageBody.chatter, hex: messageBody.color)
        let embeddedEmotes = Dictionary(
            messageBody.embeddedEmotes.map { ($0.name, $0) }
        ) { _, last in last }
        let tokens = ChatMessageTokenizer.tokenize(message: messageBody.message) { word in
            embeddedEmotes[word] ?? context.emotesByName[word]
        }

        VStack(alignment: .leading, spacing: 4) {
            if let inReplyTo = messageBody.inReplyTo {
                InReplyToMessage(
                    mentions: Array(inReplyTo.mentions),
                    message: inReplyTo.message
                )
            }

            FlowLayout(spacing: 3) {
                if let roomId = messageBody.sourceRoomId, let sourceChannel = context.sourceChannels[roomId] {
                    AvatarView(url: sourceChannel.profileImageUrl, size: badgeHeight)
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
                ChatRichEmbed(embed: embed)
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
