package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.model.UsersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HelixApi {

    @GET("streams")
    @TwitchAuth
    suspend fun getStreams(
        @Query("user_id") ids: List<String>
    ): StreamsResponse

    @GET("streams/followed")
    @TwitchAuth
    suspend fun getFollowedStreams(
        @Query("user_id") userId: String?,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): StreamsResponse

    @GET("users")
    @TwitchAuth
    suspend fun getUsersById(
        @Query("id") ids: List<String>
    ): UsersResponse

    @GET("users")
    @TwitchAuth
    suspend fun getUsersByLogin(
        @Query("login") logins: List<String>
    ): UsersResponse

    @GET("search/channels")
    @TwitchAuth
    suspend fun getChannels(
        @Query("query") query: String,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): ChannelSearchResponse

    @GET("users/follows")
    @TwitchAuth
    suspend fun getUserFollows(
        @Query("to_id") userId: String?,
        @Query("from_id") channelId: String?
    ): FollowResponse

    @GET("users/follows")
    @TwitchAuth
    suspend fun getFollowedChannels(
        @Query("from_id") userId: String?,
        @Query("first") limit: Int,
        @Query("after") offset: String?
    ): FollowResponse

    @GET("chat/emotes/set")
    @TwitchAuth
    suspend fun getEmotesFromSet(
        @Query("emote_set_id") setIds: List<String>
    ): EmoteSetResponse

    @GET("bits/cheermotes")
    @TwitchAuth
    suspend fun getCheerEmotes(
        @Query("broadcaster_id") userId: String?
    ): CheerEmotesResponse
}
