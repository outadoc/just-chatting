//
//  Container+DI.swift
//  JustChatting
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
        Container.shared.setupTwitch()
        Container.shared.setupMainNavigation()
        Container.shared.setupChat()
        Container.shared.setupMain()
    }

    private func setupHome() {
        register(ChannelSearchViewModel.self) { r in
            ChannelSearchViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }

        register(FollowedChannelsViewModel.self) { r in
            FollowedChannelsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }

        register(FollowedStreamsViewModel.self) { r in
            FollowedStreamsViewModel(
                repository: r.resolve(TwitchRepository.self)!
            )
        }
    }

    private func setupDb() {
        register(AppDatabase.self) { _ in
            AppDatabaseProvider().get()
        }

        register(RecentEmoteQueries.self) { r in
            r.resolve(AppDatabase.self)!.recentEmoteQueries
        }

        register(RecentEmotesRepository.self) { r in
            DbRecentEmotesRepository(
                recentEmoteQueries: r.resolve(RecentEmoteQueries.self)!
            )
        }
    }

    private func setupSettings() {
        register(SettingsViewModel.self) { r in
            SettingsViewModel(
                preferenceRepository: r.resolve(PreferenceRepository.self)!,
                authRepository: r.resolve(AuthRepository.self)!,
                logRepository: r.resolve(LogRepository.self)!
            )
        }

        register(LogRepository.self) { _ in
            NoopLogRepository()
        }

        register(ReadExternalDependenciesList.self) { _ in
            NoopReadExternalDependenciesList()
        }

        register(PreferenceRepository.self) { _ in
            UserDefaultsPreferenceRepository()
        }
    }

    private func setupTwitch() {
        register(Json.self) { _ in
            JsonExtKt.DefaultJson
        }

        register(OAuthAppCredentials.self) { _ in
            OAuthAppCredentials(
                clientId: "l9klwmh97qgn0s0me276ezsft5szp2",
                redirectUri: "https://just-chatting.app/auth/callback.html"
            )
        }

        register(EmotesRepository.self) { r in
            EmotesRepository(
                helixApi: r.resolve(HelixApi.self)!,
                stvEmotesApi: r.resolve(StvEmotesApi.self)!,
                bttvEmotesApi: r.resolve(BttvEmotesApi.self)!,
                recentEmotes: r.resolve(RecentEmotesRepository.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!
            )
        }

        register(TwitchRepository.self) { r in
            TwitchRepositoryImpl(
                helix: r.resolve(HelixApi.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!
            )
        }

        register(IdApi.self) { r in
            IdServer(
                httpClient: r.resolve(HttpClient.self, name: "twitch")!
            )
        }

        register(HelixApi.self) { r in
            HelixServer(
                httpClient: r.resolve(HttpClient.self, name: "twitch")!
            )
        }

        register(BttvEmotesApi.self) { r in
            BttvEmotesServer(
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(StvEmotesApi.self) { r in
            StvEmotesServer(
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(RecentMessagesApi.self) { r in
            RecentMessagesServer(
                httpClient: r.resolve(HttpClient.self)!
            )
        }
    }

    private func setupMainNavigation() {
        register(MainRouterViewModel.self) { r in
            MainRouterViewModel(
                authRepository: r.resolve(AuthRepository.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                deeplinkParser: r.resolve(DeeplinkParser.self)!,
                oAuthAppCredentials: r.resolve(OAuthAppCredentials.self)!
            )
        }
    }

    private func setupChat() {
        register(ChatViewModel.self) { r in
            ChatViewModel(
                clock: r.resolve(Clock.self)!,
                twitchRepository: r.resolve(TwitchRepository.self)!,
                emotesRepository: r.resolve(EmotesRepository.self)!,
                chatRepository: r.resolve(ChatRepository.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                emoteListSourcesProvider: r.resolve(EmoteListSourcesProvider.self)!,
                filterAutocompleteItemsUseCase: r.resolve(FilterAutocompleteItemsUseCase.self)!,
                pronounsRepository: r.resolve(PronounsRepository.self)!,
                createShortcutForChannel: r.resolve(CreateShortcutForChannelUseCase.self)!
            )
        }

        register(StreamAndUserInfoViewModel.self) { r in
            StreamAndUserInfoViewModel(
                twitchRepository: r.resolve(TwitchRepository.self)!
            )
        }

        register(FilterAutocompleteItemsUseCase.self) {
            _ in FilterAutocompleteItemsUseCase()
        }

        register(CreateShortcutForChannelUseCase.self) {
            _ in NoopCreateShortcutForChannelUseCase()
        }

        register(LiveChatWebSocket.Factory.self) { r in
            LiveChatWebSocket.Factory(
                clock: r.resolve(Clock.self)!,
                networkStateObserver: r.resolve(NetworkStateObserver.self)!,
                parser: r.resolve(TwitchIrcCommandParser.self)!,
                mapper: r.resolve(IrcMessageMapper.self)!,
                recentMessagesRepository: r.resolve(RecentMessagesRepository.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(LoggedInChatWebSocket.Factory.self) { r in
            LoggedInChatWebSocket.Factory(
                clock: r.resolve(Clock.self)!,
                networkStateObserver: r.resolve(NetworkStateObserver.self)!,
                parser: r.resolve(TwitchIrcCommandParser.self)!,
                mapper: r.resolve(IrcMessageMapper.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(MockChatWebSocket.Factory.self) { r in
            MockChatWebSocket.Factory(
                clock: r.resolve(Clock.self)!,
                networkStateObserver: r.resolve(NetworkStateObserver.self)!,
                parser: r.resolve(TwitchIrcCommandParser.self)!,
                mapper: r.resolve(IrcMessageMapper.self)!,
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(PubSubWebSocket.Factory.self) { r in
            PubSubWebSocket.Factory(
                networkStateObserver: r.resolve(NetworkStateObserver.self)!,
                httpClient: r.resolve(HttpClient.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                pubSubPluginsProvider: r.resolve(PubSubPluginsProvider.self)!
            )
        }

        register(ChatCommandHandlerFactoriesProvider.self) { r in
            class ChatCommandHandlerFactoriesProviderImpl: ChatCommandHandlerFactoriesProvider {
                let r: Resolver
                init(r: Resolver) {
                    self.r = r
                }

                func get() -> [ChatCommandHandlerFactory] {
                    return [
                        r.resolve(LiveChatWebSocket.Factory.self)!,
                        r.resolve(LoggedInChatWebSocket.Factory.self)!,
                        r.resolve(PubSubWebSocket.Factory.self)!,
                    ]
                }
            }

            return ChatCommandHandlerFactoriesProviderImpl(r: r)
        }

        register(PubSubBroadcastSettingsPlugin.self) { r in
            PubSubBroadcastSettingsPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubChannelPointsPlugin.self) { r in
            PubSubChannelPointsPlugin(
                clock: r.resolve(Clock.self)!,
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubPinnedMessagePlugin.self) { r in
            PubSubPinnedMessagePlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubPollPlugin.self) { r in
            PubSubPollPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubPredictionPlugin.self) { r in
            PubSubPredictionPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubRaidPlugin.self) { r in
            PubSubRaidPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubRichEmbedPlugin.self) { r in
            PubSubRichEmbedPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubViewerCountPlugin.self) { r in
            PubSubViewerCountPlugin(
                json: r.resolve(Json.self)!
            )
        }

        register(PubSubPluginsProvider.self) { r in
            class PubSubPluginsProviderImpl: PubSubPluginsProvider {
                let r: Resolver
                init(r: Resolver) {
                    self.r = r
                }

                func get() -> [PubSubPlugin] {
                    return [
                        r.resolve(PubSubChannelPointsPlugin.self)!,
                        r.resolve(PubSubPollPlugin.self)!,
                        r.resolve(PubSubPredictionPlugin.self)!,
                        r.resolve(PubSubBroadcastSettingsPlugin.self)!,
                        r.resolve(PubSubViewerCountPlugin.self)!,
                        r.resolve(PubSubRichEmbedPlugin.self)!,
                        r.resolve(PubSubPinnedMessagePlugin.self)!,
                        r.resolve(PubSubRaidPlugin.self)!,
                    ]
                }
            }

            return PubSubPluginsProviderImpl(r: r)
        }

        register(AggregateChatEventHandler.Factory.self) { r in
            AggregateChatEventHandler.Factory(
                chatCommandHandlerFactoriesProvider: r.resolve(ChatCommandHandlerFactoriesProvider.self)!
            )
        }

        register(ChatRepository.self) { r in
            DefaultChatRepository(
                factory: r.resolve(AggregateChatEventHandler.Factory.self)!
            )
        }

        register(TwitchIrcCommandParser.self) { r in
            TwitchIrcCommandParser(
                clock: r.resolve(Clock.self)!
            )
        }

        register(IrcMessageMapper.self) { _ in
            IrcMessageMapper()
        }

        register(RecentMessagesRepository.self) { r in
            RecentMessagesRepository(
                recentMessagesApi: r.resolve(RecentMessagesApi.self)!,
                parser: r.resolve(TwitchIrcCommandParser.self)!
            )
        }

        register(AlejoPronounsApi.self) { r in
            AlejoPronounsApi(
                httpClient: r.resolve(HttpClient.self)!
            )
        }

        register(PronounsRepository.self) { r in
            DefaultPronounsRepository(
                alejoPronounsApi: r.resolve(AlejoPronounsApi.self)!,
                preferenceRepository: r.resolve(PreferenceRepository.self)!
            )
        }

        register(ChannelBttvEmotesSource.self) { r in
            ChannelBttvEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(ChannelFfzEmotesSource.self) { r in
            ChannelFfzEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(ChannelStvEmotesSource.self) { r in
            ChannelStvEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(ChannelTwitchEmotesSource.self) { r in
            ChannelTwitchEmotesSource(
                delegateTwitchEmotesSource: r.resolve(DelegateTwitchEmotesSource.self)!
            )
        }

        register(GlobalBttvEmotesSource.self) { r in
            GlobalBttvEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(GlobalFfzEmotesSource.self) { r in
            GlobalFfzEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(GlobalStvEmotesSource.self) { r in
            GlobalStvEmotesSource(
                emotesRepository: r.resolve(EmotesRepository.self)!
            )
        }

        register(GlobalTwitchEmotesSource.self) { r in
            GlobalTwitchEmotesSource(
                delegateTwitchEmotesSource: r.resolve(DelegateTwitchEmotesSource.self)!
            )
        }

        register(DelegateTwitchEmotesSource.self) { r in
            DelegateTwitchEmotesSource(
                twitchRepository: r.resolve(TwitchRepository.self)!
            )
        }

        register(EmoteListSourcesProvider.self) { r in
            class EmoteListSourcesProviderImpl: EmoteListSourcesProvider {
                let r: Resolver
                init(r: Resolver) {
                    self.r = r
                }

                func getSources() -> [EmoteListSource] {
                    return [
                        r.resolve(ChannelTwitchEmotesSource.self)!,
                        r.resolve(ChannelBttvEmotesSource.self)!,
                        r.resolve(ChannelFfzEmotesSource.self)!,
                        r.resolve(ChannelStvEmotesSource.self)!,
                        r.resolve(GlobalTwitchEmotesSource.self)!,
                        r.resolve(GlobalBttvEmotesSource.self)!,
                        r.resolve(GlobalFfzEmotesSource.self)!,
                        r.resolve(GlobalStvEmotesSource.self)!,
                    ]
                }
            }

            return EmoteListSourcesProviderImpl(r: r)
        }
    }

    private func setupMain() {
        register(Clock.self) { _ in
            ClockSystem()
        }

        register(NetworkStateObserver.self) { _ in
            NoopNetworkStateObserver()
        }

        register(AuthRepository.self) { r in
            AuthRepository(
                api: r.resolve(IdApi.self)!,
                preferencesRepository: r.resolve(PreferenceRepository.self)!,
                oAuthAppCredentials: r.resolve(OAuthAppCredentials.self)!
            )
        }

        register(DeeplinkParser.self) { r in
            DeeplinkParser(
                oAuthAppCredentials: r.resolve(OAuthAppCredentials.self)!
            )
        }

        register(BaseHttpClientProvider.self) { r in
            AppleHttpClientProvider(
                json: r.resolve(Json.self)!
            )
        }

        register(HttpClient.self) { r in
            r.resolve(BaseHttpClientProvider.self)!.get(block: { _ in })
        }

        register(TwitchHttpClientProvider.self) { r in
            TwitchHttpClientProvider(
                baseHttpClientProvider: r.resolve(BaseHttpClientProvider.self)!,
                preferenceRepository: r.resolve(PreferenceRepository.self)!,
                oAuthAppCredentials: r.resolve(OAuthAppCredentials.self)!
            )
        }

        register(HttpClient.self, name: "twitch") { r in
            r.resolve(TwitchHttpClientProvider.self)!.get()
        }
    }
}
