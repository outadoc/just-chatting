package fr.outadoc.justchatting.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.MainApplication
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.api.IdApi
import fr.outadoc.justchatting.api.MiscApi
import fr.outadoc.justchatting.model.chat.BttvChannelDeserializer
import fr.outadoc.justchatting.model.chat.BttvChannelResponse
import fr.outadoc.justchatting.model.chat.BttvFfzDeserializer
import fr.outadoc.justchatting.model.chat.BttvFfzResponse
import fr.outadoc.justchatting.model.chat.BttvGlobalDeserializer
import fr.outadoc.justchatting.model.chat.BttvGlobalResponse
import fr.outadoc.justchatting.model.chat.CheerEmotesDeserializer
import fr.outadoc.justchatting.model.chat.CheerEmotesResponse
import fr.outadoc.justchatting.model.chat.EmoteCardDeserializer
import fr.outadoc.justchatting.model.chat.EmoteCardResponse
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
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class MainModule {

    @Provides
    fun providesApplicationContext(): Context = MainApplication.INSTANCE

    @Provides
    fun providesClock(): Clock = Clock.System

    @Singleton
    @Provides
    fun providesTwitchService(repository: ApiRepository): TwitchService {
        return repository
    }

    @Provides
    @Singleton
    fun providesPreferenceRepository(context: Context): PreferenceRepository {
        return SharedPrefsPreferenceRepository(context)
    }

    @Provides
    @Singleton
    fun providesAuthPreferencesRepository(preferenceRepository: PreferenceRepository): AuthPreferencesRepository {
        return preferenceRepository
    }

    @Provides
    @Singleton
    fun providesChatPreferencesRepository(preferenceRepository: PreferenceRepository): ChatPreferencesRepository {
        return preferenceRepository
    }

    @Provides
    @Singleton
    fun providesUserPreferencesRepository(preferenceRepository: PreferenceRepository): UserPreferencesRepository {
        return preferenceRepository
    }

    @Singleton
    @Provides
    fun providesHelixApi(
        client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): HelixApi {
        return Retrofit.Builder()
            .baseUrl("https://api.twitch.tv/helix/")
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(HelixApi::class.java)
    }

    @Singleton
    @Provides
    fun providesMiscApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): MiscApi {
        return Retrofit.Builder()
            .baseUrl("https://api.twitch.tv/") // placeholder url
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(MiscApi::class.java)
    }

    @Singleton
    @Provides
    fun providesIdApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): IdApi {
        return Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/oauth2/")
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(IdApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .registerTypeAdapter(EmoteSetResponse::class.java, EmoteSetDeserializer())
                .registerTypeAdapter(CheerEmotesResponse::class.java, CheerEmotesDeserializer())
                .registerTypeAdapter(TwitchBadgesResponse::class.java, TwitchBadgesDeserializer())
                .registerTypeAdapter(
                    RecentMessagesResponse::class.java,
                    RecentMessagesDeserializer()
                )
                .registerTypeAdapter(StvEmotesResponse::class.java, StvEmotesDeserializer())
                .registerTypeAdapter(BttvGlobalResponse::class.java, BttvGlobalDeserializer())
                .registerTypeAdapter(BttvChannelResponse::class.java, BttvChannelDeserializer())
                .registerTypeAdapter(BttvFfzResponse::class.java, BttvFfzDeserializer())
                .registerTypeAdapter(EmoteCardResponse::class.java, EmoteCardDeserializer())
                .create()
        )
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    }
                )
            }
            connectTimeout(5, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
            readTimeout(5, TimeUnit.MINUTES)
        }
        return builder.build()
    }
}
