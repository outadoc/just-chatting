//
//  SettingsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsView: View {
    @State private var viewModel = KoinHelper().getSettingsViewModel()
    @State private var showLogoutConfirmation = false

    var body: some View {
        Observing(viewModel.state) { state in
            List {
                accountSection(state: state)

                Section {
                    NavigationLink {
                        SettingsThirdPartiesView(viewModel: viewModel)
                    } label: {
                        Label("Third-party integrations", systemImage: "puzzlepiece.extension")
                    }

                    NavigationLink {
                        SettingsAppearanceView(viewModel: viewModel)
                    } label: {
                        Label("Appearance", systemImage: "paintpalette")
                    }
                }

                if let version = state.appVersionName {
                    Section("About") {
                        LabeledContent("Version", value: version)
                    }
                }
            }
            .navigationTitle("Settings")
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
                HStack(spacing: 12) {
                    AsyncImage(url: URL(string: user.profileImageUrl)) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable().scaledToFill()
                        case .failure, .empty:
                            Image(systemName: "person.circle.fill")
                                .resizable()
                                .foregroundStyle(.secondary)
                        @unknown default:
                            EmptyView()
                        }
                    }
                    .frame(width: 44, height: 44)
                    .clipShape(Circle())

                    VStack(alignment: .leading, spacing: 2) {
                        Text(user.displayName)
                            .font(.body.weight(.semibold))
                        Text(verbatim: "@\(user.login)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(.vertical, 4)
            } else {
                HStack(spacing: 12) {
                    Circle()
                        .fill(.secondary.opacity(0.3))
                        .frame(width: 44, height: 44)
                    VStack(alignment: .leading, spacing: 4) {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(.secondary.opacity(0.3))
                            .frame(width: 100, height: 14)
                        RoundedRectangle(cornerRadius: 4)
                            .fill(.secondary.opacity(0.3))
                            .frame(width: 70, height: 11)
                    }
                }
                .padding(.vertical, 4)
            }

            Button(role: .destructive) {
                showLogoutConfirmation = true
            } label: {
                Text("Log out")
            }
        }
    }
}
