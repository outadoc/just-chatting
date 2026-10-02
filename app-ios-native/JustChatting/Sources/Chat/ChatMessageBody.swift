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
    @Environment(\.displayScale) private var displayScale
    @Environment(\.openURL) private var openURL
    @Environment(\.animateEmotes) private var animateEmotes
    @Environment(\.animateGifs) private var animateGifs
    @ScaledMetric(relativeTo: .callout) private var emoteHeight: CGFloat = 29
    @ScaledMetric(relativeTo: .callout) private var badgeHeight: CGFloat = 18

    var body: some View {
        let tokens = messageBody.tokenize(
            emotes: context.emotes,
            cheerEmotes: context.cheerEmotes,
            appUserLogin: context.appUserLogin
        )

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
                    AnimatedImageView(
                        url: badge.urls.url(colorScheme: colorScheme, displayScale: displayScale),
                        animated: animateEmotes
                    )
                        .frame(width: badgeHeight, height: badgeHeight)
                        .accessibilityLabel(badge.title ?? badge.setId)
                }

                Text(chatterName)
                    .fontWeight(.bold)
                    .foregroundStyle(
                        ChatterColors.shared.color(
                            for: messageBody.chatter,
                            hex: messageBody.color,
                            colorScheme: colorScheme
                        )
                    )
                    .font(.callout)

                ForEach(Array(tokens.enumerated()), id: \.offset) { _, token in
                    tokenView(token)
                }
            }

            // The enlarged emote is drawn on its own line, like a GIF.
            if let emote = messageBody.gigantifiedEmote {
                EmoteView(emote: emote, height: emoteHeight * 4.5)
            }

            ForEach(Array(messageBody.gifs), id: \.id) { gif in
                AnimatedImageView(url: URL(string: gif.url), animated: animateGifs)
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

    /// Which badges to show is decided in `shared`; this looks up how to draw them.
    private var resolvedBadges: [TwitchBadge] {
        let displayed = messageBody.displayedBadges
        let available = displayed.sourceRoomId.map { context.sourceChannelBadges[$0] ?? [:] } ?? context.badges
        return displayed.badges.compactMap { badge in
            available[ChatMessageContext.key(setId: badge.id, version: badge.version)]
        }
    }

    @ViewBuilder
    private func tokenView(_ token: MessageToken) -> some View {
        switch onEnum(of: token) {
        case .word(let word):
            Text(word.text)
                .font(.callout)
                .italic(messageBody.isAction)

        case .emote(let emote):
            EmoteView(
                emote: emote.emote,
                overlays: emote.overlays,
                height: emoteHeight
            )

        case .mention(let mention):
            let isMentioningMe = mention.isMentionOfAppUser
            Text(mention.text)
                .font(.callout)
                .fontWeight(.bold)
                .foregroundStyle(isMentioningMe ? Color.accentColor : .primary)
                .padding(.horizontal, isMentioningMe ? 6 : 0)
                .padding(.vertical, isMentioningMe ? 1 : 0)
                .background(isMentioningMe ? Color.accentColor.opacity(0.2) : .clear, in: Capsule())

        case .link(let link):
            Text(link.text)
                .font(.callout)
                .foregroundStyle(Color.accentColor)
                .underline()
                .onTapGesture {
                    if let url = URL(string: link.url) {
                        openURL(url)
                    }
                }
                .accessibilityAddTraits(.isLink)
        }
    }
}
