//
//  OnboardingView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct OnboardingView: View {
    let viewModel: MainRouterViewModel

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            Image("LogoForeground")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 90, height: 90)
                .foregroundStyle(.primary)

            VStack(spacing: 8) {
                Text("Welcome to")
                    .font(.title3)
                    .foregroundStyle(.secondary)
                Text("Just Chatting")
                    .font(.largeTitle.bold())
            }
            .accessibilityElement(children: .combine)

            Text("In order to browse your followed channels and post in chat, you will need to link your Twitch account.")
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 32)

            Spacer()

            Button {
                viewModel.onLoginClick()
            } label: {
                Text("Continue with Twitch")
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
            }
            .padding(.horizontal, 32)
            .buttonStyle(.borderedProminent)

            Button("Try the demo") {
                viewModel.onDemoModeClick()
            }
            .padding(.bottom, 32)
        }
    }
}
