//
//  MainNavigationView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-17.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import SwiftUI
import Swinject

struct MainNavigationView: View {
    var body: some View {
        TabView {
            LiveChannelsView()
                .tabItem {
                    Image(systemName: "tv")
                    Text(MR.strings.shared.live.desc().localized())
                }

            FollowedChannelsView()
                .tabItem {
                    Image(systemName: "heart.fill")
                    Text(MR.strings.shared.channels.desc().localized())
                }

            SettingsView()
                .tabItem {
                    Image(systemName: "gear")
                    Text(MR.strings.shared.settings.desc().localized())
                }
        }
    }
}
