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
        Container.shared.setupSettings()
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
        register(AppDatabase.self) {
            _ in AppDatabaseProvider().get()
        }
        
        register(RecentEmoteQueries.self) {
            r in r.resolve(AppDatabase.self)!.recentEmoteQueries
        }
        
        register(RecentEmotesRepository.self) {
            r in DbRecentEmotesRepository(
                recentEmoteQueries: r.resolve(RecentEmoteQueries.self)!
            )
        }
    }
    
    private func setupSettings() {
        register(SettingsViewModel.self) {
            r in SettingsViewModel(
                preferenceRepository: r.resolve(PreferenceRepository.self)!,
                authRepository: r.resolve(AuthRepository.self)!,
                logRepository: r.resolve(LogRepository.self)!
            )
        }
        
        register(LogRepository.self) {
            _ in NoopLogRepository()
        }
        
        register(ReadExternalDependenciesList.self) {
            _ in NoopReadExternalDependenciesList()
        }
        
        register(PreferenceRepository.self) {
            _ in NoopPreferenceRepository()
        }
    }
}
