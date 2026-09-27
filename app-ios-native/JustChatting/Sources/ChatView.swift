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
    @State private var isAtBottom = true
    @State private var showCopiedToast = false

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
        let messages = Array(chatting.chatMessages.reversed())

        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(messages.enumerated()), id: \.element.stableId) { index, message in
                        messageRow(message: message, context: context, index: index)
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
            .onChange(of: messages.last?.stableId) { _, _ in
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
            .overlay(alignment: .top) {
                if showCopiedToast {
                    Text("Copied to clipboard")
                        .font(.callout)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .glassEffect()
                        .padding(.top, 8)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
            }
        }
        .safeAreaInset(edge: .top) {
            ChatEventsView(chatting: chatting)
        }
        .safeAreaInset(edge: .bottom) {
            VStack(spacing: 0) {
                if let constraint = chatting.messagePostConstraint {
                    SlowModeProgressView(constraint: constraint)
                }
                Observing(viewModel.inputState) { inputState in
                    ChatInputBar(
                        viewModel: viewModel,
                        inputState: inputState,
                        isEmotePickerOpen: $isEmotePickerOpen
                    )
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                if isEmotePickerOpen {
                    EmotePickerView(items: Array(chatting.pickableEmotesWithRecent)) { emote in
                        viewModel.appendEmote(emote: emote, autocomplete: true)
                    }
                    .frame(height: 300)
                    .transition(.move(edge: .bottom))
                }
            }
            .animation(.snappy, value: isEmotePickerOpen)
            .background(.bar)
        }
        .sheet(
            isPresented: Binding(
                get: { chatting.isStreamInfoVisible },
                set: { isPresented in
                    if !isPresented { viewModel.onDismissStreamInfo() }
                }
            )
        ) {
            StreamInfoSheet(user: chatting.user, stream: chatting.stream)
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
                if chatting.stream != nil,
                   let url = URL(string: "https://twitch.tv/\(chatting.user.login)") {
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

    @ViewBuilder
    private func messageRow(message: ChatListItemMessage, context: ChatMessageContext, index: Int) -> some View {
        // Messages are trimmed from the top in pairs, so parity by index stays stable per message.
        let rowBackground: Color = index.isMultiple(of: 2)
            ? Color(.systemBackground)
            : Color(.secondarySystemBackground).opacity(0.5)

        let row = ChatMessageRow(message: message, context: context)
            .padding(.horizontal, 12)
            .padding(.vertical, 2)
            .background(rowBackground)
            .blur(radius: message.isRedacted(by: context.removedContent) ? 6 : 0)

        if let messageBody = message.body {
            row
                .contentShape(Rectangle())
                .onTapGesture {
                    viewModel.onShowMessageActions(message: message)
                }
                .contextMenu {
                    if messageBody.canBeRepliedTo {
                        Button {
                            viewModel.onReplyToMessage(entry: message)
                        } label: {
                            Label("Reply", systemImage: "arrowshape.turn.up.left")
                        }
                    }
                    Button {
                        copyToClipboard(message)
                    } label: {
                        Label("Copy message", systemImage: "doc.on.doc")
                    }
                }
                .accessibilityActions {
                    if messageBody.canBeRepliedTo {
                        Button("Reply") {
                            viewModel.onReplyToMessage(entry: message)
                        }
                    }
                }
        } else {
            row
        }
    }

    private func copyToClipboard(_ message: ChatListItemMessage) {
        guard let text = message.body?.message else { return }
        UIPasteboard.general.string = text
        withAnimation { showCopiedToast = true }
        Task {
            try? await Task.sleep(for: .seconds(2))
            withAnimation { showCopiedToast = false }
        }
    }
}
