//
//  ChatView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatView: View {
    let userId: String

    @State private var viewModel = KoinHelper().getChatViewModel()
    @State private var messageText = ""
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Observing(viewModel.state) { state in
            switch onEnum(of: state) {
            case .initial, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .navigationTitle("Chat")
                    .navigationBarTitleDisplayMode(.inline)

            case .failed:
                ContentUnavailableView("Failed to load chat", systemImage: "exclamationmark.triangle")
                    .navigationTitle("Chat")
                    .navigationBarTitleDisplayMode(.inline)

            case .chatting(let chatting):
                chattingView(chatting: chatting)
            }
        }
        .onAppear {
            viewModel.loadChat(userId: userId)
        }
    }

    @ViewBuilder
    private func chattingView(chatting: ChatViewModel.StateChatting) -> some View {
        let messages = chatting.chatMessages.reversed()
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 2) {
                        ForEach(Array(messages.enumerated()), id: \.offset) { index, message in
                            messageRow(message: message)
                                .id(index)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                .onAppear {
                    proxy.scrollTo(messages.count - 1, anchor: .bottom)
                }
                .onChange(of: messages.count) { _, _ in
                    withAnimation {
                        proxy.scrollTo(messages.count - 1, anchor: .bottom)
                    }
                }
            }

            Divider()

            Observing(viewModel.inputState) { _ in
                HStack(spacing: 8) {
                    TextField("Send a message", text: $messageText)
                        .textFieldStyle(.roundedBorder)
                        .onChange(of: messageText) { _, newValue in
                            viewModel.onMessageInputChanged(
                                message: newValue,
                                selectionRange: KotlinIntRange(
                                    start: 0,
                                    endInclusive: Int32(newValue.count)
                                )
                            )
                        }

                    Button {
                        viewModel.submit(
                            screenDensity: Float(UIScreen.main.scale),
                            isDarkTheme: colorScheme == .dark
                        )
                        messageText = ""
                    } label: {
                        Image(systemName: "paperplane.fill")
                    }
                    .disabled(messageText.isEmpty)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
            }
        }
        .navigationTitle(chatting.user.displayName)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Circle()
                    .fill(chatting.connectionStatus.isAlive ? Color.green : Color.red)
                    .frame(width: 10, height: 10)
            }
        }
    }

    @ViewBuilder
    private func messageRow(message: ChatListItemMessage) -> some View {
        switch onEnum(of: message) {
        case .simple(let simple):
            chatMessageView(body: simple.body)

        case .highlighted(let highlighted):
            if let body = highlighted.body {
                chatMessageView(body: body)
                    .padding(6)
                    .background(.quaternary, in: RoundedRectangle(cornerRadius: 6))
            }

        case .notice:
            EmptyView()
        }
    }

    @ViewBuilder
    private func chatMessageView(body: ChatListItemMessage.Body) -> some View {
        (
            Text(body.chatter.displayName)
                .fontWeight(.semibold)
                .foregroundStyle(color(from: body.color))
                + Text(": ")
                + Text(body.message ?? "")
        )
        .font(.callout)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func color(from hex: String?) -> Color {
        guard let hex, hex.hasPrefix("#"), hex.count == 7,
              let rgb = UInt64(hex.dropFirst(), radix: 16) else {
            return .primary
        }
        return Color(
            red: Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8) & 0xFF) / 255,
            blue: Double(rgb & 0xFF) / 255
        )
    }
}
