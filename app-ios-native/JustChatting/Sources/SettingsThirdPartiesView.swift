//
//  SettingsThirdPartiesView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsThirdPartiesView: View {
    let viewModel: SettingsViewModel

    private static let pronounsUrl = URL(string: "https://pronouns.alejo.io")!

    var body: some View {
        Observing(viewModel.state) { state in
            let prefs = state.appPreferences
            Form {
                Section {
                    Toggle(
                        "Recent messages",
                        isOn: Binding(
                            get: { prefs.enableRecentMessages },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: prefs.with(enableRecentMessages: newValue))
                            }
                        )
                    )
                } header: {
                    Text("Recent messages")
                } footer: {
                    Text("Load recent messages when opening a chat.")
                }

                Section {
                    Toggle(
                        "Show pronouns",
                        isOn: Binding(
                            get: { prefs.enablePronouns },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: prefs.with(enablePronouns: newValue))
                            }
                        )
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
                        isOn: Binding(
                            get: { prefs.enableBttvEmotes },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: prefs.with(enableBttvEmotes: newValue))
                            }
                        )
                    )
                    Toggle(
                        "FrankerFaceZ",
                        isOn: Binding(
                            get: { prefs.enableFfzEmotes },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: prefs.with(enableFfzEmotes: newValue))
                            }
                        )
                    )
                    Toggle(
                        "7TV",
                        isOn: Binding(
                            get: { prefs.enableStvEmotes },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: prefs.with(enableStvEmotes: newValue))
                            }
                        )
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
