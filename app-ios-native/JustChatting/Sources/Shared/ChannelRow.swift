//
//  ChannelRow.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Layout of every list row showing a channel: its avatar, its name with an optional value on the
/// trailing edge, and a few lines of details under it.
///
/// Details use the same text styles in every list: `ChannelRowTitle` for the main line, then
/// `ChannelRowCategoryLine` for the category and secondary information, then tags.
struct ChannelRow<Trailing: View, Details: View>: View {
    let user: User
    /// Circles the avatar in red, like on Twitch.
    var isLive = false
    /// Dims the avatar, for channels that aren't available.
    var isDimmed = false
    @ViewBuilder let trailing: Trailing
    @ViewBuilder let details: Details

    private static var avatarSize: CGFloat { 50 }
    private static var ringPadding: CGFloat { 3 }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AvatarView(url: user.profileImageUrl, size: Self.avatarSize)
                .opacity(isDimmed ? 0.4 : 1)
                .padding(Self.ringPadding)
                .overlay {
                    if isLive {
                        Circle().strokeBorder(Color.live, lineWidth: 2)
                    }
                }

            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text(user.displayName)
                        .font(.headline)
                        .lineLimit(1)

                    Spacer(minLength: 0)

                    trailing
                        .font(.subheadline.weight(.semibold))
                        .monospacedDigit()
                }

                details
            }
            // Short rows are centered on the avatar; taller ones start at its top.
            .frame(minHeight: Self.avatarSize + 2 * Self.ringPadding)
        }
        .padding(.vertical, 8)
        .accessibilityElement(children: .combine)
    }
}

extension ChannelRow where Trailing == EmptyView {
    init(user: User, isLive: Bool = false, isDimmed: Bool = false, @ViewBuilder details: () -> Details) {
        self.init(user: user, isLive: isLive, isDimmed: isDimmed, trailing: { EmptyView() }, details: details)
    }
}

extension ChannelRow where Trailing == EmptyView, Details == EmptyView {
    init(user: User, isLive: Bool = false) {
        self.init(user: user, isLive: isLive, trailing: { EmptyView() }, details: { EmptyView() })
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
                    .fontWeight(.semibold)
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
        .font(.footnote)
        .foregroundStyle(.secondary)
    }
}
