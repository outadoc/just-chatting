//
//  MessageActionButtons.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Actions available on a chat message. Shared by its context menu, its accessibility actions and
/// the message actions sheet, so that they always offer the same thing.
struct MessageActionButtons: View {
    let message: ChatListItemMessage
    let onReply: (ChatListItemMessage) -> Void
    let onCopy: (ChatListItemMessage) -> Void

    var body: some View {
        if message.body?.canBeRepliedTo == true {
            Button {
                onReply(message)
            } label: {
                Label("Reply", systemImage: "arrowshape.turn.up.left")
            }
        }

        Button {
            onCopy(message)
        } label: {
            Label("Copy message", systemImage: "doc.on.doc")
        }
    }
}
