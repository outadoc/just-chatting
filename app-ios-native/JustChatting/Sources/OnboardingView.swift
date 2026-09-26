//
//  OnboardingView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct OnboardingView: View {
    let viewModel: MainRouterViewModel

    private let twitchPurple = Color(red: 0x77 / 255.0, green: 0x18 / 255.0, blue: 0xAD / 255.0)

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            Image(systemName: "bubble.left.and.bubble.right")
                .resizable()
                .scaledToFit()
                .frame(width: 80, height: 80)
                .foregroundStyle(twitchPurple)

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
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(twitchPurple, in: RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 32)

            Button("Try the demo") {
                viewModel.onDemoModeClick()
            }
            .padding(.bottom, 32)
        }
    }
}
