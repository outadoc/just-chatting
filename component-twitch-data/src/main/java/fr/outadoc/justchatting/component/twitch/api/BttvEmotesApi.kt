package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface BttvEmotesApi {

    @GET("3/cached/emotes/global")
    suspend fun getGlobalBttvEmotes(): BttvGlobalResponse

    @GET("3/cached/users/twitch/{channelId}")
    suspend fun getBttvEmotes(@Path("channelId") channelId: String): BttvChannelResponse

    @GET("3/cached/frankerfacez/emotes/global")
    suspend fun getBttvGlobalFfzEmotes(): BttvFfzResponse

    @GET("3/cached/frankerfacez/users/twitch/{channelId}")
    suspend fun getBttvFfzEmotes(@Path("channelId") channelId: String): BttvFfzResponse
}
