package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.api.BttvEmotesApi
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.api.IdApi
import fr.outadoc.justchatting.api.RecentMessagesApi
import fr.outadoc.justchatting.api.StvEmotesApi
import fr.outadoc.justchatting.api.TwitchBadgesApi
import fr.outadoc.justchatting.auth.AuthenticationInterceptor
import fr.outadoc.justchatting.db.AppDatabase
import fr.outadoc.justchatting.deeplink.DeeplinkParser
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.BttvChannelDeserializer
import fr.outadoc.justchatting.model.chat.BttvChannelResponse
import fr.outadoc.justchatting.model.chat.BttvFfzDeserializer
import fr.outadoc.justchatting.model.chat.BttvFfzResponse
import fr.outadoc.justchatting.model.chat.BttvGlobalDeserializer
import fr.outadoc.justchatting.model.chat.BttvGlobalResponse
import fr.outadoc.justchatting.model.chat.CheerEmotesDeserializer
import fr.outadoc.justchatting.model.chat.CheerEmotesResponse
import fr.outadoc.justchatting.model.chat.RecentMessagesDeserializer
import fr.outadoc.justchatting.model.chat.RecentMessagesResponse
import fr.outadoc.justchatting.model.chat.StvEmotesDeserializer
import fr.outadoc.justchatting.model.chat.StvEmotesResponse
import fr.outadoc.justchatting.model.chat.TwitchBadgesDeserializer
import fr.outadoc.justchatting.model.chat.TwitchBadgesResponse
import fr.outadoc.justchatting.model.helix.emote.EmoteSetDeserializer
import fr.outadoc.justchatting.model.helix.emote.EmoteSetResponse
import fr.outadoc.justchatting.repository.ApiRepository
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.ChatConnectionPool
import fr.outadoc.justchatting.repository.EmotesRepository
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.repository.RecentMessagesRepository
import fr.outadoc.justchatting.repository.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.chat.LiveChatController
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import fr.outadoc.justchatting.util.NetworkStateObserver
import fr.outadoc.justchatting.util.chat.LiveChatWebSocket
import fr.outadoc.justchatting.util.chat.LoggedInChatWebSocket
import fr.outadoc.justchatting.util.chat.PubSubRewardParser
import fr.outadoc.justchatting.util.chat.PubSubWebSocket
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

    single<TwitchService> { ApiRepository(get(), get()) }

    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }

    single { ChatConnectionPool(get()) }

    single { LiveChatController.Factory(get(), get(), get()) }
    single { LiveChatWebSocket.Factory(get(), get(), get(), get(), get()) }
    single { LoggedInChatWebSocket.Factory(get(), get(), get(), get()) }
    single { PubSubWebSocket.Factory(get(), get()) }

    single { ChatMessageParser(get()) }
    single { ChatEntryMapper(get()) }
    single { PubSubRewardParser(get()) }

    single { AuthRepository(get(), get()) }
    single { EmotesRepository(get(), get(), get(), get()) }
    single { RecentMessagesRepository(get()) }

    single { get<AppDatabase>().recentEmotes() }

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").build() }

    single<HelixApi> { createApi("https://api.twitch.tv/helix/") }
    single<TwitchBadgesApi> { createApi("https://badges.twitch.tv/") }
    single<BttvEmotesApi> { createApi("https://api.betterttv.net/") }
    single<StvEmotesApi> { createApi("https://api.7tv.app/") }
    single<RecentMessagesApi> { createApi("https://recent-messages.robotty.de/api/") }
    single<IdApi> { createApi("https://id.twitch.tv/oauth2/") }

    single { AuthenticationInterceptor(get()) }
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
                .registerTypeAdapter(BttvChannelResponse::class.java, BttvChannelDeserializer())
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
