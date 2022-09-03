package fr.outadoc.justchatting.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.api.IdApi
import fr.outadoc.justchatting.api.MiscApi
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
import fr.outadoc.justchatting.repository.AuthPreferencesRepository
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.repository.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntryMapper
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val mainModule = module {

    single<Clock> { Clock.System }

    single<TwitchService> { ApiRepository(get(), get(), get(), get()) }

    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }
    single<AuthPreferencesRepository> { get<PreferenceRepository>() }
    single<ChatPreferencesRepository> { get<PreferenceRepository>() }
    single<UserPreferencesRepository> { get<PreferenceRepository>() }

    single { ChatMessageParser() }
    single { ChatEntryMapper(get()) }

    single<HelixApi> {
        Retrofit.Builder()
            .baseUrl("https://api.twitch.tv/helix/")
            .client(get())
            .addConverterFactory(get<GsonConverterFactory>())
            .build()
            .create(HelixApi::class.java)
    }

    single<MiscApi> {
        Retrofit.Builder()
            .baseUrl("https://api.twitch.tv/") // placeholder url
            .client(get())
            .addConverterFactory(get<GsonConverterFactory>())
            .build()
            .create(MiscApi::class.java)
    }

    single<IdApi> {
        Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/oauth2/")
            .client(get())
            .addConverterFactory(get<GsonConverterFactory>())
            .build()
            .create(IdApi::class.java)
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
                    RecentMessagesDeserializer()
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }
                    )
                }
                addInterceptor(ChuckerInterceptor.Builder(get()).build())
                connectTimeout(5, TimeUnit.MINUTES)
                writeTimeout(5, TimeUnit.MINUTES)
                readTimeout(5, TimeUnit.MINUTES)
            }
            .build()
    }
}
