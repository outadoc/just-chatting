//
//  ContentView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 13/09/2024.
//  Copyright © 2024 Baptiste Candellier. All rights reserved.
//

import AuthenticationServices
import SwiftUI
import JCShared

struct MainView: View {

    @Environment(\.webAuthenticationSession) private var webAuthenticationSession

    private let receiver = DeeplinkReceiverHelper().getInstance()

    var body: some View {
        ComposeBridgeView(
            onShowAuthPage: { uri in
                Task {
                    do {
                        let urlWithToken = try await webAuthenticationSession.authenticate(
                            using: uri,
                            callbackURLScheme: "justchatting"
                        )

                        receiver.onDeeplinkReceived(uriString: urlWithToken.absoluteString)
                    } catch {
                    }
                }
            })
            .ignoresSafeArea(.all)
            .onOpenURL { url in
                receiver.onDeeplinkReceived(uriString: url.absoluteString)
            }
    }
}
