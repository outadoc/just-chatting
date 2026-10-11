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
                    message: inReplyTo.message,
                    appUserLogin: context.appUserLogin
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
                ChatGifView(url: URL(string: gif.url), animated: animateGifs)
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
            MentionText(text: mention.text, isMentionOfAppUser: mention.isMentionOfAppUser)
                .font(.callout)

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

/// A GIF posted in chat, drawn at its own aspect ratio and aligned to the leading edge.
private struct ChatGifView: View {
    let url: URL?
    let animated: Bool

    @State private var loadedRatio: LoadedRatio?

    /// Aspect ratio of the image loaded from `url`, kept with its URL so that a view reused for
    /// another GIF doesn't keep the previous one's ratio.
    private struct LoadedRatio: Equatable {
        let url: URL?
        let ratio: CGFloat
    }

    var body: some View {
        // The image view fills whatever frame it's given and centers the image inside it, so it
        // is sized to the image's ratio to keep the GIF at the leading edge.
        let ratio = loadedRatio.flatMap { $0.url == url ? $0.ratio : nil } ?? 1
        AnimatedImageView(url: url, animated: animated) { size in
            guard size.height > 0 else { return }
            loadedRatio = LoadedRatio(url: url, ratio: size.width / size.height)
        }
        .id(url)
        .aspectRatio(ratio, contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .frame(maxWidth: .infinity, maxHeight: 200, alignment: .leading)
        .frame(height: 200)
    }
}
