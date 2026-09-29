//
//  ScheduleView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ScheduleView: View {
    @SharedViewModel(\.futureTimelineViewModel) private var viewModel

    /// Start of the day whose schedule is shown, or nil for today.
    @State private var selectedDay: Date?

    /// Number of days shown in the week strip, starting today.
    private static let visibleDayCount = 7

    var body: some View {
        // Refreshes what's happening now, and the current day, every minute.
        TimelineView(.everyMinute) { context in
            Observing(viewModel.state) { state in
                let today = Calendar.current.startOfDay(for: context.date)
                let day = selectedDay ?? today
                let schedules = schedulesByDay(state.future)

                VStack(spacing: 0) {
                    WeekStrip(
                        days: days(from: today),
                        selectedDay: day,
                        onSelect: { selectedDay = $0 }
                    )

                    Divider()

                    LoadableContent(isLoading: state.isLoading, isEmpty: state.future.isEmpty) {
                        dayContent(
                            segments: schedules[day] ?? [],
                            day: day,
                            isToday: day == today,
                            now: context.date
                        )
                    } empty: {
                        ContentUnavailableView(
                            "No upcoming streams",
                            systemImage: "calendar.badge.exclamationmark"
                        )
                    }
                }
            }
        }
        .navigationTitle("Schedule")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    selectedDay = nil
                } label: {
                    Label("Today", systemImage: "calendar")
                }
            }

            ProfileToolbarItem()
        }
        .onAppear {
            viewModel.syncEverythingNow()
        }
    }

    @ViewBuilder
    private func dayContent(
        segments: [ChannelScheduleSegment],
        day: Date,
        isToday: Bool,
        now: Date
    ) -> some View {
        // Today, only show what's ongoing or yet to come.
        let ongoing = isToday ? segments.filter { $0.isOngoing(at: now) } : []
        let upcoming = isToday ? segments.filter { $0.startTime.date > now } : segments

        if ongoing.isEmpty && upcoming.isEmpty {
            ContentUnavailableView("Nothing scheduled", systemImage: "calendar")
                .frame(maxHeight: .infinity)
        } else {
            List {
                if !ongoing.isEmpty {
                    Section("Happening now") {
                        ForEach(ongoing, id: \.id) { segment in
                            ScheduleSegmentRowView(segment: segment, progress: segment.progress(at: now))
                        }
                    }
                }

                if !upcoming.isEmpty {
                    Section {
                        ForEach(upcoming, id: \.id) { segment in
                            ScheduleSegmentRowView(segment: segment)
                        }
                    } header: {
                        if isToday {
                            Text("Later today")
                        } else {
                            Text(day, format: .dateTime.weekday(.wide).month().day())
                        }
                    }
                }
            }
            .listStyle(.plain)
            .headerProminence(.increased)
            .refreshable {
                viewModel.syncEverythingNow()
            }
        }
    }

    private func days(from today: Date) -> [Date] {
        (0..<Self.visibleDayCount).compactMap { offset in
            Calendar.current.date(byAdding: .day, value: offset, to: today)
        }
    }

    /// Scheduled segments, by start of their day.
    private func schedulesByDay(_ future: [DaySchedule]) -> [Date: [ChannelScheduleSegment]] {
        var result: [Date: [ChannelScheduleSegment]] = [:]
        for daySchedule in future {
            let components = daySchedule.date.toNSDateComponents() as DateComponents
            guard let date = Calendar.current.date(from: components) else { continue }
            result[Calendar.current.startOfDay(for: date), default: []] += daySchedule.schedule
        }
        return result
    }
}

/// A week of days, as their weekday initial and number. The selected one is highlighted.
private struct WeekStrip: View {
    let days: [Date]
    let selectedDay: Date
    let onSelect: (Date) -> Void

    var body: some View {
        HStack(spacing: 4) {
            ForEach(days, id: \.self) { day in
                let isSelected = day == selectedDay
                Button {
                    onSelect(day)
                } label: {
                    VStack(spacing: 4) {
                        Text(day, format: .dateTime.weekday(.narrow))
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.secondary)

                        Text(day, format: .dateTime.day())
                            .font(.headline)
                            .monospacedDigit()
                            .foregroundStyle(isSelected ? AnyShapeStyle(.background) : AnyShapeStyle(.primary))
                            .frame(width: 36, height: 36)
                            .background {
                                if isSelected {
                                    Circle().fill(.tint)
                                }
                            }
                    }
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(day, format: .dateTime.weekday(.wide).month().day()))
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
        .padding(.horizontal, 12)
        .padding(.bottom, 12)
    }
}

private extension ChannelScheduleSegment {
    func isOngoing(at now: Date) -> Bool {
        let start = startTime.date
        guard let end = endTime?.date else { return start <= now && Calendar.current.isDate(start, inSameDayAs: now) }
        return start <= now && now < end
    }

    /// How far along the segment is at `now`, from 0 to 1, if its end is known.
    func progress(at now: Date) -> Double? {
        guard let end = endTime?.date, end > startTime.date else { return nil }
        let elapsed = now.timeIntervalSince(startTime.date)
        let total = end.timeIntervalSince(startTime.date)
        return min(max(elapsed / total, 0), 1)
    }
}
