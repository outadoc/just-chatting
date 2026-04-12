//
//  LiveStreamRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveStreamRowView: View {
    let userStream: UserStream

    var body: some View {
        HStack(spacing: 12) {
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
            .frame(width: 44, height: 44)
            .clipShape(Circle())
            .overlay(alignment: .bottomTrailing) {
                Circle()
                    .fill(.red)
                    .frame(width: 12, height: 12)
                    .offset(x: 2, y: 2)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(userStream.user.displayName)
                    .font(.body.weight(.semibold))

                Text(userStream.stream.title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)

                HStack(spacing: 4) {
                    if let category = userStream.stream.category {
                        Text(category.name)
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }

                    Spacer()

                    Label(
                        userStream.stream.viewerCount.formatted(),
                        systemImage: "person.2.fill"
                    )
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
                }
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }
}
