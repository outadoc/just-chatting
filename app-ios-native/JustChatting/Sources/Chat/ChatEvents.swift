//
//  ChatEvents.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Banners pinned to the top of the chat: room modes and connection status.
struct ChatEvents: View {
    let chatting: ChatViewModel.StateChatting

    var body: some View {
        VStack(spacing: 6) {
            RoomStateBanner(roomState: chatting.roomState)

            if !chatting.connectionStatus.isAlive {
                SlimBanner(tint: .warning) {
                    Label("Reconnecting to chat…", systemImage: "wifi.exclamationmark")
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 8)
        .padding(.top, 4)
        .animation(.default, value: chatting.roomState)
        .animation(.default, value: chatting.connectionStatus.isAlive)
    }
}
