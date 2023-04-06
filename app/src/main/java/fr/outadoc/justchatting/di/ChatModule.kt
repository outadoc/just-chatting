package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPluginsProvider
import fr.outadoc.justchatting.component.chatapi.db.AppDatabase
import fr.outadoc.justchatting.component.twitch.websocket.irc.IrcMessageMapper
import fr.outadoc.justchatting.component.twitch.websocket.irc.LiveChatWebSocket
import fr.outadoc.justchatting.component.twitch.websocket.irc.LoggedInChatWebSocket
import fr.outadoc.justchatting.component.twitch.websocket.irc.MockChatWebSocket
import fr.outadoc.justchatting.component.twitch.websocket.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.component.twitch.websocket.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.client.PubSubWebSocket
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.broadcastsettingsupdate.PubSubBroadcastSettingsPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.channelpoints.PubSubChannelPointsPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.pinnedmessage.PubSubPinnedMessagePlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.poll.PubSubPollPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.prediction.PubSubPredictionPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid.PubSubRaidPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.richembed.PubSubRichEmbedPlugin
import fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.viewercount.PubSubViewerCountPlugin
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelBttvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelFfzEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelStvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelTwitchEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.DelegateTwitchEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalBttvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalFfzEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalStvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalTwitchEmotesSource
import fr.outadoc.justchatting.feature.chat.domain.AggregateChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.ChatConnectionPool
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.FilterAutocompleteItemsUseCase
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatNotifierImpl
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.DefaultPronounsRepository
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {

    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

    single<ChatNotifier> { ChatNotifierImpl() }
    single { FilterAutocompleteItemsUseCase() }

    single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { LoggedInChatWebSocket.Factory(get(), get(), get(), get(), get(), get(), get()) }
    single { MockChatWebSocket.Factory(get(), get(), get(), get(), get(), get()) }
    single { PubSubWebSocket.Factory(get(), get(), get(), get()) }

    single {
        ChatCommandHandlerFactoriesProvider {
            if (BuildConfig.ENABLE_MOCK_IRC_ENDPOINT) {
                listOf(
                    get<MockChatWebSocket.Factory>(),
                )
            } else {
                listOf(
                    get<LiveChatWebSocket.Factory>(),
                    get<LoggedInChatWebSocket.Factory>(),
                    get<PubSubWebSocket.Factory>(),
                )
            }
        }
    }

    single { PubSubBroadcastSettingsPlugin(get()) }
    single { PubSubChannelPointsPlugin(get(), get(), get()) }
    single { PubSubPinnedMessagePlugin(get()) }
    single { PubSubPollPlugin(get()) }
    single { PubSubPredictionPlugin(get()) }
    single { PubSubRaidPlugin(get()) }
    single { PubSubRichEmbedPlugin(get()) }
    single { PubSubViewerCountPlugin(get()) }

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
    single { ChatConnectionPool(get()) }

    single { TwitchIrcCommandParser(get()) }
    single { IrcMessageMapper(get()) }

    single { get<AppDatabase>().recentEmotes() }

    single { RecentMessagesRepository(get(), get()) }
    single { AlejoPronounsApi(get()) }
    single<PronounsRepository> { DefaultPronounsRepository(get(), get()) }

    single { ChannelBttvEmotesSource(get()) }
    single { ChannelFfzEmotesSource(get()) }
    single { ChannelStvEmotesSource(get()) }
    single { ChannelTwitchEmotesSource(get()) }
    single { GlobalBttvEmotesSource(get()) }
    single { GlobalFfzEmotesSource(get()) }
    single { GlobalStvEmotesSource(get()) }
    single { GlobalTwitchEmotesSource(get()) }
    single { DelegateTwitchEmotesSource(get()) }

    single {
        EmoteListSourcesProvider {
            listOf(
                get<ChannelTwitchEmotesSource>(),
                get<ChannelBttvEmotesSource>(),
                get<ChannelFfzEmotesSource>(),
                get<ChannelStvEmotesSource>(),
                get<GlobalTwitchEmotesSource>(),
                get<GlobalBttvEmotesSource>(),
                get<GlobalFfzEmotesSource>(),
                get<GlobalStvEmotesSource>(),
            )
        }
    }
}
