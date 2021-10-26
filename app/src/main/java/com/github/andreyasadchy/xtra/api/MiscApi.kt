package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.chat.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MiscApi {

    @GET("https://badges.twitch.tv/v1/badges/global/display")
    suspend fun getGlobalBadges(): GlobalBadgesResponse

    @GET("https://badges.twitch.tv/v1/badges/channels/{channelId}/display")
    suspend fun getChannelBadges(@Path("channelId") channelId: String): GlobalBadgesResponse

    @GET("https://api.7tv.app/v2/emotes/global")
    suspend fun getGlobalStvEmotes(): Response<StvEmotesResponse>

    @GET("https://api.7tv.app/v2/users/{channel}/emotes")
    suspend fun getStvEmotes(@Path("channel") channel: String): Response<StvEmotesResponse>

    @GET("https://api.betterttv.net/3/cached/emotes/global")
    suspend fun getGlobalBttvEmotes(): Response<BttvGlobalResponse>

    @GET("https://api.betterttv.net/3/cached/users/twitch/{channelId}")
    suspend fun getBttvEmotes(@Path("channelId") channelId: String): Response<BttvChannelResponse>

    @GET("https://api.betterttv.net/3/cached/frankerfacez/emotes/global")
    suspend fun getBttvGlobalFfzEmotes(): Response<BttvFfzResponse>

    @GET("https://api.betterttv.net/3/cached/frankerfacez/users/twitch/{channelId}")
    suspend fun getBttvFfzEmotes(@Path("channelId") channelId: String): Response<BttvFfzResponse>

    @GET("https://api.frankerfacez.com/v1/set/global")
    suspend fun getGlobalFfzEmotes(): Response<FfzEmotesResponse>

    @GET("https://api.frankerfacez.com/v1/room/{channel}")
    suspend fun getFfzEmotes(@Path("channel") channel: String): Response<FfzEmotesResponse>
}