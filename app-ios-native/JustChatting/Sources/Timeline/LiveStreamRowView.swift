//
//  LiveStreamRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveStreamRowView: View {
    let userStream: UserStream

    private var tags: [String] {
        Array(userStream.stream.tags)
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AvatarView(url: userStream.user.profileImageUrl, size: 52)
                .overlay(alignment: .bottom) {
                    Text("LIVE")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundStyle(Color.onTint)
                        .padding(.horizontal, 4)
                        .padding(.vertical, 2)
                        .background(Color.live, in: Capsule())
                        .offset(y: 8)
                }
                .padding(.bottom, 8)

            VStack(alignment: .leading, spacing: 4) {
                Text(userStream.user.displayName)
                    .font(.body.weight(.semibold))

                Text(userStream.stream.title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)

                HStack(spacing: 4) {
                    if let category = userStream.stream.category {
                        Text(category.name)
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                            .lineLimit(1)
                    }

                    Spacer()

                    HStack(spacing: 2) {
                        Image(systemName: "person.2.fill")
                        Text(userStream.stream.viewerCount.formatted())
                    }
                    .font(.caption2)
                    .foregroundStyle(.tertiary)

                    Text(userStream.stream.startedAt.date, style: .relative)
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                        .monospacedDigit()
                        .padding(.leading, 6)
                }

                if !tags.isEmpty {
                    TagList(tags: tags)
                        .font(.caption2)
                }
            }
        }
        .padding(.vertical, 8)
    }
}
