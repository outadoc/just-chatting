//
//  ScheduleSegmentRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ScheduleSegmentRowView: View {
    let segment: ChannelScheduleSegment

    private var isCanceled: Bool {
        segment.canceledUntil != nil
    }

    private var timeRangeText: String {
        let start = segment.startTime.date.formatted(.dateTime.hour().minute())
        if let end = segment.endTime?.date {
            return "\(start) – \(end.formatted(.dateTime.hour().minute()))"
        }
        return start
    }

    var body: some View {
        HStack(spacing: 12) {
            AvatarView(url: segment.user.profileImageUrl, size: 44)
                .opacity(isCanceled ? 0.4 : 1.0)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(segment.user.displayName)
                        .font(.body.weight(.semibold))

                    if isCanceled {
                        Text("Canceled")
                            .font(.caption2.weight(.semibold))
                            .foregroundStyle(Color.onTint)
                            .padding(.horizontal, 5)
                            .padding(.vertical, 2)
                            .background(Color.warning, in: Capsule())
                    }
                }

                Text(segment.title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                    .strikethrough(isCanceled)

                HStack(spacing: 4) {
                    if let category = segment.category {
                        Text(category.name)
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }

                    Spacer()

                    Text(timeRangeText)
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                }
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }
}
