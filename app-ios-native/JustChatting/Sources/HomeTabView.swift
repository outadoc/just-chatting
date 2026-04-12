//
//  HomeTabView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct HomeTabView: View {
    let viewModel: MainRouterViewModel
    @State private var selectedTab: Int = 0 // 0 = Live (DefaultScreen)

    var body: some View {
        TabView(selection: $selectedTab) {
            Tab("Live", systemImage: "house", value: 0) {
                LiveChannelsView()
            }
            Tab("Schedule", systemImage: "calendar.badge.clock", value: 1) {
                Text("Schedule") // placeholder
            }
            Tab("Following", systemImage: "heart", value: 2) {
                FollowedChannelsView()
            }
            Tab("Search", systemImage: "magnifyingglass", value: 3) {
                Text("Search") // placeholder
            }
            Tab("Settings", systemImage: "person.circle", value: 4) {
                Text("Settings") // placeholder
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
