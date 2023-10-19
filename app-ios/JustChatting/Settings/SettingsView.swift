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
                    updatePreferences: { newPrefs in viewModel.update(appPreferences: newPrefs) },
                    logout: { viewModel.logout() }
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
    var logout: () -> Void

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

    private var enableBttvEmotes: Binding<Bool> {
        Binding(
            get: { appPreferences.enableBttvEmotes },
            set: { value in updatePreferences(appPreferences.doCopy(enableBttvEmotes: value)) }
        )
    }

    private var enableFfzEmotes: Binding<Bool> {
        Binding(
            get: { appPreferences.enableFfzEmotes },
            set: { value in updatePreferences(appPreferences.doCopy(enableFfzEmotes: value)) }
        )
    }

    private var enableStvEmotes: Binding<Bool> {
        Binding(
            get: { appPreferences.enableStvEmotes },
            set: { value in updatePreferences(appPreferences.doCopy(enableStvEmotes: value)) }
        )
    }

    private var showTimestamps: Binding<Bool> {
        Binding(
            get: { appPreferences.showTimestamps },
            set: { value in updatePreferences(appPreferences.doCopy(showTimestamps: value)) }
        )
    }

    private let appVersion: String = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as! String

    var body: some View {
        NavigationView {
            Form {
                Section(
                    header: Text(MR.strings.shared.settings_thirdparty_recent_header.desc().localized()),
                    footer: Text(MR.strings.shared.settings_thirdparty_recent_subtitle.desc().localized())
                ) {
                    Toggle(isOn: enableRecentMessages) {
                        Text(MR.strings.shared.settings_thirdparty_recent_title.desc().localized())
                    }
                }

                Section(header: Text(MR.strings.shared.settings_thirdparty_pronouns_header.desc().localized())) {
                    Toggle(isOn: enablePronouns) {
                        Text(MR.strings.shared.settings_thirdparty_pronouns_title.desc().localized())
                        Text(MR.strings.shared.settings_thirdparty_pronouns_subtitle.desc().localized())
                    }

                    Link(
                        MR.strings.shared.settings_thirdparty_pronouns_set_title.desc().localized(),
                        destination: URL(string: MR.strings.shared.app_pronouns_url.desc().localized())!
                    )
                }

                Section(header: Text(MR.strings.shared.settings_thirdparty_emotes_header.desc().localized())) {
                    Toggle(isOn: enableBttvEmotes) {
                        Text(MR.strings.shared.settings_thirdparty_bttv_title.desc().localized())
                        Text(MR.strings.shared.settings_thirdparty_bttv_subtitle.desc().localized())
                    }

                    Toggle(isOn: enableFfzEmotes) {
                        Text(MR.strings.shared.settings_thirdparty_ffz_title.desc().localized())
                        Text(MR.strings.shared.settings_thirdparty_ffz_subtitle.desc().localized())
                    }

                    Toggle(isOn: enableStvEmotes) {
                        Text(MR.strings.shared.settings_thirdparty_stv_title.desc().localized())
                        Text(MR.strings.shared.settings_thirdparty_stv_subtitle.desc().localized())
                    }
                }

                Section(
                    header: Text(MR.strings.shared.settings_accessibility_header.desc().localized()),
                    footer: Text(MR.strings.shared.settings_accessibility_animations_subtitle.desc().localized())
                ) {
                    Toggle(isOn: showTimestamps) {
                        Text(MR.strings.shared.settings_accessibility_timestamps_title.desc().localized())
                    }
                }

                Section(header: Text(MR.strings.shared.settings_account_header.desc().localized())) {
                    Button(
                        MR.strings.shared.settings_account_logout_action.desc().localized(),
                        role: .destructive,
                        action: logout
                    )
                }

                Section(header: Text(MR.strings.shared.settings_about_header.desc().localized())) {
                    LabeledContent(
                        MR.strings.shared.app_name.desc().localized(),
                        value: MR.strings.shared.settings_about_version.format(args: [appVersion]).localized()
                    )
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

        func logout() {
            wrapped.logout()
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
