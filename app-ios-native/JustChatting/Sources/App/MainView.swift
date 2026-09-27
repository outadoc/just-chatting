//
//  MainView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 13/09/2024.
//  Copyright © 2024 Baptiste Candellier. All rights reserved.
//

import AuthenticationServices
import JCShared
import SwiftUI

struct MainView: View {
    @Environment(\.webAuthenticationSession) private var webAuthenticationSession
    private let viewModel = KoinHelper().getMainRouterViewModel()
    @State private var router = AppRouter()

    var body: some View {
        Observing(viewModel.state) { state in
            switch onEnum(of: state) {
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loggedOut:
                OnboardingView(viewModel: viewModel)
            case .loggedIn:
                HomeTabView(router: router)
            }
        }
        .onAppear {
            viewModel.onStart()
        }
        .collect(flow: viewModel.events) { event in
            handleEvent(event)
        }
        .onOpenURL { url in
            viewModel.onDeeplinkReceived(uriString: url.absoluteString)
        }
    }

    private func handleEvent(_ event: MainRouterViewModel.Event) {
        switch onEnum(of: event) {
        case .showAuthPage(let e):
            guard let url = URL(string: (e.uri as AnyObject).description) else { return }
            Task {
                do {
                    let callback = try await webAuthenticationSession.authenticate(
                        using: url,
                        callbackURLScheme: "justchatting"
                    )
                    viewModel.onDeeplinkReceived(uriString: callback.absoluteString)
                } catch ASWebAuthenticationSessionError.canceledLogin {
                    // The user closed the login page; they can try again from onboarding.
                } catch {
                    Logger.shared.println(level: .error, tag: "MainView") {
                        "Twitch authentication failed: \(error)"
                    }
                }
            }
        case .navigateToTab(let e):
            router.selectedTab = AppTab(screen: e.screen)
        case .viewChannel(let e):
            router.openChannel(userId: e.userId)
        }
    }
}
