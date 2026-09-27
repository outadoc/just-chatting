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
                        isOn: viewModel.binding(prefs, \.showTimestamps) { $0.with(showTimestamps: $1) }
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
