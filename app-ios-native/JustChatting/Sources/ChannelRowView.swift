//
//  ChannelRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ChannelRowView: View {
    let channelFollow: ChannelFollow

    private var followedAtDate: Date {
        let s = channelFollow.followedAt.epochSeconds
        let ns = channelFollow.followedAt.nanosecondsOfSecond
        return Date(timeIntervalSince1970: Double(s) + Double(ns) / 1_000_000_000)
    }

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: channelFollow.user.profileImageUrl, size: 44)

            VStack(alignment: .leading, spacing: 2) {
                Text(channelFollow.user.displayName)
                    .font(.body.weight(.semibold))

                Text("Following since \(followedAtDate.formatted(.relative(presentation: .named)))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }
}
