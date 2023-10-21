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
    @StateObject private var viewModel = ViewModel(wrapped: Container.shared.resolve(ChatViewModel.self)!)

    var channelLogin: String
    var body: some View {
        InnerChattingView(state: viewModel.state)
            .navigationTitle(channelLogin)
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
    var body: some View {
        VStack {
            switch onEnum(of: state) {
            case .initial:
                ProgressView()
            case let .chatting(state):
                Text(state.user.displayName)
            }
        }
    }
}

struct ChattingView_Previews: PreviewProvider {
    static var previews: some View {
        ChattingView(channelLogin: "HortyUnderscore")
    }
}

private extension ChattingView {
    @MainActor
    class ViewModel: ObservableObject {
        let wrapped: ChatViewModel
        init(wrapped: ChatViewModel) {
            self.wrapped = wrapped
        }

        @Published
        private(set) var state: ChatViewModel.State = ChatViewModel.StateInitial()

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
            }
        }
    }
}
