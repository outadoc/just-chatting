package fr.outadoc.justchatting.api

import fr.outadoc.justchatting.component.twitch.model.chat.RecentMessagesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecentMessagesApi {

    @GET("v2/recent-messages/{channelLogin}")
    suspend fun getRecentMessages(
        @Path("channelLogin") channelLogin: String,
        @Query("limit") limit: Int
    ): Response<RecentMessagesResponse>
}
