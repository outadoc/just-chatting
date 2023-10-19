//
//  SettingsView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct SettingsView: View {
    @StateObject private var viewModel = ViewModel(wrapped: Container.shared.resolve(SettingsViewModel.self)!)
    var body: some View {
        VStack {
            if let appPreferences = viewModel.appPreferences {
                InnerSettingsView(
                    appPreferences: appPreferences,
                    updatePreferences: { newPrefs in
                        viewModel.update(appPreferences: newPrefs)
                    }
                )
            }
        }
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerSettingsView: View {
    var appPreferences: AppPreferences
    var updatePreferences: (AppPreferences) -> Void

    private var enableRecentMessages: Binding<Bool> {
        Binding(
            get: { appPreferences.enableRecentMessages },
            set: { value in updatePreferences(appPreferences.doCopy(enableRecentMessages: value)) }
        )
    }

    private var enablePronouns: Binding<Bool> {
        Binding(
            get: { appPreferences.enablePronouns },
            set: { value in updatePreferences(appPreferences.doCopy(enablePronouns: value)) }
        )
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text(MR.strings.shared.settings_thirdparty_header.desc().localized())) {
                    Toggle(isOn: enableRecentMessages) {
                        Text(MR.strings.shared.settings_thirdparty_recent_title.desc().localized())
                    }

                    Toggle(isOn: enablePronouns) {
                        Text(MR.strings.shared.settings_thirdparty_pronouns_title.desc().localized())
                    }
                }
            }
            .navigationTitle(Text(MR.strings.shared.settings.desc().localized()))
        }
    }
}

private extension SettingsView {
    @MainActor
    class ViewModel: ObservableObject {
        let wrapped: SettingsViewModel
        init(wrapped: SettingsViewModel) {
            self.wrapped = wrapped
        }

        @Published
        private(set) var appPreferences: AppPreferences? = nil

        func update(appPreferences: AppPreferences) {
            wrapped.updatePreferences(appPreferences: appPreferences)
        }

        func activate() async {
            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { @MainActor in
                    for await appPreferences in self.wrapped.appPreferences {
                        self.appPreferences = appPreferences
                    }
                }
            }
        }
    }
}
