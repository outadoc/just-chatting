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
        // Keeps the week strip starting on the current day. What's happening now is kept up to
        // date by the view model.
        TimelineView(.everyMinute) { context in
            Observing(viewModel.state) { state in
                let today = Calendar.current.startOfDay(for: context.date)
                let day = selectedDay ?? today
                let schedules = schedulesByDay(state.days)

                VStack(spacing: 0) {
                    WeekStrip(
                        days: days(from: today),
                        selectedDay: day,
                        onSelect: { selectedDay = $0 }
                    )

                    Divider()

                    LoadableContent(isLoading: state.isLoading, isEmpty: state.days.isEmpty) {
                        dayContent(schedule: schedules[day], day: day)
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

    /// `schedule` is already split by the view model into what's happening now and what's to come.
    @ViewBuilder
    private func dayContent(schedule: ScheduleDay?, day: Date) -> some View {
        let ongoing = schedule?.ongoing ?? []
        let upcoming = schedule?.upcoming ?? []

        if ongoing.isEmpty && upcoming.isEmpty {
            ContentUnavailableView("Nothing scheduled", systemImage: "calendar")
                .frame(maxHeight: .infinity)
        } else {
            List {
                if !ongoing.isEmpty {
                    Section("Happening now") {
                        ForEach(ongoing, id: \.segment.id) { item in
                            ScheduleSegmentRowView(segment: item.segment, progress: item.progress?.doubleValue)
                        }
                    }
                }

                if !upcoming.isEmpty {
                    Section {
                        ForEach(upcoming, id: \.id) { segment in
                            ScheduleSegmentRowView(segment: segment)
                        }
                    } header: {
                        if schedule?.isToday == true {
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

    /// The schedule of each day, by start of that day.
    private func schedulesByDay(_ days: [ScheduleDay]) -> [Date: ScheduleDay] {
        var result: [Date: ScheduleDay] = [:]
        for scheduleDay in days {
            let components = scheduleDay.date.toNSDateComponents() as DateComponents
            guard let date = Calendar.current.date(from: components) else { continue }
            result[Calendar.current.startOfDay(for: date)] = scheduleDay
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
