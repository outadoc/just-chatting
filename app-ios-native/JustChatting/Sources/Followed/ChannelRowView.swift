//
//  ChannelRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChannelRowView: View {
    let channelFollow: ChannelFollow

    var body: some View {
        ChannelRow(user: channelFollow.user) {
            ChannelRowCategoryLine(
                category: nil,
                detail: Text("Following since \(channelFollow.followedAt.date.formatted(.relative(presentation: .named)))")
            )
        }
    }
}
