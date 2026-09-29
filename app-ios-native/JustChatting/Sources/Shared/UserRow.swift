//
//  UserRow.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Simple list row for a channel: its avatar and name, with an optional line under it.
struct UserRow<Subtitle: View>: View {
    let user: User
    @ViewBuilder let subtitle: Subtitle

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: user.profileImageUrl, size: 40)

            VStack(alignment: .leading, spacing: 0) {
                Text(user.displayName)
                    .lineLimit(1)

                subtitle
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
        }
        .padding(.vertical, 2)
        .accessibilityElement(children: .combine)
    }
}

extension UserRow where Subtitle == EmptyView {
    init(user: User) {
        self.init(user: user) { EmptyView() }
    }
}
