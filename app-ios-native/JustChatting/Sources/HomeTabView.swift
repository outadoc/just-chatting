//
//  HomeTabView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct HomeTabView: View {
    let viewModel: MainRouterViewModel
    @State private var selectedTab: Int = 0 // 0 = Live (DefaultScreen)
    @State private var selectedUserId: String?

    var body: some View {
        NavigationSplitView {
            TabView(selection: $selectedTab) {
                Tab("Live", systemImage: "house", value: 0) {
                    LiveChannelsView { userId in
                        selectedUserId = userId
                    }
                }
                Tab("Schedule", systemImage: "calendar.badge.clock", value: 1) {
                    ScheduleView()
                }
                Tab("Following", systemImage: "heart", value: 2) {
                    FollowedChannelsView { userId in
                        selectedUserId = userId
                    }
                }
                Tab("Search", systemImage: "magnifyingglass", value: 3) {
                    SearchView { userId in
                        selectedUserId = userId
                    }
                }
                Tab("Settings", systemImage: "person.circle", value: 4) {
                    SettingsView()
                }
            }
        } detail: {
            if let userId = selectedUserId {
                ChatView(userId: userId)
                    .id(userId)
                    .onDisappear { selectedUserId = nil }
            } else {
                ContentUnavailableView("Select a channel", systemImage: "message")
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
