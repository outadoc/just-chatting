package fr.outadoc.justchatting.di

import com.google.gson.GsonBuilder
import fr.outadoc.justchatting.component.twitch.adapters.BttvChannelDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.BttvFfzDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.BttvGlobalDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.CheerEmotesDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.EmoteSetDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.StvEmotesDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.TwitchBadgesDeserializer
import fr.outadoc.justchatting.component.twitch.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import fr.outadoc.justchatting.component.twitch.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.model.StvEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import fr.outadoc.justchatting.db.AppDatabase
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelBttvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelFfzEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.ChannelStvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalBttvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalFfzEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.GlobalStvEmotesSource
import fr.outadoc.justchatting.feature.chat.data.emotes.TwitchEmotesSource
import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesDeserializer
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.data.websocket.LiveChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.websocket.LoggedInChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.websocket.PubSubRewardParser
import fr.outadoc.justchatting.feature.chat.data.websocket.PubSubWebSocket
import fr.outadoc.justchatting.feature.chat.domain.ChatConnectionPool
import fr.outadoc.justchatting.feature.chat.domain.LiveChatController
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntryMapper
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatNotifierImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.converter.gson.GsonConverterFactory

val chatModule = module {
    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get()) }

    single<ChatNotifier> { ChatNotifierImpl() }

    single { ChatConnectionPool(get()) }

    single { LiveChatController.Factory(get(), get(), get()) }
    single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get()) }
    single { LoggedInChatWebSocket.Factory(get(), get(), get(), get()) }
    single { PubSubWebSocket.Factory(get(), get()) }

    single { ChatMessageParser(get()) }
    single { ChatEntryMapper(get()) }
    single { PubSubRewardParser(get()) }

    single { get<AppDatabase>().recentEmotes() }

    single { RecentMessagesRepository(get()) }

    single { ChannelBttvEmotesSource(get()) }
    single { ChannelFfzEmotesSource(get()) }
    single { ChannelStvEmotesSource(get()) }
    single { GlobalBttvEmotesSource(get()) }
    single { GlobalFfzEmotesSource(get()) }
    single { GlobalStvEmotesSource(get()) }
    single { TwitchEmotesSource(get()) }

    single {
        EmoteListSourcesProvider {
            listOf(
                get<TwitchEmotesSource>(),
                get<ChannelBttvEmotesSource>(),
                get<ChannelFfzEmotesSource>(),
                get<ChannelStvEmotesSource>(),
                get<GlobalBttvEmotesSource>(),
                get<GlobalFfzEmotesSource>(),
                get<GlobalStvEmotesSource>()
            )
        }
    }

    single<GsonConverterFactory> {
        GsonConverterFactory.create(
            GsonBuilder()
                .registerTypeAdapter(EmoteSetResponse::class.java, EmoteSetDeserializer())
                .registerTypeAdapter(CheerEmotesResponse::class.java, CheerEmotesDeserializer())
                .registerTypeAdapter(
                    TwitchBadgesResponse::class.java,
                    TwitchBadgesDeserializer()
                )
                .registerTypeAdapter(
                    RecentMessagesResponse::class.java,
                    RecentMessagesDeserializer(get())
                )
                .registerTypeAdapter(StvEmotesResponse::class.java, StvEmotesDeserializer())
                .registerTypeAdapter(BttvGlobalResponse::class.java, BttvGlobalDeserializer())
                .registerTypeAdapter(
                    BttvChannelResponse::class.java,
                    BttvChannelDeserializer()
                )
                .registerTypeAdapter(BttvFfzResponse::class.java, BttvFfzDeserializer())
                .create()
        )
    }
}
