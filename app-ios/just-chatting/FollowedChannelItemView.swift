//
//  FollowedChannelItemView.swift
//  just-chatting
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

            VStack {
                if let displayName = channel.userDisplayName {
                    Text(displayName)
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
                followedAt: nil,
                profileImageURL: nil
            )
        )
    }
}
