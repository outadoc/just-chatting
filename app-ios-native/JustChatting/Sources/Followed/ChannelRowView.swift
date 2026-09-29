//
//  ChannelRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChannelRowView: View {
    let channelFollow: ChannelFollow

    var body: some View {
        UserRow(user: channelFollow.user) {
            Text("Following since \(channelFollow.followedAt.date.formatted(date: .abbreviated, time: .omitted))")
        }
    }
}
