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
                Section("Recent channels") {
                    ForEach(state.recentChannels, id: \.id) { user in
                        NavigationLink(value: user.id) {
                            recentChannelRow(user: user)
                        }
                        .swipeActions {
                            Button(role: .destructive) {
                                viewModel.onRemoveRecentChannel(user: user)
                            } label: {
                                Label("Remove", systemImage: "trash")
                            }
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
    }

    @ViewBuilder
    private var searchResultsView: some View {
        if isLoading && items.isEmpty {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if items.isEmpty {
            ContentUnavailableView.search(text: query)
        } else {
            List(selection: $selectedChannelId) {
                ForEach(Array(items.enumerated()), id: \.offset) { index, result in
                    NavigationLink(value: result.user.id) {
                        SearchResultRowView(result: result)
                    }
                    .onAppear {
                        if index >= items.count - 5 {
                            _ = pager.getItem(index: Int32(index))
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
    }

    @ViewBuilder
    private func recentChannelRow(user: User) -> some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: user.profileImageUrl)) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().scaledToFill()
                case .failure, .empty:
                    Image(systemName: "person.circle.fill")
                        .resizable()
                        .foregroundStyle(.secondary)
                @unknown default:
                    EmptyView()
                }
            }
            .frame(width: 44, height: 44)
            .clipShape(Circle())

            Text(user.displayName)
                .font(.body.weight(.semibold))
        }
        .padding(.vertical, 4)
    }
}
