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

            Text("Just Chatting")
                .font(.largeTitle.bold())

            Spacer()

            Button {
                viewModel.onLoginClick()
            } label: {
                Text("Sign in with Twitch")
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(twitchPurple, in: RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 48)
        }
    }
}
