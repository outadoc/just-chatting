//
//  ConnectionErrorView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ConnectionErrorView: View {
    let viewModel: MainRouterViewModel

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            ContentUnavailableView {
                Label("Connection error", systemImage: "wifi.exclamationmark")
            } description: {
                Text("Twitch couldn't be reached. Check your internet connection and try again.")
            }

            Spacer()

            Button {
                viewModel.onRetryClick()
            } label: {
                Text("Retry")
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
            }
            .padding(.horizontal, 32)
            .buttonStyle(.borderedProminent)

            Button("Log out") {
                viewModel.onLogoutClick()
            }
            .padding(.bottom, 32)
        }
    }
}
