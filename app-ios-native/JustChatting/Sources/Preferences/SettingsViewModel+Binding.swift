//
//  SettingsViewModel+Binding.swift
//  JustChatting
//

import JCShared
import SwiftUI

extension SettingsViewModel {
    /// Binding to one of the given preferences, which saves changes through this view model.
    ///
    ///     Toggle("7TV", isOn: viewModel.binding(prefs, \.enableStvEmotes) { $0.with(enableStvEmotes: $1) })
    func binding(
        _ preferences: AppPreferences,
        _ keyPath: KeyPath<AppPreferences, Bool>,
        set: @escaping (AppPreferences, Bool) -> AppPreferences
    ) -> Binding<Bool> {
        Binding(
            get: { preferences[keyPath: keyPath] },
            set: { newValue in
                self.updatePreferences(appPreferences: set(preferences, newValue))
            }
        )
    }
}
