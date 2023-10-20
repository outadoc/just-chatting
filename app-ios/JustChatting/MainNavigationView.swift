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
    @State private var selectedTab = MR.strings.shared.live
    var body: some View {
        NavigationStack {
            TabView(selection: $selectedTab) {
                LiveChannelsView()
                    .tabItem {
                        Image(systemName: "tv")
                        Text(MR.strings.shared.live.desc().localized())
                    }
                    .tag(MR.strings.shared.live)

                FollowedChannelsView()
                    .tabItem {
                        Image(systemName: "heart.fill")
                        Text(MR.strings.shared.channels.desc().localized())
                    }
                    .tag(MR.strings.shared.channels)

                SettingsView()
                    .tabItem {
                        Image(systemName: "gear")
                        Text(MR.strings.shared.settings.desc().localized())
                    }
                    .tag(MR.strings.shared.settings)
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
