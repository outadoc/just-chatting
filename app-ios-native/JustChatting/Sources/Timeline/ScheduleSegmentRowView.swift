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
        ChannelRow(user: segment.user, isDimmed: isCanceled) {
            Text(verbatim: timeRangeText)
                .strikethrough(isCanceled)
        } details: {
            ChannelRowTitle(text: segment.title)
                .strikethrough(isCanceled)

            ChannelRowCategoryLine(
                category: segment.category?.name,
                detail: isCanceled
                    ? Text("Canceled").fontWeight(.semibold).foregroundStyle(Color.warning)
                    : nil
            )
        }
    }
}
