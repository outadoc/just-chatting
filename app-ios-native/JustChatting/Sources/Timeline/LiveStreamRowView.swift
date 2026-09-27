//
//  LiveStreamRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveStreamRowView: View {
    let userStream: UserStream

    private var stream: JCShared.Stream { userStream.stream }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // The ring marks the channel as live.
            AvatarView(url: userStream.user.profileImageUrl, size: 50)
                .padding(3)
                .overlay(Circle().strokeBorder(Color.live, lineWidth: 2))

            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text(userStream.user.displayName)
                        .font(.headline)
                        .lineLimit(1)

                    Spacer(minLength: 0)

                    viewerCount
                }

                Text(stream.title)
                    .font(.subheadline)
                    .lineLimit(2)

                HStack(spacing: 4) {
                    if let category = stream.category {
                        Text(category.name)
                            .fontWeight(.semibold)
                            .foregroundStyle(.tint)
                            .lineLimit(1)
                        Text(verbatim: "·")
                            .foregroundStyle(.secondary)
                            .accessibilityHidden(true)
                    }
                    uptime
                }
                .font(.footnote)

                TagLine(tags: Array(stream.tags))
                    .font(.caption)
                    .padding(.top, 4)
            }
        }
        .padding(.vertical, 8)
        .accessibilityElement(children: .combine)
    }

    private var viewerCount: some View {
        let count = stream.viewerCount.formatted(.number.notation(.compactName).precision(.fractionLength(0...1)))
        return HStack(spacing: 4) {
            Circle()
                .fill(Color.live)
                .frame(width: 6, height: 6)
            Text(verbatim: count)
                .font(.subheadline.weight(.semibold))
                .monospacedDigit()
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text("\(count) viewers"))
    }

    /// How long the stream has been live, like "1h 27m". Refreshed every minute.
    private var uptime: some View {
        TimelineView(.everyMinute) { context in
            let elapsed = Duration.seconds(max(0, context.date.timeIntervalSince(stream.startedAt.date)))
            Text(elapsed.formatted(.units(allowed: [.days, .hours, .minutes], width: .narrow, maximumUnitCount: 2)))
                .foregroundStyle(.secondary)
                .monospacedDigit()
                .accessibilityLabel(
                    elapsed.formatted(.units(allowed: [.days, .hours, .minutes], width: .wide, maximumUnitCount: 2))
                )
        }
    }
}
