package fr.outadoc.justchatting.api

import fr.outadoc.justchatting.model.chat.CheerEmotesResponse
import fr.outadoc.justchatting.model.helix.channel.ChannelSearchResponse
import fr.outadoc.justchatting.model.helix.emote.EmoteSetResponse
import fr.outadoc.justchatting.model.helix.follows.FollowResponse
import fr.outadoc.justchatting.model.helix.stream.StreamsResponse
import fr.outadoc.justchatting.model.helix.user.UsersResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HelixApi {

    @GET("streams/")
    suspend fun getStreams(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("user_id") ids: List<String>
    ): StreamsResponse

    @GET("streams/followed")
    suspend fun getFollowedStreams(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("user_id") userId: String?,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): StreamsResponse

    @GET("users")
    suspend fun getUsersById(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("id") ids: List<String>
    ): UsersResponse

    @GET("users")
    suspend fun getUsersByLogin(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("login") logins: List<String>
    ): UsersResponse

    @GET("search/channels")
    suspend fun getChannels(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("query") query: String,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): ChannelSearchResponse

    @GET("users/follows")
    suspend fun getUserFollows(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("to_id") userId: String?,
        @Query("from_id") channelId: String?
    ): FollowResponse

    @GET("users/follows")
    suspend fun getFollowedChannels(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("from_id") userId: String?,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): FollowResponse

    @GET("chat/emotes/set")
    suspend fun getEmotesFromSet(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("emote_set_id") setIds: List<String>
    ): EmoteSetResponse

    @GET("bits/cheermotes")
    suspend fun getCheerEmotes(
        @Header("Client-ID") clientId: String?,
        @Header("Authorization") token: String?,
        @Query("broadcaster_id") userId: String?
    ): CheerEmotesResponse
}
