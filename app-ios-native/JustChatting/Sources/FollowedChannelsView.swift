//
//  FollowedChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct FollowedChannelsView: View {
    @Binding var selectedChannelId: String?
    @SharedViewModel(\.followedChannelsViewModel) private var viewModel

    var body: some View {
        Observing(viewModel.state) { state in
            Group {
                if state.data.isEmpty && state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if state.data.isEmpty {
                    ContentUnavailableView("No followed channels", systemImage: "heart.slash")
                } else {
                    List(state.data, id: \.user.id, selection: $selectedChannelId) { follow in
                        NavigationLink(value: follow.user.id) {
                            ChannelRowView(channelFollow: follow)
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
        .onAppear {
            viewModel.synchronize()
        }
    }
}
