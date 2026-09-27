//
//  SettingsSectionThirdParties.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsSectionThirdParties: View {
    let viewModel: SettingsViewModel

    private static let pronounsUrl = URL(string: "https://pronouns.alejo.io")!

    var body: some View {
        Observing(viewModel.state) { state in
            let prefs = state.appPreferences
            Form {
                Section {
                    Toggle(
                        "Recent messages",
                        isOn: viewModel.binding(prefs, \.enableRecentMessages) { $0.with(enableRecentMessages: $1) }
                    )
                } header: {
                    Text("Recent messages")
                } footer: {
                    Text("Load recent messages when opening a chat.")
                }

                Section {
                    Toggle(
                        "Show pronouns",
                        isOn: viewModel.binding(prefs, \.enablePronouns) { $0.with(enablePronouns: $1) }
                    )
                    ExternalLink("Set your pronouns", destination: Self.pronounsUrl)
                } header: {
                    Text("Pronouns")
                } footer: {
                    Text("Display pronouns next to usernames in chat.")
                }

                Section {
                    Toggle(
                        "BetterTTV",
                        isOn: viewModel.binding(prefs, \.enableBttvEmotes) { $0.with(enableBttvEmotes: $1) }
                    )
                    Toggle(
                        "FrankerFaceZ",
                        isOn: viewModel.binding(prefs, \.enableFfzEmotes) { $0.with(enableFfzEmotes: $1) }
                    )
                    Toggle(
                        "7TV",
                        isOn: viewModel.binding(prefs, \.enableStvEmotes) { $0.with(enableStvEmotes: $1) }
                    )
                } header: {
                    Text("Emotes")
                } footer: {
                    Text("Load third-party emote sets in chat.")
                }
            }
            .navigationTitle("Third-party integrations")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
