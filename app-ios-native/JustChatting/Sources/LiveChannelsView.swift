//
//  LiveChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveChannelsView: View {
    @State private var viewModel = KoinHelper().getLiveTimelineViewModel()

    var body: some View {
        NavigationStack {
            Observing(viewModel.state) { state in
                Group {
                    if state.live.isEmpty && state.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if state.live.isEmpty {
                        ContentUnavailableView("No live channels", systemImage: "tv.slash")
                    } else {
                        List(state.live, id: \.user.id) { userStream in
                            LiveStreamRowView(userStream: userStream)
                                .onTapGesture {
                                    viewModel.onChannelClick(userId: userStream.user.id)
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
        }
        .onAppear {
            viewModel.syncLiveStreamsPeriodically()
        }
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .navigateToChannel(let e):
                _ = e.userId // TODO: push channel/chat view
            }
        }
    }
}
