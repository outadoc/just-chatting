//
//  Container.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import Swinject
import JCShared

extension Container {
    static let shared = Container()

    func setup() {
        Container.shared.setupHome()
        Container.shared.setupDb()
    }

    private func setupHome() {
        register(ChannelSearchViewModel.self) {
            r in ChannelSearchViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
        
        register(FollowedChannelsViewModel.self) {
            r in FollowedChannelsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
        
        register(FollowedStreamsViewModel.self) {
            r in FollowedStreamsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
    }
    
    private func setupDb() {
        register(SqlDriver.self) {
            r in NativeSqlDriver()
        }

        register(AppDatabase.self) {
            r in AppDatabaseCompanion().invoke(driver: r.resolve(SqlDriver.self))
        }
    }
}
