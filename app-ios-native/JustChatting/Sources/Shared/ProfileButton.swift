//
//  ProfileButton.swift
//  JustChatting
//

import SwiftUI

extension EnvironmentValues {
    /// Profile picture of the logged-in user, shown by `ProfileButton`.
    @Entry var profileImageUrl: String?
}

/// The logged-in user's avatar, which opens the settings. Goes in the trailing edge of the
/// toolbar of every tab.
struct ProfileButton: View {
    @Environment(AppRouter.self) private var router
    @Environment(\.profileImageUrl) private var profileImageUrl

    var body: some View {
        Button {
            router.isSettingsPresented = true
        } label: {
            AvatarView(url: profileImageUrl, size: 36)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Profile and settings")
    }
}

/// Toolbar item holding a `ProfileButton`, without the glass background that would otherwise
/// surround the avatar.
struct ProfileToolbarItem: ToolbarContent {
    var body: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            ProfileButton()
        }
        .sharedBackgroundVisibility(.hidden)
    }
}
