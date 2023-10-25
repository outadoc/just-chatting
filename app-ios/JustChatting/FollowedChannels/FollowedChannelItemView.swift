//
//  FollowedChannelItemView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI

struct FollowedChannelItemView: View {
    var channel: JCShared.ChannelFollow
    var body: some View {
        HStack(spacing: 16) {
            if let avatarUrl = channel.profileImageURL {
                AvatarView(url: URL(string: avatarUrl)!)
            }

            VStack(alignment: .leading) {
                Text(channel.userDisplayName)

                if let followedAt = channel.followedAt.parseDate().formatDate() {
                    Text(MR.strings.shared.followed_at.format(args: [followedAt]).localized())
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }
}

struct FollowedChannelItemView_Previews: PreviewProvider {
    static var previews: some View {
        FollowedChannelItemView(
            channel: ChannelFollow(
                userId: "",
                userLogin: "hortyunderscore",
                userDisplayName: "HortyUnderscore",
                followedAt: "2023-10-20T20:13:00Z",
                profileImageURL: "https://placekitten.com/56/56"
            )
        )
    }
}
