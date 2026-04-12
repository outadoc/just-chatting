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
            AsyncImage(url: URL(string: channelFollow.user.profileImageUrl)) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().scaledToFill()
                case .failure, .empty:
                    Image(systemName: "person.circle.fill")
                        .resizable()
                        .foregroundStyle(.secondary)
                @unknown default:
                    EmptyView()
                }
            }
            .frame(width: 44, height: 44)
            .clipShape(Circle())

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
