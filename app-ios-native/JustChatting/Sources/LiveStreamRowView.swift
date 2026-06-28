//
//  LiveStreamRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveStreamRowView: View {
    let userStream: UserStream

    private var startedAtDate: Date {
        Date(
            timeIntervalSince1970: Double(userStream.stream.startedAt.epochSeconds)
                + Double(userStream.stream.startedAt.nanosecondsOfSecond) / 1_000_000_000
        )
    }

    private var tags: [String] {
        Array(userStream.stream.tags)
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AsyncImage(url: URL(string: userStream.user.profileImageUrl)) { phase in
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
            .frame(width: 52, height: 52)
            .clipShape(Circle())
            .overlay(alignment: .bottom) {
                Text("LIVE")
                    .font(.system(size: 9, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 4)
                    .padding(.vertical, 2)
                    .background(.red, in: Capsule())
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

                    Text(startedAtDate, style: .relative)
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                        .monospacedDigit()
                        .padding(.leading, 6)
                }

                if !tags.isEmpty {
                    FlowLayout(spacing: 4) {
                        ForEach(tags, id: \.self) { tag in
                            Text(tag)
                                .font(.caption2)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(.quaternary, in: Capsule())
                        }
                    }
                }
            }
        }
        .padding(.vertical, 8)
    }
}
