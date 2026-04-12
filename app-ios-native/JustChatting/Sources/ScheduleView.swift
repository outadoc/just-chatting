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
                        List {
                            ForEach(state.future, id: \.self) { daySchedule in
                                Section(header: Text(sectionTitle(for: daySchedule.date))) {
                                    ForEach(daySchedule.schedule, id: \.id) { segment in
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

    private func sectionTitle(for localDate: JCLocalDate) -> String {
        let components = localDate.toNSDateComponents() as DateComponents
        guard let date = Calendar.current.date(from: components) else {
            return "\(components.year)-\(components.month)-\(components.day)"
        }
        return date.formatted(.dateTime.weekday(.wide).month().day())
    }
}
