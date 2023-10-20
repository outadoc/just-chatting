//
//  FollowedChannelsView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct FollowedChannelsView: View {
    @StateObject private var viewModel = ViewModel(wrapped: Container.shared.resolve(FollowedChannelsViewModel.self)!)

    var body: some View {
        InnerFollowedChannelsView(
            loadedItems: viewModel.loadedItems,
            loadMoreItems: viewModel.loadMoreItems,
            reload: viewModel.reload
        )
        .task {
            await viewModel.activate()
        }
    }
}

private struct InnerFollowedChannelsView: View {
    var loadedItems: [JCShared.ChannelFollow]? = nil
    var loadMoreItems: () -> Void
    var reload: () -> Void

    var body: some View {
        NavigationStack {
            VStack {
                if let loadedItems {
                    List {
                        ForEach(loadedItems, id: \.userId) { item in
                            FollowedChannelItemView(channel: item)
                                .onAppear(perform: loadMoreItems)
                        }
                    }
                    .refreshable {
                        reload()
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle(Text(MR.strings.shared.channels.desc().localized()))
        }
    }
}

extension FollowedChannelsView {
    @MainActor
    class ViewModel: ObservableObject {
        let wrapped: FollowedChannelsViewModel
        init(wrapped: FollowedChannelsViewModel) {
            self.wrapped = wrapped
        }

        @Published
        private(set) var loadedItems: [JCShared.ChannelFollow]? = nil

        private let pagingCollectionViewController = PagingCollectionViewController<JCShared.ChannelFollow>()

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
