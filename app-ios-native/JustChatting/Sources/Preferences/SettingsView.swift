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

/// Settings, presented as a sheet from the profile button of any tab.
struct SettingsView: View {
    @SharedViewModel(\.settingsViewModel) private var viewModel
    @State private var showLogoutConfirmation = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Observing(viewModel.state) { state in
                List {
                    Section {
                        ProfileCard(user: state.user)
                    }

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

                    Section {
                        Button(role: .destructive) {
                            showLogoutConfirmation = true
                        } label: {
                            Text("Log out")
                                .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(for: SettingsPage.self) { page in
                switch page {
                case .thirdParties:
                    SettingsSectionThirdParties(viewModel: viewModel)
                case .appearance:
                    SettingsSectionAppearance(viewModel: viewModel)
                case .about:
                    SettingsSectionAbout(viewModel: viewModel)
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(role: .close) {
                        dismiss()
                    }
                }
            }
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
}

/// The logged-in user's avatar, names, description and account creation date.
private struct ProfileCard: View {
    let user: User?

    private static var avatarSize: CGFloat { 60 }

    var body: some View {
        if let user {
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 14) {
                    AvatarView(url: user.profileImageUrl, size: Self.avatarSize)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(user.displayName)
                            .font(.title3.weight(.semibold))
                        Text(verbatim: "@\(user.login)")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }

                if !user.description_.isEmpty {
                    Text(user.description_)
                }

                Label {
                    Text("Created on \(user.createdAt.date.formatted(date: .abbreviated, time: .omitted))")
                } icon: {
                    Image(systemName: "birthday.cake")
                }
                .font(.subheadline)
                .foregroundStyle(.secondary)
            }
            .padding(.vertical, 6)
            .accessibilityElement(children: .combine)
        } else {
            // Same size as the loaded card's first row, so that the list doesn't jump too much.
            HStack(spacing: 14) {
                Circle()
                    .fill(.fill.tertiary)
                    .frame(width: Self.avatarSize, height: Self.avatarSize)
                VStack(alignment: .leading, spacing: 6) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(.fill.tertiary)
                        .frame(width: 100, height: 16)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(.fill.tertiary)
                        .frame(width: 70, height: 12)
                }
            }
            .padding(.vertical, 6)
            .accessibilityHidden(true)
        }
    }
}
