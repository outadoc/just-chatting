//
//  HomeTabView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct HomeTabView: View {
    let viewModel: MainRouterViewModel
    @State private var selectedTab: Int = 0

    var body: some View {
        ChannelBrowserTab { navigate in
            TabView(selection: $selectedTab) {
                Tab("Live", systemImage: "house", value: 0) {
                    LiveChannelsView(navigateToChannel: navigate)
                }
                Tab("Schedule", systemImage: "calendar.badge.clock", value: 1) {
                    ScheduleView()
                }
                Tab("Following", systemImage: "heart", value: 2) {
                    FollowedChannelsView(navigateToChannel: navigate)
                }
                Tab("Search", systemImage: "magnifyingglass", value: 3) {
                    SearchView(navigateToChannel: navigate)
                }
                Tab("Settings", systemImage: "person.circle", value: 4) {
                    SettingsView()
                }
            }
            .collect(flow: viewModel.events) { event in
                switch onEnum(of: event) {
                case .navigateToTab(let e):
                    switch onEnum(of: e.screen) {
                    case .live: selectedTab = 0
                    case .future: selectedTab = 1
                    case .followed: selectedTab = 2
                    case .search: selectedTab = 3
                    case .settings: selectedTab = 4
                    }
                case .showAuthPage, .viewChannel:
                    break
                }
            }
            .onChange(of: selectedTab) { _, newTab in
                let screen: any Screen = switch newTab {
                case 1: ScreenFuture()
                case 2: ScreenFollowed()
                case 3: ScreenSearch()
                case 4: ScreenSettings()
                default: ScreenLive()
                }
                viewModel.onTabSelected(screen: screen)
            }
        }
    }
}

private struct ChannelBrowserTab<Content: View>: View {
    let content: (@escaping (String) -> Void) -> Content
    @State private var selectedUserId: String?
    @State private var preferredCompactColumn: NavigationSplitViewColumn = .sidebar

    init(_ content: @escaping (@escaping (String) -> Void) -> Content) {
        self.content = content
    }

    var body: some View {
        NavigationSplitView(preferredCompactColumn: $preferredCompactColumn) {
            content { userId in
                selectedUserId = userId
                preferredCompactColumn = .detail
            }
        } detail: {
            if let userId = selectedUserId {
                ChatView(userId: userId)
            } else {
                ContentUnavailableView("Select a channel", systemImage: "message")
            }
        }
    }
}
