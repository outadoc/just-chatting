//
//  UserHeaderView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// A user's avatar, display name and login, for the top of a sheet. List rows use `ChannelRow`.
struct UserHeaderView: View {
    let user: User

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: user.profileImageUrl, size: 52)

            VStack(alignment: .leading, spacing: 2) {
                Text(user.displayName)
                    .font(.title3.weight(.semibold))
                Text(verbatim: "@\(user.login)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
    }
}
