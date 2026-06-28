//
//  SettingsAppearanceView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsAppearanceView: View {
    let viewModel: SettingsViewModel

    var body: some View {
        Observing(viewModel.state) { state in
            let prefs = state.appPreferences
            Form {
                Section {
                    Toggle(
                        "Show timestamps",
                        isOn: Binding(
                            get: { prefs.showTimestamps },
                            set: { newValue in
                                viewModel.updatePreferences(appPreferences: AppPreferences(
                                    apiToken: prefs.apiToken,
                                    showTimestamps: newValue,
                                    enableRecentMessages: prefs.enableRecentMessages,
                                    enableFfzEmotes: prefs.enableFfzEmotes,
                                    enableStvEmotes: prefs.enableStvEmotes,
                                    enableBttvEmotes: prefs.enableBttvEmotes,
                                    enablePronouns: prefs.enablePronouns,
                                    enableNotifications: prefs.enableNotifications
                                ))
                            }
                        )
                    )
                }

                Section {
                    Button {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    } label: {
                        HStack {
                            Text("System animations")
                                .foregroundStyle(.primary)
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                                .foregroundStyle(.secondary)
                        }
                    }
                } footer: {
                    Text("Reduce motion in chat using iOS accessibility settings.")
                }
            }
            .navigationTitle("Appearance")
        }
    }
}
