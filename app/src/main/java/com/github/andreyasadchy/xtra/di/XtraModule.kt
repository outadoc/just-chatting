package com.github.andreyasadchy.xtra.di

import android.app.Application
import com.github.andreyasadchy.xtra.BuildConfig
import com.github.andreyasadchy.xtra.api.*
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenDeserializer
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenDeserializer
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenResponse
import com.github.andreyasadchy.xtra.repository.HelixRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.FetchProvider
import com.github.andreyasadchy.xtra.util.RemoteConfigParams
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

    @Singleton
    @Provides
    fun providesTwitchService(repository: HelixRepository): TwitchService {
        return repository
    }

    @Singleton
    @Provides
    fun providesHelixApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): HelixApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/helix/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(HelixApi::class.java)
    }

    @Singleton
    @Provides
    fun providesApiService(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): ApiService {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/api/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesUsherApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): UsherApi {
        return Retrofit.Builder()
                .baseUrl("https://usher.ttvnw.net/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(UsherApi::class.java)
    }

    @Singleton
    @Provides
    fun providesMiscApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): MiscApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/") //placeholder url
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
    fun providesTTVLolApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): TTVLolApi {
        return Retrofit.Builder()
                .baseUrl("https://api.ttv.lol/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("X-Donate-To", "https://ttv.lol/donate")
                            .build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(TTVLolApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGraphQLApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): GraphQLApi {
        return Retrofit.Builder()
                .baseUrl("https://gql.twitch.tv/gql/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Client-ID", RemoteConfigParams.TWITCH_CLIENT_ID_KEY)
                            .build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(GraphQLApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(GlobalBadgesResponse::class.java, GlobalBadgeDeserializer())
                .registerTypeAdapter(StvEmotesResponse::class.java, StvRoomDeserializer())
                .registerTypeAdapter(BttvGlobalResponse::class.java, BttvGlobalDeserializer())
                .registerTypeAdapter(BttvChannelResponse::class.java, BttvChannelDeserializer())
                .registerTypeAdapter(BttvFfzResponse::class.java, BttvFfzDeserializer())
                .registerTypeAdapter(ClipDataResponse::class.java, ClipDataDeserializer())
                .registerTypeAdapter(StreamPlaylistTokenResponse::class.java, StreamPlaylistTokenDeserializer())
                .registerTypeAdapter(VideoPlaylistTokenResponse::class.java, VideoPlaylistTokenDeserializer())
                .create())
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
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
    fun providesFetchConfigurationBuilder(application: Application, okHttpClient: OkHttpClient): FetchConfiguration.Builder {
        return FetchConfiguration.Builder(application)
                .enableLogging(BuildConfig.DEBUG)
                .enableRetryOnNetworkGain(true)
                .setDownloadConcurrentLimit(3)
                .setHttpDownloader(OkHttpDownloader(okHttpClient))
                .setProgressReportingInterval(1000L)
                .setAutoRetryMaxAttempts(3)
    }
}
