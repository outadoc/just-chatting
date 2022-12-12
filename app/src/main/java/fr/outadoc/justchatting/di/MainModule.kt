package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.auth.AuthenticationInterceptor
import fr.outadoc.justchatting.component.twitch.adapters.BttvChannelDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.BttvFfzDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.BttvGlobalDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.CheerEmotesDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.EmoteSetDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.StvEmotesDeserializer
import fr.outadoc.justchatting.component.twitch.adapters.TwitchBadgesDeserializer
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.component.twitch.api.IdApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.TwitchRepositoryImpl
import fr.outadoc.justchatting.component.twitch.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import fr.outadoc.justchatting.component.twitch.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.twitch.model.StvEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import fr.outadoc.justchatting.db.AppDatabase
import fr.outadoc.justchatting.deeplink.DeeplinkParser
import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesDeserializer
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.data.websocket.LiveChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.websocket.LoggedInChatWebSocket
import fr.outadoc.justchatting.feature.chat.data.websocket.PubSubRewardParser
import fr.outadoc.justchatting.feature.chat.data.websocket.PubSubWebSocket
import fr.outadoc.justchatting.feature.chat.domain.ChatConnectionPool
import fr.outadoc.justchatting.feature.chat.domain.LiveChatController
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.oss.ReadExternalDependenciesList
import fr.outadoc.justchatting.ui.chat.ChatNotifier
import fr.outadoc.justchatting.ui.chat.ChatNotifierImpl
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import kotlinx.datetime.Clock
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

val mainModule = module {

    single<Clock> { Clock.System }
    single<ConnectivityManager> { get<Context>().getSystemService()!! }
    single { NetworkStateObserver(get()) }

    single<TwitchRepository> { TwitchRepositoryImpl(get(), get()) }

    single { ReadExternalDependenciesList(get()) }
    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }

    single { ChatConnectionPool(get()) }
    single<ChatNotifier> { ChatNotifierImpl() }

    single { LiveChatController.Factory(get(), get(), get()) }
    single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get()) }
    single { LoggedInChatWebSocket.Factory(get(), get(), get(), get()) }
    single { PubSubWebSocket.Factory(get(), get()) }

    single { ChatMessageParser(get()) }
    single { ChatEntryMapper(get()) }
    single { PubSubRewardParser(get()) }

    single { AuthRepository(get(), get(), get()) }
    single { EmotesRepository(get(), get(), get(), get()) }
    single { RecentMessagesRepository(get()) }

    single { get<AppDatabase>().recentEmotes() }

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").build() }

    single {
        OAuthAppCredentials(
            clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
            redirectUri = "https://just-chatting.app/auth/callback.html".toUri()
        )
    }

    single<HelixApi> { createApi("https://api.twitch.tv/helix/") }
    single<TwitchBadgesApi> { createApi("https://badges.twitch.tv/") }
    single<BttvEmotesApi> { createApi("https://api.betterttv.net/") }
    single<StvEmotesApi> { createApi("https://api.7tv.app/") }
    single<RecentMessagesApi> { createApi("https://recent-messages.robotty.de/api/") }
    single<IdApi> { createApi("https://id.twitch.tv/oauth2/") }

    single { AuthenticationInterceptor(get(), get()) }
    single { DeeplinkParser(get()) }

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

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthenticationInterceptor>())
            .addInterceptor(ChuckerInterceptor.Builder(get()).build())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }
                    )
                }
            }
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .cache(
                Cache(
                    directory = File(get<Context>().cacheDir, "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .build()
    }
}

private inline fun <reified T> Scope.createApi(baseUrl: String): T =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(get())
        .addConverterFactory(get<GsonConverterFactory>())
        .build()
        .create(T::class.java)
