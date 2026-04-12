//
//  SearchResultRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SearchResultRowView: View {
    let result: ChannelSearchResult

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: result.user.profileImageUrl)) { phase in
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
                if result.isLive {
                    Circle()
                        .fill(.red)
                        .frame(width: 12, height: 12)
                        .offset(x: 2, y: 2)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(result.user.displayName)
                    .font(.body.weight(.semibold))

                if let gameName = result.gameName {
                    Text(gameName)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                if !result.title.isEmpty {
                    Text(result.title)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            if result.isLive {
                Label("Live", systemImage: "dot.radiowaves.left.and.right")
                    .font(.caption2.weight(.semibold))
                    .foregroundStyle(.red)
                    .labelStyle(.iconOnly)
            }
        }
        .padding(.vertical, 4)
    }
}
