//
//  ScheduleSegmentRowView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ScheduleSegmentRowView: View {
    let segment: ChannelScheduleSegment
    /// How far along the segment is, from 0 to 1, if it's happening now.
    var progress: Double?

    private var isCanceled: Bool {
        segment.canceledUntil != nil
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 0) {
                Text(segment.startTime.date, format: .dateTime.hour().minute())
                    .font(.subheadline.weight(.semibold))
                if let end = segment.endTime?.date {
                    Text(end, format: .dateTime.hour().minute())
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .monospacedDigit()
            .strikethrough(isCanceled)
            .frame(minWidth: 46, alignment: .leading)
            .padding(.top, 2)

            AvatarView(url: segment.user.profileImageUrl, size: 40)
                .opacity(isCanceled ? 0.4 : 1)

            VStack(alignment: .leading, spacing: 2) {
                Text(segment.user.displayName)
                    .font(.headline)
                    .lineLimit(1)

                if !segment.title.isEmpty {
                    ChannelRowTitle(text: segment.title)
                        .strikethrough(isCanceled)
                }

                if segment.category != nil || isCanceled {
                    ChannelRowCategoryLine(
                        category: segment.category?.name,
                        detail: isCanceled
                            ? Text("Canceled").fontWeight(.semibold).foregroundStyle(Color.warning)
                            : nil
                    )
                }

                if let progress {
                    ProgressView(value: progress)
                        .padding(.top, 6)
                }
            }
        }
        .padding(.vertical, 4)
        .accessibilityElement(children: .combine)
    }
}
