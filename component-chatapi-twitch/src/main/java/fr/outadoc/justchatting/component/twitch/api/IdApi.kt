package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.ValidationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IdApi {

    @GET("validate")
    @TwitchAuth
    suspend fun validateToken(): ValidationResponse?

    @POST("revoke")
    suspend fun revokeToken(
        @Query("client_id") clientId: String,
        @Query("token") token: String
    )
}
