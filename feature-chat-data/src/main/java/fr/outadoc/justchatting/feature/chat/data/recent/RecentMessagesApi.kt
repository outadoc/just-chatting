package fr.outadoc.justchatting.feature.chat.data.recent

import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecentMessagesApi {

    @GET("v2/recent-messages/{channelLogin}")
    suspend fun getRecentMessages(
        @Path("channelLogin") channelLogin: String,
        @Query("limit") limit: Int
    ): RecentMessagesResponse
}
