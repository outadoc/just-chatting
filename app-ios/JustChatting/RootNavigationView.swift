//
//  RootNavigationView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import AuthenticationServices
import JCShared
import SwiftUI
import Swinject

struct RootNavigationView: View {
    @State private var viewModel = ViewModel(wrapped: Container.shared.resolve(MainRouterViewModel.self)!)

    var body: some View {
        InnerRootNavigationView(
            state: viewModel.state,
            lastEvent: viewModel.lastEvent,
            onLoginClick: viewModel.onLoginClick,
            onReceiveAuthResult: { url in
                viewModel.onReceiveIntent(uri: url.absoluteString)
            }
        )
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerRootNavigationView: View {
    var state: MainRouterViewModel.State
    var lastEvent: MainRouterViewModel.Event? = nil
    var onLoginClick: () -> Void
    var onReceiveAuthResult: (URL) -> Void

    @Environment(\.webAuthenticationSession) private var webAuthenticationSession

    var body: some View {
        VStack {
            switch onEnum(of: state) {
            case .loading:
                ProgressView()
            case let .loggedOut(state):
                LoggedOutView(
                    state: state,
                    onLoginClick: onLoginClick
                )
                .padding(.all)
            case .loggedIn:
                MainNavigationView()
            }
        }
        .onChange(of: lastEvent) {
            if let event = lastEvent {
                switch onEnum(of: event) {
                case let .openInBrowser(openEvent):
                    Task {
                        let callbackUrl = try await webAuthenticationSession.authenticate(
                            using: URL(string: openEvent.uri)!,
                            callbackURLScheme: "justchatting"
                        )

                        onReceiveAuthResult(callbackUrl)
                    }
                case .viewChannel:
                    break
                }
            }
        }
    }
}

private struct LoggedOutView: View {
    var state: MainRouterViewModel.StateLoggedOut
    var onLoginClick: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text(MR.strings.shared.onboarding_title.format(args: [MR.strings.shared.app_name.desc()]).localized())
                .font(.title)

            Text(MR.strings.shared.onboarding_message.desc().localized())
                .font(.subheadline)

            Button(MR.strings.shared.onboarding_login_action.desc().localized()) {
                onLoginClick()
            }
        }
    }
}

private extension RootNavigationView {
    @Observable
    class ViewModel {
        let wrapped: MainRouterViewModel
        init(wrapped: MainRouterViewModel) {
            self.wrapped = wrapped
        }

        private(set) var state: MainRouterViewModel.State = MainRouterViewModel.StateLoading()
        private(set) var lastEvent: MainRouterViewModel.Event?

        func onLoginClick() {
            wrapped.onLoginClick()
        }

        func onReceiveIntent(uri: String) {
            wrapped.onReceiveIntent(uri: uri)
        }

        func activate() async {
            wrapped.onStart()

            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { @MainActor in
                    for await state in self.wrapped.state {
                        self.state = state
                    }
                }
                taskGroup.addTask { @MainActor in
                    for await lastEvent in self.wrapped.events {
                        self.lastEvent = lastEvent
                    }
                }
            }
        }
    }
}
