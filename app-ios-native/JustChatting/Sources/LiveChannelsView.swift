//
//  LiveChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct LiveChannelsView: View {
    @State private var viewModel = KoinHelper().getLiveTimelineViewModel()
    @State private var navigationPath = NavigationPath()

    var body: some View {
        NavigationStack(path: $navigationPath) {
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
            .navigationDestination(for: String.self) { userId in
                ChatView(userId: userId)
            }
        }
        .onAppear {
            viewModel.syncLiveStreamsPeriodically()
        }
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .navigateToChannel(let e):
                navigationPath.append(e.userId)
            }
        }
    }
}
