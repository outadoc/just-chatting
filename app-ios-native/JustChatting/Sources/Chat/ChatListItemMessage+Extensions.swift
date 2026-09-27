//
//  ChatListItemMessage+Extensions.swift
//  JustChatting
//

import Foundation
import JCShared

extension ChatListItemMessage.Body {
    /// Whether this message can be replied to (i.e. has a Twitch message id).
    var canBeRepliedTo: Bool { messageId != nil }
}

extension ChatListItemMessage {
    /// Stable identity for list diffing, mirroring the key used by the Compose ChatList.
    var stableId: String {
        if let messageId = body?.messageId {
            return messageId
        }
        return "\(timestamp.epochSeconds).\(timestamp.nanosecondsOfSecond)-\(hash)"
    }

    /// Whether this message was deleted by a moderator, or its author timed out / banned.
    func isRedacted(by removedContent: [ChatListItemRemoveContent]) -> Bool {
        removedContent.contains { rule in
            guard rule.upUntil.date > timestamp.date else { return false }
            if let messageId = rule.matchingMessageId, messageId != body?.messageId { return false }
            if let userId = rule.matchingUserId, userId != body?.chatter.id { return false }
            return true
        }
    }
}
