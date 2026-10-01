//
//  ChatView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChatView: View {
    let userId: String

    @SharedViewModel(\.chatViewModel) private var viewModel
    private let preferenceRepository = KoinHelper().getPreferenceRepository()
    @State private var showTimestamps = true
    @State private var isEmotePickerOpen = false
    @State private var copyCount = 0

    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    var body: some View {
        Observing(viewModel.state) { state in
            switch onEnum(of: state) {
            case .initial, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .navigationTitle("Chat")

            case .failed:
                ContentUnavailableView("Failed to load chat", systemImage: "exclamationmark.triangle")
                    .navigationTitle("Chat")

            case .chatting(let chatting):
                chattingView(chatting: chatting)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        // The tab bar would sit below the message input on iPhone.
        .toolbarVisibility(horizontalSizeClass == .compact ? .hidden : .automatic, for: .tabBar)
        .onAppear {
            viewModel.loadChat(userId: userId)
        }
        .onChange(of: userId) { _, newUserId in
            isEmotePickerOpen = false
            viewModel.loadChat(userId: newUserId)
        }
        .collect(flow: preferenceRepository.currentPreferences) { prefs in
            showTimestamps = prefs.showTimestamps
        }
    }

    @ViewBuilder
    private func chattingView(chatting: ChatViewModel.StateChatting) -> some View {
        let context = ChatMessageContext(chatting: chatting, showTimestamps: showTimestamps)

        ChatMessageList(
            messages: chatting.chatMessages,
            context: context,
            onShowActions: { viewModel.onShowMessageActions(message: $0) },
            onReply: { viewModel.onReplyToMessage(entry: $0) },
            onCopy: copyToClipboard
        )
        .toast("Copied to clipboard", trigger: copyCount)
        // An inset rather than a bar, so that the banners float below the navigation bar
        // instead of becoming part of it.
        .safeAreaInset(edge: .top) {
            ChatEvents(chatting: chatting)
        }
        // A bar rather than an inset, so that messages fade out behind the composer.
        .safeAreaBar(edge: .bottom) {
            VStack(spacing: 0) {
                if let constraint = chatting.messagePostConstraint {
                    ChatSlowModeProgress(constraint: constraint)
                }
                Observing(viewModel.inputState) { inputState in
                    ChatInput(
                        viewModel: viewModel,
                        inputState: inputState,
                        isEmotePickerOpen: $isEmotePickerOpen
                    )
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                if isEmotePickerOpen {
                    EmotePicker(items: Array(chatting.pickableEmotesWithRecent)) { emote in
                        viewModel.appendEmote(emote: emote, autocomplete: true)
                    }
                    .frame(height: 300)
                    .transition(.move(edge: .bottom))
                }
            }
            .animation(.snappy, value: isEmotePickerOpen)
        }
        .sheet(
            isPresented: Binding(
                get: { chatting.isStreamInfoVisible },
                set: { isPresented in
                    if !isPresented { viewModel.onDismissStreamInfo() }
                }
            )
        ) {
            StreamInfoSheet(
                user: chatting.user,
                stream: chatting.stream,
                sharedChatChannels: chatting.sharedChatChannels
            )
                .presentationDetents([.medium, .large])
        }
        .sheet(
            isPresented: Binding(
                get: { chatting.selectedMessageForActions != nil },
                set: { isPresented in
                    if !isPresented { viewModel.onDismissMessageActions() }
                }
            )
        ) {
            if let message = chatting.selectedMessageForActions {
                MessageActionsSheet(
                    message: message,
                    context: context,
                    onReply: { viewModel.onReplyToMessage(entry: $0) },
                    onCopy: copyToClipboard
                )
                .presentationDetents([.medium, .large])
            }
        }
        .navigationTitle(chatting.user.displayName)
        .navigationSubtitle(liveSubtitle(stream: chatting.stream))
        .toolbar {
            ToolbarItemGroup(placement: .primaryAction) {
                if chatting.stream != nil, let url = chatting.user.channelUrl {
                    Link(destination: url) {
                        Label("Watch live", systemImage: "play.tv")
                    }
                }
                Button {
                    viewModel.onShowStreamInfo()
                } label: {
                    Label("Stream info", systemImage: "info.circle")
                }
            }
        }
    }

    private func liveSubtitle(stream: JCShared.Stream?) -> Text {
        guard let stream else { return Text("Offline") }
        return Text("\(Int(stream.viewerCount).formatted()) viewers")
    }

    private func copyToClipboard(_ message: ChatListItemMessage) {
        guard let text = message.body?.message else { return }
        UIPasteboard.general.string = text
        copyCount += 1
    }
}
