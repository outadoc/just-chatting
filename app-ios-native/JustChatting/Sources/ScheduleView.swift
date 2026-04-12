//
//  ScheduleView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ScheduleView: View {
    @State private var viewModel = KoinHelper().getFutureTimelineViewModel()

    var body: some View {
        NavigationStack {
            Observing(viewModel.state) { state in
                Group {
                    if state.future.isEmpty && state.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if state.future.isEmpty {
                        ContentUnavailableView(
                            "No upcoming streams",
                            systemImage: "calendar.badge.exclamationmark"
                        )
                    } else {
                        let sortedDates = state.future.keys.sorted { a, b in
                            if a.year != b.year { return a.year < b.year }
                            if a.monthNumber != b.monthNumber { return a.monthNumber < b.monthNumber }
                            return a.dayOfMonth < b.dayOfMonth
                        }
                        List {
                            ForEach(sortedDates, id: \.self) { localDate in
                                Section(header: Text(sectionTitle(for: localDate))) {
                                    ForEach(state.future[localDate] ?? [], id: \.id) { segment in
                                        ScheduleSegmentRowView(segment: segment)
                                    }
                                }
                            }
                        }
                        .listStyle(.plain)
                        .refreshable {
                            viewModel.syncEverythingNow()
                        }
                    }
                }
            }
            .navigationTitle("Schedule")
        }
        .onAppear {
            viewModel.syncEverythingNow()
        }
    }

    private func sectionTitle(for localDate: LocalDate) -> String {
        let components = localDate.toNSDateComponents() as DateComponents
        guard let date = Calendar.current.date(from: components) else {
            return "\(localDate.year)-\(localDate.monthNumber)-\(localDate.dayOfMonth)"
        }
        return date.formatted(.dateTime.weekday(.wide).month().day())
    }
}
