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
                List(selection: $selectedChannelId) {
                    ForEach(FollowedSection.sections(from: state.data)) { section in
                        Section(section.title) {
                            ForEach(section.follows, id: \.user.id) { follow in
                                NavigationLink(value: follow.user.id) {
                                    ChannelRowView(channelFollow: follow)
                                }
                            }
                        }
                        .sectionIndexLabel(Text(verbatim: section.title))
                    }
                }
                .listStyle(.plain)
                .listSectionIndexVisibility(.visible)
                .refreshable {
                    viewModel.synchronize()
                }
            } empty: {
                ContentUnavailableView("No followed channels", systemImage: "heart.slash")
            }
        }
        .navigationTitle("Following")
        .toolbar {
            ProfileToolbarItem()
        }
        .onAppear {
            viewModel.synchronize()
        }
    }
}

/// Followed channels starting with the same letter, like in the Contacts app.
private struct FollowedSection: Identifiable {
    /// A letter, or "#" for names that don't start with one.
    let title: String
    let follows: [ChannelFollow]

    var id: String { title }

    static func sections(from follows: [ChannelFollow]) -> [FollowedSection] {
        let sorted = follows.sorted {
            $0.user.displayName.localizedStandardCompare($1.user.displayName) == .orderedAscending
        }

        let grouped = Dictionary(grouping: sorted) { follow in
            sectionTitle(for: follow.user.displayName)
        }

        return grouped
            .map { FollowedSection(title: $0.key, follows: $0.value) }
            // "#" before letters.
            .sorted { lhs, rhs in
                if lhs.title == "#" { return rhs.title != "#" }
                if rhs.title == "#" { return false }
                return lhs.title.localizedStandardCompare(rhs.title) == .orderedAscending
            }
    }

    private static func sectionTitle(for name: String) -> String {
        guard let first = name.first, first.isLetter else { return "#" }
        // Groups "é" with "E", like the system does.
        return String(first).folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current).uppercased()
    }
}
