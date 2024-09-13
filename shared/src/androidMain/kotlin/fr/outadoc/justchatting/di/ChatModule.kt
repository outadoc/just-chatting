package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.chat.data.irc.LiveChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.LoggedInChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.MockChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
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
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.FilterAutocompleteItemsUseCase
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import fr.outadoc.justchatting.feature.chat.presentation.mobile.AndroidChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.mobile.MobileCreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.ChannelFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalBttvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.bttv.GlobalFfzEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.stv.GlobalStvEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.ChannelTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.DelegateTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.data.twitch.GlobalTwitchEmotesSource
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.emotes.domain.GetRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.InsertRecentEmotesUseCase
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsClient
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val chatModule: Module = module {

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
    viewModel { StreamAndUserInfoViewModel(get()) }

    single<ChatNotifier> { AndroidChatNotifier(get(), get()) }

    single { FilterAutocompleteItemsUseCase() }
    single<CreateShortcutForChannelUseCase> { MobileCreateShortcutForChannelUseCase(get()) }

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
    single<ChatRepository> { DefaultChatRepository(get()) }

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
}
