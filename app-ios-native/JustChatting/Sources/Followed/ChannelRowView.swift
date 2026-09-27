//
//  ChannelRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChannelRowView: View {
    let channelFollow: ChannelFollow

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: channelFollow.user.profileImageUrl, size: 44)

            VStack(alignment: .leading, spacing: 2) {
                Text(channelFollow.user.displayName)
                    .font(.body.weight(.semibold))

                Text("Following since \(channelFollow.followedAt.date.formatted(.relative(presentation: .named)))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }
}
