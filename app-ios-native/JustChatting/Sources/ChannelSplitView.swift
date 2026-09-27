//
//  ChannelSplitView.swift
//  JustChatting
//

import SwiftUI

/// List of channels next to the selected channel's chat. Side by side on regular widths; on
/// compact widths it collapses into a stack where selecting a channel pushes its chat.
struct ChannelSplitView<Content: View>: View {
    @Binding var selectedChannelId: String?
    @ViewBuilder let content: Content

    var body: some View {
        NavigationSplitView {
            content
        } detail: {
            if let selectedChannelId {
                ChatView(userId: selectedChannelId)
            } else {
                ContentUnavailableView(
                    "No chat selected",
                    systemImage: "bubble.left.and.bubble.right",
                    description: Text("Select a channel to open its chat.")
                )
            }
        }
    }
}
