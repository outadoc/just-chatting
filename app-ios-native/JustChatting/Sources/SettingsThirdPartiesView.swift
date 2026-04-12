//
//  SettingsThirdPartiesView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsThirdPartiesView: View {
    let viewModel: SettingsViewModel

    @Environment(\.openURL) private var openURL

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
                                viewModel.updatePreferences(appPreferences: updatedPrefs(prefs, enableRecentMessages: newValue))
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
                                viewModel.updatePreferences(appPreferences: updatedPrefs(prefs, enablePronouns: newValue))
                            }
                        )
                    )
                    Button {
                        if let url = URL(string: "https://pronouns.alejo.io") {
                            openURL(url)
                        }
                    } label: {
                        HStack {
                            Text("Set your pronouns")
                                .foregroundStyle(.primary)
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                                .foregroundStyle(.secondary)
                        }
                    }
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
                                viewModel.updatePreferences(appPreferences: updatedPrefs(prefs, enableBttvEmotes: newValue))
                            }
                        )
                    )
                    Toggle(
                        "FrankerFaceZ",
                        isOn: Binding(
                            get: { prefs.enableFfzEmotes },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: updatedPrefs(prefs, enableFfzEmotes: newValue))
                            }
                        )
                    )
                    Toggle(
                        "7TV",
                        isOn: Binding(
                            get: { prefs.enableStvEmotes },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: updatedPrefs(prefs, enableStvEmotes: newValue))
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
        }
    }

    private func updatedPrefs(
        _ prefs: AppPreferences,
        enableRecentMessages: Bool? = nil,
        enablePronouns: Bool? = nil,
        enableBttvEmotes: Bool? = nil,
        enableFfzEmotes: Bool? = nil,
        enableStvEmotes: Bool? = nil
    ) -> AppPreferences {
        AppPreferences(
            apiToken: prefs.apiToken,
            showTimestamps: prefs.showTimestamps,
            enableRecentMessages: enableRecentMessages ?? prefs.enableRecentMessages,
            enableFfzEmotes: enableFfzEmotes ?? prefs.enableFfzEmotes,
            enableStvEmotes: enableStvEmotes ?? prefs.enableStvEmotes,
            enableBttvEmotes: enableBttvEmotes ?? prefs.enableBttvEmotes,
            enablePronouns: enablePronouns ?? prefs.enablePronouns,
            enableNotifications: prefs.enableNotifications
        )
    }
}
