package fr.outadoc.justchatting.di

import app.cash.sqldelight.db.SqlDriver
import fr.outadoc.justchatting.data.db.AppDatabase
import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.data.db.RecentEmoteQueries
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.data.db.UserQueries
import fr.outadoc.justchatting.feature.auth.data.TwitchAuthApi
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.chat.data.irc.LiveChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.LoggedInChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.MockChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesServer
import fr.outadoc.justchatting.feature.chat.data.pubsub.client.PubSubWebSocket
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.broadcastsettingsupdate.PubSubBroadcastSettingsPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.channelpoints.PubSubChannelPointsPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.pinnedmessage.PubSubPinnedMessagePlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.poll.PubSubPollPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.prediction.PubSubPredictionPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.raid.PubSubRaidPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.richembed.PubSubRichEmbedPlugin
import fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.viewercount.PubSubViewerCountPlugin
import fr.outadoc.justchatting.feature.chat.domain.AggregateChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.DefaultChatRepository
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPluginsProvider
import fr.outadoc.justchatting.feature.chat.presentation.ChatEventViewMapper
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.FilterAutocompleteItemsUseCase
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import fr.outadoc.justchatting.feature.deeplink.DeeplinkParser
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesServer
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.db.RecentEmotesDb
import fr.outadoc.justchatting.feature.emotes.data.stv.GlobalStvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesServer
import fr.outadoc.justchatting.feature.emotes.data.twitch.ChannelTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.DelegateTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.GlobalTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.emotes.domain.GetRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.InsertRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.preferences.data.DataStorePreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsClient
import fr.outadoc.justchatting.feature.pronouns.data.LocalPronounsDb
import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import fr.outadoc.justchatting.feature.recent.presentation.RecentChannelsViewModel
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.shared.data.LocalStreamsDb
import fr.outadoc.justchatting.feature.shared.data.LocalUsersDb
import fr.outadoc.justchatting.feature.shared.data.TwitchApiImpl
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.feature.shared.domain.LocalStreamsApi
import fr.outadoc.justchatting.feature.shared.domain.LocalUsersApi
import fr.outadoc.justchatting.feature.shared.domain.TwitchApi
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepositoryImpl
import fr.outadoc.justchatting.feature.shared.presentation.DeeplinkReceiver
import fr.outadoc.justchatting.feature.shared.presentation.MainRouterViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.TimelineViewModel
import fr.outadoc.justchatting.utils.core.DefaultJson
import fr.outadoc.justchatting.utils.http.BaseHttpClientProvider
import fr.outadoc.justchatting.utils.http.TwitchHttpClientProvider
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

