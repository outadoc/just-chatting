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
        ChannelRow(user: userStream.user, isLive: true) {
            viewerCount
        } details: {
            ChannelRowTitle(text: stream.title)

            // Refreshes the uptime every minute.
            TimelineView(.everyMinute) { context in
                ChannelRowCategoryLine(category: stream.category?.name, detail: uptime(at: context.date))
            }

            TagLine(tags: Array(stream.tags))
                .font(.caption)
                .padding(.top, 4)
        }
    }

    private var viewerCount: some View {
        let count = stream.viewerCount.formatted(.number.notation(.compactName).precision(.fractionLength(0...1)))
        return HStack(spacing: 4) {
            Circle()
                .fill(Color.live)
                .frame(width: 6, height: 6)
            Text(verbatim: count)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text("\(count) viewers"))
    }

    /// How long the stream has been live, like "1h 27m".
    private func uptime(at now: Date) -> Text {
        let elapsed = Duration.seconds(max(0, now.timeIntervalSince(stream.startedAt.date)))
        let units: Set<Duration.UnitsFormatStyle.Unit> = [.days, .hours, .minutes]
        return Text(elapsed.formatted(.units(allowed: units, width: .narrow, maximumUnitCount: 2)))
            .monospacedDigit()
            .accessibilityLabel(elapsed.formatted(.units(allowed: units, width: .wide, maximumUnitCount: 2)))
    }
}
