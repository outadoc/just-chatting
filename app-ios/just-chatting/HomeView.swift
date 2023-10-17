//
//  HomeView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct HomeView: View {
    @StateObject private var viewModel = ViewModel(wrapped: Container.shared.resolve(MainRouterViewModel.self)!)

    var body: some View {
        InnerHomeView(
            state: viewModel.state,
            lastEvent: viewModel.lastEvent,
            onLoginClick: viewModel.onLoginClick
        )
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerHomeView: View {
    var state: MainRouterViewModel.State
    var lastEvent: MainRouterViewModel.Event? = nil
    var onLoginClick: () -> Void

    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(spacing: 16) {
            Text("state: \(state), lastEvent: \(lastEvent?.description ?? "nil")")

            switch onEnum(of: state) {
            case .loading:
                ProgressView()
            case .loggedOut(let state):
                LoggedOutView(
                    state: state,
                    onLoginClick: onLoginClick
                )
            case .loggedIn:
                Text("Logged in :)")
            }
        }
    }
}

private struct LoggedOutView: View {
    var state: MainRouterViewModel.StateLoggedOut
    var onLoginClick: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text(MR.strings().onboarding_title.format(args: [MR.strings().app_name.desc()]).localized())
                .font(.title)

            Text(MR.strings().onboarding_message.desc().localized())
                .font(.subheadline)

            Button(MR.strings().onboarding_login_action.desc().localized()) {
                onLoginClick()
            }
        }
    }
}

private extension HomeView {
    @MainActor
    class ViewModel: ObservableObject {
        let wrapped: MainRouterViewModel
        init(wrapped: MainRouterViewModel) {
            self.wrapped = wrapped
        }

        @Published
        private(set) var state: MainRouterViewModel.State = MainRouterViewModel.StateLoading()

        @Published
        private(set) var lastEvent: MainRouterViewModel.Event? = nil

        func onLoginClick() {
            wrapped.onLoginClick()
        }

        func activate() async {
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
