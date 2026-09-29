//
//  UserHeaderView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// A user's avatar, display name and login, for the top of a sheet. List rows use `ChannelRow`
/// or `UserRow`.
struct UserHeaderView: View {
    let user: User
    /// Adds when the account was created next to the login.
    var showsCreationDate = false

    var body: some View {
        HStack(spacing: 14) {
            AvatarView(url: user.profileImageUrl, size: showsCreationDate ? 64 : 52)

            VStack(alignment: .leading, spacing: 2) {
                Text(user.displayName)
                    .font(showsCreationDate ? .title2.weight(.bold) : .title3.weight(.semibold))
                subtitle
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var subtitle: Text {
        let login = Text(verbatim: "@\(user.login)")
        guard showsCreationDate else { return login }
        let createdAt = user.createdAt.date.formatted(.dateTime.month(.abbreviated).year())
        return Text("\(login) · since \(createdAt)")
    }
}
