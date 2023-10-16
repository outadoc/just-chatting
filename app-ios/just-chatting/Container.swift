//
//  Container.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-16.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import JCShared
import Swinject

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

    private func setupTwitch() {
        register(Kotlinx_serialization_jsonJson.self) {
            _ in JsonExtKt.DefaultJson
        }

        register(OAuthAppCredentials.self) {
            _ in OAuthAppCredentials(
                clientId: "l9klwmh97qgn0s0me276ezsft5szp2",
                redirectUri: "https://just-chatting.app/auth/callback.html"
            )
        }

        register(EmotesRepository.self) {
            r in EmotesRepository(
                helixApi: r.resolve(HelixApi.self)!,
                stvEmotesApi: r.resolve(StvEmotesApi.self)!,
                bttvEmotesApi: r.resolve(BttvEmotesApi.self)!,
                recentEmotes: r.resolve(RecentEmotesRepository.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!
            )
        }

        register(IdApi.self) {
            r in IdServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self, name: "twitch")!
            )
        }

        register(HelixApi.self) {
            r in HelixServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self, name: "twitch")!
            )
        }

        register(BttvEmotesApi.self) {
            r in BttvEmotesServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self)!
            )
        }

        register(StvEmotesApi.self) {
            r in StvEmotesServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self)!
            )
        }

        register(RecentMessagesApi.self) {
            r in RecentMessagesServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self)!
            )
        }
    }
}
