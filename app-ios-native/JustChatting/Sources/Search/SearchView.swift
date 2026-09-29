//
//  SearchView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SearchView: View {
    @Binding var selectedChannelId: String?
    @SharedViewModel(\.channelSearchViewModel) private var viewModel
    @State private var pager = SearchResultsPager()
    @State private var query: String = ""
    @State private var items: [ChannelSearchResult] = []
    @State private var isLoading = false

    var body: some View {
        ChannelSplitView(selectedChannelId: $selectedChannelId) {
            Observing(viewModel.state) { state in
                if query.isEmpty {
                    recentChannelsView(state: state)
                } else {
                    searchResultsView
                }
            }
            .navigationTitle("Search")
            .toolbar {
                ProfileToolbarItem()
            }
        }
        .searchable(text: $query, prompt: "Search channels")
        .onAppear {
            viewModel.onStart()
        }
        .onChange(of: query) { _, newValue in
            viewModel.onQueryChange(query: newValue)
        }
        .collect(flow: viewModel.pagingData) { pagingData in
            try? await pager.submitData(pagingData: pagingData)
        }
        .collect(flow: pager.onPagesUpdatedFlow) { _ in
            items = pager.snapshot()
        }
        .collect(flow: pager.loadStateFlow) { combined in
            if case .loading = onEnum(of: combined.refresh) {
                isLoading = true
            } else {
                isLoading = false
            }
        }
    }

    @ViewBuilder
    private func recentChannelsView(state: ChannelSearchViewModel.State) -> some View {
        if state.recentChannels.isEmpty {
            ContentUnavailableView(
                "Search for channels",
                systemImage: "magnifyingglass",
                description: Text("Find Twitch channels by name")
            )
        } else {
            List(selection: $selectedChannelId) {
                Section {
                    ForEach(state.recentChannels, id: \.id) { user in
                        NavigationLink(value: user.id) {
                            UserRow(user: user)
                        }
                        .swipeActions {
                            Button(role: .destructive) {
                                viewModel.onRemoveRecentChannel(user: user)
                            } label: {
                                Label("Remove", systemImage: "trash")
                            }
                        }
                    }
                } header: {
                    HStack(alignment: .firstTextBaseline) {
                        Text("Recent")
                        Spacer()
                        Button("Clear") {
                            viewModel.onClearRecentChannels()
                        }
                        .font(.subheadline)
                    }
                }
            }
            .listStyle(.plain)
            .headerProminence(.increased)
        }
    }

    private var searchResultsView: some View {
        LoadableContent(isLoading: isLoading, isEmpty: items.isEmpty) {
            List(selection: $selectedChannelId) {
                ForEach(Array(items.enumerated()), id: \.element.user.id) { index, result in
                    NavigationLink(value: result.user.id) {
                        UserRow(user: result.user)
                    }
                    .onAppear {
                        if index >= items.count - 5 {
                            _ = pager.getItem(index: Int32(index))
                        }
                    }
                }
            }
            .listStyle(.plain)
        } empty: {
            ContentUnavailableView.search(text: query)
        }
    }
}
