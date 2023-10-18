//
//  FollowedChannelItemView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI
import JCShared

struct FollowedChannelItemView: View {
    var channel: JCShared.ChannelFollow
    var body: some View {
        VStack {
            if let displayName = channel.userDisplayName {
                Text(displayName)
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
