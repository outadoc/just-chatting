//
//  SettingsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

enum SettingsPage: Hashable {
    case thirdParties
    case appearance
    case about
}

struct SettingsView: View {
    @SharedViewModel(\.settingsViewModel) private var viewModel
    @State private var selectedPage: SettingsPage?
    @State private var showLogoutConfirmation = false

    var body: some View {
        NavigationSplitView {
            Observing(viewModel.state) { state in
                List(selection: $selectedPage) {
                    accountSection(state: state)

                    Section {
                        NavigationLink(value: SettingsPage.thirdParties) {
                            Label("Third-party integrations", systemImage: "puzzlepiece.extension")
                        }

                        NavigationLink(value: SettingsPage.appearance) {
                            Label("Appearance", systemImage: "paintpalette")
                        }
                    }

                    Section {
                        NavigationLink(value: SettingsPage.about) {
                            LabeledContent {
                                if let version = state.appVersionName {
                                    Text(version)
                                }
                            } label: {
                                Label("About", systemImage: "info.circle")
                            }
                        }
                    }
                }
            }
            .navigationTitle("Settings")
        } detail: {
            // Own stack so that pages can push further details, like the open-source licences.
            NavigationStack {
                switch selectedPage {
                case .thirdParties:
                    SettingsSectionThirdParties(viewModel: viewModel)
                case .appearance:
                    SettingsSectionAppearance(viewModel: viewModel)
                case .about:
                    SettingsSectionAbout(viewModel: viewModel)
                case nil:
                    ContentUnavailableView("No section selected", systemImage: "gearshape")
                }
            }
            .id(selectedPage)
        }
        .alert("Log out?", isPresented: $showLogoutConfirmation) {
            Button("Log out", role: .destructive) {
                viewModel.logout()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("You will need to log in again to use the app.")
        }
    }

    @ViewBuilder
    private func accountSection(state: SettingsViewModel.State) -> some View {
        Section {
            if let user = state.user {
                ChannelRow(user: user) {
                    ChannelRowCategoryLine(category: nil, detail: Text(verbatim: "@\(user.login)"))
                }
            } else {
                // Same size as the account's ChannelRow, so that the list doesn't jump once loaded.
                HStack(spacing: 12) {
                    Circle()
                        .fill(.fill.tertiary)
                        .frame(width: 56, height: 56)
                    VStack(alignment: .leading, spacing: 6) {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(.fill.tertiary)
                            .frame(width: 100, height: 14)
                        RoundedRectangle(cornerRadius: 4)
                            .fill(.fill.tertiary)
                            .frame(width: 70, height: 11)
                    }
                }
                .padding(.vertical, 8)
                .accessibilityHidden(true)
            }

            Button(role: .destructive) {
                showLogoutConfirmation = true
            } label: {
                Text("Log out")
            }
        }
    }
}
