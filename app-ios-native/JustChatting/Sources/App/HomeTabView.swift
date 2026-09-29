//
//  HomeTabView.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Root of the logged-in UI: a tab bar on iPhone, which can become a sidebar on iPad. The
/// settings are a sheet, opened from the profile button in the toolbar of every tab.
struct HomeTabView: View {
    @Bindable var router: AppRouter
    @SharedViewModel(\.settingsViewModel) private var settingsViewModel

    var body: some View {
        Observing(settingsViewModel.state) { state in
            TabView(selection: $router.selectedTab) {
                Tab("Live", systemImage: "dot.radiowaves.left.and.right", value: AppTab.live) {
                    ChannelSplitView(selectedChannelId: $router.liveChannelId) {
                        LiveChannelsView(selectedChannelId: $router.liveChannelId)
                    }
                }

                Tab("Schedule", systemImage: "calendar", value: AppTab.schedule) {
                    NavigationStack {
                        ScheduleView()
                    }
                }

                Tab("Following", systemImage: "heart", value: AppTab.followed) {
                    ChannelSplitView(selectedChannelId: $router.followedChannelId) {
                        FollowedChannelsView(selectedChannelId: $router.followedChannelId)
                    }
                }

                Tab(value: AppTab.search, role: .search) {
                    SearchView(selectedChannelId: $router.searchChannelId)
                }
            }
            .tabViewStyle(.sidebarAdaptable)
            .tabBarMinimizeBehavior(.onScrollDown)
            .environment(\.profileImageUrl, state.user?.profileImageUrl)
        }
        .environment(router)
        .sheet(isPresented: $router.isSettingsPresented) {
            SettingsView()
        }
    }
}
