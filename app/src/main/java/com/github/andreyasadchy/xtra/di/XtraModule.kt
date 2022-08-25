package com.github.andreyasadchy.xtra.di

import android.app.Application
import android.content.Context
import com.github.andreyasadchy.xtra.BuildConfig
import com.github.andreyasadchy.xtra.MainApplication
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.api.IdApi
import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.model.chat.BttvChannelDeserializer
import com.github.andreyasadchy.xtra.model.chat.BttvChannelResponse
import com.github.andreyasadchy.xtra.model.chat.BttvFfzDeserializer
import com.github.andreyasadchy.xtra.model.chat.BttvFfzResponse
import com.github.andreyasadchy.xtra.model.chat.BttvGlobalDeserializer
import com.github.andreyasadchy.xtra.model.chat.BttvGlobalResponse
import com.github.andreyasadchy.xtra.model.chat.CheerEmotesDeserializer
import com.github.andreyasadchy.xtra.model.chat.CheerEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.EmoteCardDeserializer
import com.github.andreyasadchy.xtra.model.chat.EmoteCardResponse
import com.github.andreyasadchy.xtra.model.chat.RecentMessagesDeserializer
import com.github.andreyasadchy.xtra.model.chat.RecentMessagesResponse
import com.github.andreyasadchy.xtra.model.chat.StvEmotesDeserializer
import com.github.andreyasadchy.xtra.model.chat.StvEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.TwitchBadgesDeserializer
import com.github.andreyasadchy.xtra.model.chat.TwitchBadgesResponse
import com.github.andreyasadchy.xtra.model.helix.emote.EmoteSetDeserializer
import com.github.andreyasadchy.xtra.model.helix.emote.EmoteSetResponse
import com.github.andreyasadchy.xtra.repository.ApiRepository
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.ChatPreferencesRepository
import com.github.andreyasadchy.xtra.repository.PreferenceRepository
import com.github.andreyasadchy.xtra.repository.SharedPrefsPreferenceRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.util.FetchProvider
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class XtraModule {

    @Provides
    fun providesApplicationContext(): Context {
        return MainApplication.INSTANCE
    }

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
        gsonConverterFactory: GsonConverterFactory,
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
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
            connectTimeout(5, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
            readTimeout(5, TimeUnit.MINUTES)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun providesFetchProvider(fetchConfigurationBuilder: FetchConfiguration.Builder): FetchProvider {
        return FetchProvider(fetchConfigurationBuilder)
    }

    @Singleton
    @Provides
    fun providesFetchConfigurationBuilder(
        application: Application,
        okHttpClient: OkHttpClient,
    ): FetchConfiguration.Builder {
        return FetchConfiguration.Builder(application)
            .enableLogging(BuildConfig.DEBUG)
            .enableRetryOnNetworkGain(true)
            .setDownloadConcurrentLimit(3)
            .setHttpDownloader(OkHttpDownloader(okHttpClient))
            .setProgressReportingInterval(1000L)
            .setAutoRetryMaxAttempts(3)
    }
}
