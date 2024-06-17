//
//  ChattingView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-20.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct ChattingView: View {
    @State private var viewModel = ViewModel(wrapped: Container.shared.resolve(ChatViewModel.self)!)

    @Environment(\.colorScheme) var colorScheme

    var channelLogin: String
    var body: some View {
        let displayName = switch onEnum(of: viewModel.state) {
        case .initial, .failed:
            channelLogin
        case let .chatting(state):
            state.user.displayName
        }

        InnerChattingView(
            state: viewModel.state,
            inputState: viewModel.inputState,
            updateInputMessage: { message in
                viewModel.onMessageInputChanged(
                    message: message,
                    selectionRange: KotlinIntRange(
                        start: Int32(message.count),
                        endInclusive: Int32(message.count)
                    )
                )
            },
            submit: {
                viewModel.submit(
                    screenDensity: 2.0,
                    isDarkTheme: colorScheme == .dark
                )
            },
            onEmoteClick: { emote in
                viewModel.appendEmote(emote: emote, autocomplete: true)
            },
            onChatterClick: { chatter in
                viewModel.appendChatter(chatter: chatter, autocomplete: true)
            }
        )
        .navigationTitle(displayName)
        .toolbarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadChat(channelLogin: channelLogin)
            // viewModel.onResume()
        }
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerChattingView: View {
    var state: ChatViewModel.State
    var inputState: ChatViewModel.InputState
    var updateInputMessage: (String) -> Void
    var submit: () -> Void
    var onEmoteClick: (Emote) -> Void
    var onChatterClick: (Chatter) -> Void

    var body: some View {
        VStack {
            switch onEnum(of: state) {
            case .initial, .failed:
                ProgressView()
            case let .chatting(state):
                ChatListView(
                    messages: state.chatMessages,
                    autoCompleteItems: inputState.autoCompleteItems,
                    inputMessage: inputState.message,
                    updateInputMessage: updateInputMessage,
                    submit: submit,
                    onEmoteClick: onEmoteClick,
                    onChatterClick: onChatterClick
                )
            }
        }
    }
}

private extension ChattingView {
    @Observable
    class ViewModel {
        let wrapped: ChatViewModel
        init(wrapped: ChatViewModel) {
            self.wrapped = wrapped
        }

        private(set) var state: ChatViewModel.State = ChatViewModel.StateInitial()
        private(set) var inputState: ChatViewModel.InputState = ChatViewModel.InputStateCompanion.shared.Empty

        func onResume() {
            wrapped.onResume()
        }

        func loadChat(channelLogin: String) {
            wrapped.loadChat(channelLogin: channelLogin)
        }

        func onShowUserInfo(userLogin: String) {
            wrapped.onShowUserInfo(userLogin: userLogin)
        }

        func onDismissUserInfo() {
            wrapped.onDismissUserInfo()
        }

        func onReplyToMessage(entry: ChatEventMessage?) {
            wrapped.onReplyToMessage(entry: entry)
        }

        func onMessageInputChanged(message: String, selectionRange: KotlinIntRange) {
            wrapped.onMessageInputChanged(message: message, selectionRange: selectionRange)
        }

        func onTriggerAutoComplete() {
            wrapped.onTriggerAutoComplete()
        }

        func appendEmote(emote: Emote, autocomplete: Bool) {
            wrapped.appendEmote(emote: emote, autocomplete: autocomplete)
        }

        func appendChatter(chatter: Chatter, autocomplete: Bool) {
            wrapped.appendChatter(chatter: chatter, autocomplete: autocomplete)
        }

        func submit(screenDensity: Float, isDarkTheme: Bool) {
            wrapped.submit(screenDensity: screenDensity, isDarkTheme: isDarkTheme)
        }

        func activate() async {
            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { @MainActor in
                    for await state in self.wrapped.state {
                        self.state = state
                    }
                }

                taskGroup.addTask { @MainActor in
                    for await inputState in self.wrapped.inputState {
                        self.inputState = inputState
                    }
                }
            }
        }
    }
}
