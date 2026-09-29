//
//  LiveChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveChannelsView: View {
    @Binding var selectedChannelId: String?
    @SharedViewModel(\.liveTimelineViewModel) private var viewModel

    var body: some View {
        Observing(viewModel.state) { state in
            LoadableContent(isLoading: state.isLoading, isEmpty: state.live.isEmpty) {
                List(state.live, id: \.user.id, selection: $selectedChannelId) { userStream in
                    NavigationLink(value: userStream.user.id) {
                        LiveStreamRowView(userStream: userStream)
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    viewModel.syncLiveStreamsNow()
                }
            } empty: {
                ContentUnavailableView("No live channels", systemImage: "tv.slash")
            }
        }
        .navigationTitle("Live")
        .toolbar {
            ProfileToolbarItem()
        }
        .onAppear {
            viewModel.syncLiveStreamsPeriodically()
        }
    }
}
