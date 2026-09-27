//
//  ChatMessageContext.swift
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

extension ChatMessageContext {
    var withoutTimestamps: ChatMessageContext {
        var copy = self
        copy.showTimestamps = false
        return copy
    }
}
