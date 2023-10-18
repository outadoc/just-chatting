//
//  LiveChannelsView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct LiveChannelsView: View {
    @StateObject private var viewModel = ViewModel(wrapped: Container.shared.resolve(FollowedStreamsViewModel.self)!)

    var body: some View {
        InnerLiveChannelsView(
            loadedItems: viewModel.loadedItems,
            loadMoreItems: viewModel.loadMoreItems
        )
    }
}

private struct InnerLiveChannelsView: View {
    var loadedItems: [JCShared.Stream]? = nil
    var loadMoreItems: () -> Void

    var body: some View {
        VStack {
            if let loadedItems {
                ScrollView {
                    LazyVStack {
                        ForEach(loadedItems, id: \.id) { item in
                            FollowedStreamItemView(stream: item)
                                .onAppear {
                                    // for loading more items
                                    loadMoreItems()
                                }
                        }
                    }
                }
            } else {
                ProgressView()
            }
        }
    }
}

struct FollowedStreamItemView: View {
    var stream: JCShared.Stream
    var body: some View {
        VStack {
            Text(stream.userName)
            Text(stream.title)
            if let gameName = stream.gameName {
                Text(gameName)
            }
        }
    }
}

extension LiveChannelsView {
    @MainActor
    class ViewModel: ObservableObject {
        let wrapped: FollowedStreamsViewModel
        init(wrapped: FollowedStreamsViewModel) {
            self.wrapped = wrapped
        }

        @Published
        private(set) var loadedItems: [JCShared.Stream]? = nil

        private let pagingCollectionViewController = PagingCollectionViewController<JCShared.Stream>()

        func loadMoreItems() {
            if let loadedItems {
                pagingCollectionViewController.getItem(position: Int32(loadedItems.count))
            }
        }

        func activate() async {
            await withTaskGroup(of: Void.self) { taskGroup in
                taskGroup.addTask { @MainActor in
                    for await pagingData in self.wrapped.pagingData {
                        do {
                            try await skie(self.pagingCollectionViewController).submitData(pagingData: pagingData)
                        } catch {
                            NSLog("Error when submitting paging data: \(error)")
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
