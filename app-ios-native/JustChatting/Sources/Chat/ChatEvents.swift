//
//  ChatEvents.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Banners pinned to the top of the chat: room modes, connection status and ongoing events.
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

            if let raid = outgoingRaid {
                RaidGoCard(raid: raid)
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 8)
        .padding(.top, 8)
        .animation(.default, value: chatting.roomState)
        .animation(.default, value: chatting.connectionStatus.isAlive)
        .animation(.default, value: outgoingRaid)
    }

    /// Raids being prepared aren't reported by EventSub, so only started raids are shown.
    private var outgoingRaid: Raid.Go? {
        guard let raid = chatting.ongoingEvents.outgoingRaid else { return nil }
        switch onEnum(of: raid) {
        case .go(let go): return go
        case .preparing: return nil
        }
    }
}
