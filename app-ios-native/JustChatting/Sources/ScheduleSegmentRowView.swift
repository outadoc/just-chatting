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

    private var startDate: Date {
        Date(
            timeIntervalSince1970: Double(segment.startTime.epochSeconds)
                + Double(segment.startTime.nanosecondsOfSecond) / 1_000_000_000
        )
    }

    private var endDate: Date? {
        guard let endTime = segment.endTime else { return nil }
        return Date(
            timeIntervalSince1970: Double(endTime.epochSeconds)
                + Double(endTime.nanosecondsOfSecond) / 1_000_000_000
        )
    }

    private var timeRangeText: String {
        let start = startDate.formatted(.dateTime.hour().minute())
        if let end = endDate {
            return "\(start) – \(end.formatted(.dateTime.hour().minute()))"
        }
        return start
    }

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: segment.user.profileImageUrl)) { phase in
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
            .opacity(isCanceled ? 0.4 : 1.0)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(segment.user.displayName)
                        .font(.body.weight(.semibold))

                    if isCanceled {
                        Text("Canceled")
                            .font(.caption2.weight(.semibold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 5)
                            .padding(.vertical, 2)
                            .background(.red, in: Capsule())
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
