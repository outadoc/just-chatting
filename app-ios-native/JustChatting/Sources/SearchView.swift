//
//  SearchView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SearchView: View {
    var navigateToChannel: (String) -> Void
    @State private var viewModel = KoinHelper().getChannelSearchViewModel()
    @State private var pager = SearchResultsPager()
    @State private var query: String = ""
    @State private var items: [ChannelSearchResult] = []
    @State private var isLoading = false

    var body: some View {
        Observing(viewModel.state) { state in
            if query.isEmpty {
                recentChannelsView(state: state)
            } else {
                searchResultsView
            }
        }
        .searchable(text: $query, prompt: "Search channels")
        .navigationTitle("Search")
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
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .navigateToChannel(let e):
                navigateToChannel(e.userId)
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
            List(state.recentChannels, id: \.id) { user in
                recentChannelRow(user: user)
                    .onTapGesture {
                        viewModel.onChannelClick(userId: user.id)
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
            List {
                ForEach(Array(items.enumerated()), id: \.offset) { index, result in
                    SearchResultRowView(result: result)
                        .onAppear {
                            if index >= items.count - 5 {
                                _ = pager.getItem(index: Int32(index))
                            }
                        }
                        .onTapGesture {
                            viewModel.onChannelClick(userId: result.user.id)
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

            Spacer()

            Button(role: .destructive) {
                viewModel.onRemoveRecentChannel(user: user)
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(.secondary)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
    }
}
