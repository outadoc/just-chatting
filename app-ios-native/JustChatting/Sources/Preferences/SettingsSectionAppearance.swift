//
//  SettingsSectionAppearance.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsSectionAppearance: View {
    let viewModel: SettingsViewModel

    private static let appSettingsUrl = URL(string: UIApplication.openSettingsURLString)!

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
                                viewModel.updatePreferences(appPreferences: prefs.with(showTimestamps: newValue))
                            }
                        )
                    )
                }

                Section {
                    ExternalLink("System animations", destination: Self.appSettingsUrl)
                } footer: {
                    Text("Reduce motion in chat using iOS accessibility settings.")
                }
            }
            .navigationTitle("Appearance")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
