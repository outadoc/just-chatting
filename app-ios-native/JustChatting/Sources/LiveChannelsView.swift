//
//  LiveChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveChannelsView: View {
    @Binding var selectedChannelId: String?
    @State private var viewModel = KoinHelper().getLiveTimelineViewModel()

    var body: some View {
        Observing(viewModel.state) { state in
            Group {
                if state.live.isEmpty && state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if state.live.isEmpty {
                    ContentUnavailableView("No live channels", systemImage: "tv.slash")
                } else {
                    List(state.live, id: \.user.id, selection: $selectedChannelId) { userStream in
                        NavigationLink(value: userStream.user.id) {
                            LiveStreamRowView(userStream: userStream)
                        }
                    }
                    .listStyle(.plain)
                    .refreshable {
                        viewModel.syncLiveStreamsNow()
                    }
                }
            }
        }
        .navigationTitle("Live")
        .onAppear {
            viewModel.syncLiveStreamsPeriodically()
        }
    }
}
