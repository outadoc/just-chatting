//
//  Container+DI.swift
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
        Container.shared.setupTwitch()
        Container.shared.setupMainNavigation()
        Container.shared.setupChat()
    }

    private func setupHome() {
        register(ChannelSearchViewModel.self) { _, repository in
            ChannelSearchViewModel(
                repository: repository
            )
        }

        register(FollowedChannelsViewModel.self) { _, repository in
            FollowedChannelsViewModel(
                repository: repository
            )
        }

        register(FollowedStreamsViewModel.self) { _, repository in
            FollowedStreamsViewModel(
                repository: repository
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

        register(RecentEmotesRepository.self) { _, recentEmoteQueries in
            DbRecentEmotesRepository(
                recentEmoteQueries: recentEmoteQueries
            )
        }
    }

    private func setupSettings() {
        register(SettingsViewModel.self) { _, preferenceRepository, authRepository, logRepository in
            SettingsViewModel(
                preferenceRepository: preferenceRepository,
                authRepository: authRepository,
                logRepository: logRepository
            )
        }

        register(LogRepository.self) { _ in
            NoopLogRepository()
        }

        register(ReadExternalDependenciesList.self) { _ in
            NoopReadExternalDependenciesList()
        }

        register(PreferenceRepository.self) { _ in
            NoopPreferenceRepository()
        }
    }

    private func setupTwitch() {
        register(Kotlinx_serialization_jsonJson.self) { _ in
            JsonExtKt.DefaultJson
        }

        register(OAuthAppCredentials.self) { _ in
            OAuthAppCredentials(
                clientId: "l9klwmh97qgn0s0me276ezsft5szp2",
                redirectUri: "https://just-chatting.app/auth/callback.html"
            )
        }

        register(EmotesRepository.self) { _, helixApi, stvEmotesApi, bttvEmotesApi, recentEmotes, preferencesRepository in
            EmotesRepository(
                helixApi: helixApi,
                stvEmotesApi: stvEmotesApi,
                bttvEmotesApi: bttvEmotesApi,
                recentEmotes: recentEmotes,
                preferencesRepository: preferencesRepository
            )
        }

        register(IdApi.self) { r in
            IdServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self, name: "twitch")!
            )
        }

        register(HelixApi.self) { r in
            HelixServer(
                httpClient: r.resolve(Ktor_client_coreHttpClient.self, name: "twitch")!
            )
        }

        register(BttvEmotesApi.self) { _, httpClient in
            BttvEmotesServer(
                httpClient: httpClient
            )
        }

        register(StvEmotesApi.self) { _, httpClient in
            StvEmotesServer(
                httpClient: httpClient
            )
        }

        register(RecentMessagesApi.self) { _, httpClient in
            RecentMessagesServer(
                httpClient: httpClient
            )
        }
    }

    private func setupMainNavigation() {
        register(MainRouterViewModel.self) { _, authRepository, preferencesRepository, deeplinkParser, oAuthAppCredentials in
            MainRouterViewModel(
                authRepository: authRepository,
                preferencesRepository: preferencesRepository,
                deeplinkParser: deeplinkParser,
                oAuthAppCredentials: oAuthAppCredentials
            )
        }
    }

    private func setupChat() {
        register(ChatViewModel.self) { _, clock, twitchRepository, emotesRepository, chatRepository, preferencesRepository, emoteListSourcesProvider, filterAutocompleteItemsUseCase, pronounsRepository, createShortcutForChannel in
            ChatViewModel(
                clock: clock,
                twitchRepository: twitchRepository,
                emotesRepository: emotesRepository,
                chatRepository: chatRepository,
                preferencesRepository: preferencesRepository,
                emoteListSourcesProvider: emoteListSourcesProvider,
                filterAutocompleteItemsUseCase: filterAutocompleteItemsUseCase,
                pronounsRepository: pronounsRepository,
                createShortcutForChannel: createShortcutForChannel
            )
        }

        register(StreamAndUserInfoViewModel.self) { _, twitchRepository in
            StreamAndUserInfoViewModel(
                twitchRepository: twitchRepository
            )
        }

        register(FilterAutocompleteItemsUseCase.self) {
            _ in FilterAutocompleteItemsUseCase()
        }

        register(CreateShortcutForChannelUseCase.self) {
            _ in NoopCreateShortcutForChannelUseCase()
        }

        register(LiveChatWebSocket.Factory.self) { _, clock, networkStateObserver, parser, mapper, recentMessagesRepository, preferencesRepository, httpClient in
            LiveChatWebSocket.Factory(
                clock: clock,
                networkStateObserver: networkStateObserver,
                parser: parser,
                mapper: mapper,
                recentMessagesRepository: recentMessagesRepository,
                preferencesRepository: preferencesRepository,
                httpClient: httpClient
            )
        }

        register(LoggedInChatWebSocket.Factory.self) { _, clock, networkStateObserver, parser, mapper, preferencesRepository, httpClient in
            LoggedInChatWebSocket.Factory(
                clock: clock,
                networkStateObserver: networkStateObserver,
                parser: parser,
                mapper: mapper,
                preferencesRepository: preferencesRepository,
                httpClient: httpClient
            )
        }

        register(MockChatWebSocket.Factory.self) { _, clock, networkStateObserver, parser, mapper, httpClient in
            MockChatWebSocket.Factory(
                clock: clock,
                networkStateObserver: networkStateObserver,
                parser: parser,
                mapper: mapper,
                httpClient: httpClient
            )
        }

        register(PubSubWebSocket.Factory.self) { _, networkStateObserver, httpClient, preferencesRepository, pubSubPluginsProvider in
            PubSubWebSocket.Factory(
                networkStateObserver: networkStateObserver,
                httpClient: httpClient,
                preferencesRepository: preferencesRepository,
                pubSubPluginsProvider: pubSubPluginsProvider
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

        register(PubSubBroadcastSettingsPlugin.self) { _, json in
            PubSubBroadcastSettingsPlugin(
                json: json
            )
        }

        register(PubSubChannelPointsPlugin.self) { _, clock, json in
            PubSubChannelPointsPlugin(
                clock: clock,
                json: json
            )
        }

        register(PubSubPinnedMessagePlugin.self) { _, json in
            PubSubPinnedMessagePlugin(
                json: json
            )
        }

        register(PubSubPollPlugin.self) { _, json in
            PubSubPollPlugin(
                json: json
            )
        }

        register(PubSubPredictionPlugin.self) { _, json in
            PubSubPredictionPlugin(
                json: json
            )
        }

        register(PubSubRaidPlugin.self) { _, json in
            PubSubRaidPlugin(
                json: json
            )
        }

        register(PubSubRichEmbedPlugin.self) { _, json in
            PubSubRichEmbedPlugin(
                json: json
            )
        }

        register(PubSubViewerCountPlugin.self) { _, json in
            PubSubViewerCountPlugin(
                json: json
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

        register(AggregateChatEventHandler.Factory.self) { _, chatCommandHandlerFactoriesProvider in
            AggregateChatEventHandler.Factory(
                chatCommandHandlerFactoriesProvider: chatCommandHandlerFactoriesProvider
            )
        }

        register(ChatRepository.self) { _, factory in
            DefaultChatRepository(
                factory: factory
            )
        }

        register(TwitchIrcCommandParser.self) { _, clock in
            TwitchIrcCommandParser(
                clock: clock
            )
        }

        register(IrcMessageMapper.self) { _ in
            IrcMessageMapper()
        }

        register(RecentMessagesRepository.self) { _, recentMessagesApi, parser in
            RecentMessagesRepository(
                recentMessagesApi: recentMessagesApi,
                parser: parser
            )
        }

        register(AlejoPronounsApi.self) { _, httpClient in
            AlejoPronounsApi(
                httpClient: httpClient
            )
        }

        register(PronounsRepository.self) { _, alejoPronounsApi, preferenceRepository in
            DefaultPronounsRepository(
                alejoPronounsApi: alejoPronounsApi,
                preferenceRepository: preferenceRepository
            )
        }

        register(ChannelBttvEmotesSource.self) { _, emotesRepository in
            ChannelBttvEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(ChannelFfzEmotesSource.self) { _, emotesRepository in
            ChannelFfzEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(ChannelStvEmotesSource.self) { _, emotesRepository in
            ChannelStvEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(ChannelTwitchEmotesSource.self) { _, delegateTwitchEmotesSource in
            ChannelTwitchEmotesSource(
                delegateTwitchEmotesSource: delegateTwitchEmotesSource
            )
        }

        register(GlobalBttvEmotesSource.self) { _, emotesRepository in
            GlobalBttvEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(GlobalFfzEmotesSource.self) { _, emotesRepository in
            GlobalFfzEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(GlobalStvEmotesSource.self) { _, emotesRepository in
            GlobalStvEmotesSource(
                emotesRepository: emotesRepository
            )
        }

        register(GlobalTwitchEmotesSource.self) { _, delegateTwitchEmotesSource in
            GlobalTwitchEmotesSource(
                delegateTwitchEmotesSource: delegateTwitchEmotesSource
            )
        }

        register(DelegateTwitchEmotesSource.self) { _, twitchRepository in
            DelegateTwitchEmotesSource(
                twitchRepository: twitchRepository
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
}
