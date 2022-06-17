package com.github.andreyasadchy.xtra.di

import android.app.Application
import com.github.andreyasadchy.xtra.BuildConfig
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
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelClipsDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelVideosDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelViewerListDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelViewerListDataResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipUrlsDeserializer
import com.github.andreyasadchy.xtra.model.gql.clip.ClipUrlsResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipVideoDeserializer
import com.github.andreyasadchy.xtra.model.gql.clip.ClipVideoResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowUserDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedChannelsDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedChannelsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedGamesDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedGamesDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedStreamsDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedVideosDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingGameDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingUserDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameClipsDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.game.GameClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.game.GameDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameStreamsDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.game.GameStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameVideosDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.game.GameVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenDeserializer
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenDeserializer
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchGameDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.search.SearchGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.StreamDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.stream.StreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.ViewersDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.stream.ViewersDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameStreamDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameStreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchGameStreamDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchGameStreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagStreamDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.tag.TagStreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.vod.VodGamesDataDeserializer
import com.github.andreyasadchy.xtra.model.gql.vod.VodGamesDataResponse
import com.github.andreyasadchy.xtra.model.helix.emote.EmoteSetDeserializer
import com.github.andreyasadchy.xtra.model.helix.emote.EmoteSetResponse
import com.github.andreyasadchy.xtra.repository.ApiRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.FetchProvider
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class XtraModule {

    @Singleton
    @Provides
    fun providesTwitchService(repository: ApiRepository): TwitchService {
        return repository
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
            GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
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
                .registerTypeAdapter(
                    StreamPlaylistTokenResponse::class.java,
                    StreamPlaylistTokenDeserializer()
                )
                .registerTypeAdapter(
                    VideoPlaylistTokenResponse::class.java,
                    VideoPlaylistTokenDeserializer()
                )
                .registerTypeAdapter(ClipUrlsResponse::class.java, ClipUrlsDeserializer())
                .registerTypeAdapter(ClipDataResponse::class.java, ClipDataDeserializer())
                .registerTypeAdapter(ClipVideoResponse::class.java, ClipVideoDeserializer())
                .registerTypeAdapter(GameDataResponse::class.java, GameDataDeserializer())
                .registerTypeAdapter(StreamDataResponse::class.java, StreamDataDeserializer())
                .registerTypeAdapter(ViewersDataResponse::class.java, ViewersDataDeserializer())
                .registerTypeAdapter(
                    GameStreamsDataResponse::class.java,
                    GameStreamsDataDeserializer()
                )
                .registerTypeAdapter(
                    GameVideosDataResponse::class.java,
                    GameVideosDataDeserializer()
                )
                .registerTypeAdapter(GameClipsDataResponse::class.java, GameClipsDataDeserializer())
                .registerTypeAdapter(
                    ChannelVideosDataResponse::class.java,
                    ChannelVideosDataDeserializer()
                )
                .registerTypeAdapter(
                    ChannelClipsDataResponse::class.java,
                    ChannelClipsDataDeserializer()
                )
                .registerTypeAdapter(
                    ChannelViewerListDataResponse::class.java,
                    ChannelViewerListDataDeserializer()
                )
                .registerTypeAdapter(EmoteCardResponse::class.java, EmoteCardDeserializer())
                .registerTypeAdapter(
                    SearchChannelDataResponse::class.java,
                    SearchChannelDataDeserializer()
                )
                .registerTypeAdapter(
                    SearchGameDataResponse::class.java,
                    SearchGameDataDeserializer()
                )
                .registerTypeAdapter(TagGameDataResponse::class.java, TagGameDataDeserializer())
                .registerTypeAdapter(
                    TagGameStreamDataResponse::class.java,
                    TagGameStreamDataDeserializer()
                )
                .registerTypeAdapter(TagStreamDataResponse::class.java, TagStreamDataDeserializer())
                .registerTypeAdapter(
                    TagSearchGameStreamDataResponse::class.java,
                    TagSearchGameStreamDataDeserializer()
                )
                .registerTypeAdapter(TagSearchDataResponse::class.java, TagSearchDataDeserializer())
                .registerTypeAdapter(VodGamesDataResponse::class.java, VodGamesDataDeserializer())
                .registerTypeAdapter(
                    FollowedStreamsDataResponse::class.java,
                    FollowedStreamsDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowedVideosDataResponse::class.java,
                    FollowedVideosDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowedChannelsDataResponse::class.java,
                    FollowedChannelsDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowedGamesDataResponse::class.java,
                    FollowedGamesDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowUserDataResponse::class.java,
                    FollowUserDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowingUserDataResponse::class.java,
                    FollowingUserDataDeserializer()
                )
                .registerTypeAdapter(
                    FollowingGameDataResponse::class.java,
                    FollowingGameDataDeserializer()
                )
                .create()
        )
    }

    private class AuthorizationInterceptor(val clientId: String?, val token: String? = null) :
        Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().apply {
                clientId?.let { addHeader("Client-ID", it) }
                token?.let { addHeader("Authorization", it) }
            }.build()
            return chain.proceed(request)
        }
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
        okHttpClient: OkHttpClient
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
