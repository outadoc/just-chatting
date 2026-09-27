//
//  ChatMessageList.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Messages of a chat, oldest at the top. Follows new messages while scrolled to the bottom.
struct ChatMessageList: View {
    /// Newest first, as in the view model's state.
    let messages: [ChatListItemMessage]
    let context: ChatMessageContext
    let onShowActions: (ChatListItemMessage) -> Void
    let onReply: (ChatListItemMessage) -> Void
    let onCopy: (ChatListItemMessage) -> Void

    @State private var isAtBottom = true

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(messages.enumerated().reversed()), id: \.element.stableId) { index, message in
                        row(message: message, index: index)
                    }
                    Color.clear
                        .frame(height: 1)
                        .id("bottom")
                        .onAppear { isAtBottom = true }
                        .onDisappear { isAtBottom = false }
                }
                .padding(.vertical, 4)
            }
            .defaultScrollAnchor(.bottom)
            .scrollDismissesKeyboard(.interactively)
            .onChange(of: messages.first?.stableId) { _, _ in
                guard isAtBottom else { return }
                proxy.scrollTo("bottom", anchor: .bottom)
            }
            .overlay(alignment: .bottomTrailing) {
                if !isAtBottom {
                    Button {
                        withAnimation { proxy.scrollTo("bottom", anchor: .bottom) }
                    } label: {
                        Image(systemName: "arrow.down")
                            .fontWeight(.semibold)
                            .frame(width: 40, height: 40)
                    }
                    .buttonStyle(.glass)
                    .clipShape(Circle())
                    .padding(12)
                    .accessibilityLabel("Scroll to latest messages")
                }
            }
        }
    }

    /// `index` is the message's position in `messages`, newest first.
    @ViewBuilder
    private func row(message: ChatListItemMessage, index: Int) -> some View {
        let isAlternate = ChatRowBackground.shared.isAlternate(
            index: Int32(index),
            messageCount: Int32(messages.count)
        )

        let row = ChatMessage(message: message, context: context)
            .padding(.horizontal, 12)
            .padding(.vertical, 2)
            .background(isAlternate ? Color(.secondarySystemBackground).opacity(0.5) : Color(.systemBackground))
            .blur(radius: message.isRedacted(by: context.removedContent) ? 6 : 0)

        if message.body != nil {
            let actions = MessageActionButtons(message: message, onReply: onReply, onCopy: onCopy)
            row
                .contentShape(Rectangle())
                .onTapGesture {
                    onShowActions(message)
                }
                .contextMenu { actions }
                .accessibilityActions { actions }
        } else {
            row
        }
    }
}