public val sharedModule: Module
    get() = module {
        single<Clock> { Clock.System }
        single<AuthRepository> { AuthRepository(get(), get(), get()) }
        single { DeeplinkParser(get()) }

        single<PreferenceRepository> { DataStorePreferenceRepository(get()) }

        single<TwitchHttpClientProvider> { TwitchHttpClientProvider(get(), get(), get()) }
        single { get<BaseHttpClientProvider>().get() }
        single(named("twitch")) { get<TwitchHttpClientProvider>().get() }

        factory<DeeplinkReceiver> { get<MainRouterViewModel>() }

        single { MainRouterViewModel(get(), get()) }
        viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
        viewModel { ChannelSearchViewModel(get()) }
        viewModel { FollowedChannelsViewModel(get()) }
        viewModel { RecentChannelsViewModel(get()) }
        viewModel { TimelineViewModel(get(), get()) }
        viewModel { StreamAndUserInfoViewModel(get()) }
        viewModel {
            ChatViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        single { FilterAutocompleteItemsUseCase() }

        single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get(), get()) }
        single { LoggedInChatWebSocket.Factory(get(), get(), get(), get()) }
        single { MockChatWebSocket.Factory(get(), get(), get(), get()) }
        single { PubSubWebSocket.Factory(get(), get(), get(), get()) }

        single {
            ChatCommandHandlerFactoriesProvider {
                listOf(
                    get<LiveChatWebSocket.Factory>(),
                    get<LoggedInChatWebSocket.Factory>(),
                    get<PubSubWebSocket.Factory>(),
                )
            }
        }

        single { PubSubBroadcastSettingsPlugin(get(), get()) }
        single { PubSubChannelPointsPlugin(get(), get()) }
        single { PubSubPinnedMessagePlugin(get(), get()) }
        single { PubSubPollPlugin(get(), get()) }
        single { PubSubPredictionPlugin(get()) }
        single { PubSubRaidPlugin(get(), get()) }
        single { PubSubRichEmbedPlugin(get(), get()) }
        single { PubSubViewerCountPlugin(get(), get()) }

        single {
            PubSubPluginsProvider {
                listOf(
                    get<PubSubChannelPointsPlugin>(),
                    get<PubSubPollPlugin>(),
                    get<PubSubPredictionPlugin>(),
                    get<PubSubBroadcastSettingsPlugin>(),
                    get<PubSubViewerCountPlugin>(),
                    get<PubSubRichEmbedPlugin>(),
                    get<PubSubPinnedMessagePlugin>(),
                    get<PubSubRaidPlugin>(),
                )
            }
        }

        single { AggregateChatEventHandler.Factory(get()) }
        factory<ChatRepository> { DefaultChatRepository(get()) }

        single { TwitchIrcCommandParser(get()) }
        single { ChatEventViewMapper() }

        single { RecentMessagesRepository(get(), get()) }

        single<PronounsApi> { AlejoPronounsApi(get()) }
        single { AlejoPronounsClient(get()) }
        single { PronounsRepository(get(), get(), get()) }

        factory { GetRecentEmotesUseCase(get()) }
        factory { InsertRecentEmotesUseCase(get()) }

        single { ChannelBttvEmotesSource(get(), get()) }
        single { ChannelFfzEmotesSource(get(), get()) }
        single { ChannelTwitchEmotesSource(get()) }
        single { GlobalBttvEmotesSource(get(), get()) }
        single { GlobalFfzEmotesSource(get(), get()) }
        single { GlobalStvEmotesSource(get(), get()) }
        single { GlobalTwitchEmotesSource(get()) }
        single { DelegateTwitchEmotesSource(get()) }

        single {
            EmoteListSourcesProvider {
                listOf(
                    get<ChannelTwitchEmotesSource>(),
                    get<ChannelBttvEmotesSource>(),
                    get<ChannelFfzEmotesSource>(),
                    get<GlobalTwitchEmotesSource>(),
                    get<GlobalBttvEmotesSource>(),
                    get<GlobalFfzEmotesSource>(),
                    get<GlobalStvEmotesSource>(),
                )
            }
        }

        single { AppDatabase(get<SqlDriver>()) }

        single<RecentEmoteQueries> { get<AppDatabase>().recentEmoteQueries }
        single<RecentEmotesApi> { RecentEmotesDb(get()) }

        single<UserQueries> { get<AppDatabase>().userQueries }
        single<LocalUsersApi> { LocalUsersDb(get(), get()) }

        single<StreamQueries> { get<AppDatabase>().streamQueries }
        single<LocalStreamsApi> { LocalStreamsDb(get(), get()) }

        single<PronounQueries> { get<AppDatabase>().pronounQueries }
        single<LocalPronounsApi> { LocalPronounsDb(get(), get()) }

        single<Json> { DefaultJson }

        single<TwitchRepository> { TwitchRepositoryImpl(get(), get(), get(), get()) }
        single<TwitchApi> { TwitchApiImpl(get()) }
        single { TwitchClient(get(named("twitch"))) }

        single<AuthApi> { TwitchAuthApi(get()) }
        single<BttvEmotesApi> { BttvEmotesServer(get()) }
        single<StvEmotesApi> { StvEmotesServer(get()) }
        single<RecentMessagesApi> { RecentMessagesServer(get()) }
    }
