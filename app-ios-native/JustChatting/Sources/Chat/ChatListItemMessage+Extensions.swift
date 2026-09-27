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
}
