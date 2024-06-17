//
//  MainNavigationView.swift
//  JustChatting
//
//  Created by Baptiste Candellier on 2023-10-17.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct MainNavigationView: View {
    @State private var selectedTab = Res.string.live
    var body: some View {
        NavigationStack {
            TabView(selection: $selectedTab) {
                LiveChannelsView()
                    .tabItem {
                        Image(systemName: "tv")
                        Text(Res.string.live.desc().localized())
                    }
                    .tag(Res.string.live)

                FollowedChannelsView()
                    .tabItem {
                        Image(systemName: "heart.fill")
                        Text(Res.string.channels.desc().localized())
                    }
                    .tag(Res.string.channels)

                SettingsView()
                    .tabItem {
                        Image(systemName: "gear")
                        Text(Res.string.settings.desc().localized())
                    }
                    .tag(Res.string.settings)
            }
            .navigationDestination(for: Screen.self) { screen in
                switch screen {
                case let .channel(channelLogin):
                    ChattingView(channelLogin: channelLogin)
                }
            }
            .navigationTitle(Text(selectedTab.desc().localized()))
        }
    }
}

enum Screen: Hashable {
    case channel(channelLogin: String)
}
