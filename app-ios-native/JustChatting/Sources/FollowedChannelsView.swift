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
            LoadableContent(isLoading: state.isLoading, isEmpty: state.data.isEmpty) {
                List(state.data, id: \.user.id, selection: $selectedChannelId) { follow in
                    NavigationLink(value: follow.user.id) {
                        ChannelRowView(channelFollow: follow)
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    viewModel.synchronize()
                }
            } empty: {
                ContentUnavailableView("No followed channels", systemImage: "heart.slash")
            }
        }
        .navigationTitle("Following")
        .onAppear {
            viewModel.synchronize()
        }
    }
}
