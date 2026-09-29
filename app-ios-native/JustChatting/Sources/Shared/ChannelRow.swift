//
//  ChannelRow.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// List row for a live channel: its avatar, its name with an optional value on the trailing
/// edge, and a few lines of details under it. Rows that only need a name use `UserRow`.
///
/// Details use the same text styles in every list: `ChannelRowTitle` for the main line, then
/// `ChannelRowCategoryLine` for the category and secondary information, then tags.
struct ChannelRow<Trailing: View, Details: View>: View {
    let user: User
    @ViewBuilder let trailing: Trailing
    @ViewBuilder let details: Details

    private static var avatarSize: CGFloat { 48 }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AvatarView(url: user.profileImageUrl, size: Self.avatarSize)

            VStack(alignment: .leading, spacing: 2) {
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text(user.displayName)
                        .font(.headline)
                        .lineLimit(1)

                    Spacer(minLength: 0)

                    trailing
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .monospacedDigit()
                }

                details
            }
            // Short rows are centered on the avatar; taller ones start at its top.
            .frame(minHeight: Self.avatarSize)
        }
        .padding(.vertical, 4)
        .accessibilityElement(children: .combine)
    }
}

/// Main line of a channel row's details, like a stream title.
struct ChannelRowTitle: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.subheadline)
            .lineLimit(2)
    }
}

/// Line of a channel row's details showing its category, if any, then secondary information.
struct ChannelRowCategoryLine: View {
    let category: String?
    var detail: Text?

    var body: some View {
        HStack(spacing: 4) {
            if let category {
                Text(category)
                    .fontWeight(.medium)
                    .foregroundStyle(.tint)
                    .lineLimit(1)
            }

            if category != nil, detail != nil {
                Text(verbatim: "·")
                    .accessibilityHidden(true)
            }

            if let detail {
                detail
                    .lineLimit(1)
            }
        }
        .font(.subheadline)
        .foregroundStyle(.secondary)
    }
}
