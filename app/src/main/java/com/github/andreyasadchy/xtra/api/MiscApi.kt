package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.chat.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MiscApi {

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    suspend fun getVideoChatLog(@Header("Client-ID") clientId: String?, @Path("id") videoId: String?, @Query("content_offset_seconds") offsetSeconds: Double, @Query("limit") limit: Int): VideoMessagesResponse

    @GET("https://api.twitch.tv/v5/videos/{id}/comments")
    suspend fun getVideoChatLogAfter(@Header("Client-ID") clientId: String?, @Path("id") videoId: String?, @Query("cursor") cursor: String, @Query("limit") limit: Int): VideoMessagesResponse

    @GET("https://badges.twitch.tv/v1/badges/global/display")
    suspend fun getGlobalBadges(): Response<TwitchBadgesResponse>

    @GET("https://badges.twitch.tv/v1/badges/channels/{channelId}/display")
    suspend fun getChannelBadges(@Path("channelId") channelId: String): Response<TwitchBadgesResponse>

    @GET("https://recent-messages.robotty.de/api/v2/recent-messages/{channelLogin}")
    suspend fun getRecentMessages(@Path("channelLogin") channelLogin: String, @Query("limit") limit: String): Response<RecentMessagesResponse>

    @GET("https://api.7tv.app/v2/emotes/global")
    suspend fun getGlobalStvEmotes(): Response<StvEmotesResponse>

    @GET("https://api.7tv.app/v2/users/{channelId}/emotes")
    suspend fun getStvEmotes(@Path("channelId") channelId: String): Response<StvEmotesResponse>

    @GET("https://api.betterttv.net/3/cached/emotes/global")
    suspend fun getGlobalBttvEmotes(): Response<BttvGlobalResponse>

    @GET("https://api.betterttv.net/3/cached/users/twitch/{channelId}")
    suspend fun getBttvEmotes(@Path("channelId") channelId: String): Response<BttvChannelResponse>

    @GET("https://api.betterttv.net/3/cached/frankerfacez/emotes/global")
    suspend fun getBttvGlobalFfzEmotes(): Response<BttvFfzResponse>

    @GET("https://api.betterttv.net/3/cached/frankerfacez/users/twitch/{channelId}")
    suspend fun getBttvFfzEmotes(@Path("channelId") channelId: String): Response<BttvFfzResponse>
}