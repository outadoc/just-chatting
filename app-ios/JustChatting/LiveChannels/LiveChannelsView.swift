//
//  LiveChannelsView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct LiveChannelsView: View {
    @State private var viewModel = ViewModel(wrapped: Container.shared.resolve(FollowedStreamsViewModel.self)!)

    var body: some View {
        InnerLiveChannelsView(
            loadedItems: viewModel.loadedItems,
            loadMoreItems: viewModel.loadMoreItems,
            reload: viewModel.reload
        )
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerLiveChannelsView: View {
    var loadedItems: [JCShared.Stream]? = nil
    var loadMoreItems: () -> Void
    var reload: () -> Void

    var body: some View {
        VStack {
            if let loadedItems {
                List {
                    ForEach(loadedItems, id: \.id) { item in
                        NavigationLink(value: Screen.channel(channelLogin: item.userLogin)) {
                            LiveChannelItemView(stream: item)
                                .onAppear(perform: loadMoreItems)
                        }
                    }
                }
                .listStyle(.inset)
                .refreshable {
                    reload()
                }
            } else {
                ProgressView()
            }
        }
    }
}

private extension LiveChannelsView {
    @Observable
    class ViewModel {
        let wrapped: FollowedStreamsViewModel
        init(wrapped: FollowedStreamsViewModel) {
            self.wrapped = wrapped
        }

        private(set) var loadedItems: [JCShared.Stream]?

        private let pagingCollectionViewController = PagingCollectionViewController<JCShared.Stream>()

        func loadMoreItems() {
            if let loadedItems {
                pagingCollectionViewController.getItem(
                    position: Int32(loadedItems.count - 1)
                )
            }
        }

        func reload() {
            pagingCollectionViewController.refresh()
        }

        func activate() async {
            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { @MainActor in
                    for await pagingData in self.wrapped.pagingData {
                        do {
                            try await skie(self.pagingCollectionViewController).submitData(pagingData: pagingData)
                        } catch is CancellationError {
                        } catch {
                            logError(tag: "FollowedChannelsView", "Error when submitting paging data", error: error)
                        }
                    }
                }

                taskGroup.addTask { @MainActor in
                    for await loadedItems in self.pagingCollectionViewController.itemsFlow {
                        self.loadedItems = loadedItems
                    }
                }
            }
        }
    }
}
