//
//  UserHeaderView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// A user's avatar, display name and login.
struct UserHeaderView: View {
    enum Size {
        /// For list rows.
        case regular
        /// For the top of a sheet.
        case large
    }

    let user: User
    var size: Size = .regular

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: user.profileImageUrl, size: avatarSize)

            VStack(alignment: .leading, spacing: 2) {
                Text(user.displayName)
                    .font(nameFont)
                Text(verbatim: "@\(user.login)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var avatarSize: CGFloat {
        switch size {
        case .regular: 44
        case .large: 52
        }
    }

    private var nameFont: Font {
        switch size {
        case .regular: .body.weight(.semibold)
        case .large: .title3.weight(.semibold)
        }
    }
}
