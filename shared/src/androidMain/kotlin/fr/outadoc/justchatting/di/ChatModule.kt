package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.component.chatapi.common.handler.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPluginsProvider
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
import fr.outadoc.justchatting.feature.chat.data.irc.IrcMessageMapper
import fr.outadoc.justchatting.feature.chat.domain.AggregateChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.DefaultChatRepository
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.FilterAutocompleteItemsUseCase
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import fr.outadoc.justchatting.feature.chat.presentation.mobile.DefaultChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.mobile.MobileCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.stv.GlobalStvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.ChannelTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.GlobalTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.domain.DelegateTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsClient
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val chatModule: Module = module {

    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { StreamAndUserInfoViewModel(get()) }

    single<ChatNotifier> { DefaultChatNotifier(get(), get()) }

    single { FilterAutocompleteItemsUseCase() }
    single<CreateShortcutForChannelUseCase> { MobileCreateShortcutForChannelUseCase(get()) }

    single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get(), get(), get()) }
    single { LoggedInChatWebSocket.Factory(get(), get(), get(), get(), get(), get()) }
    single { MockChatWebSocket.Factory(get(), get(), get(), get(), get()) }
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

    single { PubSubBroadcastSettingsPlugin(get()) }
    single { PubSubChannelPointsPlugin(get(), get()) }
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
    single<ChatRepository> { DefaultChatRepository(get()) }

    single { TwitchIrcCommandParser(get()) }
    single { fr.outadoc.justchatting.feature.chat.data.irc.IrcMessageMapper() }

    single { RecentMessagesRepository(get(), get()) }

    single<PronounsApi> { AlejoPronounsApi(get(), get()) }
    single { AlejoPronounsClient(get()) }
    single { PronounsRepository(get()) }

    single { ChannelBttvEmotesSource(get()) }
    single { ChannelFfzEmotesSource(get()) }
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
                get<GlobalTwitchEmotesSource>(),
                get<GlobalBttvEmotesSource>(),
                get<GlobalFfzEmotesSource>(),
                get<GlobalStvEmotesSource>(),
            )
        }
    }
}
