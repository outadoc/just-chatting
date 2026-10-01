//
//  SettingsSectionAppearance.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsSectionAppearance: View {
    let viewModel: SettingsViewModel

    var body: some View {
        Observing(viewModel.state) { state in
            let prefs = state.appPreferences
            Form {
                Section {
                    Toggle(
                        "Show timestamps",
                        isOn: viewModel.binding(prefs, \.showTimestamps) { $0.with(showTimestamps: $1) }
                    )
                    Toggle(
                        "Show animated emotes",
                        isOn: viewModel.binding(prefs, \.showAnimatedEmotes) { $0.with(showAnimatedEmotes: $1) }
                    )
                    Toggle(
                        "Show animated GIFs",
                        isOn: viewModel.binding(prefs, \.showAnimatedGifs) { $0.with(showAnimatedGifs: $1) }
                    )
                }
            }
            .navigationTitle("Appearance")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
