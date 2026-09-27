//
//  HomeTabView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Root of the logged-in UI: a tab bar on iPhone, which can become a sidebar on iPad.
struct HomeTabView: View {
    @Bindable var router: AppRouter

    var body: some View {
        TabView(selection: $router.selectedTab) {
            Tab("Live", systemImage: "house", value: AppTab.live) {
                ChannelSplitView(selectedChannelId: $router.liveChannelId) {
                    LiveChannelsView(selectedChannelId: $router.liveChannelId)
                }
            }

            Tab("Schedule", systemImage: "calendar.badge.clock", value: AppTab.schedule) {
                NavigationStack {
                    ScheduleView()
                }
            }

            Tab("Following", systemImage: "heart", value: AppTab.followed) {
                ChannelSplitView(selectedChannelId: $router.followedChannelId) {
                    FollowedChannelsView(selectedChannelId: $router.followedChannelId)
                }
            }

            Tab("Settings", systemImage: "person.circle", value: AppTab.settings) {
                SettingsView()
            }

            Tab(value: AppTab.search, role: .search) {
                SearchView(selectedChannelId: $router.searchChannelId)
            }
        }
        .tabViewStyle(.sidebarAdaptable)
        .tabBarMinimizeBehavior(.onScrollDown)
    }
}
