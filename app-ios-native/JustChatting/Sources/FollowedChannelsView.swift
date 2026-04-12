//
//  FollowedChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct FollowedChannelsView: View {
    @State private var viewModel = KoinHelper().getFollowedChannelsViewModel()

    var body: some View {
        NavigationStack {
            Observing(viewModel.state) { state in
                Group {
                    if state.data.isEmpty && state.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if state.data.isEmpty {
                        ContentUnavailableView("No followed channels", systemImage: "heart.slash")
                    } else {
                        List(state.data, id: \.user.id) { follow in
                            ChannelRowView(channelFollow: follow)
                                .onTapGesture {
                                    viewModel.onChannelClick(userId: follow.user.id)
                                }
                        }
                        .listStyle(.plain)
                        .refreshable {
                            viewModel.synchronize()
                        }
                    }
                }
            }
            .navigationTitle("Following")
        }
        .onAppear {
            viewModel.synchronize()
        }
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .navigateToChannel(let e):
                _ = e.userId // TODO: push channel/chat view
            }
        }
    }
}
