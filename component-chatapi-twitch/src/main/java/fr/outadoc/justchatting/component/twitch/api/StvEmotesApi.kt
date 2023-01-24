package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.StvEmotesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface StvEmotesApi {

    @GET("v2/emotes/global")
    suspend fun getGlobalStvEmotes(): StvEmotesResponse

    @GET("v2/users/{channelId}/emotes")
    suspend fun getStvEmotes(@Path("channelId") channelId: String): StvEmotesResponse
}
