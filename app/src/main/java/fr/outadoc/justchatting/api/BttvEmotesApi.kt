package fr.outadoc.justchatting.api

import fr.outadoc.justchatting.component.twitch.parser.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.parser.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.parser.model.BttvGlobalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BttvEmotesApi {

    @GET("3/cached/emotes/global")
    suspend fun getGlobalBttvEmotes(): Response<BttvGlobalResponse>

    @GET("3/cached/users/twitch/{channelId}")
    suspend fun getBttvEmotes(@Path("channelId") channelId: String): Response<BttvChannelResponse>

    @GET("3/cached/frankerfacez/emotes/global")
    suspend fun getBttvGlobalFfzEmotes(): Response<BttvFfzResponse>

    @GET("3/cached/frankerfacez/users/twitch/{channelId}")
    suspend fun getBttvFfzEmotes(@Path("channelId") channelId: String): Response<BttvFfzResponse>
}
