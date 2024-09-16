//
//  ContentView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 13/09/2024.
//  Copyright © 2024 Baptiste Candellier. All rights reserved.
//

import AuthenticationServices
import UIKit
import SwiftUI
import JCShared

struct ComposeView: UIViewControllerRepresentable {

    var onShowAuthPage: (URL) -> Void

    init(onShowAuthPage: @escaping (URL) -> Void) {
        self.onShowAuthPage = onShowAuthPage
    }

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.getMainViewController(onShowAuthPage: onShowAuthPage)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct MainView: View {

    @Environment(\.webAuthenticationSession) private var webAuthenticationSession

    private let receiver = DeeplinkReceiverHelper().getInstance()

    var body: some View {
        ComposeView(
            onShowAuthPage: { uri in
                Task {
                    do {
                        let urlWithToken = try await webAuthenticationSession.authenticate(
                            using: uri,
                            callbackURLScheme: "justchatting"
                        )

                        receiver.onDeeplinkReceived(uri: urlWithToken.absoluteString)
                    } catch {
                    }
                }
            })
            .ignoresSafeArea(.all)
            .onOpenURL { url in
                receiver.onDeeplinkReceived(uri: url.absoluteString)
            }
    }
}
