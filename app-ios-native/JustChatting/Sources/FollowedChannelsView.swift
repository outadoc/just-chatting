//
//  FollowedChannelsView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct FollowedChannelsView: View {
    @State private var viewModel = KoinHelper().getFollowedChannelsViewModel()
    @State private var navigationPath = NavigationPath()

    var body: some View {
        NavigationStack(path: $navigationPath) {
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
            .navigationDestination(for: String.self) { userId in
                ChatView(userId: userId)
            }
        }
        .onAppear {
            viewModel.synchronize()
        }
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .navigateToChannel(let e):
                navigationPath.append(e.userId)
            }
        }
    }
}
