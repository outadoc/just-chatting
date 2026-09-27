//
//  ScheduleView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct ScheduleView: View {
    @SharedViewModel(\.futureTimelineViewModel) private var viewModel

    var body: some View {
        Observing(viewModel.state) { state in
            LoadableContent(isLoading: state.isLoading, isEmpty: state.future.isEmpty) {
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
            } empty: {
                ContentUnavailableView(
                    "No upcoming streams",
                    systemImage: "calendar.badge.exclamationmark"
                )
            }
        }
        .navigationTitle("Schedule")
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
