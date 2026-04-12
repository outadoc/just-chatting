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
    @State private var viewModel = KoinHelper().getMainRouterViewModel()

    var body: some View {
        Observing(viewModel.state) { state in
            switch onEnum(of: state) {
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loggedOut:
                OnboardingView(viewModel: viewModel)
            case .loggedIn:
                HomeTabView(viewModel: viewModel)
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
                } catch {}
            }
        case .navigateToTab, .viewChannel:
            break
        }
    }
}
